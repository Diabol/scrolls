package se.diabol.scrolls.plugins

import se.diabol.scrolls.plugins.GitPlugin
import spock.lang.Specification

/**
 * Spock test cases for the GitPlugins
 */
class GitPluginTest extends Specification {

    def "single repository config should generate a valid git report"() {
        given: 'a single repository config for the scrolls project'
            Map report = null
            ConfigObject config = new ConfigSlurper().parse("""
                cmd = "git --no-pager"
                repositoryRoot = "./"
                moduleRegexps = [
                    "scrolls": "^/src/.*"
                ]
                changeTypeRegexps = [
                    "engine": "^src/main/grovy/se/diabol/scrolls/engine.*",
                    "plugins": "^src/main/groovy/se/diabol/scrolls/plugins.*",
                    "config": "^src/main/resources/.*\\\\.groovy",
                    "templates": "^src/main/resources/.*\\\\.ftl",
                    "test": ".*/src/test.*",
                    "buil": ".*/build\\\\.gradle"
                ]
            """)
            GitPlugin gitPlugin = new GitPlugin(config: config)

        when: 'a report is generated between version t0.1 and t0.2'
            report = gitPlugin.generate([old: [name: 'scrolls', version: 't0.1'], new: [name: 'scrolls', version: 't0.2']])

        then: 'the report should contain 1 commits from 1 person with 7 files changed'
        println report
            report != null
            report.summary.nbrOfChanges == 1
            report.summary.nbrOfPeople == 1
            report.summary.nbrOfRespoitories == 1
            report.summary.nbrOfFiles == 7
    }

    def "multi repository config should generate a valid git report"() {
        given: 'a multi repository config for the scrolls project'
            Map report = null
            String dir = System.getProperty("user.dir") ?: 'scrolls'
            String[] dirParts = dir.contains('/') ? dir.split('/') : dir.split("\\\\")
            def module = dirParts[dirParts.length-1]
            def oldVersion = [
                    name: 'scrolls',
                    version: 't0.1',
                    repos: [
                            scrolls: [name: "$module", version: "t0.1"]
                    ]
            ]
            def newVersion = [
                    name: 'scrolls',
                    version: 't0.2',
                    repos: [
                            scrolls: [name: "$module", version: "t0.2"]
                    ]
            ]
            ConfigObject config = new ConfigSlurper().parse("""
                    cmd = "git --no-pager"
                    repositoryRoot = "../"
                    moduleRegexps = [
                        "scrolls": "src/.*"
                    ]
                    changeTypeRegexps = [
                        "engine": "^src/main/grovy/se/diabol/scrolls/engine.*",
                        "plugins": "^src/main/groovy/se/diabol/scrolls/plugins.*",
                        "config": "^src/main/resources/.*\\\\.groovy",
                        "templates": "^src/main/resources/.*\\\\.ftl",
                        "test": ".*/src/test.*",
                        "buil": ".*/build\\\\.gradle"
                    ]
                """)
            GitPlugin gitPlugin = new GitPlugin(config: config)

        when: 'a report is generated between version t0.1 and t0.2'
            report = gitPlugin.generate([old: oldVersion, new: newVersion])

        then: 'the report should contain 1 commits from 1 person with 7 files changed'
            println report
            report != null
            report.summary.nbrOfChanges == 1
            report.summary.nbrOfPeople == 1
            report.summary.nbrOfRespoitories == 1
            report.summary.nbrOfFiles == 7
    }
}
