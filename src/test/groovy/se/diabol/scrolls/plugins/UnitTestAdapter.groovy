package se.diabol.scrolls.plugins

import com.sun.grizzly.tcp.Adapter
import com.sun.grizzly.tcp.Request
import com.sun.grizzly.tcp.Response
import com.sun.grizzly.util.buf.ByteChunk


abstract class UnitTestAdapter implements Adapter {

    abstract processRequest(Request request)

    void service(Request request, Response response) {
        def content = new ByteChunk()
        request.doRead(content)
        def processResult = processRequest(request)
        def bytes = processResult.content.bytes
        response.status = processResult.status
        def chunk = new ByteChunk()
        chunk.append(bytes, 0, bytes.length)
        response.contentLength = bytes.length
        response.contentType = 'text/json'
        response.outputBuffer.doWrite(chunk, response)
        response.finish()
    }

    void afterService(Request request, Response response) {
        request.recycle()
        response.recycle()
    }

}
