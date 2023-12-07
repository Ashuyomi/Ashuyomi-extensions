package eu.kanade.tachiyomi.extension.id.manhwadesu

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
<<<<<<< HEAD
import java.util.concurrent.TimeUnit
=======
import org.jsoup.nodes.Element
>>>>>>> upstream/master

class ManhwaDesu : MangaThemesia("ManhwaDesu", "https://manhwadesu.one", "id", "/komik") {

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .rateLimit(4)
        .build()

    override fun Element.imgAttr(): String {
        attributes()
            .find { it.key.endsWith("original-src") }
            ?.let { return absUrl(it.key) }

        return when {
            hasAttr("data-lazy-src") -> attr("abs:data-lazy-src")
            hasAttr("data-src") -> attr("abs:data-src")
            else -> attr("abs:src")
        }
    }
}
