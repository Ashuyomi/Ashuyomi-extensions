package eu.kanade.tachiyomi.multisrc.kemono

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class KemonoGenerator : ThemeSourceGenerator {

    override val themeClass = "Kemono"

    override val themePkg = "kemono"

<<<<<<< HEAD
    override val baseVersionCode = 5
=======
    override val baseVersionCode = 8
>>>>>>> upstream/master

    override val sources = listOf(
        SingleLang("Kemono", "https://kemono.party", "all", isNsfw = true),
        SingleLang("Coomer", "https://coomer.party", "all", isNsfw = true),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            KemonoGenerator().createAll()
        }
    }
}
