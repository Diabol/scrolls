# Scrolls
The Automated Delivery Report Engine

## What and why?
Scrolls is a highly extensible tool for generating rich release notes or delivery reports combining tangible data from 
several sources. It's a tool that let's you pull data from many different sources and present it in reports of different
 formats. It has a number of built in adapters for different data sources e.g. Git, Subversion, Github, Jira, Jenkins 
 and SonarCube and can easily be extended with plugins.
 
Scrolls purpose is to support the principles of Continuous Delivery by providing traceability and transparency in the 
delivery process. It gives you the ability to take fast and informed decisions while delivering quality software at 
speed. Stop spending countless hours in status report meetings or running around collecting information about 
*who did what and why?* to be able to decide if the system is releasable or not!

## Install

## Configure

## Run
Scrolls main interface is it's configuration file and it's command line interface. The basic concept is that plugins
(i.e. services) are configured in the configuration file and run-time options are given to the cli.

Developers can run using gradle: 

```
    gradle run -Drun.args="--old-version 1.0.0 --new-version 2.0.0"
```

### Using Scrolls with multiple repositories
Scrolls also supports report generation over multipe repositories. This is a typical scenario when you have dependencies
components that need to to be syncronized in a coordinated release. In this case you can view the version of the system
as a set of components unique versions. The unique combination of versioned components compose a unique version of the
entire system. The format for specifying a multi repo version is JSON:

```
{
    "name": "scrolls",
    "version": "1.0.0",
    "repos": {
        "scrolls-core": {"name": "scrolls-core", "version": "1.0.0"},
        "scrolls-api": {"name": "scrolls-api", "version": "1.0.0"},
    }
}
```

Use the `--multirepo (-m)` option to tell scrolls to process the version input as multiple repositories. You can
reference the json as files:
```
    scrolls --multirepo --old-version file://version-1.0.0.json --new-version file://version-1.0.1.json
```

## Plugins
Plugins are built by implementing the ScrollsPlugin interface and configuring them in the configuration file. Scrolls 
ships with a few standard plugins to be usable out of the box. Currently, Git support is provided by the GitPlugin
which knows how to pull data from a git repository. Plugins are allowed to depend on input from other plugins,
it's up to the plugin author to make sure the plugin works with the input it will receive (and to inform the user of the
required input).

## Reports
Reports are based on FreeMarker templates with a default report layout provided. Users are free to override the global 
template, the stylesheet or individual parts of the template (there's one individual part per plugin). Overrides work by 
using the --templates option. Specify a directory and place your own .ftl files in this directory, naming them to 
indicate which template you want to override (as explains in the below list).

 * scrolls-html.ftl for the main/global template
 * &lt;pluginname&gt;.ftl to override just a plugins part of the template (e.g. git.ftl)
 * scrolls.css to override the stylesheet used (It too is a FreeMarker template, just named differently)

## Extend
