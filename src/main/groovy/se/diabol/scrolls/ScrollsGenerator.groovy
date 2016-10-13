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

    static void main(String[] args) {
        def cli = new CliBuilder()
        cli.h(longOpt: 'help', required: false, 'show usage information')
        cli.e(longOpt: 'environment', argName: 'environment', required: false, args: 1, 'The environment to check version against')
        cli.v1(longOpt: 'version1', argName: 'version1', required: false, args: 1, 'If no environment is specified this is the version to compare with')
        cli.v2(longOpt: 'version2', argName: 'version2', required: true, args: 1, 'The second version to compare with')
        cli.r(longOpt: 'repositoryRoot', argName: 'repositoryRoot', required: false, args: 1, 'Git repositories root dir here to find all components [./]')
        cli.c(longOpt: 'configPath', argName: 'configPath', required: false, args: 1, 'Path to configPath file')
        cli.o(longOpt: 'output', argName: 'fileName', required: false, args: 1, 'Output file name [./Scrolls.html]')
        cli.f(longOpt: 'failsafe',  required: false, 'Should script fail on errors? [false]')
        cli.t(longOpt: 'template',  required: false, args: 1, 'Path to FreeMarker html template [./]')
        cli.oc(longOpt: 'omitClosed',  required: false, 'Omit closed issues when linking commits? [true]')
        cli.opt(longOpt: 'options',  required: false, args: 1, 'Logging option params for git log')

        def opt = cli.parse(args)
        if (!opt) { return }
        if (opt.help) {
            cli.usage();
            return
        }

        def environment = opt.environment == "--" ? null : opt.environment
        def version1 = opt.version1 == "--" ? null : opt.version1
        def version2 = opt.version2
        def configPath = opt.'configPath'
        def output = opt.output ?: "Scrolls.html"
        def template = opt.template ?: null
        boolean failsafe = opt.failsafe

        def configUrl = configPath ? new File(configPath).toURI().toURL() : ScrollsGenerator.class.getClassLoader().getResource("scrolls-config.groovy")

        println "Reading config from: ${configUrl}"
        def config = new ConfigSlurper().parse(configUrl)

        config.put('omitClosed', opt.oc)

        if (opt.opt) {
            config.put('logOptions', opt.opt)
        } else {
            config.put('logOptions', '')
        }

        if (!(environment || version1)) {
            println "Either option e (environment) or v1 (version1) must be specified"
            return
        }

        try {
            def rnc = new ScrollsGenerator(config:  config)
            rnc.generateScrolls(environment, version1, version2, template, output)
        } catch (Exception e) {
            println "Failed to create release notes for env ${environment} from version ${version1} to version ${version2}"
            e.printStackTrace()
            new File(output).withPrintWriter {writer ->
                writer.println "Failed to create release notes for env ${environment} from version ${version1} to version ${version2} with remote ${remote}"
                e.printStackTrace(writer)
            }
            if (!failsafe) {
                System.exit(1)
            }
        }
    }
}
