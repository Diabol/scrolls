package se.diabol.scrolls

import com.sun.grizzly.tcp.Request
import groovy.json.JsonBuilder
import com.sun.grizzly.tcp.Response
import com.sun.grizzly.util.buf.ByteChunk
import com.sun.grizzly.tcp.Adapter

/**
 * Grizzly Adapter that simulates jira REST api for unit test purpose
 */
class JiraAdapter implements Adapter {

    def projects = JiraData.projects
    def issues = JiraData.issues

    public void service(Request request, Response response) {
        println "JiraAdapter received request '${request.unparsedURI()}'"
        def content = new ByteChunk()
        request.doRead(content)
        def bytes = null
        def processResult = processRequest(request)
        bytes = processResult.content.bytes
        response.status = processResult.status
        def chunk = new ByteChunk()
        chunk.append(bytes, 0, bytes.length)
        response.contentLength = bytes.length
        response.contentType = 'text/json'
        response.outputBuffer.doWrite(chunk, response)
        response.finish()
    }

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

    public void afterService(Request request, Response response) {
        request.recycle()
        response.recycle()
    }

    public void fireAdapterEvent(string, object) {}
}
