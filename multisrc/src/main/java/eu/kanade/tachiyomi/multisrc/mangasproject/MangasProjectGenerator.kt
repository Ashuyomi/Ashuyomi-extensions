package eu.kanade.tachiyomi.multisrc.mangasproject

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MangasProjectGenerator : ThemeSourceGenerator {

    override val themePkg = "mangasproject"

    override val themeClass = "MangasProject"

    override val baseVersionCode: Int = 10

    override val sources = listOf(
        SingleLang("Leitor.net", "https://leitor.net", "pt-BR", className = "LeitorNet", isNsfw = true, overrideVersionCode = 3),
        SingleLang("Mangá Livre", "https://mangalivre.net", "pt-BR", className = "MangaLivre", isNsfw = true, overrideVersionCode = 2),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MangasProjectGenerator().createAll()
        }
    }
}
