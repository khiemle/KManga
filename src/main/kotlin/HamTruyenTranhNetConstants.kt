import models.StorySelector

class HamTruyenTranhNetConstants {
    companion object {
        const val HOST_URL = "http://www.hamtruyentranh.net"
    }
}

object KimetsuNoYaibaSelector: StorySelector( storyPath = "http://www.hamtruyentranh.net/truyen/kimetsu-no-yaiba-1221.html",
    storyName = "kimetsu-no-yaiba-1221",
    chapterListPages = "div.each-page div.page-chapter  img",
    chapterListPagesBackUp = "div.each-page img",
    storyListChapters = "div.content p a"

)

object DragonballSelector: StorySelector(
    storyName = "7-vien-ngoc-rong-268",
    storyPath = "http://www.hamtruyentranh.net/truyen/7-vien-ngoc-rong-268.html",
    chapterListPages = "div.each-page img",
    chapterListPagesBackUp = "div.each-page div.page-chapter  img",
    storyListChapters = "div.content p a",
)