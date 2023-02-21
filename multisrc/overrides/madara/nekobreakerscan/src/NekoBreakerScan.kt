package eu.kanade.tachiyomi.extension.pt.nekobreakerscan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class NekoBreakerScan : Madara(
    "NekoBreaker Scan",
    "https://nekobreakerscan.com",
    "pt-BR",
    SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()
}
