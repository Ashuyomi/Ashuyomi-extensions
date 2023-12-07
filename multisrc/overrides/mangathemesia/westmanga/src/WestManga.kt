package eu.kanade.tachiyomi.extension.id.westmanga

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class WestManga : MangaThemesia("West Manga", "https://westmanga.info", "id") {
    // Formerly "West Manga (WP Manga Stream)"
    override val id = 8883916630998758688

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .rateLimit(4)
        .build()

    override val seriesDetailsSelector = ".seriestucontent"
    override val seriesTypeSelector = ".infotable tr:contains(Type) td:last-child"

    override val hasProjectPage = true
}
