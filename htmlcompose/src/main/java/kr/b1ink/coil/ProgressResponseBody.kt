package kr.b1ink.coil

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

internal class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressId: String
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource = bufferedSource ?: source(source = responseBody.source())
        .buffer()
        .also {
            bufferedSource = it
        }

    private fun source(source: Source): Source = object : ForwardingSource(delegate = source) {
        var totalBytesRead = 0f
        var oldProgress = -1f

        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            // bytesRead가 -1이면 스트림의 끝을 의미합니다.
            if (bytesRead != -1L) {
                totalBytesRead += bytesRead
            }
            val contentLength = responseBody.contentLength().toFloat()
            if (contentLength > 0) {
                val progress = (totalBytesRead / contentLength).coerceIn(0f, 1f)
                if (oldProgress != progress) {
                    oldProgress = progress
                    ProgressManager.onProgress(progressId, progress)
                }
            }
            return bytesRead
        }
    }
}