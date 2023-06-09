package eu.kanade.tachiyomi.multisrc.heancms

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class HeanCmsGenerator : ThemeSourceGenerator {

    override val themePkg = "heancms"

    override val themeClass = "HeanCms"

    override val baseVersionCode: Int = 13

    override val sources = listOf(
        SingleLang("Omega Scans", "https://omegascans.org", "en", isNsfw = true, overrideVersionCode = 17),
        SingleLang("Reaper Scans", "https://reaperscans.net", "pt-BR", overrideVersionCode = 35),
        SingleLang("YugenMangas", "https://yugenmangas.com", "es", isNsfw = true, overrideVersionCode = 1),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HeanCmsGenerator().createAll()
        }
    }
}
