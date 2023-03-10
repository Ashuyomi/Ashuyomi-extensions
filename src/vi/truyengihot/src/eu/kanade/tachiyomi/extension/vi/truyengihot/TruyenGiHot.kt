package eu.kanade.tachiyomi.extension.vi.truyengihot

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import uy.kohesive.injekt.injectLazy
import java.util.Calendar

class TruyenGiHot : ParsedHttpSource() {

    override val name: String = "TruyenGiHot"

    override val baseUrl: String = "https://truyengihotne.net"

    override val lang: String = "vi"

    override val supportsLatest: Boolean = true

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .rateLimit(1)
        .build()

    override fun headersBuilder(): Headers.Builder =
        super.headersBuilder().add("Referer", "$baseUrl/")

    private val json: Json by injectLazy()

    companion object {
        const val PREFIX_ID_SEARCH = "id:"
    }

    override fun popularMangaRequest(page: Int): Request =
        searchMangaRequest(
            page,
            "",
            FilterList(
                SortFilter(
                    getSortItems(),
                    Filter.Sort.Selection(2, false),
                ),
            ),
        )

    override fun popularMangaSelector(): String = searchMangaSelector()

    override fun popularMangaFromElement(element: Element): SManga =
        searchMangaFromElement(element)

    override fun popularMangaNextPageSelector(): String = searchMangaNextPageSelector()

    override fun latestUpdatesRequest(page: Int): Request =
        searchMangaRequest(
            page,
            "",
            FilterList(
                SortFilter(
                    getSortItems(),
                    Filter.Sort.Selection(0, false),
                ),
            ),
        )

    override fun latestUpdatesSelector(): String = searchMangaSelector()

    override fun latestUpdatesFromElement(element: Element): SManga =
        searchMangaFromElement(element)

    override fun latestUpdatesNextPageSelector(): String = searchMangaNextPageSelector()

    override fun fetchSearchManga(
        page: Int,
        query: String,
        filters: FilterList,
    ): Observable<MangasPage> {
        return when {
            query.startsWith(PREFIX_ID_SEARCH) -> {
                var id = query.removePrefix(PREFIX_ID_SEARCH).trim()
                if (!id.endsWith(".html")) {
                    id += ".html"
                }
                if (!id.startsWith("/")) {
                    id = "/$id"
                }

                fetchMangaDetails(
                    SManga.create().apply {
                        url = id
                    },
                )
                    .map { MangasPage(listOf(it.apply { url = id }), false) }
            }
            else -> super.fetchSearchManga(page, query, filters)
        }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url =
            "$baseUrl/tim-kiem-nang-cao.html?listType=table&page=$page".toHttpUrl().newBuilder()
                .apply {
                    val genres = mutableListOf<String>()
                    val genresEx = mutableListOf<String>()

                    addQueryParameter("text_add", query)

                    (if (filters.isEmpty()) getFilterList() else filters).forEach {
                        when (it) {
                            is UriFilter -> it.addToUri(this)
                            is GenreFilter -> it.state.forEach { genre ->
                                when (genre.state) {
                                    Filter.TriState.STATE_INCLUDE -> genres.add(genre.id)
                                    Filter.TriState.STATE_EXCLUDE -> genresEx.add(genre.id)
                                    else -> {}
                                }
                            }
                            else -> {}
                        }
                    }

                    addQueryParameter("tag_add", genres.joinToString(","))
                    addQueryParameter("tag_remove", genresEx.joinToString(","))
                }.build().toString()
        return GET(url, headers)
    }

    override fun searchMangaSelector(): String = "ul.cw-list li"

    override fun searchMangaFromElement(element: Element): SManga = SManga.create().apply {
        val anchor = element.select("span.title a")
        setUrlWithoutDomain(anchor.attr("href"))
        title = anchor.text()
        thumbnail_url = baseUrl + element.select("span.thumb").attr("style")
            .substringAfter("url('")
            .substringBefore("')")
    }

    override fun searchMangaNextPageSelector(): String = "li.page-next a:not(.disabled)"

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.select(".cover-title").text()
        author = document.select("p.cover-artist:contains(T??c gi???) a").joinToString { it.text() }
        genre = document.select("a.manga-tags").joinToString { it.text().removePrefix("#") }
        thumbnail_url = document.select("div.cover-image img").attr("abs:src")

