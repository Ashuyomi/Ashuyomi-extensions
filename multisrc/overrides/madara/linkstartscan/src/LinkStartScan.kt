package eu.kanade.tachiyomi.extension.pt.linkstartscan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class LinkStartScan : Madara(
    "Link Start Scan",
    "https://www.linkstartscan.xyz",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val useNewChapterEndpoint = true
}
