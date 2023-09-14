package eu.kanade.tachiyomi.extension.pt.wickedwitchscan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class WickedWitchScan : Madara(
    "Wicked Witch Scan",
    "https://wickedwitchscan.com",
    "pt-BR",
    SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()
}
