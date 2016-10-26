package se.diabol.scrolls.plugins

import com.mashape.unirest.http.Unirest

class GitHubPlugin implements ScrollsPlugin {
    def config

    @Override
    String getName() {
        return 'github'
    }

    @Override
    Map generate(Map input) {
        def report = [:]

        // What are the assumptions for the pull request API?
        def url = "${config.apiUrl}/repos/${config.owner}/${config.repo}/pulls"

        def request = Unirest.get(url).header("accept", "application/vnd.github.v3+json")
        if (config.token) {
            request.queryString("access_token", config.token)
        }

        def response = request.asJson()

        if (response.status != 200) {
            // How to report error?
            println "ERROR: github - Got status code ${response.status} but expected 200 for ${request.url}"
            return [:]
        }

        report['pulls'] = [:]
        response.getBody().array.each {
            report['pulls'][it.number] = [
                title: it.title,
                state: it.state,
                by: it.user.login, // Possibly to lookup to get more user details, or include link to user
                created: Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", it.created_at as String),
                updated: Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", it.updated_at as String),
                html_url: it.html_url,
                from: it.head.ref,
                to: it.base.ref
            ]
        }

        return report
    }

    @Override
    Map getConfigInfo() {
        return [owner: 'The owner of the repository',
                token: 'The oauth/personal access token for the user (token is only for private repositories)',
                repo: 'The name of the repository']
    }
}
