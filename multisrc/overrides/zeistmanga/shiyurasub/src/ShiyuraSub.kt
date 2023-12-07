package eu.kanade.tachiyomi.extension.id.shiyurasub

import eu.kanade.tachiyomi.multisrc.zeistmanga.Language
import eu.kanade.tachiyomi.multisrc.zeistmanga.ZeistManga
<<<<<<< HEAD
=======
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
>>>>>>> upstream/master

class ShiyuraSub : ZeistManga("ShiyuraSub", "https://shiyurasub.blogspot.com", "id") {

    override val hasFilters = true

<<<<<<< HEAD
    override fun getLanguageList(): List<Language> = listOf(
        Language(intl.all, ""),
    )
=======
    override val supportsLatest = false

    override fun popularMangaRequest(page: Int): Request = latestUpdatesRequest(page)
    override fun popularMangaParse(response: Response): MangasPage = latestUpdatesParse(response)

    override val pageListSelector = "main.content article.container"

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()
        val profileManga = document.selectFirst("main.content.post")!!
        return SManga.create().apply {
            thumbnail_url = profileManga.selectFirst("div.grid img")!!.attr("abs:src")
            description = profileManga.select("#synopsis").text()
            genre = profileManga.select("div.my-5 > a[rel=tag]")
                .joinToString { it.text() }
        }
    }
>>>>>>> upstream/master
}
