package se.diabol.scrolls.plugins

import groovyx.net.http.*
import se.diabol.scrolls.engine.ScrollsPlugin

import java.nio.charset.StandardCharsets

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.*

class JiraPlugin implements ScrollsPlugin {
    def iconEpic
    def iconBug
    def iconStory
    def iconTask
    def iconFeature

    def config

    @Override
    String getName() {
        return 'jira'
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

        def issues = []
        def nbrOfIssues = 0
        def nbrOfStories = 0
        def nbrOfEpics = 0

        // iterate issues and get info from jira
        jiraRefs.each { key ->
            def json = getIssue(key)

            if (json) {
                if (json.fields.customfield_10058) { // check if the issue has been released, if so, do not include in list
                    def releaseDate = new Date().parse("yyyy-M-d", json.fields.customfield_10058)
                    def currentState = json.fields.status.name
                    def releasedState = issueReleased(releaseDate, currentState)

                    println "** Release date for linked issue: ${json.fields.customfield_10058}"
                    println "** Status for linked issue: ${json.fields.status.name}"
                    println "** Released already: ${releasedState}"


                    if (config.omitClosed && releasedState) {
                        println "** --> Issue " + key + " skipped as it has already been released."
                        return
                    }
                }

                def added = addIssueToEpicIfApplicable(issues, json)
                boolean epicAdded  = added[0]
                boolean storyAdded = added[1]

                if (!storyAdded) {
                    def issueIcon = getIssueIcon(json.fields.issuetype.name)

                    issues.add([
                            key: json.key,
                            status: json.fields.status.name,
                            title: json.fields.summary,
                            link: "${config.baseUrl}/browse/${json.key}",
                            type: json.fields.issuetype.name,
                            icon: issueIcon,
                            stories: []
                    ])
                }

                if ((epicAdded) || "Epic" == json.fields.issuetype.name) {
                    nbrOfEpics++
                } else if (storyAdded) {
                    nbrOfStories++
                    nbrOfIssues++
                } else {
                    nbrOfIssues++
                }
            }
        }

        return [summary: [nbrOfIssues: nbrOfIssues, nbrOfStories: nbrOfStories, nbrOfEpics: nbrOfEpics], issues: issues]
    }

    @Override
    Map getConfigInfo() {
        return [baseUrl: 'The JIRA server instance base url',
                username: 'The JIRA user allowed to query issues',
                password: 'The JIRA user password',
                omitClosed: 'Omit issues that are released and closed',
                inputFrom: 'git (this plugin parses git commit logs for issue keys!']
    }

    def getProjects() {
        print "Fetching jira project\t"
        return doQuery("${config.baseUrl}/rest/api/latest/project")
    }

    def getIssue(key) {
        print "Fetching jira issue: ${key}\t"
        return doQuery("${config.baseUrl}/rest/api/latest/issue/${key}")
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


    def issueReleased(releaseDate, currentState) {
        def currentDate = new Date()
        return (releaseDate.before(currentDate) && (currentState in ['In use', 'Closed']))
    }

    def addIssueToEpicIfApplicable(issues, check) {
        boolean epicAdded  = false
        boolean storyAdded = false

        // TODO: replace this custom field with the non-test jira server custom field name
        if (check.customfield_11622) {
            def issueIcon = getIssueIcon(check.fields.issuetype.name)

            issues.each { issue ->
                if (issue.key == check.customfield_11622) {
                    issue.stories.add([
                                        key: check.key,
                                        status: check.fields.status.name,
                                        title: check.fields.summary,
                                        link: "${config.baseUrl}/browse/${check.key}",
                                        type: check.fields.issuetype.name,
                                        icon: issueIcon
                                       ])
                    storyAdded = true
                }
            }

            if (!storyAdded) {
                def newEpic = getIssue(check.customfield_11622)
                issues.add([
                    key: newEpic.key,
                    status: newEpic.fields.status.name,
                    title: newEpic.fields.summary,
                    link: "${config.baseUrl}/browse/${newEpic.key}",
                    type: newEpic.fields.issuetype.name,
                    icon: iconEpic,
                    stories: [
                                [
                                   key: check.key,
                                   status: check.fields.status.name,
                                   title: check.fields.summary,
                                   link: "${config.baseUrl}/browse/${check.key}",
                                   type: check.fields.issuetype.name,
                                   icon: issueIcon
                                ]
                              ]
                            ])

                epicAdded = true
            }
        }

        return [epicAdded, storyAdded]
    }

    def getIssueIcon(type) {
        switch(type) {
            case "Epic":  return iconEpic
            case "Bug":   return iconBug
            case "Story": return iconStory
            case "Task":  return iconTask
            case "Service Request": return iconFeature
            default:      return iconTask
        }
    }
}
