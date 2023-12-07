package eu.kanade.tachiyomi.extension.pt.arkhamscan

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class ArkhamScan : MangaThemesia(
    "Arkham Scan",
    "https://arkhamscan.com",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val altNamePrefix = "Nomes alternativos: "
}
