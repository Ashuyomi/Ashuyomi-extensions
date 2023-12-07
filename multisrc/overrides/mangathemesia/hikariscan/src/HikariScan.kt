package eu.kanade.tachiyomi.extension.pt.hikariscan

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class HikariScan : MangaThemesia(
    "Hikari Scan",
    "https://hikariscan.org",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val altNamePrefix = "Nomes alternativos: "

    override val seriesArtistSelector = ".tsinfo .imptdt:contains(Artista) > i"
    override val seriesAuthorSelector = ".tsinfo .imptdt:contains(Autor) > i"
    override val seriesTypeSelector = ".tsinfo .imptdt:contains(Tipo) > a"
}
