package se.diabol.scrolls

class Scrolls {
    static void main(String[] args) {
        def options = parseOptions(args)
        if (!options) {
            return
        }

        def config = readConfig(options.config)
        def plugins = initializePlugins(config)

        if (options.help) {
            println "---Plugins---"
            plugins.each { name, plugin ->
                println "${name} config"
                plugin.getConfigInfo().each { item, desc ->
                    println "  ${item}: ${desc}"
                }
            }
            return
        }

        String output = options.output ?: "Scrolls.html"

        try {
            def scrollsGenerator = new ScrollsGenerator(config, options, plugins)
            scrollsGenerator.generate(options.'old-version' as String, options.'new-version' as String, output)
        } catch (Exception e) {
            def msg = "Failed to create scrolls for versions ${options.'old-version'} to ${options.'new-version'}"
            println msg
            e.printStackTrace()
            new File(output).withPrintWriter {writer ->
                writer.println msg
                e.printStackTrace(writer)
            }
        }
    }

    static OptionAccessor parseOptions(String[] args) {
        def cli = new CliBuilder(usage: 'scrolls --old-version 1.0.0 --new-version 2.0.0')
        cli._(longOpt: 'old-version', required: true, args: 1, 'The old version to compare with')
        cli._(longOpt: 'new-version', required: true, args: 1, 'The new version to compare with')

        cli.h(longOpt: 'help', 'show usage information')
        cli.c(longOpt: 'config', args: 1, 'Path to config file')
        cli.o(longOpt: 'output', args: 1, 'Output file name [./scrolls.html]')
        cli.t(longOpt: 'templates', args: 1, 'Override default templates from this directory')

        def options = cli.parse(args)

        if (options && options.help) {
            cli.usage()
        }
        return options
    }

    static readConfig(fileName) {
        URL path
        if (fileName) {
            path = new File(fileName as String).toURI().toURL()
        } else {
            path = ScrollsGenerator.class.getClassLoader().getResource("scrolls-config.groovy")
        }

        println "Reading configuration from: ${path}"
        return new ConfigSlurper().parse(path)
    }

    static initializePlugins(config) {
        def plugins = [:]
        println "Scanning config for plugins..."
        config.each { key, items ->
            if (key != 'scrolls') { // Ignore scrolls config section, all other sections are assumed to be plugin sections
                ScrollsPlugin plugin = Class.forName(items.plugin as String).newInstance(config: items) as ScrollsPlugin
                plugins[plugin.name] = plugin
                println "  ${plugin.name} initialized"
            }
        }
        println "...plugin scanning done"
        return plugins
    }
}
