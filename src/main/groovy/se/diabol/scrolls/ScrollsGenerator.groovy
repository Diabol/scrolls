package se.diabol.scrolls

import freemarker.template.*;

class ScrollsGenerator {

    def config

    def getRepositoryInfo(version1, version2) {
        def repositoryInfo
        if (config.repositoryType == "git") {
            println "\nUsing GitReportGenerator..."
            GitReportGenerator reportGenerator = new GitReportGenerator(
                    modulesRegexps: config.moduleRegexps,
                    changeTypeRegexps: config.changeTypeRegexps
            )
            repositoryInfo = reportGenerator.createReport(version1, version2, config.logOptions)
        } else {
            throw new IllegalArgumentException("Unsupported repository type: ${config.repositoryType}")
        }
        return repositoryInfo
    }

    def getLinkedJiraIssues(jiraInfo) {
        def linkedIssues = []

        if (!jiraInfo) {
            return linkedIssues
        }

        jiraInfo.issues.each { issue->
            if ("Epic".equals(issue.type)) {
                issue.stories.each { story->
                    linkedIssues.add(story.key)
                }
            } else {
                linkedIssues.add(issue.key)
            }
        }

        return linkedIssues
    }

    def getJiraInfo(commitComments) {
        JiraReportGenerator jr = new JiraReportGenerator(
            baseUrl: config.jiraBaseUrl,
            username: config.jiraUsername,
            password: config.jiraPassword,
            iconEpic: config.iconEpic,
            iconBug: config.iconBug,
            iconStory: config.iconStory,
            iconTask: config.iconTask,
            iconFeature: config.iconFeature,
            excludeClosedIssues: config.omitClosed
        )

        def jiraInfo = jr.createJiraReport(commitComments)
        return jiraInfo
    }

    def generateHtmlReport(header, svn, jira, tests, sonar, template, outputfile) {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(getClass(),"/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        Template temp;
        if (template!=null) {
            temp = cfg.getTemplate(template);
        } else {
            temp = cfg.getTemplate("releasenotes-template.html");
        }
        println "Read template ${template}: "+ (temp ? "ok" : "not ok")
        Map binding = [header: header]
        if (svn!=null) binding.repository = svn
        if (jira!=null) binding.jira = jira
        if (tests!=null) binding.tests = tests
        if (sonar!=null) binding.sonar = sonar
        Writer out = new OutputStreamWriter(new FileOutputStream(outputfile));
        temp.process(binding, out);
        out.close();
    }

    def generateReleaseNotes(environment, remote, version1, version2, template, outputfile, user, service) {
        def header = [
                component: config.component,
                environment: environment? environment : "",
                date: new Date().format("yyyy-MM-dd HH:mm:ss"),
                oldVersion: version1,
                newVersion: version2,
                jenkinsUrl: config.jenkinsUrl? config.jenkinsUrl : ""
        ]

        def repoInfo = getRepositoryInfo(version1, version2)

        def jiraInfo
        if (repoInfo != null) {
            println "\nRepository report:\n\n${repoInfo}\n\n"
            println "\n*** Commits: " + repoInfo.commits

            jiraInfo = getJiraInfo(repoInfo.commits)
            println "\n*** JiraInfo: " + jiraInfo
        }

        def watchers = []

        repoInfo?.commits.each { commit ->
          if ("git".equals(config.repositoryType)) {
             watchers.add(commit.email)
             println("Added: ${commit.email} to watchers list")
          } else {
             watchers.add(commit.author)
             println("Added: ${commit.author} to watchers list")
          }
        }
        generateHtmlReport(header, repoInfo, jiraInfo, null, null, template, outputfile)
    }

    static void main(String[] args) {
        def cli = new CliBuilder()
        cli.h( longOpt: 'help', required: false, 'show usage information' )
        cli.e( longOpt: 'environment', argName: 'environment', required: false, args: 1, 'The environment to check version against')
        cli.v1( longOpt: 'version1', argName: 'version1', required: false, args: 1, 'If no environment is specificed this is the version to compare with' )
        cli.v2( longOpt: 'version2', argName: 'version2', required: true, args: 1, 'The second version to compare with')
        cli.r( longOpt: 'repositoryRoot', argName: 'repositoryRoot', required: false, args: 1, 'Git repositories root dir here to find all components [./]')
        cli.c( longOpt: 'configPath', argName: 'configPath', required: false, args: 1, 'Path to configPath file')
        cli.o( longOpt: 'output', argName: 'fileName', required: false, args: 1, 'Output file name [./ReleaseNotes.html]')
        cli.f( longOpt: 'failsafe',  required: false, 'Should script fail on errors? [false]' )
        cli.t( longOpt: 'template',  required: false, args: 1, 'Path to freemaker html template [./]' )
        cli.u( longOpt: 'user',  required: false, args: 1, 'Current user []' )
        cli.z( longOpt: 'remote',  required: false, args: 1, 'If set, perform a remote scm log when collecting log information. Otherwise operate on the local directory []' )
        cli.oc( longOpt: 'omitClosed',  required: false, 'Omit closed issues when linking commits? [true]' )
        cli.s( longOpt: 'service', argName: 'service', required: true, args: 1, 'The service being deployed')
        cli.os( longOpt: 'omitSonar',  required: false, 'Omit sonar metrics from report? [false]' )
        cli.opt( longOpt: 'options',  required: false, args: 1, 'Logging option params for git log' )

        def opt = cli.parse(args)
        if (!opt) { return }
        if (opt.h) {
            cli.usage();
            return
        }

        def env = opt.e.equals("--") ? null : opt.e
        def version1 = opt.v1.equals("--") ? null : opt.v1
        def version2 = opt.v2
        def reposRoot = opt.r
        def configPath = opt.c
        def out = opt.o ? opt.o : "ReleaseNotes.html"
        def template = opt.t ? opt.t : null
        boolean failsafe = opt.f
        def user = opt.u
        def remote = opt.z
        def service = opt.s

        def configUrl = configPath? new File(configPath).toURI().toURL() : ScrollsGenerator.class.getClassLoader().getResource("releasenotes-config.groovy")

        println "Reading config from: ${configUrl}"
        def config = new ConfigSlurper().parse(configUrl)

        if (opt.oc) {
            config.put('omitClosed', true)
        } else {
            config.put('omitClosed', false)
        }

        if (opt.os) {
            config.put('omitSonar', true)
        } else {
            config.put('omitSonar', false)
        }

        if (opt.opt) {
            config.put('logOptions', opt.opt)
        } else {
            config.put('logOptions', "")
        }

        if (!(env || version1)) {
            println "Either option e (environment) or v1 (version1) must be specified"
            return
        }

        if (out =='jira' && !env) {
            println( "You must specify environment when creating jira release request" )
            return
        }

        try {
            def rnc = new ScrollsGenerator(config:  config)
            rnc.generateReleaseNotes(env, remote, version1, version2, template, out, user, service)
        } catch (Exception e) {
            println "Failed to create release notes for env ${env} from version ${version1} to version ${version2} with remote ${remote}"
            e.printStackTrace()
            new File(out).withPrintWriter {writer ->
                writer.println "Failed to create release notes for env ${env} from version ${version1} to version ${version2} with remote ${remote}"
                e.printStackTrace(writer)
            }
            if (!failsafe) {
                System.exit(1)
            }
        }
    }
}
