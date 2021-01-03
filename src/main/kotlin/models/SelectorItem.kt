package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectorItem(
    @SerialName("story_path")
    val storyPath: String,
    @SerialName("story_name")
    val storyName: String,
    @SerialName("chapter_list_pages")
    val chapterListPages: String,
    @SerialName("chapter_list_pages_back_up")
    val chapterListPagesBackUp: String,
    @SerialName("story_list_chapters")
    val storyListChapters: String,
)

