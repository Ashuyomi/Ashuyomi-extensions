package eu.kanade.tachiyomi.extension.pt.origamiorpheans

import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class OrigamiOrpheans : MangaThemesia(
    "Origami Orpheans",
    "https://origami-orpheans.com.br",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    // Scanlator migrated from Madara to WpMangaReader.
    override val versionId = 2

    override val client: OkHttpClient = super.client.newBuilder()
        .build()

    override val altNamePrefix = "Nomes alternativos: "
}
