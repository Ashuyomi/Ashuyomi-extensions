package eu.kanade.tachiyomi.extension.id.kumapoi

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

<<<<<<< HEAD
class KumaPoi : MangaThemesia("KumaPoi", "https://kumapoi.club", "id") {
    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
=======
class KumaPoi : MangaThemesia("KumaPoi", "https://kumapoi.info", "id") {
    override val client: OkHttpClient = super.client.newBuilder()
>>>>>>> upstream/master
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
}
