package eu.kanade.tachiyomi.extension.ar.mangaspark

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.model.SManga
import org.jsoup.nodes.Element

class MangaSpark : Madara("MangaSpark", "https://mangaspark.com", "ar") {
    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            select(popularMangaUrlSelector).first()?.let {
                manga.setUrlWithoutDomain(it.attr("abs:href"))
                manga.title = it.ownText()
            }

            select("img").first()?.let {
                manga.thumbnail_url = imageFromElement(it)?.replace("mangaspark", "mangalek")
            }
        }

        return manga
    }

<<<<<<< HEAD
=======
class MangaSpark : Madara(
    "MangaSpark",
    "https://mangaspark.org",
    "ar",
    dateFormat = SimpleDateFormat("d MMMM، yyyy", Locale("ar")),
) {
>>>>>>> upstream/master
    override val chapterUrlSuffix = ""

    override val useNewChapterEndpoint = false

    override fun searchPage(page: Int): String = if (page == 1) "" else "page/$page/"
}
