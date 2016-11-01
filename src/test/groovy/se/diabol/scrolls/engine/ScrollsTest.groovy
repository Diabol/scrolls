package se.diabol.scrolls.engine

import org.junit.Rule
import org.junit.contrib.java.lang.system.Assertion
import org.junit.contrib.java.lang.system.ExpectedSystemExit
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.contrib.java.lang.system.internal.CheckExitCalled
import spock.lang.Specification

class ScrollsTest extends Specification {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog()

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog()

    def setup() {
        systemOutRule.clearLog()
        systemErrRule.clearLog()
    }

    def "should display help when help option provided"() {
        expect:
        exit.expectSystemExitWithStatus(0)
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            void checkAssertion() throws Exception {
                assert systemOutRule.getLog().contains('usage: scrolls')
                assert !systemErrRule.getLog().contains('ERROR')
            }
        })

        invokeMainWithArgs(['--help'])
    }

    def "should display error for missing required option old-version when it's not provided"() {
        expect:
        exit.expectSystemExit()
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            void checkAssertion() throws Exception {
                assert systemErrRule.getLog().contains('ERROR: Missing required option: old-version')
            }
        })

        invokeMainWithArgs([])
    }

    def "should display error for missing required option new-version when it's not provided"() {
        expect:
        exit.expectSystemExit()
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            void checkAssertion() throws Exception {
                assert systemErrRule.getLog().contains('ERROR: Missing required option: new-version')
            }
        })

        invokeMainWithArgs(['--old-version', '1.0.0'])
    }

    def "multirepo config as json file should be read and validated" () {
        setup:
            def versionMap
            def version = new File('version.json')
            version.write("""{
                "name": "scrolls",
                "version": "1.0.0",
                "repos": {
                    "scrolls-core": {"name": "scrolls-core", "version": "1.0.0"}
                }
            }""")


        when: 'version is specified by json file: file://version.json'
            versionMap = Scrolls.validateMultiRepo("file://version.json")

        then: 'the json file should be parsed and validated correctly'
            versionMap !=null
            versionMap.name?.equals('scrolls')
            versionMap.version?.equals('1.0.0')
            versionMap.repos != null
            versionMap.repos['scrolls-core']
            versionMap.repos['scrolls-core'].name.equals('scrolls-core')
            versionMap.repos['scrolls-core'].version.equals('1.0.0')

        cleanup:
            version.delete()
    }

    def "multirepo config as inline json text should be read and validated" () {
        setup:
            def versionMap = null

        when: 'version is specified by json as inline argument'
            versionMap = Scrolls.validateMultiRepo("""{
                "name": "scrolls",
                "version": "1.0.0",
                "repos": {
                    "scrolls-core": {"name": "scrolls-core", "version": "1.0.0"}
                }
            }""")

        then: 'the json should be parsed and validated correctly'
            versionMap !=null
            versionMap.name?.equals('scrolls')
            versionMap.version?.equals('1.0.0')
            versionMap.repos != null
            versionMap.repos['scrolls-core']
            versionMap.repos['scrolls-core'].name.equals('scrolls-core')
            versionMap.repos['scrolls-core'].version.equals('1.0.0')
    }

    private static void invokeMainWithArgs(args) {
        try {
            Scrolls.main(args as String[])
        } catch (CheckExitCalled ignored) {
            // We have to ignore this due to how System.exit and ExpectedSystemExit seems to work to avoid exiting the test prematurely
        }
    }
}
