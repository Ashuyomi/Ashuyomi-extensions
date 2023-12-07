package eu.kanade.tachiyomi.extension.id.manhwalandmom

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
<<<<<<< HEAD
import java.util.concurrent.TimeUnit
=======
import java.text.SimpleDateFormat
import java.util.Locale
>>>>>>> upstream/master

class ManhwaLandMom : MangaThemesia(
    "ManhwaLand.mom",
    "https://manhwaland.lat",
    "id",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("id")),
) {

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .rateLimit(4)
        .build()
}
