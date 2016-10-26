package se.diabol.scrolls.plugins

import com.sun.grizzly.tcp.Request
import groovy.json.JsonBuilder

/**
 * Grizzly adapter that simulates parts of the GitHub api to use with unit tests
 */
class GitHubAdapter extends UnitTestAdapter {

    static String owner = "JohnJohnson"
    static String repoWithPullRequests = "JohnsRepo"
    static String repoWithNoPullRequests = "NoPullsRepo"
    static String token = "JohnsSecretToken"

    @Override
    def processRequest(Request request) {
        def result = [:]

        if (!(request.getHeader('accept') == "application/vnd.github.v3+json")) {
            result.status = HttpURLConnection.HTTP_BAD_REQUEST
            result.content = new JsonBuilder([error: "Invalid accept header for request: ${request.getHeader('accept')}"]).toPrettyString()
        } else if (request.unparsedURI().toString().contains("?access_token=${token}")) {
            result.status = HttpURLConnection.HTTP_OK
            result.content = new JsonBuilder(GitHubData.pullData).toPrettyString()
        } else if (request.unparsedURI().startsWith("/repos/${owner}/${repoWithPullRequests}/pulls")) {
            result.status = HttpURLConnection.HTTP_OK
            result.content = new JsonBuilder(GitHubData.pullData).toPrettyString()
        } else if (request.unparsedURI().startsWith("/repos/${owner}/${repoWithNoPullRequests}/pulls")) {
            result.status = HttpURLConnection.HTTP_OK
            result.content = new JsonBuilder(GitHubData.emptyPullData).toPrettyString()
        } else {
            println "GitHubAdapter received request it has no mapping for: ${request}"
            result.status = HttpURLConnection.HTTP_BAD_REQUEST
            result.content = new JsonBuilder([error: "No mapping for request: ${request}"]).toPrettyString()
        }

        return result
    }
}
