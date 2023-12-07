package eu.kanade.tachiyomi.multisrc.heancms

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class HeanCmsGenerator : ThemeSourceGenerator {

    override val themePkg = "heancms"

    override val themeClass = "HeanCms"

<<<<<<< HEAD
    override val baseVersionCode: Int = 13

    override val sources = listOf(
        SingleLang("Glorious Scan", "https://gloriousscan.com", "pt-BR", overrideVersionCode = 17),
        SingleLang("Omega Scans", "https://omegascans.org", "en", isNsfw = true, overrideVersionCode = 17),
        SingleLang("Reaper Scans", "https://reaperscans.net", "pt-BR", overrideVersionCode = 35),
        SingleLang("YugenMangas", "https://yugenmangas.net", "es", isNsfw = true, overrideVersionCode = 4),
=======
    override val baseVersionCode: Int = 20

    override val sources = listOf(
        SingleLang("Omega Scans", "https://omegascans.org", "en", isNsfw = true, overrideVersionCode = 18),
        SingleLang("Perf Scan", "https://perf-scan.fr", "fr"),
        SingleLang("Reaper Scans", "https://reaperscans.net", "pt-BR", overrideVersionCode = 36),
        SingleLang("Temple Scan", "https://templescan.net", "en", isNsfw = true, overrideVersionCode = 16),
        SingleLang("YugenMangas", "https://yugenmangas.net", "es", isNsfw = true, overrideVersionCode = 9),
>>>>>>> upstream/master
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HeanCmsGenerator().createAll()
        }
    }
}
