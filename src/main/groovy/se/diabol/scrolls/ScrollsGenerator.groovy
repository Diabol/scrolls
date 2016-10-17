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

        Map reports = [:]
        plugins.each { name, plugin ->
            def pluginConfig = config."${name}" as Map
            reports[name] = plugin.generate(pluginConfig, oldVersion, newVersion)
        }

        generateHtmlReport(header, reports, outputFile)
    }
}
