package eu.kanade.tachiyomi.multisrc.comicake

import android.os.Build
import eu.kanade.tachiyomi.AppInfo
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale

abstract class ComiCake(
    override val name: String,
    final override val baseUrl: String,
    override val lang: String,
    readerEndpoint: String = COMICAKE_DEFAULT_READER_ENDPOINT,
    apiEndpoint: String = COMICAKE_DEFAULT_API_ENDPOINT,
) : HttpSource() {

    override val versionId = 1
    override val supportsLatest = true
    private val readerBase = baseUrl + readerEndpoint
    private var apiBase = baseUrl + apiEndpoint

    private val userAgent = "Mozilla/5.0 (" +
        "Android ${Build.VERSION.RELEASE}; Mobile) " +
        "Tachiyomi/${AppInfo.getVersionName()}"

    override fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", userAgent)
    }

    override fun popularMangaRequest(page: Int): Request {
        return GET("$apiBase/comics.json?ordering=-created_at&page=$page") // Not actually popular, just latest added to system
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val res = response.body.string()
        return getMangasPageFromComicsResponse(res)
    }

    private fun getMangasPageFromComicsResponse(json: String, nested: Boolean = false): MangasPage {
        val response = JSONObject(json)
        val results = response.getJSONArray("results")
        val mangas = ArrayList<SManga>()
        val ids = mutableListOf<Int>()

        for (i in 0 until results.length()) {
            val obj = results.getJSONObject(i)
            if (nested) {
                val nestedComic = obj.getJSONObject("comic")
                val id = nestedComic.getInt("id")
                if (ids.contains(id)) {
                    continue
                }
                ids.add(id)
                val manga = SManga.create()
                manga.url = id.toString()
                manga.title = nestedComic.getString("name")
                mangas.add(manga)
            } else {
                val id = obj.getInt("id")
                if (ids.contains(id)) {
                    continue
                }
                ids.add(id)
                mangas.add(parseComicJson(obj))
            }
        }

        return MangasPage(mangas, !(response.getString("next").isNullOrEmpty() || response.getString("next") == "null"))
    }

    // Shenanigans to allow "open in webview" to show a webpage instead of JSON
    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return client.newCall(apiMangaDetailsRequest(manga))
            .asObservableSuccess()
            .map { response ->
                mangaDetailsParse(response).apply { initialized = true }
            }
    }

    private fun apiMangaDetailsRequest(manga: SManga): Request {
        return GET("$apiBase/comics/${manga.url}.json")
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        return GET(manga.description!!.substringAfterLast("\n"))
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val comicJson = JSONObject(response.body.string())
        return parseComicJson(comicJson, true)
    }

    private fun parseComicJson(obj: JSONObject, human: Boolean = false) = SManga.create().apply {
        url = if (human) {
            "$readerBase/series/${obj.getString("slug")}/"
        } else {
            obj.getInt("id").toString() // Yeah, I know... Feel free to improve on this
        }
        title = obj.getString("name")
        thumbnail_url = obj.getString("cover")
        author = parseListNames(obj.getJSONArray("author"))
        artist = parseListNames(obj.getJSONArray("artist"))
        description = obj.getString("description") +
            "\n\n${readerBase}series/${obj.getString("slug")}/" // webpage for "open in webview"
        genre = parseListNames(obj.getJSONArray("tags"))
        status = SManga.UNKNOWN
    }

    private fun parseListNames(arr: JSONArray): String {
        val hold = ArrayList<String>(arr.length())
        for (i in 0 until arr.length())
            hold.add(arr.getJSONObject(i).getString("name"))
        return hold.sorted().joinToString(", ")
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        // TODO filters
        return GET("$apiBase/comics.json?page=$page&search=$query")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val res = response.body.string()
        return getMangasPageFromComicsResponse(res)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$apiBase/chapters.json?page=$page&expand=comic")
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val res = response.body.string()
        return getMangasPageFromComicsResponse(res, true)
    }

    private fun parseChapterJson(obj: JSONObject) = SChapter.create().apply {
        name = obj.getString("title") // title will always have content, vs. name that's an optional field
        chapter_number = (obj.getInt("chapter") + (obj.getInt("subchapter") / 10.0)).toFloat()
        date_upload = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.getDefault()).parse(obj.getString("published_at"))?.time ?: 0L
        // TODO scanlator field by adding team to expandable in CC (low priority given the use case of CC)
        url = obj.getString("manifest")
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET("$apiBase/chapters.json?comic=${manga.url}&ordering=-volume%2C-chapter%2C-subchapter&n=1000", headers) // There's no pagination in Tachiyomi for chapters so we get 1k chapters
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapterJson = JSONObject(response.body.string())
        val results = chapterJson.getJSONArray("results")
        val ret = ArrayList<SChapter>()
        for (i in 0 until results.length()) {
            ret.add(parseChapterJson(results.getJSONObject(i)))
        }
        return ret
    }

    override fun pageListParse(response: Response): List<Page> {
        val webPub = JSONObject(response.body.string())
        val readingOrder = webPub.getJSONArray("readingOrder")
        val ret = ArrayList<Page>()
        for (i in 0 until readingOrder.length()) {
            val pageUrl = readingOrder.getJSONObject(i).getString("href")
            ret.add(Page(i, "", pageUrl))
        }
        return ret
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException("This method should not be called!")

    companion object {
        private const val COMICAKE_DEFAULT_API_ENDPOINT = "/api" // Highly unlikely to change
        private const val COMICAKE_DEFAULT_READER_ENDPOINT = "/r/" // Can change based on CC config
    }
}
