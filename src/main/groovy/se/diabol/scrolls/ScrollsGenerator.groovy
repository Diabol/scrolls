package se.diabol.scrolls

import freemarker.template.*

import java.nio.charset.StandardCharsets;

class ScrollsGenerator {

    def config

    def getRepositoryReport(version1, version2) {
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

    def generateHtmlReport(header, repository, jira, templateName, outputFile) {
        Configuration cfg = new Configuration()
        cfg.setClassForTemplateLoading(getClass(), "/")
        cfg.defaultEncoding = StandardCharsets.UTF_8.name()
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)

        def templateNameToRead = templateName ?: 'scrolls-template.html'
        Template template = cfg.getTemplate(templateNameToRead)

        println "Read template ${templateName}: " + (template ? "ok" : "not ok")
        Map binding = [header: header]
        if (repository) {
            binding.repository = repository
        }
        if (jira) {
            binding.jira = jira
        }

        def outFile = new File(outputFile)
        outFile.withWriter {
            template.process(binding, it)
        }
    }

    def generateScrolls(environment, version1, version2, templateName, outputFile) {
        def header = [
                component: config.component,
                environment: environment ? environment : "",
                date: new Date().format("yyyy-MM-dd HH:mm:ss"),
                oldVersion: version1,
                newVersion: version2,
                jenkinsUrl: config.jenkinsUrl? config.jenkinsUrl : ""
        ]

        def repositoryReport = getRepositoryReport(version1, version2)
        def jiraReport = null
        if (repositoryReport) {
            println "\nRepository report:\n\n${repositoryReport}\n\n"
            println "\n*** Commits: " + repositoryReport.commits

            jiraReport = getJiraInfo(repositoryReport.commits)
            println "\n*** JiraInfo: " + jiraReport
        }

        def watchers = []

        repositoryReport?.commits?.each { commit ->
            if ("git" == config.repositoryType) {
                watchers.add(commit.email)
                println("Added: ${commit.email} to watchers list")
            } else {
                watchers.add(commit.author)
                println("Added: ${commit.author} to watchers list")
            }
        }
        generateHtmlReport(header, repositoryReport, jiraReport, templateName, outputFile)
    }

    static OptionAccessor parseOptions(String[] args) {
        def cli = new CliBuilder()
        cli.h(longOpt: 'help', required: false, 'show usage information')
        cli._(longOpt: 'old-version', argName: 'oldVersion', required: true, args: 1, 'The old version to compare with')
        cli._(longOpt: 'new-version', argName: 'newVersion', required: true, args: 1, 'The new version to compare with')
        cli.c(longOpt: 'config', required: false, args: 1, 'Path to config file')
        cli.o(longOpt: 'output', required: false, args: 1, 'Output file name [./Scrolls.html]')

        def options = cli.parse(args)
        if (!options) {
            return
        }
        if (options.help) {
            cli.usage()
        }

        return options
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
            rnc.generateScrolls(options.oldVersion, options.newVersion, output)
        } catch (Exception e) {
            def msg = "Failed to create release notes for versions ${options.oldVersion} to ${options.newVersion}"
            println msg
            e.printStackTrace()
            new File(output).withPrintWriter {writer ->
                writer.println msg
                e.printStackTrace(writer)
            }
        }
    }

    static readConfig(config) {
        def path = config ? new File(config).toURI().toURL() : ScrollsGenerator.class.getClassLoader().getResource("scrolls-config.groovy")

        println "Reading configuration from: ${path}"
        return new ConfigSlurper().parse(path)
    }
}
