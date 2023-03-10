package eu.kanade.tachiyomi.extension.ru.mintmanga

import eu.kanade.tachiyomi.multisrc.grouple.GroupLe
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

class MintManga : GroupLe("MintManga", "https://mintmanga.live", "ru") {

    override val id: Long = 6

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = super.searchMangaRequest(page, query, filters).url.newBuilder()
        (if (filters.isEmpty()) getFilterList().reversed() else filters.reversed()).forEach { filter ->
            when (filter) {
                is GenreList -> filter.state.forEach { genre ->
                    if (genre.state != Filter.TriState.STATE_IGNORE) {
                        url.addQueryParameter(genre.id, arrayOf("=", "=in", "=ex")[genre.state])
                    }
                }
                is Category -> filter.state.forEach { category ->
                    if (category.state != Filter.TriState.STATE_IGNORE) {
                        url.addQueryParameter(category.id, arrayOf("=", "=in", "=ex")[category.state])
                    }
                }
                is AgeList -> filter.state.forEach { age ->
                    if (age.state != Filter.TriState.STATE_IGNORE) {
                        url.addQueryParameter(age.id, arrayOf("=", "=in", "=ex")[age.state])
                    }
                }
                is More -> filter.state.forEach { more ->
                    if (more.state != Filter.TriState.STATE_IGNORE) {
                        url.addQueryParameter(more.id, arrayOf("=", "=in", "=ex")[more.state])
                    }
                }
                is FilList -> filter.state.forEach { fils ->
                    if (fils.state != Filter.TriState.STATE_IGNORE) {
                        url.addQueryParameter(fils.id, arrayOf("=", "=in", "=ex")[fils.state])
                    }
                }
                is OrderBy -> {
                    if (url.toString().contains("&") && filter.state < 6) {
                        url.addQueryParameter("sortType", arrayOf("RATING", "POPULARITY", "YEAR", "NAME", "DATE_CREATE", "DATE_UPDATE")[filter.state])
                    } else {
                        val ord = arrayOf("rate", "popularity", "year", "name", "created", "updated", "votes")[filter.state]
                        val ordUrl = "$baseUrl/list?sortType=$ord&offset=${70 * (page - 1)}".toHttpUrlOrNull()!!.newBuilder()
                        return GET(ordUrl.toString(), headers)
                    }
                }
                else -> {}
            }
        }
        return if (url.toString().contains("&")) {
            GET(url.toString().replace("=%3D", "="), headers)
        } else {
            popularMangaRequest(page)
        }
    }

    private class OrderBy : Filter.Select<String>(
        "????????????????????",
        arrayOf("???? ????????????????????????", "?????????????????? ????????????", "???? ????????", "???? ??????????", "??????????????", "???? ???????? ????????????????????", "???? ????????????????"),
    )

    private class Genre(name: String, val id: String) : Filter.TriState(name)

    private class GenreList(genres: List<Genre>) : Filter.Group<Genre>("??????????", genres)
    private class Category(categories: List<Genre>) : Filter.Group<Genre>("??????????????????", categories)
    private class AgeList(ages: List<Genre>) : Filter.Group<Genre>("???????????????????? ????????????????????????", ages)
    private class More(moren: List<Genre>) : Filter.Group<Genre>("????????????", moren)
    private class FilList(fils: List<Genre>) : Filter.Group<Genre>("??????????????", fils)

    override fun getFilterList() = FilterList(
        OrderBy(),
        Category(getCategoryList()),
        GenreList(getGenreList()),
        AgeList(getAgeList()),
        More(getMore()),
        FilList(getFilList()),
    )
    private fun getFilList() = listOf(
        Genre("?????????????? ??????????????", "s_high_rate"),
        Genre("??????????", "s_single"),
        Genre("?????? ????????????????", "s_mature"),
        Genre("??????????????????????", "s_completed"),
        Genre("????????????????????", "s_translated"),
        Genre("???????????????? ??????????????", "s_abandoned_popular"),
        Genre("??????????????", "s_many_chapters"),
        Genre("?????????????? ????????????????", "s_wait_upload"),
        Genre("?????????? ??????????", "s_not_pessimized"),
    )
    private fun getMore() = listOf(
        Genre("??????????", "el_6641"),
        Genre("?? ??????????", "el_4614"),
        Genre("??????", "el_1355"),
        Genre("???????????? ??????????????????????????", "el_5232"),
        Genre("???? ??????", "el_1874"),
        Genre("??????????????", "el_1348"),
    )

    private fun getAgeList() = listOf(
        Genre("R(16+)", "el_3968"),
        Genre("NC-17(18+)", "el_3969"),
        Genre("R18+(18+)", "el_3990"),
    )

    private fun getCategoryList() = listOf(
        Genre("OEL-??????????", "el_6637"),
        Genre("????????????????", "el_1332"),
        Genre("??????", "el_2220"),
        Genre("????????????", "el_2741"),
        Genre("????????????", "el_1903"),
        Genre("??????????", "el_6421"),
        Genre("????????????", "el_1873"),
        Genre("??????????????", "el_1875"),
        Genre("????????????", "el_5688"),
    )

    private fun getGenreList() = listOf(
        Genre("????????????", "el_1346"),
        Genre("???????????? ??????????????????", "el_1334"),
        Genre("??????????", "el_1333"),
        Genre("?????????????????? ??????????????", "el_1347"),
        Genre("?????????????????????? ??????????????", "el_1337"),
        Genre("????????????????", "el_1343"),
        Genre("????????????", "el_1349"),
        Genre("??????????", "el_1310"),
        Genre("????????", "el_5229"),
        Genre("????????????", "el_6420"),
        Genre("??????????????", "el_1311"),
        Genre("??????????????????", "el_1351"),
        Genre("??????????????", "el_1328"),
        Genre("????????", "el_1318"),
        Genre("?????????????? ????????????????????", "el_1325"),
        Genre("??????????????????", "el_5676"),
        Genre("????????????????????????????", "el_1327"),
        Genre("????????????????????????????????", "el_1342"),
        Genre("??????????????????????", "el_1322"),
        Genre("????????????????????", "el_1335"),
        Genre("??????????????????", "el_1313"),
        Genre("?????????????????????? ????????????", "el_1316"),
        Genre("????????????????????????????????????", "el_1350"),
        Genre("??????????", "el_1314"),
        Genre("??????????-????", "el_1320"),
        Genre("??????????", "el_1326"),
        Genre("??????????-????", "el_1330"),
        Genre("??????????", "el_1321"),
        Genre("????????????", "el_1329"),
        Genre("????????????", "el_6631"),
        Genre("????????????????", "el_1344"),
        Genre("??????????????", "el_1341"),
        Genre("??????????", "el_1317"),
        Genre("??????", "el_6632"),
        Genre("??????????????", "el_1323"),
        Genre("??????????", "el_1319"),
        Genre("??????????????", "el_1340"),
        Genre("????????", "el_1354"),
        Genre("??????", "el_1315"),
        Genre("??????", "el_1336"),
    )
}
