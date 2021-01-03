package models

data class Chapter(
    val id: String,
    val chapterId: String,
    val title: String,
    val pages: List<Page>
)