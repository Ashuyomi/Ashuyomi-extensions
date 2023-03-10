package eu.kanade.tachiyomi.extension.all.mangapark

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class MangaParkFactory : SourceFactory {
    override fun createSources(): List<Source> = languages.map { MangaPark(it.lang, it.siteLang) }
}

class LanguageOption(val lang: String, val siteLang: String = lang)
private val languages = listOf(
    // LanguageOption("<Language Format>","<Language Format used in site.>"),
    LanguageOption("af"),
    LanguageOption("sq"),
    LanguageOption("am"),
    LanguageOption("ar"),
    LanguageOption("hy"),
    LanguageOption("az"),
    LanguageOption("be"),
    LanguageOption("bn"),
    LanguageOption("bs"),
    LanguageOption("bg"),
    LanguageOption("my"),
    LanguageOption("km"),
    LanguageOption("ca"),
    LanguageOption("ceb"),
    LanguageOption("zh"),
    LanguageOption("zh-Hans", "zh_hk"),
    LanguageOption("zh-Hant", "zh_tw"),
    LanguageOption("hr"),
    LanguageOption("cs"),
    LanguageOption("da"),
    LanguageOption("nl"),
    LanguageOption("en"),
    LanguageOption("eo"),
    LanguageOption("et"),
    LanguageOption("fo"),
    LanguageOption("fil"),
    LanguageOption("fi"),
    LanguageOption("fr"),
    LanguageOption("ka"),
    LanguageOption("de"),
    LanguageOption("el"),
    LanguageOption("gn"),
    LanguageOption("ht"),
    LanguageOption("ha"),
    LanguageOption("he"),
    LanguageOption("hi"),
    LanguageOption("hu"),
    LanguageOption("is"),
    LanguageOption("ig"),
    LanguageOption("id"),
    LanguageOption("ga"),
    LanguageOption("it"),
    LanguageOption("ja"),
    LanguageOption("jv"),
    LanguageOption("kk"),
    LanguageOption("ko"),
    LanguageOption("ku"),
    LanguageOption("ky"),
    LanguageOption("lo"),
    LanguageOption("lv"),
    LanguageOption("lt"),
    LanguageOption("lb"),
    LanguageOption("mk"),
    LanguageOption("mg"),
    LanguageOption("ms"),
    LanguageOption("ml"),
    LanguageOption("mt"),
    LanguageOption("mi"),
    LanguageOption("mo"),
    LanguageOption("mn"),
    LanguageOption("ne"),
    LanguageOption("no"),
    LanguageOption("ny"),
    LanguageOption("ps"),
    LanguageOption("fa"),
    LanguageOption("pl"),
    LanguageOption("pt"),
    LanguageOption("pt-BR", "pt_br"),
    LanguageOption("ro"),
    LanguageOption("rm"),
    LanguageOption("ru"),
    LanguageOption("sm"),
    LanguageOption("sr"),
    LanguageOption("sh"),
    LanguageOption("st"),
    LanguageOption("sn"),
    LanguageOption("sd"),
    LanguageOption("si"),
    LanguageOption("sk"),
    LanguageOption("sl"),
    LanguageOption("so"),
    LanguageOption("es"),
    LanguageOption("es-419", "es_419"),
    LanguageOption("sw"),
    LanguageOption("sv"),
    LanguageOption("tg"),
    LanguageOption("ta"),
    LanguageOption("th"),
    LanguageOption("ti"),
    LanguageOption("to"),
    LanguageOption("tr"),
    LanguageOption("tk"),
    LanguageOption("uk"),
    LanguageOption("ur"),
    LanguageOption("uz"),
    LanguageOption("vi"),
    LanguageOption("yo"),
    LanguageOption("zu"),
    LanguageOption("other", "_t"),
)
