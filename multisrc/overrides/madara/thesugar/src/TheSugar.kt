package eu.kanade.tachiyomi.extension.pt.thesugar

<<<<<<< HEAD:multisrc/overrides/madara/thesugar/src/TheSugar.kt
import eu.kanade.tachiyomi.multisrc.madara.Madara
=======
import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.GET
>>>>>>> upstream/master:multisrc/overrides/mangathemesia/sssscanlator/src/SSSScanlator.kt
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.Page
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TheSugar : Madara(
    "The Sugar",
    "https://thesugarscan.com",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

<<<<<<< HEAD:multisrc/overrides/madara/thesugar/src/TheSugar.kt
    override val useNewChapterEndpoint = true
=======
    override fun imageRequest(page: Page): Request {
        val newHeaders = headersBuilder()
            .set("Referer", page.url)
            .set("Accept", "image/avif,image/webp,*/*")
            .set("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
            .set("Sec-Fetch-Dest", "image")
            .set("Sec-Fetch-Mode", "no-cors")
            .set("Sec-Fetch-Site", "same-origin")
            .build()

        return GET(page.imageUrl!!, newHeaders)
    }
>>>>>>> upstream/master:multisrc/overrides/mangathemesia/sssscanlator/src/SSSScanlator.kt
}
