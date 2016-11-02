package se.diabol.scrolls.plugins

import com.mashape.unirest.http.Unirest

class JiraPlugin extends JiraPluginBase implements ScrollsPlugin {

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
        def jiraRefs = input.keys

        def issues = []
        def nbrOfIssues = 0
        def nbrOfStories = 0
        def nbrOfEpics = 0

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


    @Override
    Map getConfigInfo() {
        return [baseUrl: 'The JIRA server instance base url',
                username: 'The JIRA user allowed to query issues',
                password: 'The JIRA user password',
                omitClosed: 'Omit issues that are released and closed',
                inputFrom: 'git (this plugin parses git commit logs for issue keys!']
    }

    @Override
    String getTemplateName() {
        return config.template;
    }

    @Override
    List getImageResources() {
        def resources = icons.values().collect {
            "/images/jira/${it}"
        }

        return resources.unique(false)
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
