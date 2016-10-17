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
}
