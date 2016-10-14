<!-- Tests section -->
<#if report??>
    <p>
        Version ${report.summary.newVersion.version} was tested in ${report.summary.env} with ${report.summary.newVersion.nbrOfFailures} failures on a total of ${report.summary.newVersion.nbrOfTests} <span class="small">(${report.summary.newVersion.nbrOfTestsDelta})</span> tests
        [<a href="${report.summary.detailsLink}">details</a>]
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
            <#list report.modules as module>
            <tr>
                <td>${module.name}</td>
                <td>${module.duration} <span class="small">(${module.durationDiff})</span></td>
                <td>${module.failed} <span class="small">(${module.failedDiff})</span></td>
                <td>${module.fixed}</td>
                <td>${module.total} <span class="small">(${module.totalDiff})</span></td>
            </tr>
            </#list>
        </tbody>
    </table>
</#if>