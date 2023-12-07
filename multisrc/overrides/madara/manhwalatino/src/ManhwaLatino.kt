package eu.kanade.tachiyomi.extension.es.manhwalatino

import eu.kanade.tachiyomi.multisrc.madara.Madara
<<<<<<< HEAD
=======
import eu.kanade.tachiyomi.source.model.SChapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.jsoup.nodes.Element
>>>>>>> upstream/master
import java.text.SimpleDateFormat
import java.util.Locale

class ManhwaLatino : Madara(
    "Manhwa-Latino",
    "https://manhwa-latino.com",
    "es",
    SimpleDateFormat("dd/MM/yyyy", Locale("es")),
) {

<<<<<<< HEAD
    override val supportsLatest = false
=======
    override val client: OkHttpClient = super.client.newBuilder()
        .addInterceptor { chain ->
            val request = chain.request()
            val headers = request.headers.newBuilder()
                .removeAll("Accept-Encoding")
                .build()
            val response = chain.proceed(request.newBuilder().headers(headers).build())
            if (response.headers("Content-Type").contains("application/octet-stream") && response.request.url.toString().endsWith(".jpg")) {
                val orgBody = response.body.bytes()
                val newBody = orgBody.toResponseBody("image/jpeg".toMediaTypeOrNull())
                response.newBuilder()
                    .body(newBody)
                    .build()
            } else {
                response
            }
        }
        .build()
>>>>>>> upstream/master

    override val useNewChapterEndpoint = true

    override val chapterUrlSelector = "a:eq(1)"

    override val mangaDetailsSelectorStatus = "div.post-content_item:contains(Estado del comic) > div.summary-content"
<<<<<<< HEAD
=======
    override val mangaDetailsSelectorDescription = "div.post-content_item:contains(Resumen) div.summary-container"
    override val pageListParseSelector = "div.page-break img.wp-manga-chapter-img"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()

        with(element) {
            select(chapterUrlSelector).first()?.let { urlElement ->
                chapter.url = urlElement.attr("abs:href").let {
                    it.substringBefore("?style=paged") + if (!it.endsWith(chapterUrlSuffix)) chapterUrlSuffix else ""
                }
                chapter.name = urlElement.wholeText().substringAfter("\n")
            }

            chapter.date_upload = select("img:not(.thumb)").firstOrNull()?.attr("alt")?.let { parseRelativeDate(it) }
                ?: select("span a").firstOrNull()?.attr("title")?.let { parseRelativeDate(it) }
                ?: parseChapterDate(select(chapterDateSelector()).firstOrNull()?.text())
        }

        return chapter
    }
>>>>>>> upstream/master
}
