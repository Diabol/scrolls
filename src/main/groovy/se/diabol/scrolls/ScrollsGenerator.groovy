package se.diabol.scrolls

import freemarker.template.*
import org.reflections.Reflections

import java.nio.charset.StandardCharsets;

class ScrollsGenerator {

    def config
    static plugins = new Reflections('se.diabol.scrolls').getSubTypesOf(ScrollsPlugin)

    /*
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

        return jr.createJiraReport(commitComments)
    }
    */

    def generateHtmlReport(Map header, Map reports, String outputFile) {
        Configuration cfg = new Configuration()
        cfg.setClassForTemplateLoading(getClass(), "/")
        cfg.defaultEncoding = StandardCharsets.UTF_8.name()
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        def templateNameToRead = config.templateName ?: 'scrolls-template.html'
        Template template = cfg.getTemplate(templateNameToRead)

        println "Read template ${templateNameToRead}: " + (template ? "ok" : "not ok")
        Map binding = [header: header]
        binding.reports = reports

        new File(outputFile).withWriter {
            template.process(binding, it)
        }
    }

    def generateScrolls(String oldVersion, String newVersion, String outputFile) {
        Map header = [
                component: config.component,
                date: new Date().format("yyyy-MM-dd HH:mm:ss"),
                oldVersion: oldVersion,
                newVersion: newVersion,
        ]

        Map reports
        plugins.each {
            Class<ScrollsPlugin> pluginClass = Class.forName(it.name) as Class<ScrollsPlugin>
            ScrollsPlugin plugin = pluginClass.getConstructor().newInstance()

            def pluginConfig = config.(plugin.getName()) as Map
            reports[plugin.getName()] = plugin.generate(pluginConfig, oldVersion, newVersion)
        }

        /*
        def repositoryReport = getRepositoryReport(oldVersion, newVersion)
        def jiraReport = null
        if (repositoryReport) {
            println "\nRepository report:\n\n${repositoryReport}\n\n"
            println "\n*** Commits: " + repositoryReport.commits

            jiraReport = getJiraInfo(repositoryReport.commits)
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
            def rnc = new ScrollsGenerator(config:  config)
            rnc.generateScrolls(options.'old-version' as String, options.'new-version' as String, output)
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

        cli.h(longOpt: 'help', required: false, 'show usage information')
        cli.c(longOpt: 'config', required: false, args: 1, 'Path to config file')
        cli.o(longOpt: 'output', required: false, args: 1, 'Output file name [./Scrolls.html]')

        def options = cli.parse(args)

        if (options && options.help) {
            cli.usage()
            plugins.each {  // TODO: This needs to use the actual instances...
                println it.name
            }
            return null
        } else {
            return options
        }
    }

    static readConfig(def config) {
        def path
        if (config) {
            path = new File(config).toURI().toURL()
        } else {
            path = ScrollsGenerator.class.getClassLoader().getResource("scrolls-config.groovy")
        }

        println "Reading configuration from: ${path}"
        return new ConfigSlurper().parse(path)
    }
}
