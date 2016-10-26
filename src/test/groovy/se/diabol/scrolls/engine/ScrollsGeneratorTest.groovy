package se.diabol.scrolls.engine

import se.diabol.scrolls.engine.ScrollsGenerator
import spock.lang.Specification

class ScrollsGeneratorTest extends Specification {

    def scrollsOutputFile = "build/Scrolls.html"

    def "generate html report"() {
        given: "Remove old release notes"
        "rm -f ${scrollsOutputFile}"

        when: "generating html report"
        ScrollsGenerator generator = new ScrollsGenerator([], [templates: false], []);
        generator.generateHtmlReport(headerDataMock, [git: gitDataMock, jira: jiraDataMock], scrollsOutputFile);

        then: "html report is generated without exceptions"
        "ls ${scrollsOutputFile}".execute().getText() == "${scrollsOutputFile}\n"
        def htmlContent = new File(scrollsOutputFile).text
        htmlContent.contains("<td class=\"label\">New version:</td><td>V2-new</td>")
        htmlContent.contains("<p>Total 155 changes by 3 people, total 20 files</p>")
        htmlContent.contains("<p>2 Jira issues from 1 stories and 1 epics affected</p>")
    }

    def headerDataMock = [
            component: "componentA",
            date: new Date().format("yyyy-MM-dd HH:mm:ss"),
            oldVersion: "V1-old",
            newVersion: "V2-new",
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
