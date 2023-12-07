package eu.kanade.tachiyomi.extension.id.mangayaro

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class Mangayaro : MangaThemesia("Mangayaro", "https://www.mangayaro.id", "id") {

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .rateLimit(4)
        .build()

    override val seriesAuthorSelector = ".tsinfo .imptdt:contains(seniman) i"
}
