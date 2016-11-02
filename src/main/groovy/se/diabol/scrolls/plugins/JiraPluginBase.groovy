package se.diabol.scrolls.plugins

import com.mashape.unirest.http.Unirest

/**
 * This is a Abstract Base Class for the JiraPlugin and it holds some common methods for integrating with JIRA
 */
abstract class JiraPluginBase {
    public getProjects() {
        return doQuery("${config.baseUrl}/rest/api/latest/project")
    }

    public getIssue(key) {
        return doQuery("${config.baseUrl}/rest/api/latest/issue/${key}")
    }

    private doQuery(String url) {
        def headers = ['User-Agent': 'Mozilla/5.0',
                       'Authorization': "Basic " + "${config.username}:${config.password}".bytes.encodeBase64().toString()]
        def request = Unirest.get(url).headers(headers)
        def response = request.asJson()

        if (response.status != 200) {
            println "ERROR: jira - Got status code ${response.status} but expected 200 for ${request.url}"
            return [:]
        }

        if (response.body.isArray()) {
            return response.body.array
        } else {
            return response.body.object
        }
    }
}
