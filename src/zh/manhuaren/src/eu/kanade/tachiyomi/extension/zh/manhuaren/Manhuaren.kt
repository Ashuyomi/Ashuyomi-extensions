package eu.kanade.tachiyomi.extension.zh.manhuaren

import android.text.format.DateFormat
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit.MINUTES

class Manhuaren : HttpSource() {
    override val lang = "zh"
    override val supportsLatest = true
    override val name = "漫画人"
    override val baseUrl = "http://mangaapi.manhuaren.com"

    private val pageSize = 20
    private val baseHttpUrl = baseUrl.toHttpUrlOrNull()!!

    private val c = "4e0a48e1c0b54041bce9c8f0e036124d"
    private val cacheControl: CacheControl by lazy { CacheControl.Builder().maxAge(10, MINUTES).build() }
    private val userId = (100000000..4294967295).random().toString()

    private fun generateGSNHash(url: HttpUrl): String {
        var s = c + "GET"
        url.queryParameterNames.toSortedSet().forEach {
            if (it != "gsn") {
                s += it
                s += urlEncode(url.queryParameterValues(it)[0])
            }
        }
        s += c
        return hashString("MD5", s)
    }

    private fun myGet(url: HttpUrl): Request {
        val now = DateFormat.format("yyyy-MM-dd+HH:mm:ss", Date()).toString()
        val realUrl = url.newBuilder()
            .setQueryParameter("gsm", "md5")
            .setQueryParameter("gft", "json")
            .setQueryParameter("gak", "android_manhuaren2")
            .setQueryParameter("gat", "")
            .setQueryParameter("gui", userId)
            .setQueryParameter("gts", now) // timestamp yyyy-MM-dd+HH:mm:ss
            .setQueryParameter("gut", "0") // user type
            .setQueryParameter("gem", "1")
            .setQueryParameter("gaui", "1")
            .setQueryParameter("gln", "") // location
            .setQueryParameter("gcy", "US") // country
            .setQueryParameter("gle", "zh") // language
            .setQueryParameter("gcl", "dm5") // umeng channel
            .setQueryParameter("gos", "1") // OS (int)
            .setQueryParameter("gov", "22_5.1.1") // "{Build.VERSION.SDK_INT}_{Build.VERSION.RELEASE}"
            .setQueryParameter("gav", "7.0.1") // app version
            .setQueryParameter("gdi", "358240051111110") // device info
            .setQueryParameter("gfcl", "dm5") // umeng channel config
            .setQueryParameter("gfut", "1688140800000") // first used time
            .setQueryParameter("glut", "1688140800000") // last used time
            .setQueryParameter("gpt", "com.mhr.mangamini") // package name
            .setQueryParameter("gciso", "us") // https://developer.android.com/reference/android/telephony/TelephonyManager#getSimCountryIso()
            .setQueryParameter("glot", "") // longitude
            .setQueryParameter("glat", "") // latitude
            .setQueryParameter("gflot", "") // first location longitude
            .setQueryParameter("gflat", "") // first location latitude
            .setQueryParameter("glbsaut", "0") // is allow location (0 or 1)
            .setQueryParameter("gac", "") // area code
            .setQueryParameter("gcut", "GMT+8") // time zone
            .setQueryParameter("gfcc", "") // first country code
            .setQueryParameter("gflg", "") // first language
            .setQueryParameter("glcn", "") // country name
            .setQueryParameter("glcc", "") // country code
            .setQueryParameter("gflcc", "") // first location country code

        return Request.Builder()
            .url(realUrl.setQueryParameter("gsn", generateGSNHash(realUrl.build())).build())
            .headers(headers)
            .cacheControl(cacheControl)
            .build()
    }

    override fun headersBuilder(): Headers.Builder {
        val yqciMap = HashMap<String, Any?>().apply {
            put("at", -1)
            put("av", "7.0.1") // app version
            put("ciso", "us") // https://developer.android.com/reference/android/telephony/TelephonyManager#getSimCountryIso()
            put("cl", "dm5") // umeng channel
            put("cy", "US") // country
            put("di", "358240051111110") // device info
            put("dm", "Android SDK built for x86") // Build.MODEL
            put("fcl", "dm5") // umeng channel config
            put("ft", "mhr") // from type
            put("fut", "1688140800000") // first used time
            put("installation", "dm5")
            put("le", "zh") // language
            put("ln", "") // location
            put("lut", "1688140800000") // last used time
            put("nt", 4)
            put("os", 1) // OS (int)
            put("ov", "22_5.1.1") // "{Build.VERSION.SDK_INT}_{Build.VERSION.RELEASE}"
            put("pt", "com.mhr.mangamini") // package name
            put("rn", "1440x2952") // screen "{width}x{height}"
            put("st", 0)
        }
        val yqppMap = HashMap<String, Any?>().apply {
            put("ciso", "us") // https://developer.android.com/reference/android/telephony/TelephonyManager#getSimCountryIso()
            put("laut", "0") // is allow location (0 or 1)
            put("lot", "") // longitude
            put("lat", "") // latitude
            put("cut", "GMT+8") // time zone
            put("fcc", "") // first country code
            put("flg", "") // first language
            put("lcc", "") // country code
            put("lcn", "") // country name
            put("flcc", "") // first location country code
            put("flot", "") // first location longitude
            put("flat", "") // first location latitude
            put("ac", "") // area code
        }

        return Headers.Builder().apply {
            add("X-Yq-Yqci", JSONObject(yqciMap).toString())
            add("X-Yq-Key", userId)
            add("yq_is_anonymous", "1")
            add("x-request-id", UUID.randomUUID().toString())
            add("X-Yq-Yqpp", JSONObject(yqppMap).toString())
            add("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; Android SDK built for x86 Build/LMY48X) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36")
        }
    }

