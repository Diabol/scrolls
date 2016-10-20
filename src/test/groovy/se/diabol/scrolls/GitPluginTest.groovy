package se.diabol.scrolls

import spock.lang.Specification

/**
 * Spock test cases for the GitPlugins
 */
class GitPluginTest extends Specification {

    def "should do something"() {
        setup:

        given: 'a single repository config for scrolls'

        when: 'a report is generated between version t0.1 and t0.2'

        then: 'the report should contain 2 commits from 1 person'

    }

}
