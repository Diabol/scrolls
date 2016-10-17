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
        final ByteArrayOutputStream sout = new ByteArrayOutputStream()
        System.out = new PrintStream(sout)

        //given: 'nothing'

        when: 'help option provided'
        Scrolls.invokeMethod('main', ['--help'].toArray())

        then: "help is displayed"

        String out = sout.toString(StandardCharsets.UTF_8.name())
        assert out.contains('usage: scrolls')
    }

    def "no scrolls generated when version options are missing"() {
        final ByteArrayOutputStream sout = new ByteArrayOutputStream()
        System.out = new PrintStream(sout)

        // given: nothing
        when: 'no versions given'
        Scrolls.invokeMethod('main', [].toArray())

        then: "error is returned"
        String out = sout.toString(StandardCharsets.UTF_8.name())
        assert out.contains('error: Missing required options: old-version, new-version')
    }
}
