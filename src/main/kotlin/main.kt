import core.MangaHelper
import core.pdf

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
        MangaHelper.getStory(
            selector = selector,
            skipDownload = true,
            from = from,
            limit = limit
        )
    } while (selected != 0)
}