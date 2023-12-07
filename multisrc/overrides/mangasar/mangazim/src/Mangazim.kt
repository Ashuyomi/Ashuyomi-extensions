package eu.kanade.tachiyomi.extension.pt.mangazim

import eu.kanade.tachiyomi.multisrc.mangasar.MangaSar
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element

class Mangazim : MangaSar("Mangazim", "https://mangazim.com", "pt-BR") {

    override fun chapterListPaginatedRequest(mangaUrl: String, page: Int): Request {
        return GET(baseUrl + mangaUrl, headers)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return response.asJsoup()
            .select("ul.full-chapters-list > li > a")
            .map(::chapterFromElement)
    }

    private fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        name = element.selectFirst("span.cap-text")!!.text()
        date_upload = element.selectFirst("span.chapter-date")?.text()?.toDate() ?: 0L
        setUrlWithoutDomain(element.attr("href"))
    }
}
