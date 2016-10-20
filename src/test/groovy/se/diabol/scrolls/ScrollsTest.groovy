package se.diabol.scrolls

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class ScrollsTest extends Specification {

    private PrintStream outBefore

    private def setup() {
        outBefore = System.out
    }

    private def cleanup() {
        System.setOut(outBefore)
    }

    def "should display help when help option provided"() {
        given:
        final ByteArrayOutputStream sout = new ByteArrayOutputStream()
        System.out = new PrintStream(sout)

        when: 'help option provided'
        Scrolls.invokeMethod('main', ['--help'].toArray())

        then: 'output contains usage message'
        sout.toString(StandardCharsets.UTF_8.name()).contains('usage: scrolls')
    }

    def "no scrolls generated when version options are missing"() {
        given:
        final ByteArrayOutputStream sout = new ByteArrayOutputStream()
        System.out = new PrintStream(sout)

        when: 'no options are provided'
        Scrolls.invokeMethod('main', [].toArray())

        then: 'output contains error message'
        sout.toString(StandardCharsets.UTF_8.name()).contains('error: Missing required options: old-version, new-version')
    }

    def "multirepo config as json file should be read and validated" () {
        setup:
            def versionMap = null
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

}
