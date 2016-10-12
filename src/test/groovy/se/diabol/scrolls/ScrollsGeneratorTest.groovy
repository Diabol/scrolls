package se.diabol.scrolls

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import spock.lang.Specification

@RunWith(Sputnik)
class ScrollsGeneratorTest extends Specification {

    private PrintStream outBefore;
    def releaseNotesFilePath = "build/ReleaseNotes.html";

    def setup() {
        outBefore = System.out;
    }

    def cleanup() {
        System.setOut(outBefore);
    }

    def "generate html report"() {
        setup: "Remove old release notes"
        "rm -f ${releaseNotesFilePath}"

        when: "generating html report"
        ScrollsGenerator generator = new ScrollsGenerator();
        generator.generateHtmlReport(headerDataMock, gitDataMock, jiraDataMock, null, null, null, releaseNotesFilePath);

        then: "html report is generated without exceptions"
        assert "ls ${releaseNotesFilePath}".execute().getText() == "${releaseNotesFilePath}\n"
        def htmlContent = new File(releaseNotesFilePath).text
        assert htmlContent.contains("<td class=\"label\" class>New version:</td><td>V2-new</td>")
        assert htmlContent.contains("<p>Total 155 changes by 3 people, total 20 files</p>")
        assert htmlContent.contains("<p>2 Jira issues from 1 stories and 1 epics affected</p>")
    }

    def "no release request should be generated when missing environment arg"() {

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));

        given: "out is a release request in jira"

        when: "no environment argument provided when running rel req gen"
        ScrollsGenerator.invokeMethod("main", ["-v1", "0.0.9", "-v2", "1.0.0", "-o", "jira", "-s", "chips-helloworld"].toArray())

        then: "something happens"
        String out = new String(myOut.toByteArray(), "UTF-8");
        assert out.contains("You must specify environment when creating jira release request")
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
