package se.diabol.scrolls

class AbstractReportGenerator {

    static def execCommand(String command, String workingDirectory='.') {
        println "execCommand(${command},${workingDirectory})"
        def proc = command.execute(null, new File(workingDirectory))
        def sout = new StringBuffer()
        def serr = new StringBuffer()
        proc.consumeProcessOutput(sout,serr)
        proc.waitFor()
        if (proc.exitValue() != 0) {
            println "Failed to execute command: ${command}"
            println serr
            println sout
            throw new Exception("Failed to Execute command: ${command} : ${serr.toString()}")
        }
        return [status  : proc.exitValue(), stdout: sout, stderr: serr]
    }
}
