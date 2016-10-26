package se.diabol.scrolls.plugins

class GitPlugin implements ScrollsPlugin {

    def config

    @Override
    Map generate(Map input) {
        Map oldVersion = input.'old'
        Map newVersion = input.'new'

        println "oldVersion=$oldVersion"
        println "newVersion=$newVersion"

        Map commits = [:]
        if (oldVersion.repos) {
            println "Detected multiple repository configuration"
            newVersion.repos.each() { name, repo ->
                def commitLog = getCommitLog(
                        oldVersion.repos[repo.name].version,
                        newVersion.repos[repo.name].version,
                        config.repositoryRoot+oldVersion.repos[repo.name].name)
                commits[oldVersion.name] = []
                commitLog.each {c ->
                    commits[repo.name].add(
                            getCommitDetails(c.key, config.repositoryRoot+oldVersion.repos[repo.name].name)
                    )
                }
            }
        } else {
            println "Detected single repository configuration"
            def commitLog = getCommitLog(oldVersion.version, newVersion.version, config.repositoryRoot)
            commits[oldVersion.name] = []
            commitLog.each {c ->
                commits[oldVersion.name].add(
                        getCommitDetails(c.key, config.repositoryRoot)
                )
            }
        }

        def summary = calcSummary(commits)
        def modules = parseModules(commits)

        return [summary: summary, modules: modules, commits: commits]
    }

    @Override
    String getName() {
        return 'git'
    }

    @Override
    Map getConfigInfo() {
        return [git: 'git and commandline options. Default: git --no-pager',
                repositoryRoot: 'path relative to git repository root. Default: ./',
                moduleRegexps: 'map of modules and regexps. Defaults to: [default: ".*"]',
                changeTypeRegexps: 'map types of changes depending on where in the repository they are found. E.g. [api: ".*/api/.*]']
    }

    def getCommitLog(tag1, tag2, repo){
        //println "Running git log on ${config.repositoryRoot}"
        def command

        if (tag1.isInteger() && (tag1.toInteger() == 0)) {
            command = "${config.cmd} log  --color=never --pretty=oneline ${tag2}"
        } else {
            command = "${config.cmd} log  --color=never --pretty=oneline ${tag1}..${tag2}"
        }

        def result = execCommand(command, repo)
        def commits = [] as HashMap
        result.stdout.eachLine { l ->
            def match = l =~ /^([a-z0-9]*) (.*)$/
            if (match.matches()) {
                commits.put(match[0][1],match[0][2])
            }
        }
        //println "Found ${commits.size()} commits:\n${commits}"
        return commits
    }

    def getCommitDetails(id, repo) {
        def result = execCommand("${config.cmd} show --name-only --format=Commit:%H%nAuthor:%cN<%cE>%nEmail:%aE%nDate:%ci%nMessage:%s%nFiles: ${id}", repo)
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
        return commitDetails
    }

    static def calcSummary(commits) {
        def summary = [
                nbrOfChanges: commits.collect {it.value}.flatten().size(),
                nbrOfPeople: commits.collect{it.value}.flatten().collect{it.author}.unique().size(),
                nbrOfRespoitories: commits.size(),
                nbrOfFiles: commits.collect{it.value}.flatten().collect{it.files}.flatten().unique().size()
        ]
        return summary
    }

    def parseModules(commits) {
        def modules = [] as HashMap
        commits.collect{it.value}.flatten().each { commit ->
            //println "Analyzing: ${commit}"
            commit.files.each {file ->
                config.moduleRegexps.each { mod,modRegExp ->
                    println("Checking file ${file} against mod ${mod} and expr ${modRegExp}: " + (file =~ /${modRegExp}/))
                    if (file =~ /${modRegExp}/ ) {
                        commit.module = mod
                        def changeTypes = [] as HashSet
                        config.changeTypeRegexps.each { tag,tagRegExp ->
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

    static def execCommand(String command, String workingDirectory='.') {
        //println "execCommand(${command},${workingDirectory})"
        def proc = command.execute(null as String[], new File(workingDirectory))
        def sout = new StringBuffer()
        def serr = new StringBuffer()
        proc.consumeProcessOutput(sout,serr)
        proc.waitFor()
        if (proc.exitValue() != 0) {
            println "Failed to execute command: ${command}"
            println serr
            println sout
            throw new Exception("Failed to Execute command: ${command} : ${serr.toString()}")
        }
        return [status  : proc.exitValue(), stdout: sout, stderr: serr]
    }
}
