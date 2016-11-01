/*
 * Welcome to the configuration file for Scrolls. For each plugin you must define a section declaring two fields, the
 * plugin class (key: plugin, value: full package path) and what input it expects (key: inputFrom, value: plugin name
 * or 'versions'). You must also add the configuration specific to the plugin (and it's up the plugin to provide this)
 * inputFrom = 'versions' means the plugin will receive the old and new versions set on the commandline as input.
 */
scrolls {
    component = "scrolls"
}


git {
    plugin = "se.diabol.scrolls.plugins.GitPlugin"
    inputFrom = "versions"

    cmd = "git --no-pager"
    repositoryRoot = "./"
    moduleRegexps = [
        "module1": "^/module1/.*",
        "module2": "^/module2/.*",
        "module3": "^/module3/.*"
    ]
    changeTypeRegexps = [
        "api": ".*/api/.*",
        "java": ".*/src/main/java.*",
        "test": ".*/src/test/java.*",
        "config": ".*/src/main/resources.*\\.xml",
        "build": ".*/build\\.gradle"
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
    username = "andreas@diabol.se"
    password = "Hcm400tc"
}

jira {
    plugin = "se.diabol.scrolls.plugins.JiraPlugin"
    inputFrom = "jiraCommitParser"

    baseUrl = "https://orbra7.atlassian.net"
    username = "andreas@diabol.se"
    password = "Hcm400tc"
    omitClosed = true
}
