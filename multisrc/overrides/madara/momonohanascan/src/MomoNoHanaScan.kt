package eu.kanade.tachiyomi.extension.pt.momonohanascan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class MomoNoHanaScan : Madara(
    "Momo no Hana Scan",
    "https://momonohanascan.com",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val useNewChapterEndpoint = true
}
