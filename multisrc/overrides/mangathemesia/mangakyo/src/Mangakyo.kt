package eu.kanade.tachiyomi.extension.id.mangakyo

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
<<<<<<< HEAD
import java.util.concurrent.TimeUnit

class Mangakyo : MangaThemesia("Mangakyo", "https://mangakyo.id", "id") {
=======
import java.text.SimpleDateFormat
import java.util.Locale

class Mangakyo : MangaThemesia(
    "Mangakyo",
    "https://mangakyo.org",
    "id",
    "/komik",
    SimpleDateFormat("MMM d, yyyy", Locale("id")),
) {
>>>>>>> upstream/master

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .rateLimit(4)
        .build()

    override val seriesTitleSelector = ".ts-breadcrumb li:last-child span"
    override val seriesAuthorSelector = ".infotable tr:contains(Pengarang) td:last-child"
    override val seriesDescriptionSelector = ".entry-content[itemprop=description] p:not(:contains(melapor ke fanspage))"
    override val seriesAltNameSelector = ".infotable tr:contains(Alternatif) td:last-child"
    override val seriesTypeSelector = ".infotable tr:contains(Tipe) td:last-child"
}
