<!-- Sonar section -->
<#if report??>
    <p>
        Totally ${report.summary.linesOfCode?string.computer} lines of code with unit test coverage ${report.summary.coverage}% and ${report.summary.violations} reported violations
        [<a href="${report.summary.detailsLink}">details</a>]
    </p>
    <table>
        <thead>
        <tr>
            <th colspan="2">Metric</th>
            <th>Value <span class="small">(diff)</span></th>
        </tr>
        </thead>
        <tbody>
            <#list report.metrics as metric>
            <tr>
                <td>${metric.name}</td>
                <td>
                    <#if metric.icon??>
                        <img src="${metric.icon}" alt=""/>
                    </#if>
                </td>
                <td>${metric.value} <span class="small">(${metric.valueDiff})</span></td>
            </tr>
            </#list>
        </tbody>
    </table>
</#if>
