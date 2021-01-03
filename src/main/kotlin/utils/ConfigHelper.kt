package utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.SelectorItem
import models.StorySelector
import java.io.File
import java.io.IOException

fun getJsonDataFromFile(fileName: String): String? {
    val jsonString: String
    try {
        val fileInputStream = File(fileName).inputStream()
        jsonString = fileInputStream.bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

fun getListSelectorFromJson(fileName: String): List<SelectorItem> {
    val jsonData = getJsonDataFromFile(fileName)
    jsonData ?: return listOf()
    return Json.decodeFromString(jsonData)
}

fun mapToSelector(listSelectorItems: List<SelectorItem>) : List<StorySelector> {
    return listSelectorItems.map { selectorItem ->
        StorySelector(
            storyPath = selectorItem.storyPath,
            storyName = selectorItem.storyName,
            chapterListPages = selectorItem.chapterListPages,
            chapterListPagesBackUp = selectorItem.chapterListPagesBackUp,
            storyListChapters = selectorItem.storyListChapters
        )
    }.toList()
}

fun getStorySelectors(fileName: String = "ham_truyen_tranh_net.json"): List<StorySelector> {
    return mapToSelector(getListSelectorFromJson(fileName))
}