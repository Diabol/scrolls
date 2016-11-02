package se.diabol.scrolls.engine

import spock.lang.Specification

class ScrollsGeneratorTest extends Specification {

    def outputDirectory = 'build/scrolls-test-report'
    def indexFile = "${outputDirectory}/index.html"

    private def setup() {
        // We clean before a test is run and after, to be sure we have a clean state
        "rm -rf ${outputDirectory}".execute()
    }

    private def cleanup() {
        // We clean before a test is run and after, to be sure we have a clean state
        "rm -rf ${outputDirectory}".execute()
    }

    def "generate html report"() {
        given: "Generator with config to generate report to build dir"
        def config = [scrolls: [outputDirectory: outputDirectory]]
        ScrollsGenerator generator = new ScrollsGenerator(config, [templates: false], []);

        when: "generating html report with data mocks"
        generator.generateHtmlReport(headerDataMock, [git: gitDataMock, jira: jiraDataMock]);

        then: "html report is generated without exceptions"
        "ls ${indexFile}".execute().text == "${indexFile}\n"
        def htmlContent = new File(indexFile).text
        htmlContent.contains("<td class=\"label\">New version:</td><td>V2-new</td>")
        htmlContent.contains("<p>Total 155 changes by 3 people, total 20 files</p>")
        htmlContent.contains("<p>2 Jira issues from 1 stories and 1 epics affected</p>")
    }

    def headerDataMock = [
            component: "componentA",
            date: new Date().format("yyyy-MM-dd HH:mm:ss"),
            oldVersion: "V1-old",
            newVersion: "V2-new",
            templates: [git: "git.ftl", jira: "jira.ftl"]
    ]

    def gitDataMock = [
            summary: [
                    nbrOfChanges: 155,
                    nbrOfRepositories: 1,
                    nbrOfPeople: 3,
                    nbrOfFiles: 20
            ],
            modules: [],
            commits: [scrolls: [
                    [
                        rev: "rev",
                        author: "me",
                        date: new Date(),
                        message: "DM-666 The evil bug fixed",
                        files: new ArrayList<>(),
                        nbrOfFiles: 0
                    ]
            ]]
    ]

    def jiraDataMock = [
            summary: [
                    nbrOfIssues: 2,
                    nbrOfStories: 1,
                    nbrOfEpics: 1
            ],
            issues: [[
                    key: "key",
                    status: "NEW",
                    title: "SOMETHING",
                    link: "url",
                    type: "TYPE",
                    icon: "ICON",
                    stories: []
            ]]
    ]
}
