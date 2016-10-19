package se.diabol.scrolls

import com.sun.grizzly.tcp.Request
import groovy.json.JsonBuilder

/**
 * Grizzly Adapter that simulates jira REST api for unit test purpose
 */
class JiraAdapter extends UnitTestAdapter {

    def projects = JiraData.projects
    def issues = JiraData.issues

    @Override
    def processRequest(Request request) {
        def result = [:]
        if (!request.getHeader('Authorization').startsWith('Basic')) {
            result.status = HttpURLConnection.HTTP_UNAUTHORIZED
            result.content = new JsonBuilder([error: 'Unauthorized: requires basic authentication!']).toPrettyString()
        } else if (request.unparsedURI().startsWith("/rest/api/latest/project")) {
            result.status = HttpURLConnection.HTTP_OK
            result.content = new JsonBuilder(projects).toPrettyString()
        } else if (request.unparsedURI().startsWith("/rest/api/latest/issue/")) {
            def key = request.unparsedURI().toString().split('/').last()
            def issue = issues.find { it.key == key}
            result.status = issue? HttpURLConnection.HTTP_OK : HttpURLConnection.HTTP_NOT_FOUND
            result.content = issue? new JsonBuilder(issue).toPrettyString() : new JsonBuilder([error: "Issue ${key } not found"]).toPrettyString()
        } else {
            result.status = HttpURLConnection.HTTP_BAD_REQUEST
            result.content = new JsonBuilder([error: 'Failed: bad request']).toPrettyString()
        }
        return result
    }
}
