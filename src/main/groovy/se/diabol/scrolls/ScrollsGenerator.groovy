package se.diabol.scrolls

import freemarker.cache.ClassTemplateLoader
import freemarker.cache.FileTemplateLoader
import freemarker.cache.MultiTemplateLoader
import freemarker.cache.TemplateLoader
import freemarker.template.*
import org.reflections.Reflections

import java.nio.charset.StandardCharsets;

class ScrollsGenerator {

    def config
    def freemarkerConfig

    static Map plugins = [:]
    static {
        println "Scanning for plugins..."
        new Reflections('se.diabol.scrolls').getSubTypesOf(ScrollsPlugin).each {
            Class<ScrollsPlugin> pluginClass = Class.forName(it.name) as Class<ScrollsPlugin>
            ScrollsPlugin plugin = pluginClass.getConstructor().newInstance()
            plugins[plugin.getName()] = plugin
            println "  ${plugin.getName()} initialized"
        }
        println "...plugin scanning done"
    }

    ScrollsGenerator(config) {
        this.config = config
        this.freemarkerConfig = initializeFreemarker(config)
    }

    /**
     * Initialize FreeMarker with support for loading templates from both path (when set with --templates option) and
     * classpath resource (which is the default)
     *
     * @param config
     * @return
     */
    private Configuration initializeFreemarker(config) {
        Configuration freemarkerConfig = new Configuration()
        freemarkerConfig.defaultEncoding = StandardCharsets.UTF_8.name()
        freemarkerConfig.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        if (config.templates) {
            TemplateLoader[] loaders = new TemplateLoader[2]
            loaders[0] = new FileTemplateLoader(new File(config.templates as String))
            loaders[1] = new ClassTemplateLoader(getClass(), '/')
            freemarkerConfig.setTemplateLoader(new MultiTemplateLoader(loaders))
        } else {
            freemarkerConfig.setClassForTemplateLoading(getClass(), '/')
        }

        return freemarkerConfig
    }

    def generateHtmlReport(Map header, Map reports, String outputFile) {
        def templateNameToRead = config.scrolls.templateName ?: 'scrolls-html.ftl'
        Template template = freemarkerConfig.getTemplate(templateNameToRead)

        println "Read template ${templateNameToRead}: " + (template ? "ok" : "not ok")
        Map binding = [header: header]
        binding.reports = reports

        new File(outputFile).withWriter {
            template.process(binding, it)
        }
    }

    def generate(String oldVersion, String newVersion, String outputFile) {
        Map header = [
                component: config.scrolls.component,
                date: new Date().format("yyyy-MM-dd HH:mm:ss"),
                oldVersion: oldVersion,
                newVersion: newVersion,
        ]

        Map reports = [:]
        plugins.each { name, plugin ->
            def pluginConfig = config."${name}" as Map
            reports[name] = plugin.generate(pluginConfig, oldVersion, newVersion)
        }

        /* TODO: Jira report depends on commit log, need to figure out a good way to deal with that
        def repositoryReport = getRepositoryReport(oldVersion, newVersion)
        def jiraReport = null
        if (repositoryReport) {
            println "\nRepository report:\n\n${repositoryReport}\n\n"
            println "\n*** Commits: " + repositoryReport.commits

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

            jiraReport = jr.createJiraReport(repositoryReport.commits)
            println "\n*** JiraInfo: " + jiraReport
        }*/

        generateHtmlReport(header, reports, outputFile)
    }

    static void main(String[] args) {
        def options = parseOptions(args)
        if (!options) {
            return
        }

        String output = options.output ?: "Scrolls.html"
        def config = readConfig(options.config)

        try {
            def scrollsGenerator = new ScrollsGenerator(config)
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
        cli.o(longOpt: 'output', args: 1, 'Output file name [./Scrolls.html]')
        cli.t(longOpt: 'templates', args: 1, 'Override default templates from this directory')

        def options = cli.parse(args)

        if (options && options.help) {
            cli.usage()
            println "---Plugins---"
            plugins.each { name, plugin ->
                println "${name} config"
                plugin.getConfigInfo().each { item, desc ->
                    println "  ${item}: ${desc}"
                }
            }
            return null
        } else {
            return options
        }
    }

    static readConfig(fileName) {
        URL path
        if (fileName) {
            path = new File(fileName).toURI().toURL()
        } else {
            path = ScrollsGenerator.class.getClassLoader().getResource("scrolls-config.groovy")
        }

        println "Reading configuration from: ${path}"
        return new ConfigSlurper().parse(path)
    }
}
