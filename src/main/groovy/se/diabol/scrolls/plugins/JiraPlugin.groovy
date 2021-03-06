package se.diabol.scrolls.plugins

import com.mashape.unirest.http.Unirest

class JiraPlugin implements ScrollsPlugin {

    def config

    def icons = [
            bug: 'bug.svg',
            epic: 'epic.svg',
            feature: 'feature.svg',
            improvement: 'feature.svg',
            story: 'story.svg',
            task: 'task.svg'
    ]

    @Override
    String getName() {
        return 'jira'
    }

    @Override
    Map generate(Map input) {
        def issues = []
        def nbrOfIssues = 0
        def nbrOfStories = 0
        def nbrOfEpics = 0

        def jiraRefs = getKeysFromCommits(input.commits)

        jiraRefs.each { key ->
            def json = getIssue(key)

            if (json) {
                if (json.fields.has('customfield_10058')) { // check if the issue has been released, if so, do not include in list
                    def releaseDate = new Date().parse("yyyy-M-d", json.fields.customfield_10058)
                    def currentState = json.fields.status.name
                    def releasedState = issueReleased(releaseDate, currentState)

                    if (config.omitClosed && releasedState) {
                        // Ignoring issue key as it has already been released
                        return
                    }
                }

                def (epicAdded, storyAdded) = addIssueToEpicIfApplicable(issues, json)

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

                if (epicAdded || "Epic" == json.fields.issuetype.name) {
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

    HashSet getKeysFromCommits(commits) {
        def projectKeys = projects.collect { it.key }

        def jiraRefs = [] as HashSet
        // Scan all commit comments for project key references
        commits.each { message ->
            projectKeys.each { key ->
                def matcher = message =~ /(?i)(?:^|:|,|'|"|\/|\s+)($key-\d+)/ // match key exactly at beginning of line, after comma, after colon, or after whitespace. This will prevent false matches, for example project 'AM' matching in 'BAM'
                matcher.each { m ->
                    jiraRefs.add(m[1].trim())
                }
            }
        }

        return jiraRefs
    }

    @Override
    Map getConfigInfo() {
        return [baseUrl: 'The JIRA server instance base url',
                username: 'The JIRA user allowed to query issues',
                password: 'The JIRA user password',
                omitClosed: 'Omit issues that are released and closed',
                inputFrom: 'git (this plugin parses git commit logs for issue keys!']
    }

    @Override
    List getImageResources() {
        def resources = icons.values().collect {
            "/images/jira/${it}"
        }

        return resources.unique(false)
    }

    def getProjects() {
        return doQuery("${config.baseUrl}/rest/api/latest/project")
    }

    def getIssue(key) {
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

    static def issueReleased(releaseDate, currentState) {
        def currentDate = new Date()
        return (releaseDate.before(currentDate) && (currentState in ['In use', 'Closed']))
    }

    def addIssueToEpicIfApplicable(issues, check) {
        boolean epicAdded  = false
        boolean storyAdded = false

        // TODO: replace this custom field with the non-test jira server custom field name
        if (check.has('customfield_11622')) {
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
                    icon: getIssueIcon('epic'),
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

        return new Tuple(epicAdded, storyAdded)
    }

    def getIssueIcon(String type) {
        def normalizedType = type.toLowerCase()
        def icon = 'task'
        if (normalizedType in icons.keySet()) {
            icon = normalizedType
        }

        return "images/${name}/${icons[icon]}"
    }
}
