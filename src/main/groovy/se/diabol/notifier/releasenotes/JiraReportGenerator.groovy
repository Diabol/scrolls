package se.diabol.notifier.releasenotes

import groovyx.net.http.*

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.*

//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2')
//@GrabExclude("org.codehaus.groovy:groovy")

/**
 * Misc operations towards the Jira REST API
 *
 */
class JiraReportGenerator
{

    def baseUrl
    def username
    def password
    def iconEpic
    def iconBug
    def iconStory
    def iconTask
    def iconFeature
    def excludeClosedIssues

    def getProjects() {
        print "Fetching jira project\t"
        def http = new HTTPBuilder( baseUrl + '/rest/api/latest/project')
        http.encoderRegistry = new EncoderRegistry( charset: 'utf-8' )
        http.request( GET, JSON ) { req ->
            headers.'User-Agent' = 'Mozilla/5.0'
            headers.'Authorization' = 'Basic ' + "${username}:${password}".toString().bytes.encodeBase64().toString()

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

    def getIssue(key) {
        print "Fetching jira issue: ${key}\t"
        def http = new HTTPBuilder( baseUrl + '/rest/api/latest/issue/'+key)
        http.encoderRegistry = new EncoderRegistry( charset: 'utf-8' )
        http.request( GET, JSON ) { req ->
            headers.'User-Agent' = 'Mozilla/5.0'
            headers.'Authorization' = 'Basic ' + "${username}:${password}".toString().bytes.encodeBase64().toString()

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

    def createJiraReport(commitInfoList) {
        def projects = getProjects()
        def projectKeys = projects.collect { it.key }
        def jiraRefs = [] as HashSet
        // iterate through all commit comments and match against jira refs for each project key
        commitInfoList.each { message ->
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

        println "** Omitting closed issues: " + excludeClosedIssues

        // iterate issues and get info from jira
        jiraRefs.each {key ->
            def json = getIssue(key)

            if (json) {
                if (json.fields.customfield_10058) { // check if the issue has been released, if so, do not include in list
                    def releaseDate = new Date().parse("yyyy-M-d", json.fields.customfield_10058)
                    def currentState = json.fields.status.name
                    def releasedState = issueReleased(releaseDate, currentState)

                    println "** Release date for linked issue:" + json.fields.customfield_10058
                    println "** Status for linked issue:" + json.fields.status.name
                    println "** Released already: " + releasedState


                    if (excludeClosedIssues && releasedState) {
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
                        link: "$baseUrl/browse/${json.key}",
                        type: json.fields.issuetype.name,
                        icon: issueIcon,
                        stories: []
                    ])
                }

                if ((epicAdded) || ("Epic".equals(json.fields.issuetype.name))) {
                    nbrOfEpics++
                } else if (storyAdded) {
                    nbrOfStories++
                    nbrOfIssues++
                } else {
                    nbrOfIssues++
                }
            }
        }

        def report  = [summary: [nbrOfIssues: nbrOfIssues, nbrOfStories: nbrOfStories, nbrOfEpics: nbrOfEpics], issues: issues]

        return report
    }

    def issueReleased(releaseDate, currentState) {
        def currentDate = new Date()

        if (releaseDate.before(currentDate) && ("In use".equals(currentState) || "Closed".equals(currentState))) {
            return true
        }

        return false
    }

    def addIssueToEpicIfApplicable(issues, check) {
        boolean epicAdded  = false
        boolean storyAdded = false

        // TODO: replace this custom field with the non-test jira server custom field name
        if (check.customfield_11622) {
            def issueIcon = getIssueIcon(check.fields.issuetype.name)

            issues.each { issue ->
                if (issue.key.equals(check.customfield_11622)) {
                    issue.stories.add([
                                        key: check.key,
                                        status: check.fields.status.name,
                                        title: check.fields.summary,
                                        link: "$baseUrl/browse/${check.key}",
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
                    link: "$baseUrl/browse/${newEpic.key}",
                    type: newEpic.fields.issuetype.name,
                    icon: iconEpic,
                    stories: [
                                [
                                   key: check.key,
                                   status: check.fields.status.name,
                                   title: check.fields.summary,
                                   link: "$baseUrl/browse/${check.key}",
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

    static void main(String[] args) {
    }
}