    private fun hashString(type: String, input: String): String {
        val hexChars = "0123456789abcdef"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }

        return result.toString()
    }

    private fun urlEncode(str: String?): String {
        return URLEncoder.encode(str, "UTF-8")
            .replace("+", "%20")
            .replace("%7E", "~")
            .replace("*", "%2A")
    }

    private fun mangasFromJSONArray(arr: JSONArray): MangasPage {
        val ret = ArrayList<SManga>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.getInt("mangaId")
            ret.add(
                SManga.create().apply {
                    title = obj.getString("mangaName")
                    thumbnail_url = obj.getString("mangaCoverimageUrl")
                    author = obj.optString("mangaAuthor")
                    status = when (obj.getInt("mangaIsOver")) {
                        1 -> SManga.COMPLETED
                        0 -> SManga.ONGOING
                        else -> SManga.UNKNOWN
                    }
                    url = "/v1/manga/getDetail?mangaId=$id"
                },
            )
        }
        return MangasPage(ret, arr.length() != 0)
    }

    private fun mangasPageParse(response: Response): MangasPage {
        val res = response.body.string()
        val arr = JSONObject(res).getJSONObject("response").getJSONArray("mangas")
        return mangasFromJSONArray(arr)
    }

    override fun popularMangaRequest(page: Int): Request {
        val url = baseHttpUrl.newBuilder()
            .addQueryParameter("subCategoryType", "0")
            .addQueryParameter("subCategoryId", "0")
            .addQueryParameter("start", (pageSize * (page - 1)).toString())
            .addQueryParameter("limit", pageSize.toString())
            .addQueryParameter("sort", "0")
            .addPathSegments("/v2/manga/getCategoryMangas")
            .build()
        return myGet(url)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val url = baseHttpUrl.newBuilder()
            .addQueryParameter("subCategoryType", "0")
            .addQueryParameter("subCategoryId", "0")
            .addQueryParameter("start", (pageSize * (page - 1)).toString())
            .addQueryParameter("limit", pageSize.toString())
            .addQueryParameter("sort", "1")
            .addPathSegments("/v2/manga/getCategoryMangas")
            .build()
        return myGet(url)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        return mangasPageParse(response)
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        return mangasPageParse(response)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        var url = baseHttpUrl.newBuilder()
            .addQueryParameter("start", (pageSize * (page - 1)).toString())
            .addQueryParameter("limit", pageSize.toString())
        if (query != "") {
            url = url.addQueryParameter("keywords", query)
                .addPathSegments("/v1/search/getSearchManga")
            return myGet(url.build())
        }
        filters.forEach { filter ->
            when (filter) {
                is SortFilter -> url = url.setQueryParameter("sort", filter.getId())
                is CategoryFilter -> {
                    url = url.setQueryParameter("subCategoryId", filter.getId())
                        .setQueryParameter("subCategoryType", filter.getType())
                }
                else -> {}
            }
        }
        url = url.addPathSegments("/v2/manga/getCategoryMangas")
        return myGet(url.build())
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val res = response.body.string()
        val obj = JSONObject(res).getJSONObject("response")
        if (obj.has("result")) {
            return mangasFromJSONArray(obj.getJSONArray("result"))
        }
        return mangasFromJSONArray(obj.getJSONArray("mangas"))
    }

    override fun mangaDetailsParse(response: Response) = SManga.create().apply {
        val res = response.body.string()
        val obj = JSONObject(res).getJSONObject("response")
        title = obj.getString("mangaName")
        thumbnail_url = ""
        obj.optString("mangaCoverimageUrl").let {
            if (it != "") { thumbnail_url = it }
        }
        if (thumbnail_url == "" || thumbnail_url == "http://mhfm5.tel.cdndm5.com/tag/category/nopic.jpg") {
            obj.optString("mangaPicimageUrl").let {
                if (it != "") { thumbnail_url = it }
            }
        }
        if (thumbnail_url == "") {
            obj.optString("shareIcon").let {
                if (it != "") { thumbnail_url = it }
            }
        }

        val arr = obj.getJSONArray("mangaAuthors")
        val tmparr = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            tmparr.add(arr.getString(i))
        }
        author = tmparr.joinToString(", ")

        genre = obj.getString("mangaTheme").replace(" ", ", ")

        status = when (obj.getInt("mangaIsOver")) {
            1 -> SManga.COMPLETED
            0 -> SManga.ONGOING
            else -> SManga.UNKNOWN
        }

        description = obj.getString("mangaIntro")
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        return myGet((baseUrl + manga.url).toHttpUrlOrNull()!!)
    }

    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)

    private fun getChapterName(type: String, name: String, title: String): String {
        return (if (type == "mangaEpisode") "[番外] " else "") + name + (if (title == "") "" else ": $title")
    }

    private fun chaptersFromJSONArray(type: String, arr: JSONArray): List<SChapter> {
        val ret = ArrayList<SChapter>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            ret.add(
                SChapter.create().apply {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    name = if (obj.getInt("isMustPay") == 1) { "(锁) " } else { "" } + getChapterName(type, obj.getString("sectionName"), obj.getString("sectionTitle"))
                    date_upload = dateFormat.parse(obj.getString("releaseTime"))?.time ?: 0L
                    chapter_number = obj.getInt("sectionSort").toFloat()
                    url = "/v1/manga/getRead?mangaSectionId=${obj.getInt("sectionId")}"
                },
            )
        }
        return ret
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val res = response.body.string()
        val obj = JSONObject(res).getJSONObject("response")
        val ret = ArrayList<SChapter>()
        listOf("mangaEpisode", "mangaWords", "mangaRolls").forEach {
            if (obj.has(it)) {
                ret.addAll(chaptersFromJSONArray(it, obj.getJSONArray(it)))
            }
        }
        return ret
    }

    override fun pageListParse(response: Response): List<Page> {
        val res = response.body.string()
        val obj = JSONObject(res).getJSONObject("response")
        val ret = ArrayList<Page>()
        val host = obj.getJSONArray("hostList").getString(0)
        val arr = obj.getJSONArray("mangaSectionImages")
        val query = obj.getString("query")
        for (i in 0 until arr.length()) {
            ret.add(Page(i, "$host${arr.getString(i)}$query", "$host${arr.getString(i)}$query"))
        }
        return ret
    }

    override fun pageListRequest(chapter: SChapter): Request {
        val url = (baseUrl + chapter.url).toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("netType", "4")
            .addQueryParameter("loadreal", "1")
            .addQueryParameter("imageQuality", "2")
            .build()
        return myGet(url)
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException("This method should not be called!")

    override fun getFilterList() = FilterList(
        SortFilter(
            "状态",
            arrayOf(
                Pair("热门", "0"),
                Pair("更新", "1"),
                Pair("新作", "2"),
                Pair("完结", "3"),
            ),
        ),
        CategoryFilter(
            "分类",
            arrayOf(
                Category("全部", "0", "0"),
                Category("热血", "0", "31"),
                Category("恋爱", "0", "26"),
                Category("校园", "0", "1"),
                Category("百合", "0", "3"),
                Category("耽美", "0", "27"),
                Category("伪娘", "0", "5"),
                Category("冒险", "0", "2"),
                Category("职场", "0", "6"),
                Category("后宫", "0", "8"),
                Category("治愈", "0", "9"),
                Category("科幻", "0", "25"),
                Category("励志", "0", "10"),
                Category("生活", "0", "11"),
                Category("战争", "0", "12"),
                Category("悬疑", "0", "17"),
                Category("推理", "0", "33"),
                Category("搞笑", "0", "37"),
                Category("奇幻", "0", "14"),
                Category("魔法", "0", "15"),
                Category("恐怖", "0", "29"),
                Category("神鬼", "0", "20"),
                Category("萌系", "0", "21"),
                Category("历史", "0", "4"),
                Category("美食", "0", "7"),
                Category("同人", "0", "30"),
                Category("运动", "0", "34"),
                Category("绅士", "0", "36"),
                Category("机甲", "0", "40"),
                Category("限制级", "0", "61"),
                Category("少年向", "1", "1"),
                Category("少女向", "1", "2"),
                Category("青年向", "1", "3"),
                Category("港台", "2", "35"),
                Category("日韩", "2", "36"),
                Category("大陆", "2", "37"),
                Category("欧美", "2", "52"),
            ),
        ),
    )

    private data class Category(val name: String, val type: String, val id: String)

    private class SortFilter(
        name: String,
        val vals: Array<Pair<String, String>>,
        state: Int = 0,
    ) : Filter.Select<String>(
        name,
        vals.map { it.first }.toTypedArray(),
        state,
    ) {
        fun getId() = vals[state].second
    }

    private class CategoryFilter(
        name: String,
        val vals: Array<Category>,
        state: Int = 0,
    ) : Filter.Select<String>(
        name,
        vals.map { it.name }.toTypedArray(),
        state,
    ) {
        fun getId() = vals[state].id
        fun getType() = vals[state].type
    }
}
