package se.diabol.notifier.releasenotes

/**
 * Created with IntelliJ IDEA.
 * User: andreas
 * Date: 2013-01-30
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
class AbstractReportGenerator {

    def execCommand(cmd, workdir='.') {
        println "execCommand(${cmd},${workdir})"
        def proc = cmd.execute(null,new File(workdir))
        def sout = new StringBuffer()
        def serr = new StringBuffer()
        proc.consumeProcessOutput(sout,serr)
        proc.waitFor()
        if (proc.exitValue()!=0) {
            println "Failed to execute command: ${cmd}"
            println serr
            println sout
            throw new Exception("Failed to Execute command: ${cmd} : ${serr.toString()}")
        }
        return [status  : proc.exitValue(), stdout: sout, stderr: serr]
    }
}
