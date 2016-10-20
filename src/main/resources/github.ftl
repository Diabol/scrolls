<#if report??>
<div>
    <h2>GitHub Pull Requests</h2>
    <#if report.pulls?size gt 0>
    <table>
        <tr>
            <th>#</th>
            <th>Title</th>
            <th>State</th>
            <th>Requester</th>
            <th>Created</th>
            <th>Updated</th>
            <th>From branch</th>
            <th>To branch</th>
        </tr>

        <#list report.pulls as number, pr>
        <tr>
            <td>${number}</td>
            <td><a href="${pr.html_url}">${pr.title}</a></td>
            <td>${pr.state}</td>
            <td>${pr.by}</td>
            <td>${pr.created?string("yyyy-MM-dd HH:mm:ss")}</td>
            <td>${pr.updated?string("yyyy-MM-dd HH:mm:ss")}</td>
            <td>${pr.from}</td>
            <td>${pr.to}</td>
        </tr>
        </#list>
    <#else>
        There are no open pull requests
    </#if>
</#if>