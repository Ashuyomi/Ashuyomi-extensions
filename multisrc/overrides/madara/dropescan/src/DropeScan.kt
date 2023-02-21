package eu.kanade.tachiyomi.extension.pt.dropescan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale

class DropeScan : Madara(
    "Drope Scan",
    "https://dropescan.com",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val useNewChapterEndpoint = true

    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/manga/page/$page/?m_orderby=views", headers)

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/manga/page/$page/?m_orderby=latest", headers)
}
