package se.diabol.scrolls.engine

import groovy.json.JsonSlurper
import se.diabol.scrolls.plugins.ScrollsPlugin

class Scrolls {
    static void main(String[] args) {
        def options = parseOptions(args)
        if (!options) {
            println "Failed to parse options"
            System.exit ExitCodes.FAILED_TO_PARSE_OPTIONS
        }

        Map oldVersion = [:]
        Map newVersion = [:]
        if (options.multirepo) {
            oldVersion = validateMultiRepo(options.'old-version')
            newVersion = validateMultiRepo(options.'new-version')
        } else {
            oldVersion.name = options.name? options.name : ''
            oldVersion.version = options.'old-version'
            newVersion.name = options.name? options.name : ''
            newVersion.version = options.'new-version'
        }

        if (options.help) {
            displayPluginHelp(options)
            System.exit ExitCodes.OK
        }

        if (!validateRequiredOptions(options)) {
            System.exit ExitCodes.MISSING_REQUIRED_OPTIONS
        }

        def (config, plugins) = initialize(options)

        // Set output directory where CLI takes precedence over config file and fail if it already exists (would mean overwrite!)
        config.scrolls.outputDirectory = options.output ?: config.scrolls.outputDirectory
        if (new File(config.scrolls.outputDirectory as String).exists()) {
            println "ERROR: Output directory already exists, bailing out"
            System.exit ExitCodes.RUNTIME_FAILURE
        }

        try {
            def scrollsGenerator = new ScrollsGenerator(config, options, plugins)
            scrollsGenerator.generate(oldVersion, newVersion)
        } catch (all) {
            def msg = "ERROR: Failed to create scrolls for versions ${options.'old-version'} to ${options.'new-version'}"
            println msg
            all.printStackTrace()
            System.exit ExitCodes.RUNTIME_FAILURE
        }
    }

    static displayPluginHelp(options) {
        def (_, plugins) = initialize(options)
        println "---Plugins---"
        plugins.each { name, plugin ->
            println "${name} config"
            plugin.getConfigInfo().each { item, desc ->
                println "  ${item}: ${desc}"
            }
        }
    }

    static Tuple initialize(options) {
        def config = null
        try {
            config = readConfig(options.config)
        } catch (all) {
            println "ERROR: ${all.message}, aborting..."
            System.exit ExitCodes.FAILED_TO_PARSE_CONFIG
        }

        validatePluginInputs(config)

        def plugins = null
        try {
            plugins = initializePlugins(config)
        } catch (RuntimeException e) {
            println "ERROR: ${e.message}, aborting..."
            System.exit ExitCodes.FAILED_TO_INITIALIZE
        }

        return new Tuple(config, plugins)
    }

    static validatePluginInputs(config) {
        def availableInputs = config.keySet().findAll { it != 'scrolls' }
        availableInputs << 'versions'

        config.each { key, values ->
            if (key != 'scrolls' && !(values.inputFrom in availableInputs)) {
                println "ERROR: Config section ${key} is configured with inputFrom: ${values.inputFrom}, but ${values.inputFrom} does not exist in the configuration. Possibly typo?"
                System.exit ExitCodes.FAILED_TO_PARSE_CONFIG
            }
        }
    }

    static validateRequiredOptions(options) {
        def valid = true
        // We avoid using required: with CliBuilder, as it reports errors when only --help is used.
        if (!options.'old-version') {
            println "ERROR: Missing required option: old-version"
            valid = false
        }
        if (!options.'new-version') {
            println "ERROR: Missing required option: new-version"
            valid = false
        }

        return valid
    }

    static OptionAccessor parseOptions(String[] args) {
        def cli = new CliBuilder(usage: 'scrolls --old-version 1.0.0 --new-version 2.0.0')
        cli._(longOpt: 'old-version', args: 1, 'The old version to compare with.')
        cli._(longOpt: 'new-version', args: 1, 'The new version to compare with.')

        cli.h(longOpt: 'help', 'Show usage information.')
        cli.c(longOpt: 'config', args: 1, 'Path to config file.')
        cli.o(longOpt: 'output', args: 1, "Output directory. Defaults to scrolls-report.")
        cli.t(longOpt: 'templates', args: 1, 'Override default templates from this directory.')
        cli.m(longOpt: 'multirepo', args: 0, 'Use multiple repositories, requires a json structure input of old- and new version')

        def options = cli.parse(args)

        if (options && options.help) {
            cli.usage()
        }

        if (options?.multirepo) {
            options.'old-version' = validateMultiRepo(options.'old-version')
            options.'new-version' = validateMultiRepo(options.'new-version')
        }

        return options
    }

    static Map validateMultiRepo(String version) {
        def multiversion = null
        if (version?.startsWith("{")) {
             multiversion = new JsonSlurper().parse(version.toCharArray())
        } else if (version?.startsWith('file://')) {
            def file = new File(version.substring(7)).toURI().toURL()
            multiversion = new JsonSlurper().parse(file)
        } else  {
            println "unsupported multirepo configuration"
            assert false
        }
        assert multiversion?.name != null
        assert multiversion?.version != null
        multiversion?.repos.values().each {
            assert it.name != null
            assert it.version != null
        }
        return multiversion
    }

    static readConfig(fileName) {
        URL path
        if (fileName) {
            path = new File(fileName as String).toURI().toURL()
        } else {
            path = Scrolls.class.getClassLoader().getResource("scrolls-config.groovy")
        }

        println "Reading configuration from: ${path}"
        return new ConfigSlurper().parse(path)
    }

    static Map initializePlugins(config) {
        def plugins = [:]
        println "Scanning config for plugins"
        config.each { key, items ->
            if (key != 'scrolls') { // Ignore scrolls config section, all other sections are assumed to be plugin sections
                items.outputDirectory = config.scrolls.outputDirectory
                try {
                    ScrollsPlugin plugin = Class.forName(items.plugin as String).newInstance(config: items) as ScrollsPlugin
                    plugins[plugin.name] = plugin
                    println "  ${plugin.name} plugin initialized"
                } catch (ClassNotFoundException e) {
                    println "  ERROR: Failed to load ${items.plugin} (${e.message} not found). Is your classpath correct?"
                    throw new RuntimeException("Plugin initialization failed")
                }
            }
        }
        return plugins
    }
}
