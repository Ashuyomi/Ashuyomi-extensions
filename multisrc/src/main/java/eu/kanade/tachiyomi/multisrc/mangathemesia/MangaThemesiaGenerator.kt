package eu.kanade.tachiyomi.multisrc.mangathemesia

import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

// Formerly WPMangaStream & WPMangaReader -> MangaThemesia
class MangaThemesiaGenerator : ThemeSourceGenerator {

    override val themePkg = "mangathemesia"

    override val themeClass = "MangaThemesia"

    override val baseVersionCode: Int = 24

    override val sources = listOf(

        // Extenções Modificadas:

        SingleLang("Mundo Mangá-Kun", "https://mundomangakun.com.br", "pt-BR", className = "MundoMangaKun", isNsfw = true, overrideVersionCode = 1),
        SingleLang("Mangás Chan", "https://mangaschan.com", "pt-BR", className = "MangasChan", overrideVersionCode = 1),
        SingleLang("Silence Scan", "https://silencescan.com.br", "pt-BR", isNsfw = true, overrideVersionCode = 6),

        // Extenções Oficiais:
        MultiLang("Asura Scans", "https://www.asurascans.com", listOf("en", "tr"), className = "AsuraScansFactory", pkgName = "asurascans", overrideVersionCode = 18),
        MultiLang("Flame Scans", "https://flamescans.org", listOf("en"), className = "FlameScansFactory", pkgName = "flamescans", overrideVersionCode = 4),
        MultiLang("Komik Lab", "https://komiklab.com", listOf("en", "id"), className = "KomikLabFactory", pkgName = "komiklab", overrideVersionCode = 1),
        MultiLang("Miau Scan", "https://miauscan.com", listOf("es", "pt-BR")),
        SingleLang("Animated Glitched Scans", "https://anigliscans.com", "en"),
        SingleLang("Arcane scan", "https://arcanescan.fr", "fr"),
        SingleLang("Arena Scans", "https://arenascans.net", "en"),
        SingleLang("ARESManga", "https://aresmanga.net", "ar", pkgName = "iimanga", overrideVersionCode = 4),
        SingleLang("Azure Scans", "https://azuremanga.com", "en", overrideVersionCode = 1),
        SingleLang("Boosei", "https://boosei.net", "id", overrideVersionCode = 2),
        SingleLang("Cartel de Manhwas", "https://carteldemanhwas.com", "es", overrideVersionCode = 5),
        SingleLang("Clayrer", "https://clayrer.net", "es"),
        SingleLang("Constellar Scans", "https://constellarscans.com", "en", isNsfw = true, overrideVersionCode = 14),
        SingleLang("Cosmic Scans", "https://cosmicscans.com", "en", overrideVersionCode = 1),
        SingleLang("Diskus Scan", "https://diskusscan.com", "pt-BR", overrideVersionCode = 7),
        SingleLang("Dojing.net", "https://dojing.net", "id", isNsfw = true, className = "DojingNet"),
        SingleLang("DuniaKomik.id", "https://duniakomik.id", "id", className = "DuniaKomikId"),
        SingleLang("FlameScans.fr", "https://flamescans.fr", "fr", className = "FlameScansFR"),
        SingleLang("Franxx Mangás", "https://franxxmangas.net", "pt-BR", className = "FranxxMangas", isNsfw = true),
        SingleLang("Gecenin Lordu", "https://geceninlordu.com", "tr", overrideVersionCode = 1),
        SingleLang("GoGoManga", "https://gogomanga.fun", "en", overrideVersionCode = 1),
        SingleLang("Gremory Mangas", "https://gremorymangas.com", "es"),
        SingleLang("Hanuman Scan", "https://hanumanscan.com", "en"),
        SingleLang("Heroxia", "https://heroxia.com", "id", isNsfw = true),
        SingleLang("Imagine Scan", "https://imaginescan.com.br", "pt-BR", isNsfw = true, overrideVersionCode = 1),
        SingleLang("InariManga", "https://inarimanga.com", "es"),
        SingleLang("Infernal Void Scans", "https://void-scans.com", "en", overrideVersionCode = 5),
        SingleLang("Kanzenin", "https://kanzenin.xyz", "id", isNsfw = true),
        SingleLang("Kiryuu", "https://kiryuu.id", "id", overrideVersionCode = 6),
        SingleLang("Komik AV", "https://komikav.com", "id", overrideVersionCode = 1),
        SingleLang("Komik Cast", "https://komikcast.site", "id", overrideVersionCode = 18),
        SingleLang("KomikDewasa", "https://komikdewasa.org", "id", isNsfw = true),
        SingleLang("Komik Station", "https://komikstation.co", "id", overrideVersionCode = 3),
        SingleLang("KomikIndo.co", "https://komikindo.co", "id", className = "KomikindoCo", overrideVersionCode = 3),
        SingleLang("KomikMama", "https://komikmama.co", "id", overrideVersionCode = 1),
        SingleLang("KomikManhwa", "https://komikmanhwa.me", "id", isNsfw = true),
        SingleLang("KumaPoi", "https://kumapoi.me", "id", isNsfw = true),
        SingleLang("Komiku.com", "https://komiku.com", "id", className = "KomikuCom"),
        SingleLang("Kuma Scans (Kuma Translation)", "https://kumascans.com", "en", className = "KumaScans", overrideVersionCode = 1),
        SingleLang("LianScans", "https://www.lianscans.my.id", "id", isNsfw = true),
        SingleLang("Magus Manga", "https://magusmanga.com", "ar"),
        SingleLang("Manga Indo.me", "https://mangaindo.me", "id", className = "MangaIndoMe"),
        SingleLang("Manga Raw.org", "https://mangaraw.org", "ja", className = "MangaRawOrg", overrideVersionCode = 1),
        SingleLang("Mangacim", "https://www.mangacim.com", "tr", overrideVersionCode = 1),
        SingleLang("MangaKita", "https://mangakita.net", "id", overrideVersionCode = 1),
        SingleLang("Mangakyo", "https://mangakyo.id", "id", overrideVersionCode = 1),
        SingleLang("Mangasusu", "https://mangasusu.co.in", "id", isNsfw = true, overrideVersionCode = 1),
        SingleLang("MangaTale", "https://mangatale.co", "id"),
        SingleLang("MangaWT", "https://mangawt.com", "tr", overrideVersionCode = 5),
        SingleLang("Mangayaro", "https://mangayaro.net", "id"),
        SingleLang("MangaSwat", "https://swatmanga.net", "ar", overrideVersionCode = 6),
        SingleLang("MangKomik", "https://mangkomik.net", "id", overrideVersionCode = 1),
        SingleLang("Manhwa Freak", "https://manhwafreak.com", "en", overrideVersionCode = 1),
        SingleLang("ManhwaDesu", "https://manhwadesu.org", "id", isNsfw = true, overrideVersionCode = 3),
        SingleLang("ManhwaIndo", "https://manhwaindo.id", "id", isNsfw = true, overrideVersionCode = 2),
        SingleLang("ManhwaLand.mom", "https://manhwaland.guru", "id", isNsfw = true, className = "ManhwaLandMom", overrideVersionCode = 3),
        SingleLang("ManhwaList", "https://manhwalist.in", "id", overrideVersionCode = 2),
        SingleLang("Manhwax", "https://manhwax.com", "en", isNsfw = true),
        SingleLang("Mareceh", "https://mareceh.com", "id", isNsfw = true, pkgName = "mangceh", overrideVersionCode = 10),
        SingleLang("Martial Manga", "https://martialmanga.com", "es"),
        SingleLang("MasterKomik", "https://masterkomik.com", "id", overrideVersionCode = 1),
        SingleLang("MELOKOMIK", "https://melokomik.xyz", "id"),
        SingleLang("Mihentai", "https://mihentai.com", "all", isNsfw = true, overrideVersionCode = 2),
        SingleLang("MirrorDesu", "https://mirrordesu.me", "id", isNsfw = true),
        SingleLang("Mode Scanlator", "https://modescanlator.com", "pt-BR", overrideVersionCode = 8),
        SingleLang("Nekomik", "https://nekomik.com", "id"),
        SingleLang("Ngomik", "https://ngomik.net", "id", overrideVersionCode = 2),
        SingleLang("NIGHT SCANS", "https://nightscans.org", "en", isNsfw = true, className = "NightScans", overrideVersionCode = 1),
        SingleLang("Nocturnal Scans", "https://nocturnalscans.com", "en", overrideVersionCode = 1),
        SingleLang("Omega Scans", "https://omegascans.org", "en", isNsfw = true),
        SingleLang("Origami Orpheans", "https://origami-orpheans.com.br", "pt-BR", overrideVersionCode = 9),
        SingleLang("Ozul Scans", "https://ozulscans.com", "ar"),
        SingleLang("Patatescans", "https://patatescans.com", "fr", isNsfw = true, overrideVersionCode = 2),
        SingleLang("Phantom Scans", "https://phantomscans.com", "en", overrideVersionCode = 1),
        SingleLang("Phoenix Fansub", "https://phoenixfansub.com", "es", overrideVersionCode = 2),
        SingleLang("Pi Scans", "https://piscans.in", "id", overrideVersionCode = 1),
        SingleLang("PMScans", "https://rackusreads.com", "en", overrideVersionCode = 3),
        SingleLang("Raiki Scan", "https://raikiscan.com", "es"),
        SingleLang("Rawkuma", "https://rawkuma.com/", "ja"),
        SingleLang("Readkomik", "https://readkomik.com", "en", className = "ReadKomik", overrideVersionCode = 1),
        SingleLang("Realm Scans", "https://realmscans.com", "en", overrideVersionCode = 5),
        SingleLang("Ryukonesia", "https://ryukonesia.net", "id"),
        SingleLang("Sekaikomik", "https://www.sekaikomik.pro", "id", isNsfw = true, overrideVersionCode = 10),
        SingleLang("Sekte Doujin", "https://sektedoujin.lol", "id", isNsfw = true, overrideVersionCode = 4),
        SingleLang("Senpai Ediciones", "http://senpaiediciones.com", "es"),
        SingleLang("Shadow Mangas", "https://shadowmangas.com", "es"),
        SingleLang("Shea Manga", "https://sheakomik.com", "id", overrideVersionCode = 4),
        SingleLang("Snudae Scans", "https://snudaescans.com", "en", isNsfw = true, className = "BatotoScans", overrideVersionCode = 1),
        SingleLang("Summer Fansub", "https://smmr.in", "pt-BR", isNsfw = true),
        SingleLang("Surya Scans", "https://suryascans.com", "en"),
        SingleLang("Sushi-Scan", "https://sushiscan.ru", "fr", className = "SushiScan", overrideVersionCode = 1),
        SingleLang("Tarot Scans", "https://www.tarotscans.com", "tr"),
        SingleLang("The Apollo Team", "https://theapollo.team", "en"),
        SingleLang("Tsundoku Traduções", "https://tsundoku.com.br", "pt-BR", className = "TsundokuTraducoes", overrideVersionCode = 9),
        SingleLang("TukangKomik", "https://tukangkomik.id", "id", overrideVersionCode = 1),
        SingleLang("TurkToon", "https://turktoon.com", "tr"),
        SingleLang("Uzay Manga", "https://uzaymanga.com", "tr", overrideVersionCode = 4),
        SingleLang("Walpurgi Scan", "https://www.walpurgiscan.com", "it", overrideVersionCode = 6, className = "WalpurgisScan", pkgName = "walpurgisscan"),
        SingleLang("West Manga", "https://westmanga.info", "id", overrideVersionCode = 1),
        SingleLang("World Romance Translation", "https://wrt.my.id", "id", overrideVersionCode = 10),
        SingleLang("xCaliBR Scans", "https://xcalibrscans.com", "en", overrideVersionCode = 4),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MangaThemesiaGenerator().createAll()
        }
    }
}
