package core

import models.StorySelector
import org.jsoup.Jsoup
import utils.downloadImage
import java.io.File

class MangaHelper {
    companion object {
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

        private fun getStory(
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

        private fun getChapter(
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
                println(url)
                if (url.startsWith("http").not()) {
                    return@map "${HamTruyenTranhNetConstants.HOST_URL}/$url"
                }
                url
            }
        }

        private fun getChapterNameFromPath(path: String): String {
            return path.split("/").last().split(".").first()
        }

        private fun getFileNameFromPath(path: String): String {
            return path.split("/").last()
        }
    }
}

