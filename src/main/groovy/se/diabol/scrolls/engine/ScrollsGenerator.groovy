package se.diabol.scrolls.engine

import freemarker.cache.ClassTemplateLoader
import freemarker.cache.FileTemplateLoader
import freemarker.cache.MultiTemplateLoader
import freemarker.cache.TemplateLoader
import freemarker.template.*

import java.nio.charset.StandardCharsets;

class ScrollsGenerator {

    def config
    Configuration freemarkerConfig
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
        freemarkerConfig.logTemplateExceptions = false

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

        Template template
        try {
            print "Parsing template ${templateNameToRead}..."
            template = freemarkerConfig.getTemplate(templateNameToRead)
            println "OK"
        } catch (IOException e) {
            println "FAIL!"
            println "ERROR: ${e.message}"
        }

        Map dataModel = [header: header, reports: reports]

        new File(outputFile).withWriter {
            try {
                print "Processing template..."
                template.process(dataModel, it)
                println "OK"
            } catch (TemplateException e) {
                println "FAIL!"
                println "ERROR: ${e.message}"
                println "ERROR: DataModel: ${dataModel}"
            }
        }
    }

    def generate(Map oldVersion, Map newVersion, String outputFile) {
        Map header = [
                component: config.scrolls.component,
                date: new Date().format("yyyy-MM-dd HH:mm:ss"),
                oldVersion: oldVersion.version,
                newVersion: newVersion.version,
        ]

        println "Collecting data..."

        Map versions = [old: oldVersion.version, new: newVersion.version]
        Map reports = [:]
        Map executions = buildExecutionMap()

        // Run the plugins that require versions (and make sure we drop them from further execution)
        executions.remove('versions').each {
            print "  from ${it.name}..."
            reports[it.name] = it.plugin.generate(versions)
            println "OK"
        }

        // TODO: Replace hackish loopCounter with topological sorting of dependencies (with cycle detection before running!)
        int loopCounter = 0
        while (executions.keySet().size() > 0) {
            def names = executions.keySet()
            names.each {
                if (it in reports) {
                    executions[it].each {
                        print "  from ${it.name}..."
                        reports[it.name] = it.plugin.generate(reports[it.config.inputFrom])
                        println "OK"
                    }
                    executions.remove(it)
                } else {
                    loopCounter += 1
                }
            }

            if (loopCounter == 10) {
                throw new RuntimeException("Failed to resolve plugin dependencies, please make sure your configuration has no cycles.")
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
