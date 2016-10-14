<!DOCTYPE HTML>
<html>
<#escape x as x?html>
<head>
    <meta charset="UTF-8">
    <title>Scrolls for ${header.component}</title>
    <style type="text/css">
        <#include "scrolls.css" parse=false>
    </style>
    <script>
        function toggleVisibility(id) {
            var e = document.getElementById(id);
            if(e.className == 'show')
                e.className = 'hide';
            else
                e.className = 'show';
        }
    </script>
</head>
<body>
    <div id="header">
        <h1>Release Notes - ${header.component}</h1>
            <table>
                <tr>
                    <td class="label">Old Version:</td>
                    <td>
                        ${header.oldVersion}
                    </td>
                    <td class="label">New version:</td><td>${header.newVersion}</td>
                </tr>
                <tr>
                    <td class="label">Date:</td><td>${header.date}</td>
                </tr>
            </table>
    </div>

    <div id="leftColumn" class="left">
        <!-- Repository section  -->
            <div>
                <h2>Change Summary</h2>
                <#if repository??>
                <p>Total ${repository.summary.nbrOfChanges} changes by ${repository.summary.nbrOfPeople} people, total ${repository.summary.nbrOfFiles} files</p>
                <table id="changes">
                    <thead>
                        <tr>
                            <th>Module</th>
                            <th>#</th>
                            <th>Changes</th>
                            <th>Persons</th>
                        </tr>
                    </thead>
                    <tbody>
                    <#list repository.modules as m>
                        <tr>
                            <td>${m.name}</td>
                            <td>${m.nbrOfChanges}</td>
                            <td>
                                <#list m.changeTypes as t>
                                <span class="change">${t}</span>
                                </#list>
                            </td>
                            <td>
                                <#list m.people as p>
                                    <span class="author">${p}</span>
                                </#list>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
             </div>
             <div>
                <h2>Change Log</h2>
                <div id="commitLog">
                    <#list repository.commits as c>
                    <div class="commit" id="${c.rev}">
                        <div class="commitHeader">

                            <a class="commitId" href="#" alt="link to code browser">${c.rev}</a>
                            <span class="commitDate">${c.date?string("yyyy-MM-dd HH:mm:ss")}</span>
                            <span class="commitAuthor">${c.author}</span>
                        </div>
                        <div class="commitMessage">
                            ${c.message} <a href="#" onclick="toggleVisibility('${c.rev}-details')">[${c.nbrOfFiles} files]</a>
                        </div>
                        <div class="commitDetails hide" id="${c.rev}-details">
                            <ul>
                                <#list c.files as f>
                                    <li class="commitFile">${f}</li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#list>
                </div>
                <#else>
                    Nothing to show
                </#if>
            </div>
    </div>
        <div id="rightColumn" class="right">
            <!-- Jira section -->
            <div>
                <h2>Jira</h2>
                <#if jira??>
                    <p>${jira.summary.nbrOfIssues} Jira issues from ${jira.summary.nbrOfStories} stories and ${jira.summary.nbrOfEpics} epics affected</p>
                    <ul>
                        <#list jira.issues as e>
                           <li>
                               <img src="${e.icon}" alt="${e.type}"/>
                               <a href="${e.link}">${e.key}</a><span class="jiraTitle">${e.title}</span>
                                <ul>
                                    <#list e.stories as s>
                                        <li>
                                            <img src="${s.icon}" alt="${s.type}"/>
                                            <#if s.status == "Closed">
                                                <a href="${s.link}" class="closed">${s.key}</a><span class="jiraTitle">${s.title}</span>
                                            </#if>
                                            <#if s.status != "Closed">
                                                <a href="${s.link}" class="open">${s.key}</a><span class="jiraTitle">${s.title}</span>
                                            </#if>
                                        </li>
                                    </#list>
                                </ul>
                            </li>
                        </#list>
                    </ul>
                <#else>
                    Nothing to show
                </#if>
            </div>
            <!-- Tests section -->
            <div id="tests">
                <h2>Automated Tests</h2>
                <#if tests??>
                    <p>
                        Version ${tests.summary.newVersion.version} was tested in ${tests.summary.env} with ${tests.summary.newVersion.nbrOfFailures} failures on a total of ${tests.summary.newVersion.nbrOfTests} <span class="small">(${tests.summary.newVersion.nbrOfTestsDelta})</span> tests
                        [<a href="${tests.summary.detailsLink}">details</a>]
                    </p>
                        <table>
                            <thead>
                                <tr>
                                    <th>Module</th>
                                    <th>Duration</th>
                                    <th>Failed</th>
                                    <th>Fixed</th>
                                    <th>Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list tests.modules as m>
                                    <tr>
                                        <td>${m.name}</td>
                                        <td>${m.duration} <span class="small">(${m.durationDiff})</span></td>
                                        <td>${m.failed} <span class="small">(${m.failedDiff})</span></td>
                                        <td>${m.fixed}</td>
                                        <td>${m.total} <span class="small">(${m.totalDiff})</span></td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                <#else>
                    Nothing to show
                </#if>
            </div>
            <!-- Sonar section -->
            <div id="sonar">
                <h2>Sonar Analysis</h2>
                <#if sonar??>
                <p>
                    Totally ${sonar.summary.linesOfCode?string.computer} lines of code with unit test coverage ${sonar.summary.coverage}% and ${sonar.summary.violations} reported violations
                    [<a href="${sonar.summary.detailsLink}">details</a>]
                </p>
                <table>
                    <thead>
                        <tr>
                            <th colspan="2">Metric</th>
                            <th>Value <span class="small"(diff)</span></th>
                        </tr>
                    </thead>
                    <tbody>
                    <#list sonar.metrics as m>
                        <tr>
                            <td>${m.name}</td>
                            <td>
                                <#if m.icon??>
                                    <img src="${m.icon}" alt=""/>
                                </#if>
                            </td>
                            <td>${m.value} <span class="small">(${m.valueDiff})</span></td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
                <#else>
                    Nothing to show
                </#if>
            </div>
        </div>
</body>
</#escape>
</html>