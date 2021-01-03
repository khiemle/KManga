package utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

fun downloadImage(path: String, dstFile: String) {
    print("Downloading file = $path ... ")
    val request = Request.Builder().url(path).build()
    val client = with(OkHttpClient.Builder()) {
        addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            if (originalResponse.isSuccessful) {
                val responseBody = originalResponse.body
                responseBody ?: return@addNetworkInterceptor originalResponse.newBuilder().body(responseBody).build()
                val progressResponseBody = ProgressResponseBody(responseBody) { _, _, isDone ->
                    if (isDone) {
                        println("Done")
                    }
                }
                originalResponse.newBuilder().body(progressResponseBody).build()
            } else {
                originalResponse
            }

        }
    }.build()

    try {
        val execute = client.newCall(request).execute()
        val outputStream = FileOutputStream(dstFile)

        val body = execute.body
        body?.let {
            with(outputStream) {
                write(body.bytes())
                close()
            }
        }
    } catch (e: Exception) {
        println("Error, message = ${e.message}")
        e.printStackTrace()
    }
}