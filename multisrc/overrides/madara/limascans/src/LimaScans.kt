package eu.kanade.tachiyomi.extension.pt.limascans

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

<<<<<<< HEAD:multisrc/overrides/madara/limascans/src/LimaScans.kt
class LimaScans : Madara(
    "Lima Scans",
    "http://limascans.xyz/v2",
=======
class CeriseScan : Madara(
    "Cerise Scan",
    "https://cerisescan.com",
>>>>>>> upstream/master:multisrc/overrides/madara/cerisescans/src/CeriseScan.kt
    "pt-BR",
    SimpleDateFormat("dd 'de' MMMMM 'de' yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

    override fun mangaDetailsRequest(manga: SManga): Request {
        return GET(baseUrl + manga.url.removePrefix("/v2"), headers)
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET(baseUrl + manga.url.removePrefix("/v2"), headers)
    }

    override fun pageListRequest(chapter: SChapter): Request {
        if (chapter.url.startsWith("http")) {
            return GET(chapter.url.replace("/home1", ""), headers)
        }

        return GET(baseUrl + chapter.url.replace("/home1", ""), headers)
    }
}
