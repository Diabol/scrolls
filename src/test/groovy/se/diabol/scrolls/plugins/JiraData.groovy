package se.diabol.scrolls.plugins
/**
 * Created with IntelliJ IDEA.
 * User: andreas
 * Date: 2013-01-24
 * Time: 20:30
 * To change this template use File | Settings | File Templates.
 */
class JiraData {

    static def projects = [
            [
                    self: "http://www.example.com/jira/rest/api/2/project/EX",
                    id: "10000",
                    key: "EX",
                    name: "Example",
                    avatarUrls: [
                            "16x16": "http://www.example.com/jira/secure/projectavatar?size=small&pid=10000",
                            "48x48": "http://www.example.com/jira/secure/projectavatar?size=large&pid=10000"
                    ]
            ],
            [
                        self: "http://www.example.com/jira/rest/api/2/project/EX2",
                        id: "10000",
                        key: "EX2",
                        name: "Example2",
                        avatarUrls: [
                                "16x16": "http://www.example.com/jira/secure/projectavatar?size=small&pid=10000",
                                "48x48": "http://www.example.com/jira/secure/projectavatar?size=large&pid=10000"
                        ]
            ]
    ]

    static def issues = [
            [
                    "expand": "renderedFields,names,schema,transitions,operations,editmeta,changelog",
                    "id": "10002",
                    "self": "http://www.example.com/jira/rest/api/2/issue/10002",
                    "key": "EX-1",
                    "fields": [
                        "status": ["name": "Open"],
                        "summary": "example bug report",
                        "issuetype": ["name": "Task"],
                        "sub-tasks": [
                                [
                                    "id": "10000",
                                    "type": [
                                    "id": "10000",
                                    "name": "",
                                    "inward": "Parent",
                                    "outward": "Sub-task"
                                ],
                                    "outwardIssue": [
                                    "id": "10003",
                                    "key": "EX-2",
                                    "self": "http://www.example.com/jira/rest/api/2/issue/EX-2",
                                    "fields": [
                                        "status": [
                                            "iconUrl": "http://www.example.com/jira//images/icons/statuses/open.png",
                                            "name": "Open"
                                        ]
                                    ]
                                ]
                                ]
                        ],
                        "timetracking": [
                            "originalEstimate": "10m",
                            "remainingEstimate": "3m",
                            "timeSpent": "6m",
                            "originalEstimateSeconds": 600,
                            "remainingEstimateSeconds": 200,
                            "timeSpentSeconds": 400
                        ],
                        "project": [
                            "self": "http://www.example.com/jira/rest/api/2/project/EX",
                            "id": "10000",
                            "key": "EX",
                            "name": "Example",
                            "avatarUrls": [
                                "16x16": "http://www.example.com/jira/secure/projectavatar?size=small&pid=10000",
                                "48x48": "http://www.example.com/jira/secure/projectavatar?size=large&pid=10000"
                            ]
                        ],
                        "updated": 1,
                        "description": "example bug report",
                        "issuelinks": [
                                [
                                    "id": "10001",
                                    "type": [
                                    "id": "10000",
                                    "name": "Dependent",
                                    "inward": "depends on",
                                    "outward": "is depended by"
                                ],
                                    "outwardIssue": [
                                    "id": "10004L",
                                    "key": "PRJ-2",
                                    "self": "http://www.example.com/jira/rest/api/2/issue/PRJ-2",
                                    "fields": [
                                        "status": [
                                            "iconUrl": "http://www.example.com/jira//images/icons/statuses/open.png",
                                            "name": "Open"
                                        ]
                                    ]
                                ]
                                ],
                                [
                                    "id": "10002",
                                    "type": [
                                    "id": "10000",
                                    "name": "Dependent",
                                    "inward": "depends on",
                                    "outward": "is depended by"
                                ],
                                    "inwardIssue": [
                                    "id": "10004",
                                    "key": "PRJ-3",
                                    "self": "http://www.example.com/jira/rest/api/2/issue/PRJ-3",
                                    "fields": [
                                        "status": [
                                            "iconUrl": "http://www.example.com/jira//images/icons/statuses/open.png",
                                            "name": "Open"
                                        ]
                                    ]
                                ]
                                ]
                        ],
                        "attachment": [
                                [
                                    "self": "http://www.example.com/jira/rest/api/2.0/attachments/10000",
                                    "filename": "picture.jpg",
                                    "author": [
                                    "self": "http://www.example.com/jira/rest/api/2/user?username=fred",
                                    "name": "fred",
                                    "avatarUrls": [
                                        "16x16": "http://www.example.com/jira/secure/useravatar?size=small&ownerId=fred",
                                        "48x48": "http://www.example.com/jira/secure/useravatar?size=large&ownerId=fred"
                                    ],
                                    "displayName": "Fred F. User",
                                    "active": false
                                ],
                                    "created": "2013-01-14T19:54:04.828-0600",
                                    "size": 23123,
                                    "mimeType": "image/jpeg",
                                    "content": "http://www.example.com/jira/attachments/10000",
                                    "thumbnail": "http://www.example.com/jira/secure/thumbnail/10000"
                                ]
                        ],
                        "watcher": [
                            "self": "http://www.example.com/jira/rest/api/2/issue/EX-1/watchers",
                            "isWatching": false,
                            "watchCount": 1,
                            "watchers": [
                                    [
                                        "self": "http://www.example.com/jira/rest/api/2/user?username=fred",
                                        "name": "fred",
                                        "avatarUrls": [
                                        "16x16": "http://www.example.com/jira/secure/useravatar?size=small&ownerId=fred",
                                        "48x48": "http://www.example.com/jira/secure/useravatar?size=large&ownerId=fred"
                                    ],
                                        "displayName": "Fred F. User",
                                        "active": false
                                    ]
                            ]
                        ],
                        "comment": [
                                [
                                    "self": "http://www.example.com/jira/rest/api/2.0/issue/10010/comment/10000",
                                    "id": "10000",
                                    "author": [
                                    "self": "http://www.example.com/jira/rest/api/2.0/user?username=fred",
                                    "name": "fred",
                                    "displayName": "Fred F. User",
                                    "active": false
                                ],
                                    "body": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.",
                                    "updateAuthor": [
                                    "self": "http://www.example.com/jira/rest/api/2.0/user?username=fred",
                                    "name": "fred",
                                    "displayName": "Fred F. User",
                                    "active": false
                                ],
                                    "created": "2013-01-14T19:54:04.710-0600",
                                    "updated": "2013-01-14T19:54:04.711-0600",
                                    "visibility": [
                                    "type": "role",
                                    "value": "Administrators"
                                ]
                                ]
                        ],
                        "worklog": [
                                [
                                    "self": "http://www.example.com/jira/rest/api/2.0/issue/10010/worklog/10000",
                                    "author": [
                                    "self": "http://www.example.com/jira/rest/api/2.0/user?username=fred",
                                    "name": "fred",
                                    "displayName": "Fred F. User",
                                    "active": false
                                ],
                                    "updateAuthor": [
                                    "self": "http://www.example.com/jira/rest/api/2.0/user?username=fred",
                                    "name": "fred",
                                    "displayName": "Fred F. User",
                                    "active": false
                                ],
                                    "comment": "I did some work here.",
                                    "visibility": [
                                    "type": "group",
                                    "value": "jira-developers"
                                ],
                                    "started": "2013-01-14T19:54:05.463-0600",
                                    "timeSpent": "3h 20m",
                                    "timeSpentSeconds": 12000,
                                    "id": "100028"
                                ]
                        ]
                    ],
                    "names": [
                        "sub-tasks": "sub-tasks",
                        "timetracking": "timetracking",
                        "project": "project",
                        "updated": "updated",
                        "description": "description",
                        "issuelinks": "issuelinks",
                        "attachment": "attachment",
                        "watcher": "watcher",
                        "comment": "comment",
                        "worklog": "worklog"
                    ],
                    "schema": []
            ],
            [
                "expand": "renderedFields,names,schema,transitions,operations,editmeta,changelog",
                "id": "10003",
                "self": "http://www.example.com/jira/rest/api/2/issue/10003",
                "key": "EX-3",
                "fields": [
                    "status": ["name": "Closed"],
                    "summary": "Example story",
                    "issuetype": ["name": "Story"],
                    "project": [
                        "self": "http://www.example.com/jira/rest/api/2/project/EX",
                        "id": "10000",
                        "key": "EX",
                        "name": "Example",
                        "avatarUrls": [
                            "16x16": "http://www.example.com/jira/secure/projectavatar?size=small&pid=10000",
                            "48x48": "http://www.example.com/jira/secure/projectavatar?size=large&pid=10000"
                        ]
                    ],
                    "updated": 1,
                    "description": "example story description",
                    "customfield_10058":"2014-03-11"
                ],
                "names": [
                    "project": "project",
                    "updated": "updated",
                    "description": "description"
                ]
            ]
        ]
}
