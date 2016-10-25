<#if report??>
    <h2>Jira issues</h2>
    <p>${report.summary.nbrOfIssues} Jira issues from ${report.summary.nbrOfStories} stories and ${report.summary.nbrOfEpics} epics affected</p>
    <ul>
        <#list report.issues as issue>
            <li>
                <img src="${issue.icon}" alt="${issue.type}"/>
                <a href="${issue.link}">${issue.key}</a><span class="jiraTitle">${issue.title}</span>
                <ul>
                    <#list issue.stories as story>
                        <li>
                            <img src="${story.icon}" alt="${story.type}"/>
                            <#if story.status == "Closed">
                                <a href="${story.link}" class="closed">${story.key}</a><span class="jiraTitle">${story.title}</span>
                            </#if>
                            <#if story.status != "Closed">
                                <a href="${story.link}" class="open">${story.key}</a><span class="jiraTitle">${story.title}</span>
                            </#if>
                        </li>
                    </#list>
                </ul>
            </li>
        </#list>
    </ul>
</#if>
