package se.diabol.scrolls

import freemarker.cache.ClassTemplateLoader
import freemarker.cache.FileTemplateLoader
import freemarker.cache.MultiTemplateLoader
import freemarker.cache.TemplateLoader
import freemarker.template.*

import java.nio.charset.StandardCharsets;

class ScrollsGenerator {

    def config
    def freemarkerConfig
    def plugins

    ScrollsGenerator(config, options, plugins) {
        this.config = config
        this.freemarkerConfig = initializeFreemarker(options.templates)
        this.plugins = plugins
    }

    /**
     * Initialize FreeMarker with support for loading templates from both path (when set with --templates option) and
     * classpath resource (which is the default)
     *
     * @param config
     * @return
     */
    private Configuration initializeFreemarker(templates) {
        Configuration freemarkerConfig = new Configuration()
        freemarkerConfig.defaultEncoding = StandardCharsets.UTF_8.name()
        freemarkerConfig.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        if (templates) {
            TemplateLoader[] loaders = new TemplateLoader[2]
            loaders[0] = new FileTemplateLoader(new File(templates as String))
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
        Map binding = [header: header, reports: reports]

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

        Map versions = [old: oldVersion, new: newVersion]
        Map reports = [:]
        Map executions = buildExecutionMap()

        // Run the plugins that require versions (and make sure we drop them from further execution)
        executions.remove('versions').each {
            reports[it.name] = it.plugin.generate(versions)
        }

        // TODO: Replace hackish loopCounter with topological sorting of dependencies (with cycle detection before running!)
        int loopCounter = 0
        while (executions.keySet().size() > 0) {
            def names = executions.keySet()
            names.each {
                if (it in reports) {
                    executions[it].each {
                        reports[it.name] = it.plugin.generate(reports[it.config.inputFrom])
                    }
                    executions.remove(it)
                } else {
                    loopCounter += 1
                }
            }

            if (loopCounter == 10) {
                println "Failed to resolve plugin dependencies, please make sure your configuration has no cycles"
                break
            }
        }

        generateHtmlReport(header, reports, outputFile)
    }

    def buildExecutionMap() {
        Map executions = [:]

        // Build call time lists (considering dependencies to be very simple)
        plugins.each {
            name, plugin ->
                def pluginConfig = config."${name}" as Map
                def depends = pluginConfig.inputFrom ?: 'versions'
                def insert = [name: name, plugin: plugin, config: pluginConfig]

                if (depends in executions) {
                    executions[depends] << insert
                } else {
                    executions[depends] = [insert]
                }
        }

        return executions
    }
}
