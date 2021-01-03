import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import utils.ProgressResponseBody
import java.io.*


fun getStory(
    path: String,
    dst: String,
    storyListChaptersSelector: String? = KimetsuNoYaibaSelector.STORY_LIST_CHAPTERS
) {
    val doc = Jsoup.connect(path).get()
    doc.select(storyListChaptersSelector).map { aTag ->
        aTag.attr("href")
    }.map { chapterPath ->
        getChapter(
            path = "${HamTruyenTranhNetConstants.HOST_URL}/$chapterPath",
            dst = dst,
            chapterListPageSelector = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES
        )
    }
}

fun getChapter(
    path: String,
    dst: String,
    chapterListPageSelector: String? = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES
) {
    val chapterDir = "$dst/${getChapterNameFromPath(path)}"
    File(chapterDir).mkdirs()
    val doc = Jsoup.connect(path).get()
    doc.select(chapterListPageSelector).map { imgTag ->
        imgTag.attr("src")
    }.map { url ->
        downloadImage(path = url, dstFile = "${chapterDir}/${getFileNameFromPath(url)}")
    }
}

fun getChapterNameFromPath(path: String): String {
    return path.split("/").last().split(".").first()
}

fun getFileNameFromPath(path: String): String {
    return path.split("/").last()
}


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

fun main(args: Array<String>) {
    getStory(
        path = "http://www.hamtruyentranh.net/truyen/kimetsu-no-yaiba-1221.html",
        dst = "./imgs/${KimetsuNoYaibaSelector.STORY_NAME}",
        storyListChaptersSelector = KimetsuNoYaibaSelector.STORY_LIST_CHAPTERS
    )
}