package se.diabol.scrolls

import com.sun.grizzly.http.SelectorThread
import spock.lang.Shared
import spock.lang.Specification


class GitHubPluginTest extends Specification {

    static int githubPort = 8282
    @Shared SelectorThread selectorThread
    @Shared def publicRepoWithPullsConfig = [apiUrl: "http://localhost:${githubPort}",
                                             owner: GitHubAdapter.owner,
                                             repo: GitHubAdapter.repoWithPullRequests]
    @Shared def publicRepoNoPullsConfig = [apiUrl: "http://localhost:${githubPort}",
                                           owner : GitHubAdapter.owner,
                                           repo  : GitHubAdapter.repoWithNoPullRequests]
    @Shared def privateRepoWithPullsConfig = [apiUrl: "http://localhost:${githubPort}",
                                              owner : GitHubAdapter.owner,
                                              repo  : GitHubAdapter.repoWithPullRequests,
                                              token : GitHubAdapter.token]


    def setupSpec() {
        setup: "Grizzly Adapter on port ${githubPort} to simulate the GitHub API"
        selectorThread = new SelectorThread()
        selectorThread.port = githubPort
        selectorThread.adapter = new GitHubAdapter()
        selectorThread.listen()
    }

    def cleanupSpec() {
        cleanup: "Stop the Grizzly Adapter"
        selectorThread.stopEndpoint()
    }

    def "plugin name should match github"() {
        given:
        def plugin = new GitHubPlugin(config: [])

        expect:
        plugin.name == 'github'
    }

    def "generate should handle repo with no pull requests"() {
        given: "GitHubPlugin configured for repo without pull requests"
        def plugin = new GitHubPlugin(config: publicRepoNoPullsConfig)

        when: "generate is run"
        def dataModel = plugin.generate(old: "1.0.0", new: "2.0.0")

        then: "dataModel for pulls is empty"
        dataModel.pulls.isEmpty()
    }

    def "Pull request information should be added to data model"() {
        given: "GitHubPlugin configured for repo with pull requests"
        def plugin = new GitHubPlugin(config: publicRepoWithPullsConfig)

        when: "generate is run"
        def dataModel = plugin.generate(old: "1.0.0", new: "2.0.0")

        then: "dataModel for pulls contains the sent pull requests"
        'pulls' in dataModel.keySet()
        dataModel.pulls.size() == 1
        def pullRequest = dataModel.pulls[1]
        println pullRequest

        and: "Pull request contains all expected keys"
        ['title', 'state', 'by', 'created', 'updated', 'html_url', 'from', 'to'].every {
            it in pullRequest
        }

        and: "Pull request contains expected values"
        pullRequest.title == "First Pull Request"
        pullRequest.state == 'open'
        pullRequest.by == GitHubAdapter.owner
        pullRequest.html_url == "https://github.com/${GitHubAdapter.owner}/${GitHubAdapter.repoWithPullRequests}/pull/1"
    }

    def "Pull request should work with token"() {
        given:
        def plugin = new GitHubPlugin(config: privateRepoWithPullsConfig)

        when:
        def dataModel = plugin.generate(old: "1.0.0", new: "2.0.0")

        then:
        'pulls' in dataModel.keySet()
        dataModel.pulls.size() == 1
        dataModel.pulls[1].title == "First Pull Request"
    }
}
