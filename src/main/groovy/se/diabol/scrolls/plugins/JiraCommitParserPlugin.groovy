package se.diabol.scrolls.plugins

import com.mashape.unirest.http.Unirest


/**
 * Created by andreas on 2016-10-27.
 */
class JiraCommitParserPlugin extends JiraPluginBase implements ScrollsPlugin {
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

    @Override
    Map getConfigInfo() {
        return [pattern: 'regex pattern to serach for JIRA issue keys in commit messages [/(?i)(?:^|:|,|\'|"|\\/|\\s+)($key-\\d+)/]']
    }

    @Override
    String getTemplateName() {
        return null
    }

    @Override
    List getImageResources() {
        return null
    }
}
