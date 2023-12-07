package eu.kanade.tachiyomi.extension.ar.mangastarz

import eu.kanade.tachiyomi.multisrc.madara.Madara
<<<<<<< HEAD
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Headers
import org.jsoup.nodes.Element

class MangaStarz : Madara("Manga Starz", "https://mangastarz.com", "ar") {

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("Referer", baseUrl)

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            select(popularMangaUrlSelector).first()?.let {
                manga.setUrlWithoutDomain(it.attr("abs:href"))
                manga.title = it.ownText()
            }

            select("img").first()?.let {
                manga.thumbnail_url = imageFromElement(it)?.replace("mangastarz", "mangalek")
            }
        }

        return manga
    }
=======
import java.text.SimpleDateFormat
import java.util.Locale
>>>>>>> upstream/master

class MangaStarz : Madara(
    "Manga Starz",
    "https://mangastarz.org",
    "ar",
    dateFormat = SimpleDateFormat("d MMMM، yyyy", Locale("ar")),
) {
    override val chapterUrlSuffix = ""

    override val useNewChapterEndpoint = false

    override fun searchPage(page: Int): String = if (page == 1) "" else "page/$page/"
}
