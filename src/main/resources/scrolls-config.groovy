/*
 * Welcome to the configuration file for Scrolls. For each plugin you must define a section declaring two fields, the
 * plugin class (key: plugin, value: full package path) and what input it expects (key: inputFrom, value: plugin name
 * or 'versions'). You must also add the configuration specific to the plugin (and it's up the plugin to provide this)
 * inputFrom = 'versions' means the plugin will receive the old and new versions set on the commandline as input.
 */
scrolls {
    component = "scrolls"

    outputDirectory = "./scrolls-report"
}

git {
    plugin = "se.diabol.scrolls.plugins.GitPlugin"
    inputFrom = "versions"
    template = "git.ftl"

    cmd = "git --no-pager"
    repositoryRoot = "./"
    moduleRegexps = [
        "Api": "^plugin-api/.*",
        "Scrolls": "^src/.*",
    ]
    changeTypeRegexps = [
        "build": ".*build\\.gradle",
        "config": ".*src/main/resources.*\\.groovy",
        "ftl": ".*\\.ftl",
        "images": ".*resources/images/.*",
        "source": ".*src/main/groovy/.*",
        "test": ".*src/test/groovy/.*",
    ]
}

//github {
//    plugin = 'se.diabol.scrolls.plugins.GitHubPlugin'
//    inputFrom = 'versions'
//
//    apiUrl = 'https://api.github.com'
//    owner = 'Diabol'
//    repo = 'scrolls'
//}

jiraCommitParser {
    plugin = "se.diabol.scrolls.plugins.JiraCommitParserPlugin"
    inputFrom = "git"

    baseUrl = "https://orbra7.atlassian.net"
    username = ""
    password = ""
}

jira {
    plugin = "se.diabol.scrolls.plugins.JiraPlugin"
    inputFrom = "jiraCommitParser"
    template = "jira.ftl"

    baseUrl = "https://orbra7.atlassian.net"
    username = ""
    password = ""
    omitClosed = true
}
