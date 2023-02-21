package eu.kanade.tachiyomi.extension.pt.muitomanga

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.injectLazy
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil

class MuitoManga : ParsedHttpSource() {

    override val name = "Muito Mangá"

    override val baseUrl = "https://muitomanga.com"

    override val lang = "pt-BR"

    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addInterceptor(::directoryCacheIntercept)
        .build()

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("Accept", ACCEPT)
        .add("Accept-Language", ACCEPT_LANGUAGE)
        .add("Referer", "$baseUrl/")

    private val json: Json by injectLazy()

    private val directoryCache: MutableMap<Int, String> = mutableMapOf()

    override fun popularMangaRequest(page: Int): Request {
        val newHeaders = headersBuilder()
            .set("Accept", ACCEPT_JSON)
            .set("Referer", "$baseUrl/lista-de-mangas")
            .add("X-Page", page.toString())
            .add("X-Requested-With", "XMLHttpRequest")
            .build()

        return GET("$baseUrl/lib/diretorio.json?pagina=1&tipo_pag=$DIRECTORY_TYPE_POPULAR&pega_busca=", newHeaders)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val directory = json.decodeFromString<MuitoMangaDirectoryDto>(response.body.string())
        val totalPages = ceil(directory.results.size.toDouble() / ITEMS_PER_PAGE)
        val currentPage = response.request.header("X-Page")!!.toInt()

        val mangaList = directory.results
            .drop(ITEMS_PER_PAGE * (currentPage - 1))
            .take(ITEMS_PER_PAGE)
            .map(::popularMangaFromObject)

        return MangasPage(mangaList, hasNextPage = currentPage < totalPages)
    }

    private fun popularMangaFromObject(manga: MuitoMangaTitleDto): SManga = SManga.create().apply {
        title = manga.title
        thumbnail_url = manga.image
        url = "/manga/" + manga.url
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val newHeaders = headersBuilder()
            .set("Accept", ACCEPT_JSON)
            .set("Referer", "$baseUrl/lista-de-mangas/mais-vistos")
            .add("X-Page", page.toString())
            .add("X-Requested-With", "XMLHttpRequest")
            .build()

        return GET("$baseUrl/lib/diretorio.json?pagina=1&tipo_pag=$DIRECTORY_TYPE_LATEST&pega_busca=", newHeaders)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/buscar".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("q", query)

        return GET(url.toString(), headers)
    }

    override fun searchMangaSelector() = "div.content_post div.anime"

    override fun searchMangaFromElement(element: Element): SManga = SManga.create().apply {
        title = element.select("h3 a").first()!!.text()
        thumbnail_url = element.select("div.capaMangaBusca img").first()!!.attr("src")
        setUrlWithoutDomain(element.select("a").first()!!.attr("abs:href"))
    }

    override fun searchMangaNextPageSelector(): String? = null

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        val infoElement = document.select("div.content_post").first()!!
        val isFinished = infoElement.select("span.series_autor2 > span.series_autor").firstOrNull()

        title = document.select("div.content div.widget-title h1").first()!!.text()
        author = infoElement.select("span.series_autor2").first()!!.ownText()
        genre = infoElement.select("ul.lancamento-list a").joinToString { it.text() }
        description = document.select("ul.lancamento-list ~ p").text().trim()
        status = if (isFinished != null) SManga.COMPLETED else SManga.ONGOING
        thumbnail_url = infoElement.select("div.capaMangaInfo img").first()!!.attr("data-src")
    }

    override fun chapterListSelector() = "div.manga-chapters div.single-chapter"

    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        name = element.select("a").first()!!.text()
        date_upload = element.select("small[title]").first()!!.text().toDate()
        scanlator = element.select("div.scanlator2 a").joinToString { it.text().trim() }
        setUrlWithoutDomain(element.select("a").first()!!.attr("abs:href"))
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select("script:containsData(imagens_cap)").first()!!
            .data()
            .substringAfter("[")
            .substringBefore("]")
            .split(",")
            .mapIndexed { i, imageUrl ->
                val fixedImageUrl = imageUrl
                    .replace("\"", "")
                    .replace("\\/", "/")
                Page(i, document.location(), fixedImageUrl)
            }
    }

    override fun imageUrlParse(document: Document) = ""

    override fun imageRequest(page: Page): Request {
        val newHeaders = headersBuilder()
            .set("Accept", ACCEPT_IMAGE)
            .set("Referer", page.url)
            .build()

        return GET(page.imageUrl!!, newHeaders)
    }

    override fun popularMangaSelector(): String = throw UnsupportedOperationException("Not used")

    override fun popularMangaFromElement(element: Element): SManga = throw UnsupportedOperationException("Not used")

    override fun popularMangaNextPageSelector(): String = throw UnsupportedOperationException("Not used")

    override fun latestUpdatesSelector(): String = throw UnsupportedOperationException("Not used")

    override fun latestUpdatesFromElement(element: Element): SManga = throw UnsupportedOperationException("Not used")

    override fun latestUpdatesNextPageSelector(): String = throw UnsupportedOperationException("Not used")

    private fun directoryCacheIntercept(chain: Interceptor.Chain): Response {
        if (!chain.request().url.toString().contains("diretorio.json")) {
            return chain.proceed(chain.request())
        }

        val directoryType = chain.request().url.queryParameter("tipo_pag")!!.toInt()

        if (directoryCache.containsKey(directoryType)) {
            val jsonContentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
            val responseBody = directoryCache[directoryType]!!.toResponseBody(jsonContentType)

            return Response.Builder()
                .code(200)
                .protocol(Protocol.HTTP_1_1)
                .request(chain.request())
                .message("OK")
                .body(responseBody)
                .build()
        }

        val response = chain.proceed(chain.request())
        val responseContentType = response.body.contentType()
        val responseString = response.body.string()

        directoryCache[directoryType] = responseString

        return response.newBuilder()
            .body(responseString.toResponseBody(responseContentType))
            .build()
    }

    private fun String.toDate(): Long {
        return runCatching { DATE_FORMATTER.parse(this)?.time }
            .getOrNull() ?: 0L
    }

    companion object {
        private const val ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9," +
            "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
        private const val ACCEPT_IMAGE = "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
        private const val ACCEPT_JSON = "application/json, text/javascript, */*; q=0.01"
        private const val ACCEPT_LANGUAGE = "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7,es;q=0.6,gl;q=0.5"

        private const val ITEMS_PER_PAGE = 21
        private const val DIRECTORY_TYPE_POPULAR = 5
        private const val DIRECTORY_TYPE_LATEST = 6

        private val DATE_FORMATTER by lazy {
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        }
    }
}