        val tags = document.select("img.top-tags.top-tags-full").map {
            it.attr("src").substringAfterLast("/").substringBefore(".png")
        }
        status = when {
            tags.contains("ongoing") -> SManga.ONGOING
            tags.contains("drop") -> SManga.CANCELLED
            tags.contains("full") -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }

        description = document.select("div.product-synopsis-content").run {
            select("p").first()?.prepend("|truyengihay-split|")
            text().substringAfter("|truyengihay-split|").substringBefore(" Xem th??m")
        }
    }

    override fun chapterListSelector(): String = "ul.episode-list li a"

    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        setUrlWithoutDomain(element.attr("href"))

        val infoBlock = element.selectFirst("span.info")!!
        name = infoBlock.select("span.no").text()
        date_upload = parseChapterDate(infoBlock.select("span.date").text())
    }

    private fun parseChapterDate(date: String): Long {
        val trimmedDate = date.substringBefore(" tr?????c").split(" ")

        val calendar = Calendar.getInstance().apply {
            val amount = -trimmedDate[0].toInt()
            val field = when (trimmedDate[1]) {
                "gi??y" -> Calendar.SECOND
                "ph??t" -> Calendar.MINUTE
                "gi???" -> Calendar.HOUR_OF_DAY
                "ng??y" -> Calendar.DAY_OF_MONTH
                "tu???n" -> Calendar.WEEK_OF_MONTH
                "th??ng" -> Calendar.MONTH
                "n??m" -> Calendar.YEAR
                else -> Calendar.SECOND
            }
            add(field, amount)
        }

        return calendar.timeInMillis
    }

    override fun pageListParse(document: Document): List<Page> {
        val tokenScript = document.selectFirst("script:containsData(_token)")?.data()
            ?: throw Exception("Kh??ng t??m ???????c token l???y ???nh c???a chapter")
        val token = tokenScript
            .substringAfter("_token = \"")
            .substringBefore("\";")

        val chapterInfoScript = document.selectFirst("script:containsData(mangaSLUG)")?.data()
            ?: throw Exception("Kh??ng t??m th???y th??ng tin c???a chapter")
        val chapterInfo = chapterInfoScript.split(";", "\n").associate {
            if (!it.contains("=")) {
                return@associate Pair("", "")
            }
            val kv = it.trim().split("=")
            val key = kv[0].removePrefix("var ").trim()
            val value = kv[1].trim().removeSurrounding("\"")
            Pair(key, value)
        }

        val formBody = FormBody.Builder()
            .add("token", token)
            .add("chapter_id", chapterInfo["cid"]!!)
            .add("m_slug", chapterInfo["mangaSLUG"]!!)
            .add("m_id", chapterInfo["mangaID"]!!)
            .add("chapter", chapterInfo["chapter"]!!)
            .add("g_id", chapterInfo["g_id"]!!)
            .build()
        val request = POST("$baseUrl/frontend_controllers/chapter/content.php", headers, formBody)
        val response = client.newCall(request).execute().body.use {
            it.string()
        }

        val pageHtml = json.parseToJsonElement(response).jsonObject["content"]!!.jsonPrimitive.content
        val pages = Jsoup.parseBodyFragment(pageHtml, baseUrl)

        if (pages.getElementById("getImage_form") != null) {
            throw Exception("Truy???n ???? b??? kho??!")
        }

        return Jsoup.parseBodyFragment(pageHtml, baseUrl).select("img").mapIndexed { idx, it ->
            Page(idx, imageUrl = it.attr("abs:src"))
        }
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")

    override fun getFilterList(): FilterList = FilterList(
        SearchTypeFilter(),
        CategoryFilter(),
        PublicationTypeFilter(),
        CountryFilter(),
        StatusFilter(),
        ScanlatorFilter(),
        SortFilter(getSortItems()),
        GenreFilter(),
    )

    interface UriFilter {
        fun addToUri(builder: HttpUrl.Builder)
    }

    open class UriPartFilter(
        name: String,
        private val query: String,
        private val vals: Array<Pair<String, String>>,
        state: Int = 0,
    ) : UriFilter, Filter.Select<String>(name, vals.map { it.first }.toTypedArray(), state) {
        override fun addToUri(builder: HttpUrl.Builder) {
            builder.addQueryParameter(query, vals[state].second)
        }
    }

    private class SearchTypeFilter : UriPartFilter(
        "T??m t??? kho?? theo",
        "text_type",
        arrayOf(
            Pair("T??n truy???n", "name"),
            Pair("T??c gi???", "authors"),
        ),
    )

    private class CategoryFilter : UriPartFilter(
        "Ph??n lo???i",
        "type_add",
        arrayOf(
            Pair("T???t c???", ""),
            Pair("Truy???n 18+", "truyen-tranh"),
            Pair("Ng??n t??nh", "ngon-tinh"),
        ),
    )

    private class PublicationTypeFilter : UriPartFilter(
        "Th??? lo???i",
        "genre_add",
        arrayOf(
            Pair("T???t c???", ""),
            Pair("Manga", "manga"),
            Pair("Manhua", "manhua"),
            Pair("Manhwa", "manhwa"),
            Pair("T??? s??ng t??c", "tu-sang-tac"),
            Pair("Kh??c", "khac"),
        ),
    )

    private class CountryFilter : UriPartFilter(
        "Qu???c gia",
        "country_add",
        arrayOf(
            Pair("T???t c???", ""),
            Pair("??u M???", "au-my"),
            Pair("H??n Qu???c", "han-quoc"),
            Pair("Kh??c", "khac"),
            Pair("Nh???t B???n", "nhat-ban"),
            Pair("Trung Qu???c", "trung-quoc"),
            Pair("Vi???t Nam", "viet-nam"),
        ),
    )

    private class StatusFilter : UriPartFilter(
        "Tr???ng th??i",
        "status_add",
        arrayOf(
            Pair("T???t c???", "0"),
            Pair("Full", "1"),
            Pair("Ongoing", "2"),
            Pair("Drop", "3"),
        ),
    )

    private class SortFilter(
        private val vals: Array<Pair<String, String>>,
        state: Selection = Selection(2, false),
    ) : UriFilter,
        Filter.Sort("S???p x???p", vals.map { it.first }.toTypedArray(), state) {
        override fun addToUri(builder: HttpUrl.Builder) {
            builder.addQueryParameter("order_add", vals[state?.index ?: 2].second)
            builder.addQueryParameter(
                "order_by_add",
                if (state?.ascending == true) "ASC" else "DESC",
            )
        }
    }

    private fun getSortItems(): Array<Pair<String, String>> = arrayOf(
        Pair("M???i c???p nh???t", "last_update"),
        Pair("L?????t xem", "views"),
        Pair("Hot", "total_vote"),
        Pair("Vote", "count_vote"),
        Pair("T??n A-Z", "name"),
    )

    private class Genre(name: String, val id: String) : Filter.TriState(name)

    // console.log([...document.querySelectorAll(".wrapper-search-tag .search-content span")].map(e => `Genre("${e.innerText.trim()}", "${e.dataset.val}")`).join(",\n"))
    private class GenreFilter : Filter.Group<Genre>(
        "Ch??? ?????",
        listOf(
            Genre("16+", "16"),
            Genre("18+", "18"),
            Genre("1Vs1", "1vs1"),
            Genre("3d", "3d"),
            Genre("3some", "3some"),
            Genre("??c n???", "ac-nu"),
            Genre("??c Qu???", "ac-quy"),
            Genre("Action", "action"),
            Genre("Adult", "adult"),
            Genre("Adventure", "adventure"),
            Genre("ai c???p", "ai-cap"),
            Genre("??m Nh???c", "am-nhac"),
            Genre("Anh ch??? em", "anh-chi-em"),
            Genre("anh ch??? em k???", "anh-chi-em-ke"),
            Genre("anh h??ng", "anh-hung"),
            Genre("Anime", "anime"),
            Genre("artist cg", "artist-cg"),
            Genre("??u C???", "au-co"),
            Genre("B??ch H???p", "bach-hop"),
            Genre("bad boy", "bad-boy"),
            Genre("b???n th??n", "ban-than"),
            Genre("B???o L???c", "bao-luc"),
            Genre("Bdsm", "bdsm"),
            Genre("BE", "be"),
            Genre("B?? ???n", "bi-an"),
            Genre("Bi k???ch", "bi-kich"),
            Genre("b??? v???t b???", "bi-vut-bo"),
            Genre("big breast", "big-breast"),
            Genre("BL/B??ch h???p", "bl-bach-hop"),
            Genre("blowjobs", "blowjobs"),
            Genre("b??? tr???n", "bo-tron"),
            Genre("c??i ch???t", "cai-chet"),
            Genre("C???n ?????i", "can-dai"),
            Genre("C???u Huy???t", "cau-huyet"),
            Genre("Ch??u ??u", "chau-au"),
            Genre("che", "che"),
            Genre("Chi???n Tranh", "chien-tranh"),
            Genre("Chuy???n Sinh", "chuyen-sinh"),
            Genre("Chuy???n Th???", "chuyen-the"),
            Genre("C??? ?????i", "co-dai"),
            Genre("C??? Trang", "co-trang"),
            Genre("con g??i n??", "con-gai-no"),
            Genre("con ngo??i d?? th??", "con-ngoai-da-thu"),
            Genre("c??ng s???", "cong-so"),
            Genre("Cung ?????u", "cung-dau"),
            Genre("?????p trai Nam ch??nh", "dep-trai-nam-chinh"),
            Genre("D??? Gi???i", "di-gioi"),
            Genre("D??? N??ng", "di-nang"),
            Genre("??i???n V??n", "dien-van"),
            Genre("dl site", "dl-site"),
            Genre("???? Th???", "do-thi"),
            Genre("??o???n V??n", "doan-van"),
            Genre("?????c ??c N??? ch??nh", "doc-ac-nu-chinh"),
            Genre("Drama", "drama"),
            Genre("???????c nh???n nu??i", "duoc-nhan-nuoi"),
            Genre("Ecchi", "ecchi"),
            Genre("Fantasy", "fantasy"),
            Genre("Game", "game"),
            Genre("G??y c???n", "gay-can"),
            Genre("Gia ????nh", "gia-dinh"),
            Genre("gi??? g??i/trai", "gia-gai-trai"),
            Genre("Giai c???p qu?? t???c", "giai-cap-quy-toc"),
            Genre("giam c???m", "giam-cam"),
            Genre("giang h???", "giang-ho"),
            Genre("H??i H?????c", "hai-huoc"),
            Genre("h??ng kh???ng", "hang-khung"),
            Genre("h??ng x??m", "hang-xom"),
            Genre("H??nh ?????ng", "hanh-dong"),
            Genre("Harem", "harem"),
            Genre("HE", "he"),
            Genre("H??? Th???ng", "he-thong"),
            Genre("Hentai", "hentai"),
            Genre("Hi???n ?????i", "hien-dai"),
            Genre("Hi???u l???m", "hieu-lam"),
            Genre("Ho??n ?????i", "hoan-doi"),
            Genre("Ho??ng gia", "hoang-gia"),
            Genre("Ho???t H??nh", "hoat-hinh"),
            Genre("H???c ???????ng", "hoc-duong"),
            Genre("h???c sinh", "hoc-sinh"),
            Genre("h???i h???n", "hoi-han"),
            Genre("H???i h???p", "hoi-hop"),
            Genre("Huy???n ???o", "huyen-ao"),
            Genre("??t che", "it-che"),
            Genre("kaka*page", "kaka-page"),
            Genre("kh??? d??m", "kho-dam"),
            Genre("Khoa H???c", "khoa-hoc"),
            Genre("kh??ng che", "khong-che"),
            Genre("Kh??ng M??u", "khong-mau"),
            Genre("Ki???m Hi???p", "kiem-hiep"),
            Genre("Kinh D???", "kinh-di"),
            Genre("L??ng m???n", "lang-man"),
            Genre("lezh*n", "lezh-n"),
            Genre("L???ch S???", "lich-su"),
            Genre("Light Novel", "light-novel"),
            Genre("Live action", "live-action"),
            Genre("lo???n lu??n", "loan-luan"),
            Genre("Loli", "loli"),
            Genre("ma", "ma"),
            Genre("Ma C?? R???ng", "ma-ca-rong"),
            Genre("mang thai", "mang-thai"),
            Genre("Manga", "manga"),
            Genre("Manhua", "manhua"),
            Genre("Manhwa", "manhwa"),
            Genre("M???t Th???", "mat-the"),
            Genre("m??? k???", "me-ke"),
            Genre("M?? t??? ????? ch???", "mo-ta-de-che"),
            Genre("mystery", "mystery"),
            Genre("nam duy nh???t", "nam-duy-nhat"),
            Genre("nav*r", "nav-r"),
            Genre("n??t v??? ?????p", "net-ve-dep"),
            Genre("Netflix", "netflix"),
            Genre("Ng??y th??", "ngay-tho"),
            Genre("ngo???i t??nh", "ngoai-tinh"),
            Genre("Ng??n T??nh", "ngon-tinh"),
            Genre("Ng?????c", "nguoc"),
            Genre("ng?????i h???u", "nguoi-hau"),
            Genre("nh??n th??", "nhan-thu"),
            Genre("Nh??n v???t ch??nh", "nhan-vat-chinh"),
            Genre("nh??n v???t game", "nhan-vat-game"),
            Genre("Ninja", "ninja"),
            Genre("n?? l???", "no-le"),
            Genre("ntr", "ntr"),
            Genre("N??? C?????ng", "nu-cuong"),
            Genre("n??? duy nh???t", "nu-duy-nhat"),
            Genre("N??? Ph???", "nu-phu"),
            Genre("Oan gia", "oan-gia"),
            Genre("OE", "oe"),
            Genre("old man", "old-man"),
            Genre("oneshot", "oneshot"),
            Genre("otome game", "otome-game"),
            Genre("otp", "otp"),
            Genre("ph???n di???n", "phan-dien"),
            Genre("Ph??p Thu???t", "phep-thuat"),
            Genre("Phi??u L??u", "phieu-luu"),
            Genre("Phim B???", "phim-bo"),
            Genre("Phim Chi???u R???p", "phim-chieu-rap"),
            Genre("Phim L???", "phim-le"),
            Genre("prologue", "prologue"),
            Genre("psychological", "psychological"),
            Genre("qu??i v???t", "quai-vat"),
            Genre("Qu??n S???", "quan-su"),
            Genre("Qu?? t???c", "quy-toc"),
            Genre("rape", "rape"),
            Genre("S???c", "sac"),
            Genre("S???ch", "sach"),
            Genre("SE", "se"),
            Genre("seinen", "seinen"),
            Genre("sex toy", "sex-toy"),
            Genre("shoujo", "shoujo"),
            Genre("Shoujo Ai", "shoujo-ai"),
            Genre("Si??u N??ng L???c", "sieu-nang-luc"),
            Genre("slice of life", "slice-of-life"),
            Genre("Smut", "smut"),
            Genre("S??? th??ch tra t???n", "so-thich-tra-tan"),
            Genre("S???ng", "sung"),
            Genre("supernatural", "supernatural"),
            Genre("t??i sinh", "tai-sinh"),
            Genre("T??m L??", "tam-ly"),
            Genre("th???m du", "tham-du"),
            Genre("Th??m Hi???m", "tham-hiem"),
            Genre("Th???n Tho???i", "than-thoai"),
            Genre("th??nh n???", "thanh-nu"),
            Genre("thanh xu??n v?????n tr?????ng", "thanh-xuan-vuon-truong"),
            Genre("th???y/c?? gi??o", "thay-co-giao"),
            Genre("thay ?????i c???t truy???n", "thay-doi-cot-truyen"),
            Genre("thay ?????i gi???i t??nh", "thay-doi-gioi-tinh"),
            Genre("Th??? Thao", "the-thao"),
            Genre("thu???n h??a", "thuan-hoa"),
            Genre("Ti??n Hi???p", "tien-hiep"),
            Genre("Ti???u Thuy???t", "tieu-thuyet"),
            Genre("T??nh C???m", "tinh-cam"),
            Genre("T??nh Tay Ba", "tinh-tay-ba"),
            Genre("T???ng T??i", "tong-tai"),
            Genre("tr?? xanh", "tra-xanh"),
            Genre("Trailer", "trailer"),
            Genre("Trinh Th??m", "trinh-tham"),
            Genre("Tr???ng Sinh", "trong-sinh"),
            Genre("Truy???n M??u", "truyen-mau"),
            Genre("tsundere", "tsundere"),
            Genre("T??? S??ng T??c", "tu-sang-tac"),
            Genre("t?????ng t?????ng", "tuong-tuong"),
            Genre("tuy???n t???p", "tuyen-tap"),
            Genre("v??? h??n th??", "vi-hon-the"),
            Genre("Vi???t Nam", "viet-nam"),
            Genre("V?? Thu???t", "vo-thuat"),
            Genre("V?? Tr???", "vu-tru"),
            Genre("Webtoon", "webtoon"),
            Genre("x??c tua", "xuc-tua"),
            Genre("Xuy??n Kh??ng", "xuyen-khong"),
            Genre("Xuy??n kh??ng/Tr???ng sinh", "xuyen-khong-trong-sinh"),
            Genre("Yandere", "yandere"),
            Genre("Yuri", "yuri"),
        ),
    )

    // console.log([...document.querySelectorAll(".wrapper-search-group .search-content span")].map(e => `Pair("${e.innerText.trim()}", "${e.dataset.val}")`).join(",\n"))
    private class ScanlatorFilter : UriPartFilter(
        "Nh??m d???ch",
        "group_add",
        arrayOf(
            Pair("T???t c???", "0"),
            Pair("Aling - Ti???u Thuy???t", "383"),
            Pair("Angela Di???p L???c", "361"),
            Pair("AUTHOR TI???U M??Y", "362"),
            Pair("Boom novel", "403"),
            Pair("C?? chua Team", "421"),
            Pair("Camellia", "300"),
            Pair("C???u Mu???n Review G?? N??o?", "342"),
            Pair("Chloe's Library", "392"),
            Pair("Delion", "376"),
            Pair("Ecchi Land", "26"),
            Pair("Fluer", "396"),
            Pair("Gangster", "327"),
            Pair("Hien serena", "330"),
            Pair("Ho??? Y", "417"),
            Pair("Khu V?????n B?? M???t C???a Rosaria", "401"),
            Pair("Laziel", "377"),
            Pair("Lazy Bee", "420"),
            Pair("Lil Pan", "334"),
            Pair("Lindy", "399"),
            Pair("L??? Lem Hangul", "6"),
            Pair("Lycoris Radiata - Ti???u Hoa", "407"),
            Pair("MARY C??M TR??", "423"),
            Pair("Mary H??? L???c", "38"),
            Pair("M??y", "349"),
            Pair("M???y Dus GL", "425"),
            Pair("M???y L??nh M???nh", "424"),
            Pair("M??y M??y", "409"),
            Pair("meoluoihamchoi", "385"),
            Pair("Mi??u T???c", "343"),
            Pair("M???c Tr??", "306"),
            Pair("M???t Chi???c M??o M??u ??en", "390"),
            Pair("Nam T??? Sa Page", "20"),
            Pair("N?? V???", "393"),
            Pair("N???I C??M TR??", "382"),
            Pair("N???I C??M TR?? 18+", "426"),
            Pair("??? C???a Sien", "321"),
            Pair("Reviewer", "369"),
            Pair("Reviews", "419"),
            Pair("RINNIE", "341"),
            Pair("Rose The One", "337"),
            Pair("Roselight Team", "402"),
            Pair("Song T???", "305"),
            Pair("The Present Translator", "404"),
            Pair("Thi??n M???c Th???t T??", "304"),
            Pair("Th?? Vi???n Latsya", "370"),
            Pair("Ti???m K???o D???o Ng??n Ngon", "418"),
            Pair("Ti???u Mi??u Ng???c", "395"),
            Pair("Ti???u Thuy???t Nh?? M??y", "347"),
            Pair("Ti???u V??", "360"),
            Pair("TI???U VY", "388"),
            Pair("tieu.yet", "355"),
            Pair("Tr?? V?? B??nh", "40"),
            Pair("Traham", "319"),
            Pair("Truy???n d???ch Team Behira", "410"),
            Pair("Truy???n T???ng H???p", "23"),
            Pair("Windyzzz", "379"),
            Pair("X??m B??n Hoa", "364"),
            Pair("Yu", "406"),
            Pair("????o L?? T???u", "345"),
            Pair("?????o San H??", "397"),
            Pair("??i???n Th???t", "373"),
        ),
    )
}
