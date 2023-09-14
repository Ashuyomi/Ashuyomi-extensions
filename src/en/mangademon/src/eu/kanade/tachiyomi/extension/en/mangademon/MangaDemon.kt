package eu.kanade.tachiyomi.extension.en.mangademon

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale

class MangaDemon : ParsedHttpSource() {

    override val lang = "en"
    override val supportsLatest = true
    override val name = "Manga Demon"
    override val baseUrl = "https://manga-demon.org"

    override val client = network.cloudflareClient.newBuilder()
        .rateLimit(1)
        .build()

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", baseUrl)

    // latest
    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/updates.php?list=$page", headers)
    }

    override fun latestUpdatesNextPageSelector() = ".pagination a:contains(Next)"

    override fun latestUpdatesSelector() = "div.leftside"

    override fun latestUpdatesFromElement(element: Element) = SManga.create().apply {
        element.select("a").apply {
            title = attr("title")
            setUrlWithoutDomain(attr("href"))
        }
        thumbnail_url = element.select("img").attr("abs:src")
    }

    // Popular
    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/browse.php?list=$page", headers)
    }

    override fun popularMangaNextPageSelector() = latestUpdatesNextPageSelector()

    override fun popularMangaSelector() = latestUpdatesSelector()

    override fun popularMangaFromElement(element: Element) = latestUpdatesFromElement(element)

    // Search
    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        return if (query.isNotEmpty()) {
            super.fetchSearchManga(page, query, filters)
        } else {
            client.newCall(filterSearchRequest(page, filters))
                .asObservableSuccess()
                .map(::filterSearchParse)
        }
    }

    private fun filterSearchRequest(page: Int, filters: FilterList): Request {
        val url = "$baseUrl/browse.php".toHttpUrl().newBuilder().apply {
            addQueryParameter("list", page.toString())
            filters.forEach { filter ->
                when (filter) {
                    is GenreFilter -> {
                        filter.checked.forEach { genre ->
                            addQueryParameter("genre[]", genre)
                        }
                    }
                    is StatusFilter -> {
                        addQueryParameter("status", filter.selected)
                    }
                    is SortFilter -> {
                        addQueryParameter("orderby", filter.selected)
                    }
                    else -> {}
                }
            }
        }.build()

        return GET(url, headers)
    }

    private fun filterSearchParse(response: Response) = popularMangaParse(response)

    override fun getFilterList() = getFilters()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search.php".toHttpUrl().newBuilder()
            .addQueryParameter("manga", query)
            .build()
        return GET(url, headers)
    }

    override fun searchMangaSelector() = "a.boxsizing"

    override fun searchMangaFromElement(element: Element) = SManga.create().apply {
        title = element.text()
        setUrlWithoutDomain(element.attr("href"))
        val urlSorter = title.replace(":", "%20")
        thumbnail_url = ("https://readermc.org/images/thumbnails/$urlSorter.webp")
    }

    override fun searchMangaNextPageSelector() = null

    // Manga details
    override fun mangaDetailsParse(document: Document): SManga {
        val infoElement = document.select("article")

        return SManga.create().apply {
            title = infoElement.select("h1.novel-title").text()
            author = infoElement.select("div.author").text().drop(7)
            status = parseStatus(infoElement.select("span:has(small:containsOwn(Status))").text())
            genre = infoElement.select("a.property-item").joinToString { it.text() }
            description = infoElement.select("p.description").text()
            thumbnail_url = infoElement.select("img#thumbonail").attr("src")
        }
    }

    private fun parseStatus(status: String?) = when {
        status == null -> SManga.UNKNOWN
        status.contains("Ongoing", ignoreCase = true) -> SManga.ONGOING
        status.contains("Completed", ignoreCase = true) -> SManga.COMPLETED
        else -> SManga.UNKNOWN
    }

    override fun chapterListSelector() = "ul.chapter-list li"

    // Get Chapters
    override fun chapterFromElement(element: Element): SChapter {
        return SChapter.create().apply {
            element.select("a").let { urlElement ->
                setUrlWithoutDomain(urlElement.attr("href"))
                name = element.select("strong.chapter-title").text()
            }
            val date = element.select("time.chapter-update").text()
            date_upload = parseDate(date)
        }
    }

    private fun parseDate(dateStr: String): Long {
        return runCatching { DATE_FORMATTER.parse(dateStr)?.time }
            .getOrNull() ?: 0L
    }

    companion object {
        private val DATE_FORMATTER by lazy {
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        }

        private val loadMoreEndpointRegex by lazy { Regex("""GET[^/]+([^=]+)""") }
    }

    override fun pageListParse(document: Document): List<Page> {
        val baseImages = document.select("img.imgholder")
            .map { it.attr("abs:src") }
            .toMutableList()

        baseImages.addAll(loadMoreImages(document))

        return baseImages.mapIndexed { i, img -> Page(i, "", img) }
    }

    private fun loadMoreImages(document: Document): List<String> {
        val buttonHtml = document.selectFirst("img.imgholder ~ button")
            ?.attr("onclick")?.replace("\"", "\'")
            ?: return emptyList()

        val id = buttonHtml.substringAfter("\'").substringBefore("\'").trim()
        val funcName = buttonHtml.substringBefore("(").trim()

        val endpoint = document.selectFirst("script:containsData($funcName)")
            ?.data()
            ?.let { loadMoreEndpointRegex.find(it)?.groupValues?.get(1) }
            ?: return emptyList()

        val response = client.newCall(GET("$baseUrl$endpoint=$id", headers)).execute()

        if (!response.isSuccessful) {
            response.close()
            return emptyList()
        }

        return response.use { it.asJsoup() }
            .select("img")
            .map { it.attr("abs:src") }
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")
}
