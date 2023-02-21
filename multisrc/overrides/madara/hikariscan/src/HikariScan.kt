package eu.kanade.tachiyomi.extension.pt.hikariscan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class HikariScan : Madara(
    "HikariScan",
    "https://hikariscan.com.br/",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val useNewChapterEndpoint = true

    override val popularMangaUrlSelector = "div.post-title a:last-child"
}
