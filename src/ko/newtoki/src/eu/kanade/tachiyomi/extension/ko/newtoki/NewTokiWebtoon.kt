package eu.kanade.tachiyomi.extension.ko.newtoki

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

object NewTokiWebtoon : NewToki("NewToki", "webtoon", newTokiPreferences) {
    // / ! DO NOT CHANGE THIS !  Prevent to treating as a new site
    override val id = NEWTOKI_ID

    override val baseUrl get() = "https://$NEWTOKI_PREFIX$domainNumber.com"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = ("$baseUrl/webtoon" + (if (page > 1) "/p$page" else "")).toHttpUrl().newBuilder()
        filters.forEach { filter ->
            when (filter) {
                is SearchTargetTypeList -> {
                    if (filter.state > 0) {
                        url.addQueryParameter("toon", filter.values[filter.state])
                    }
                }

                is SearchSortTypeList -> {
                    val state = filter.state ?: return@forEach
                    url.addQueryParameter("sst", arrayOf("as_update", "wr_hit", "wr_good")[state.index])
                    url.addQueryParameter("sod", if (state.ascending) "asc" else "desc")
                }

                else -> {}
            }
        }

        // Incompatible with Other Search Parameter
        if (!query.isBlank()) {
            url.addQueryParameter("stx", query)
        } else {
            filters.forEach { filter ->
                when (filter) {
                    is SearchYoilTypeList -> {
                        if (filter.state > 0) {
                            url.addQueryParameter("yoil", filter.values[filter.state])
                        }
                    }

                    is SearchJaumTypeList -> {
                        if (filter.state > 0) {
                            url.addQueryParameter("jaum", filter.values[filter.state])
                        }
                    }

                    is SearchGenreTypeList -> {
                        if (filter.state > 0) {
                            url.addQueryParameter("tag", filter.values[filter.state])
                        }
                    }

                    else -> {}
                }
            }
        }

        return GET(url.toString(), headers)
    }

    private class SearchTargetTypeList : Filter.Select<String>("Type", arrayOf("??????", "????????????", "????????????", "BL/GL", "????????????"))

    // [...document.querySelectorAll("form.form td")[1].querySelectorAll("a")].map((el, i) => `"${el.innerText.trim()}"`).join(',\n')
    private class SearchYoilTypeList : Filter.Select<String>(
        "Day of the Week",
        arrayOf(
            "??????",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "??????",
        ),
    )

    // [...document.querySelectorAll("form.form td")[2].querySelectorAll("a")].map((el, i) => `"${el.innerText.trim()}"`).join(',\n')
    private class SearchJaumTypeList : Filter.Select<String>(
        "Jaum",
        arrayOf(
            "??????",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "???",
            "a-z",
            "0-9",
        ),
    )

    // [...document.querySelectorAll("form.form td")[3].querySelectorAll("a")].map((el, i) => `"${el.innerText.trim()}"`).join(',\n')
    private class SearchGenreTypeList : Filter.Select<String>(
        "Genre",
        arrayOf(
            "??????",
            "?????????",
            "??????",
            "??????",
            "????????????",
            "?????????",
            "?????????",
            "??????",
            "?????????",
            "??????",
            "??????",
            "??????",
        ),
    )

    private class SearchSortTypeList : Filter.Sort(
        "Sort",
        arrayOf(
            "??????(???????????????)",
            "?????????",
            "?????????",
        ),
    )

    override fun getFilterList() = FilterList(
        SearchTargetTypeList(),
        SearchSortTypeList(),
        Filter.Separator(),
        Filter.Header(ignoredForTextSearch()),
        SearchYoilTypeList(),
        SearchJaumTypeList(),
        SearchGenreTypeList(),
    )
}
