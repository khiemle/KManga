package utils
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

class ProgressResponseBody(val responseBody: ResponseBody,
                           val downloadProgressFun: (bytesRead: Long, contentLength: Long, isDone: Boolean) -> Unit) : ResponseBody() {

    lateinit var bufferedSource: BufferedSource

    override fun contentLength(): Long = responseBody.contentLength()

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun source(): BufferedSource {
        if (!::bufferedSource.isInitialized) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead: Long = 0
            override fun read(sink: Buffer, byteCount: Long): Long {
                val read: Long = super.read(sink, byteCount)
                totalBytesRead += if (read != -1L) read else 0
                downloadProgressFun(totalBytesRead, responseBody.contentLength(), read == -1L)
                return read
            }
        }
    }
}