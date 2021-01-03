import id.jasoet.funpdf.HtmlToPdf
import id.jasoet.funpdf.PageOrientation
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import utils.ProgressResponseBody
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

fun getStory(
    path: String,
    dst: String,
    storyListChaptersSelector: String? = KimetsuNoYaibaSelector.STORY_LIST_CHAPTERS,
    chapterListPageSelector: String? = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES,
    chapterListPageSelectorBackup: String? = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES_BACK_UP,
    storyName: String = KimetsuNoYaibaSelector.STORY_NAME,
    skipDownload: Boolean = false,
    from: Int = 0,
    limit: Int = -1,
) {
    val doc = Jsoup.connect(path).get()
    println("Getting story $storyName ...")
    doc.select(storyListChaptersSelector).map { aTag ->
        aTag.attr("href")
    }.reversed().filterIndexed { index, _ ->
        index >= from && (if (limit < 0) true else index < from + limit)
    }.map { chapterPath ->
        val chapterName = getChapterNameFromPath(chapterPath)
        print("Getting chapter $chapterName ... ")
        var imageUrls = getChapter(
            path = "${HamTruyenTranhNetConstants.HOST_URL}/$chapterPath",
            dst = dst,
            chapterListPageSelector = chapterListPageSelector,
            skipDownload = skipDownload
        )
        if (imageUrls.isNullOrEmpty()) {
            imageUrls = getChapter(
                path = "${HamTruyenTranhNetConstants.HOST_URL}/$chapterPath",
                dst = dst,
                chapterListPageSelector = chapterListPageSelectorBackup,
                skipDownload = skipDownload
            )
        }
        println("Done")
        chapterName to imageUrls
    }.map { pair ->
        val (chapterName, imageUrls) = pair
        createPdfByLib(imageUrls, pdfName = "./${storyName}/${storyName}-$chapterName.pdf")
    }
    println("Finished story $storyName from $from and got $limit chapter")
}

fun getChapter(
    path: String,
    dst: String,
    chapterListPageSelector: String? = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES,
    skipDownload: Boolean = false
): List<String> {
    val chapterDir = "$dst/${getChapterNameFromPath(path)}"
    File(chapterDir).mkdirs()
    val doc = Jsoup.connect(path).get()
    return doc.select(chapterListPageSelector).map { imgTag ->
        imgTag.attr("src")
    }.map { url ->
        val dstFile = "${chapterDir}/${getFileNameFromPath(url)}"
        if (!skipDownload) {
            downloadImage(path = url, dstFile = dstFile)
        }
        url
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

val pdf by lazy {
    HtmlToPdf(executable = "/usr/bin/wkhtmltopdf") {
        orientation(PageOrientation.PORTRAIT)
        pageSize("A5")
        marginTop("0in")
        marginBottom("0in")
        marginLeft("0in")
        marginRight("0in")
    }
}

fun createPdfByLib(lstImages: List<String>, pdfName: String) {
    print("Wrapping image urls and creating $pdfName file ... ")
    val imageUrls = lstImages.joinToString(separator = "") { imageUrl ->
        "<img style=\"width:100%; max-height:960px;\" src=\"$imageUrl\" /><br/>"
    }
    val htmlString = "<html><body>${imageUrls}</body></html>"
    val outputFile = Paths.get(pdfName).toFile()
    pdf.convert(input = htmlString,output = outputFile) // will always return null if output is redirected
    println("Done")
}

fun main(args: Array<String>) {
    println(pdf.toString())
    println("+-----------------------------------------+")
    println("|                  KManga                 |")
    println("+-----------------------------------------+")
    println("Notice, please install wkhtmltopdf first if it haven't installed yet:")
    println("brew install wkhtmltopdf --cask")
    println("+-----------------------------------------+")
    getStory(
        path = KimetsuNoYaibaSelector.STORY_PATH,
        dst = KimetsuNoYaibaSelector.STORY_NAME,
        storyListChaptersSelector = KimetsuNoYaibaSelector.STORY_LIST_CHAPTERS,
        chapterListPageSelector = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES,
        chapterListPageSelectorBackup = KimetsuNoYaibaSelector.CHAPTER_LIST_PAGES_BACK_UP,
        storyName = KimetsuNoYaibaSelector.STORY_NAME,
        skipDownload = true,
        from = 0,
        limit = 2
    )

//    getStory(
//        path = DragonballSelector.STORY_PATH,
//        dst = DragonballSelector.STORY_NAME,
//        storyListChaptersSelector = DragonballSelector.STORY_LIST_CHAPTERS,
//        chapterListPageSelector = DragonballSelector.CHAPTER_LIST_PAGES,
//        chapterListPageSelectorBackup = DragonballSelector.CHAPTER_LIST_PAGES_BACK_UP,
//        storyName = DragonballSelector.STORY_NAME,
//        skipDownload = true,
//        from = 0,
//        limit = 2
//    )

}