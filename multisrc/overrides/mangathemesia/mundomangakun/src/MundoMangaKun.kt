package eu.kanade.tachiyomi.extension.pt.mundomangakun

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class MundoMangaKun : MangaThemesia(
    "Mundo Mangá-Kun",
    "https://mundomangakun.com.br",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    // Changed their theme from a custom one to MangaThemesia.
    // The URLs are incompatible between the versions.
    override val versionId = 2

    override val client: OkHttpClient = super.client.newBuilder()
        .build()
}
