package eu.kanade.tachiyomi.extension.en.mmscans

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import org.jsoup.nodes.Element

class MMScans : Madara("MMScans", "https://mm-scans.org", "en") {

    // The site customized the listing and does not include a .manga class.
    override val filterNonMangaItems = false

    override val popularMangaUrlSelector = "div.item-summary a"
    override fun chapterListSelector() = "li.chapter-li"
    override fun searchMangaSelector() = "a"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            select(popularMangaUrlSelector).first()?.let {
                manga.setUrlWithoutDomain(it.attr("abs:href"))
                manga.title = it.selectFirst("h3")!!.ownText()
            }

            select("img").first()?.let {
                manga.thumbnail_url = imageFromElement(it)
            }
        }

        return manga
    }

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()

        with(element) {
            select(chapterUrlSelector).first()?.let { urlElement ->
                chapter.url = urlElement.attr("abs:href").let {
                    it.substringBefore("?style=paged") + if (!it.endsWith(chapterUrlSuffix)) chapterUrlSuffix else ""
                }
                chapter.name = urlElement.selectFirst(".chapter-title-date p")!!.text()
            }
            chapter.date_upload = parseChapterDate(select(chapterDateSelector()).firstOrNull()?.text())
        }

        return chapter
    }

    override fun searchMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            manga.setUrlWithoutDomain(attr("abs:href"))
            select("div.post-title h3").first()?.let {
                manga.title = it.ownText()
            }
            select("img").first()?.let {
                manga.thumbnail_url = imageFromElement(it)
            }
        }

        return manga
    }

    override val mangaDetailsSelectorDescription = "div.summary-text p"
    override val mangaDetailsSelectorGenre = "div.genres-content"
}
