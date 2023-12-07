package eu.kanade.tachiyomi.extension.id.nekomik

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class Nekomik : MangaThemesia("Nekomik", "https://nekomik.com", "id") {

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
<<<<<<< HEAD
=======

    override fun pageListParse(document: Document): List<Page> {
        val obfuscatedJs = document.selectFirst("script:containsData(fromCharCode)")?.data()
            ?: return super.pageListParse(document)

        val data = QuickJs.create().use { context ->
            context.evaluate(
                """
                ts_reader = { run: function(...args) { whatever = args[0] } };
                $obfuscatedJs;
                JSON.stringify(whatever);
                """.trimIndent(),
            ) as String
        }

        val tsReader = json.decodeFromString<TSReader>(data)
        val imageUrls = tsReader.sources.firstOrNull()?.images ?: return emptyList()
        return imageUrls.mapIndexed { index, imageUrl -> Page(index, document.location(), imageUrl) }
    }

    @Serializable
    data class TSReader(
        val sources: List<ReaderImageSource>,
    )

    @Serializable
    data class ReaderImageSource(
        val source: String,
        val images: List<String>,
    )
>>>>>>> upstream/master
}
