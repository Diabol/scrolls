package se.diabol.scrolls

import com.sun.grizzly.http.SelectorThread
import spock.lang.Shared
import spock.lang.Specification

class JiraPluginTest extends Specification {

    static int jiraPort = 8282
    @Shared def st
    @Shared JiraPlugin jiraPlugin
    @Shared def config = [baseUrl: "http://localhost:${jiraPort}",
                          username: 'test',
                          password: 'qwerty',
                          omitClosed: true]

    def setupSpec() {
        setup: "Grizzly Adapter on port 8282 to simulate the Jira API"
        st = new SelectorThread()
        st.port = jiraPort
        st.adapter = new JiraAdapter()
        st.listen()

        and: "create the class under test: JiraPlugin"
        jiraPlugin = new JiraPlugin(config: config)
    }

    def cleanupSpec() {
        cleanup: "Stop the Grizzly Adapter"
        st.stopEndpoint()
    }

    def "getProjects should retrieve projects from jira rest api"() {
        when: "retrieving project list form jira"
        def projects = jiraPlugin.getProjects()

        then: "2 projects are found"
        projects.size() == 2
        projects.collect {it.key} == ['EX','EX2']
    }

    def "getIssue should retrieve issue from jira rest api"() {
        when: "retrieving issue from jira"
        def issue = jiraPlugin.getIssue("EX-1")

        then: "Issue is found and name and symmary is readable"
        issue?.key == 'EX-1'
        issue?.fields?.status?.name == 'Open'
        issue?.fields?.summary == 'example bug report'
    }

    def "generate should find jira refs and get info from jira api"() {
        when: "calling createJiraReport with a list of 3 commit comments"
        def issues = jiraPlugin.generate([commits: [
                "Comment with a correct jira ref EX-1, ok",
                "comment with an incorrect ref EX-2",
                "comment without jira ref",
                "Comment for a deployed issue (Closed and with deploy date) EX-3 that should not be included."]])

        then: "the result should contain one jira issue"
        issues?.issues?.size() == 1
        issues.issues[0].key == 'EX-1'
        issues.issues[0].status == 'Open'
        issues.issues[0].title == 'example bug report'
        issues.issues[0].link == "http://localhost:${st.port}/browse/EX-1"
    }

}