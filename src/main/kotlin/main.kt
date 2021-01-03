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
    selector: StorySelector,
    skipDownload: Boolean = false,
    from: Int = 0,
    limit: Int = -1,
) {
    getStory(
        path = selector.storyPath,
        dst = selector.storyName,
        storyListChaptersSelector = selector.storyListChapters,
        chapterListPageSelector = selector.chapterListPages,
        chapterListPageSelectorBackup = selector.chapterListPagesBackUp,
        storyName = selector.storyName,
        skipDownload = skipDownload,
        from = from,
        limit = limit
    )
}

fun getStory(
    path: String,
    dst: String,
    storyListChaptersSelector: String? = KimetsuNoYaibaSelector.storyListChapters,
    chapterListPageSelector: String? = KimetsuNoYaibaSelector.chapterListPages,
    chapterListPageSelectorBackup: String? = KimetsuNoYaibaSelector.chapterListPagesBackUp,
    storyName: String = KimetsuNoYaibaSelector.storyName,
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
    chapterListPageSelector: String? = KimetsuNoYaibaSelector.chapterListPages,
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
    pdf.convert(input = htmlString, output = outputFile) // will always return null if output is redirected
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

    var selected: Int? = 0
    do {
        println("0 - Exit")
        println("1 - ${KimetsuNoYaibaSelector.storyName}")
        println("2 - ${DragonballSelector.storyName}")
        selected = readLine()?.toIntOrNull() ?: 0
        if (selected == 0) continue
        print("From chapter index = ")
        val from = readLine()?.toIntOrNull() ?: continue
        print("How many chapter = ")
        val limit = readLine()?.toIntOrNull() ?: continue
        val selector = when (selected) {
            1 -> KimetsuNoYaibaSelector
            2 -> DragonballSelector
            else -> null
        }
        selector ?: continue
        getStory(
            selector = selector,
            skipDownload = true,
            from = from,
            limit = limit
        )
    } while (selected != 0)
}