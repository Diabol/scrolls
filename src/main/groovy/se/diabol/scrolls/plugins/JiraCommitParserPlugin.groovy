package se.diabol.scrolls.plugins

import com.mashape.unirest.http.Unirest


/**
 * Created by andreas on 2016-10-27.
 */
class JiraCommitParserPlugin implements ScrollsPlugin {
    def config

    @Override
    String getName() {
        return "jiraCommitParser"
    }

    @Override
    Map generate(Map input) {
        def projects = getProjects()
        def projectKeys = projects.collect { it.key }
        def jiraRefs = [] as HashSet
        // iterate through all commit comments and match against jira refs for each project key
        input.commits.each { message ->
            projectKeys.each { key ->
                def matcher = message =~ /(?i)(?:^|:|,|'|"|\/|\s+)($key-\d+)/ // match key exactly at beginning of line, after comma, after colon, or after whitespace. This will prevent false matches, for example project 'AM' matching in 'BAM'
                matcher.each { m ->
                    jiraRefs.add(m[1].trim())
                }
            }
        }
        return [keys: jiraRefs]
    }

    def getProjects() {
        print "Fetching jira project\t"
        return doQuery("${config.baseUrl}/rest/api/latest/project")
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

    @Override
    Map getConfigInfo() {
        return [pattern: 'regex pattern to serach for JIRA issue keys in commit messages [/(?i)(?:^|:|,|\'|"|\\/|\\s+)($key-\\d+)/]']
    }

    @Override
    List getImageResources() {
        return null
    }
}
