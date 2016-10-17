scrolls {
    component = "scrolls"
}

git {
    git = "git --no-pager"
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

jira {
    jiraBaseUrl="https://jira.atlassian.com"
    jiraUsername="scrolls"
    jiraPassword="password"
    jiraReleaseRequestProjectKey="NOT"
    jiraComponent="Scrolls"
}
