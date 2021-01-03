import core.MangaHelper
import core.pdf
import models.StorySelector
import utils.getStorySelectors

fun main() {
    println(pdf.toString())
    println("+-----------------------------------------+")
    println("|                  KManga                 |")
    println("+-----------------------------------------+")
    println("Notice, please install wkhtmltopdf first if it haven't installed yet:")
    println("brew install wkhtmltopdf --cask")
    println("+-----------------------------------------+")

    var selected: Int? = 0
    do {
        val lstStorySelector = getStorySelectors("ham_truyen_tranh_net.json")
        selected = menuV2(lstStorySelector)
        if (selected == 0) continue
        print("From chapter index = ")
        val from = readLine()?.toIntOrNull() ?: continue
        print("How many chapter = ")
        val limit = readLine()?.toIntOrNull() ?: continue
        val selector = if (selected > 0 && selected <= lstStorySelector.size)
            lstStorySelector[selected-1]
        else null
        selector ?: continue
        MangaHelper.getStory(
            selector = selector,
            skipDownload = true,
            from = from,
            limit = limit
        )
    } while (selected != 0)
}

private fun menuV1(): Int {
    println("0 - Exit")
    println("1 - ${KimetsuNoYaibaSelector.storyName}")
    println("2 - ${DragonballSelector.storyName}")
    return readLine()?.toIntOrNull() ?: 0
}

private fun menuV2(lstStorySelector: List<StorySelector>): Int {
    println("0 - Exit")
    lstStorySelector.forEachIndexed { index, storySelector ->
        println("${index + 1} - ${storySelector.storyName}")
    }
    return readLine()?.toIntOrNull() ?: 0
}