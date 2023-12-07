package eu.kanade.tachiyomi.multisrc.comicake

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class ComiCakeGenerator : ThemeSourceGenerator {

    override val themePkg = "comicake"

    override val themeClass = "ComiCake"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        SingleLang("WhimSubs", "https://whimsubs.xyz", "en"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ComiCakeGenerator().createAll()
        }
    }
}
