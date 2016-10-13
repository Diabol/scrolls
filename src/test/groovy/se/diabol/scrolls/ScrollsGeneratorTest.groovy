package se.diabol.scrolls

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@RunWith(Sputnik)
class ScrollsGeneratorTest extends Specification {

    private PrintStream outBefore
    def scrollsOutputFile = "build/Scrolls.html"

    def setup() {
        outBefore = System.out
    }

    def cleanup() {
        System.setOut(outBefore)
    }

    def "generate html report"() {
        setup: "Remove old release notes"
        "rm -f ${scrollsOutputFile}"

        when: "generating html report"
        ScrollsGenerator generator = new ScrollsGenerator();
        generator.generateHtmlReport(headerDataMock, gitDataMock, jiraDataMock, null, scrollsOutputFile);

        then: "html report is generated without exceptions"
        assert "ls ${scrollsOutputFile}".execute().getText() == "${scrollsOutputFile}\n"
        def htmlContent = new File(scrollsOutputFile).text
        assert htmlContent.contains("<td class=\"label\" class>New version:</td><td>V2-new</td>")
        assert htmlContent.contains("<p>Total 155 changes by 3 people, total 20 files</p>")
        assert htmlContent.contains("<p>2 Jira issues from 1 stories and 1 epics affected</p>")
    }

    def 'no scrolls generated when environment or version1 options are missing'() {
        final ByteArrayOutputStream sout = new ByteArrayOutputStream()
        System.setOut(new PrintStream(sout))

        //given: 'nothing'
        when: 'no version1 given'
        ScrollsGenerator.invokeMethod('main', ['-v2', '1.0.0'].toArray())

        then: "error is returned"
        String out = sout.toString(StandardCharsets.UTF_8.name())
        assert out.contains('Either option e (environment) or v1 (version1) must be specified')
    }

    static def headerDataMock = [
            component: "componentA",
            environment: "CI",
            date: new Date().format("yyyy-MM-dd HH:mm:ss"),
            oldVersion: "V1-old",
            newVersion: "V2-new",
            jenkinsUrl: "http://localhost"
    ]

    static def gitDataMock = [
            summary: [
                    nbrOfChanges: 155,
                    nbrOfPeople: 3,
                    nbrOfFiles: 20
            ],
            modules: [],
            commits: [[
                    rev: "rev",
                    author: "me",
                    date: new Date(),
                    message: "DM-666 The evil bug fixed",
                    files: new ArrayList<>(),
                    nbrOfFiles: 0
            ]]
    ]

    static def jiraDataMock = [
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
