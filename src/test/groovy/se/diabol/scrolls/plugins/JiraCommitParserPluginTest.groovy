package se.diabol.scrolls.plugins

import com.sun.grizzly.http.SelectorThread
import spock.lang.Shared
import spock.lang.Specification

class JiraCommitParserPluginTest extends Specification {
    static int jiraPort = 8282
    @Shared def st
    @Shared JiraCommitParserPlugin plugin
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
        plugin = new JiraCommitParserPlugin(config: config)
    }

    def cleanupSpec() {
        cleanup: "Stop the Grizzly Adapter"
        st.stopEndpoint()
    }

    def "generate should find jira refs in commiit messages"() {
        when: "calling generate with a list of 4 commit comments"
        def result = plugin.generate([commits: [
                "Comment with a correct jira ref EX-1, ok",
                "EX2-2 comment with another correct ref ",
                "comment with an incorrect ref ABC-12 (no such project)",
                "comment without jira ref",
                "Comment with a jira ref in lower case ex-3"]])

        then: "the result should contain three jira result"
        result?.keys?.size() == 3
        result.keys.contains("EX-1")
        result.keys.contains("EX2-2")
        result.keys.contains("ex-3")
    }
}
