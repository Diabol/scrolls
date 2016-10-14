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
    <h1>Scrolls for - ${header.component}</h1>
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

    <#list reports as name, report>
    <div class="plugin-report">
        <#include "${name}.ftl">
    </div>
    </#list>
</body>
</#escape>
</html>
