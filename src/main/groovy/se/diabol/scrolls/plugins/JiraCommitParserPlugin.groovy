package se.diabol.scrolls.plugins

import groovyx.net.http.EncoderRegistry
import groovyx.net.http.HTTPBuilder

import java.nio.charset.StandardCharsets

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET


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
        def http = new HTTPBuilder(url)
        http.encoderRegistry = new EncoderRegistry(charset: StandardCharsets.UTF_8.name())
        http.request(GET, JSON) { req ->
            headers.'User-Agent' = 'Mozilla/5.0'
            headers.'Authorization' = 'Basic ' + "${config.username}:${config.password}".toString().bytes.encodeBase64().toString()

            response.success = { resp, json ->
                println Success: resp.status
                return json
            }

            response.failure = { resp, json ->
                println Failed: resp.status
                println json
                return
            }
        }
    }

    @Override
    Map getConfigInfo() {
        return [pattern: 'regex pattern to serach for JIRA issue keys in commit messages [/(?i)(?:^|:|,|\'|"|\\/|\\s+)($key-\\d+)/]']
    }
}
