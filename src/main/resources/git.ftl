<!-- Plugin: git  -->
<#if report??>
<div>
    <h2>Git Change Summary</h2>
    <p>Total ${report.summary.nbrOfChanges} changes by ${report.summary.nbrOfPeople} people, total ${report.summary.nbrOfFiles} files</p>
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
            <#list report.modules as module>
            <tr>
                <td>${module.name}</td>
                <td>${module.nbrOfChanges}</td>
                <td>
                    <#list module.changeTypes as changeType>
                        <span class="change">${changeType}</span>
                    </#list>
                </td>
                <td>
                    <#list module.people as author>
                        <span class="author">${author}</span>
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
        <#list report.commits as commit>
            <div class="commit" id="${commit.rev}">
                <div class="commitHeader">

                    <a class="commitId" href="#">${commit.rev}</a>
                    <span class="commitDate">${commit.date?string("yyyy-MM-dd HH:mm:ss")}</span>
                    <span class="commitAuthor">${commit.author}</span>
                </div>
                <div class="commitMessage">
                ${commit.message} <a href="#" onclick="toggleVisibility('${commit.rev}-details')">[${commit.files?size} files]</a>
                </div>
                <div class="commitDetails hide" id="${commit.rev}-details">
                    <ul>
                        <#list commit.files as files>
                            <li class="commitFile">${files}</li>
                        </#list>
                    </ul>
                </div>
            </div>
        </#list>
    </div>
</#if>
