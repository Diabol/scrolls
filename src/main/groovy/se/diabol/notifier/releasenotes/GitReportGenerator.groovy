package se.diabol.notifier.releasenotes

class GitReportGenerator extends AbstractReportGenerator {

    def git = "git --no-pager"
    def repositoryRoot = "./"
    def modulesRegexps = ["default": ".*"]
    def changeTypeRegexps = [:]

    def getCommitLog(tag1,tag2, gitLogOptions){
        println "Running git log on ${repositoryRoot}"
        def command

        if (tag1.isInteger() && (tag1.toInteger() == 0)) {
            command = "${git} log --pretty=oneline ${gitLogOptions} ${tag2}"
        } else {
            command = "${git} log --pretty=oneline ${gitLogOptions} ${tag1}..${tag2}"
        }

        def result = execCommand(command, repositoryRoot)
        def commits = [] as HashMap
        result.stdout.eachLine { l ->
            def match = l =~ /^([a-z0-9]*) (.*)$/
            if (match.matches()) {
                commits.put(match[0][1],match[0][2])
            }
        }
        println "Found ${commits.size()} commits:\n${commits}"
        return commits
    }

    def getCommitDetails(id) {
        def result = execCommand("${git} show --name-only --format=Commit:%H%nAuthor:%cN<%cE>%nEmail:%aE%nDate:%ci%nMessage:%s%nFiles: ${id}", repositoryRoot)
        //println "Found git commit details for [${id}]:\n${result.stdout}"
        def commitDetails = [] as HashMap
        boolean headerDone = false;
        result.stdout.eachLine { line ->
            if (!headerDone) {
                def match = line =~ /Commit:(.*)/
                if (match.matches()) {
                    commitDetails.rev=match[0][1]
                }
                match = line =~ /Author:(.*)/
                if (match.matches()) {
                    commitDetails.author=match[0][1]
                }
                match = line =~ /Email:(.*)/
                if (match.matches()) {
                    commitDetails.email=match[0][1]
                }
                match = line =~ /Date:(.*)/
                if (match.matches()) {
                    commitDetails.date=Date.parse("yyyy-MM-dd HH:mm:ss Z",match[0][1])
                }
                match = line =~ /Message:(.*)/
                if (match.matches()) {
                    commitDetails.message=match[0][1]
                }
                match = line =~ /Files:/
                if (match.matches()) {
                    commitDetails.files=[]
                    headerDone = true
                }
            } else if (line.trim()) {
                commitDetails.files.add(line)
            }
        }
        // This debug log is here because this function fails mysteriously sometimes. Remove when resolved.
        println "DEBUG: getCommitDetails result for id: ${id} - ${commitDetails}"
        return commitDetails
    }

    def calcSummary(commits) {
        def summary = [
                nbrOfChanges: commits.size(),
                nbrOfPeople: commits.collect{it.author}.unique().size(),
                nbrOfFiles: commits.collect{it.files}.unique().size()
        ]
        return summary
    }

    def parseModules(commits) {
        def modules = [] as HashMap
        commits.each { commit ->
            //println "Analyzing: ${commit}"
            commit.files.each {file ->
                modulesRegexps.each { mod,modRegExp ->
                    println("Checking file ${file} against mod ${mod} and expr ${modRegExp}: " + (file =~ /${modRegExp}/))
                    if (file =~ /${modRegExp}/ ) {
                        commit.module = mod
                        def changeTypes = [] as HashSet
                        changeTypeRegexps.each { tag,tagRegExp ->
                            println "Matching: '${file}' with change type regexp: '${tagRegExp}'"
                            if (file =~ /${tagRegExp}/) {
                                println "Matched! Adding changetype: '${tag}'"
                                changeTypes.add(tag)
                            }
                        }
                        if (changeTypes.isEmpty()) {
                            changeTypes.add("other")
                        }
                        //println "Matched '${file}' against '${modRegExp}'! Adding changed types ${changeTypes} to module ${mod}"
                        if (modules.containsKey(mod)) {
                            modules."${mod}".commits.add(commit.rev)
                            modules."${mod}".people.add(commit.author)
                            modules."${mod}".changeTypes.addAll(changeTypes)
                        } else {
                            modules."${mod}" = [name: mod, commits: [commit.rev] as HashSet, people: [commit.author] as HashSet, changeTypes: changeTypes as HashSet]
                        }
                    }
                }
            }
        }
        modules.each {k,v ->
            v.nbrOfChanges = v.commits.size()
        }
        return modules.values()
    }

    def createReport(tag1,tag2, gitLogOptions) {
        def commits = []
        def commitLog = getCommitLog(tag1,tag2, gitLogOptions)
        commitLog.each {c ->
            commits.add(getCommitDetails(c.key))
        }
        def summary = calcSummary(commits)
        //println "Commits:\n${commits}"
        def modules = parseModules(commits)

        return [summary: summary, modules: modules, commits: commits]
    }

    public static void main(String[] args) {
        GitReportGenerator grg = new GitReportGenerator(
            repositoryRoot: "./",
             modulesRegexps: ["eb": "^web/.*"],
             changeTypeRegexps: ["javascript": ".*/javascript/.*\\.js"]

        )
        def report = grg.createReport("release-1.0.990","release-1.0.999", "")
        println "\n\nFinal report:\n${report}"
    }
}
