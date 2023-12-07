package eu.kanade.tachiyomi.extension.all.pixiv

import android.util.LruCache
import eu.kanade.tachiyomi.network.asObservable
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import rx.Observable
import uy.kohesive.injekt.injectLazy
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

class Pixiv(override val lang: String) : HttpSource() {
    override val name = "Pixiv"
    override val baseUrl = "https://www.pixiv.net"
    override val supportsLatest = true

    private val siteLang: String = if (lang == "all") "ja" else lang
    private val illustCache by lazy { LruCache<String, PixivIllust>(50) }

    private val json: Json by injectLazy()
    private val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH) }

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", "$baseUrl/")
        .add("Accept-Language", siteLang)

    private fun apiRequest(method: String, path: String, params: Map<String, String> = emptyMap()) = Request(
        url = baseUrl.toHttpUrl().newBuilder()
            .addEncodedPathSegments("ajax$path")
            .addEncodedQueryParameter("lang", siteLang)
            .apply { params.forEach { (k, v) -> addEncodedQueryParameter(k, v) } }
            .build(),

        headers = headersBuilder().add("Accept", "application/json").build(),
        method = method,
    )

    private inline fun <reified T> apiResponseParse(response: Response): T {
        if (!response.isSuccessful) {
            throw Exception(response.message)
        }

        return response.body.string()
            .let { json.decodeFromString<PixivApiResponse<T>>(it) }
            .apply { if (error) throw Exception(message ?: response.message) }
            .let { it.body!! }
    }

    private fun illustUrlToId(url: String): String =
        url.substringAfterLast("/")

    private fun urlEncode(string: String): String =
        URLEncoder.encode(string, "UTF-8").replace("+", "%20")

    private fun parseTimestamp(string: String) =
        runCatching { dateFormat.parse(string)?.time!! }.getOrDefault(0)

    private fun parseSearchResult(result: PixivSearchResult) = SManga.create().apply {
        url = "/artworks/${result.id!!}"
        title = result.title ?: ""
        thumbnail_url = result.url
    }

<<<<<<< HEAD
    private fun fetchIllust(url: String): Observable<PixivIllust> =
        Observable.fromCallable { illustCache.get(url) }.filter { it != null }.switchIfEmpty(
            Observable.defer {
                client.newCall(illustRequest(url)).asObservable()
                    .map { illustParse(it) }
                    .doOnNext { illustCache.put(url, it) }
            },
        )

    private fun illustRequest(url: String): Request =
        apiRequest("GET", "/illust/${illustUrlToId(url)}")

    private fun illustParse(response: Response): PixivIllust =
        apiResponseParse(response)
=======
    private var searchNextPage = 1
    private var searchHash: Int? = null
    private lateinit var searchIterator: Iterator<SManga>

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        val filters = filters.list as PixivFilters
        val hash = Pair(query, filters).hashCode()

        if (hash != searchHash || page == 1) {
            searchHash = hash

            lateinit var searchSequence: Sequence<PixivIllust>
            lateinit var predicates: List<(PixivIllust) -> Boolean>

            if (query.isNotBlank()) {
                searchSequence = makeIllustSearchSequence(
                    word = query,
                    order = filters.order,
                    mode = filters.rating,
                    sMode = "s_tc",
                    type = filters.type,
                    dateBefore = filters.dateBefore.ifBlank { null },
                    dateAfter = filters.dateAfter.ifBlank { null },
                )

                predicates = buildList {
                    filters.makeTagsPredicate()?.let(::add)
                    filters.makeUsersPredicate()?.let(::add)
                }
            } else if (filters.users.isNotBlank()) {
                searchSequence = makeUserIllustSearchSequence(
                    nick = filters.users,
                    type = filters.type,
                )

                predicates = buildList {
                    filters.makeTagsPredicate()?.let(::add)
                    filters.makeRatingPredicate()?.let(::add)
                }
            } else {
                searchSequence = makeIllustSearchSequence(
                    word = filters.tags.ifBlank { "漫画" },
                    order = filters.order,
                    mode = filters.rating,
                    sMode = filters.searchMode,
                    type = filters.type,
                    dateBefore = filters.dateBefore.ifBlank { null },
                    dateAfter = filters.dateAfter.ifBlank { null },
                )

                predicates = emptyList()
            }

            if (predicates.isNotEmpty()) {
                searchSequence = searchSequence.filter { predicates.all { p -> p(it) } }
            }

            searchIterator = searchSequence.toSManga().iterator()
            searchNextPage = 2
        } else {
            require(page == searchNextPage++)
        }

        val mangas = searchIterator.truncateToList(50).toList()
        return Observable.just(MangasPage(mangas, hasNextPage = mangas.isNotEmpty()))
    }

    private fun makeIllustSearchSequence(
        word: String,
        sMode: String,
        order: String?,
        mode: String?,
        type: String?,
        dateBefore: String?,
        dateAfter: String?,
    ) = sequence<PixivIllust> {
        val call = ApiCall("/touch/ajax/search/illusts")

        call.url.addQueryParameter("word", word)
        call.url.addEncodedQueryParameter("s_mode", sMode)
        type?.let { call.url.addEncodedQueryParameter("type", it) }
        order?.let { call.url.addEncodedQueryParameter("order", it) }
        mode?.let { call.url.addEncodedQueryParameter("mode", it) }
        dateBefore?.let { call.url.addEncodedQueryParameter("ecd", it) }
        dateAfter?.let { call.url.addEncodedQueryParameter("scd", it) }

        for (p in countUp(start = 1)) {
            call.url.setEncodedQueryParameter("p", p.toString())

            val illusts = call.executeApi<PixivResults>().illusts!!
            if (illusts.isEmpty()) break

            for (illust in illusts) {
                if (illust.is_ad_container == 1) continue
                if (illust.type == "2") continue

                yield(illust)
            }
        }
    }

    private fun makeUserIllustSearchSequence(nick: String, type: String?) = sequence<PixivIllust> {
        val searchUsers = HttpCall("/search_user.php?s_mode=s_usr")
            .apply { url.addQueryParameter("nick", nick) }

        val fetchUserIllusts = ApiCall("/touch/ajax/user/illusts")
            .apply { type?.let { url.setEncodedQueryParameter("type", it) } }

        for (p in countUp(start = 1)) {
            searchUsers.url.setEncodedQueryParameter("p", p.toString())

            val userIds = Jsoup.parse(searchUsers.execute().body.string())
                .select(".user-recommendation-item > a").eachAttr("href")
                .map { it.substringAfterLast('/') }

            if (userIds.isEmpty()) break

            for (userId in userIds) {
                fetchUserIllusts.url.setEncodedQueryParameter("id", userId)

                for (p in countUp(start = 1)) {
                    fetchUserIllusts.url.setEncodedQueryParameter("p", p.toString())

                    val illusts = fetchUserIllusts.executeApi<PixivResults>().illusts!!
                    if (illusts.isEmpty()) break

                    yieldAll(illusts)
                }
            }
        }
    }

    override fun getFilterList() = FilterList(PixivFilters())

    private fun Sequence<PixivIllust>.toSManga() = sequence<SManga> {
        val seriesIdsSeen = mutableSetOf<String>()

        forEach { illust ->
            val series = illust.series

            if (series == null) {
                val manga = SManga.create()
                manga.setUrlWithoutDomain("/artworks/${illust.id!!}")
                manga.title = illust.title ?: "(null)"
                manga.thumbnail_url = illust.url
                yield(manga)
            } else if (seriesIdsSeen.add(series.id!!)) {
                val manga = SManga.create()
                manga.setUrlWithoutDomain("/user/${series.userId!!}/series/${series.id}")
                manga.title = series.title ?: "(null)"
                manga.thumbnail_url = series.coverImage ?: illust.url
                yield(manga)
            }
        }
    }

    private var latestMangaNextPage = 1
    private lateinit var latestMangaIterator: Iterator<SManga>

    override fun fetchLatestUpdates(page: Int): Observable<MangasPage> {
        if (page == 1) {
            latestMangaIterator = sequence {
                val call = ApiCall("/touch/ajax/latest?type=manga")

                for (p in countUp(start = 1)) {
                    call.url.setEncodedQueryParameter("p", p.toString())

                    val illusts = call.executeApi<PixivResults>().illusts!!
                    if (illusts.isEmpty()) break

                    for (illust in illusts) {
                        if (illust.is_ad_container == 1) continue
                        yield(illust)
                    }
                }
            }
                .toSManga()
                .iterator()

            latestMangaNextPage = 2
        } else {
            require(page == latestMangaNextPage++)
        }

        val mangas = latestMangaIterator.truncateToList(50).toList()
        return Observable.just(MangasPage(mangas, hasNextPage = mangas.isNotEmpty()))
    }

    private val getIllustCached by lazy {
        lruCached<String, PixivIllust>(25) { illustId ->
            val call = ApiCall("/touch/ajax/illust/details?illust_id=$illustId")
            return@lruCached call.executeApi<PixivIllustDetails>().illust_details!!
        }
    }

    private val getSeriesIllustsCached by lazy {
        lruCached<String, List<PixivIllust>>(25) { seriesId ->
            val call = ApiCall("/touch/ajax/illust/series_content/$seriesId")
            var lastOrder = 0

            return@lruCached buildList {
                while (true) {
                    call.url.setEncodedQueryParameter("last_order", lastOrder.toString())

                    val illusts = call.executeApi<PixivSeriesContents>().series_contents!!
                    if (illusts.isEmpty()) break

                    addAll(illusts)
                    lastOrder += illusts.size
                }
            }
        }
    }

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        val (id, isSeries) = parseSMangaUrl(manga.url)

        if (isSeries) {
            val series = ApiCall("/touch/ajax/illust/series/$id")
                .executeApi<PixivSeriesDetails>().series!!

            val illusts = getSeriesIllustsCached(id)

            if (series.id != null && series.userId != null) {
                manga.setUrlWithoutDomain("/user/${series.userId}/series/${series.id}")
            }

            series.title?.let { manga.title = it }
            series.caption?.let { manga.description = it }

            illusts.firstOrNull()?.author_details?.user_name?.let {
                manga.artist = it
                manga.author = it
            }

            val tags = illusts.flatMap { it.tags ?: emptyList() }.toSet()
            if (tags.isNotEmpty()) manga.genre = tags.joinToString()

            val coverImage = series.coverImage?.let { if (it.isString) it.content else null }
            (coverImage ?: illusts.firstOrNull()?.url)?.let { manga.thumbnail_url = it }
        } else {
            val illust = getIllustCached(id)

            illust.id?.let { manga.setUrlWithoutDomain("/artworks/$it") }
            illust.title?.let { manga.title = it }

            illust.author_details?.user_name?.let {
                manga.artist = it
                manga.author = it
            }

            illust.comment?.let { manga.description = it }
            illust.tags?.let { manga.genre = it.joinToString() }
            illust.url?.let { manga.thumbnail_url = it }
        }

        return Observable.just(manga)
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        val (id, isSeries) = parseSMangaUrl(manga.url)

        val illusts = when (isSeries) {
            true -> getSeriesIllustsCached(id)
            false -> listOf(getIllustCached(id))
        }

        val chapters = illusts.mapIndexed { i, illust ->
            SChapter.create().apply {
                setUrlWithoutDomain("/artworks/${illust.id!!}")
                name = illust.title ?: "(null)"
                date_upload = (illust.upload_timestamp ?: 0) * 1000
                chapter_number = (illusts.size - i).toFloat()
            }
        }

        return Observable.just(chapters)
    }

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> {
        val illustId = chapter.url.substringAfterLast('/')

        val pages = ApiCall("/ajax/illust/$illustId/pages")
            .executeApi<List<PixivIllustPage>>()
            .mapIndexed { i, it -> Page(i, chapter.url, it.urls!!.original!!) }

        return Observable.just(pages)
    }

    override fun chapterListParse(response: Response): List<SChapter> =
        throw UnsupportedOperationException("Not used.")

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException("Not used.")

    override fun latestUpdatesParse(response: Response): MangasPage =
        throw UnsupportedOperationException("Not used.")

    override fun latestUpdatesRequest(page: Int): Request =
        throw UnsupportedOperationException("Not used.")

    override fun mangaDetailsParse(response: Response): SManga =
        throw UnsupportedOperationException("Not used.")

    override fun pageListParse(response: Response): List<Page> =
        throw UnsupportedOperationException("Not used.")

    override fun popularMangaParse(response: Response): MangasPage =
        throw UnsupportedOperationException("Not used.")
>>>>>>> upstream/master

    override fun popularMangaRequest(page: Int): Request =
        searchMangaRequest(page, "", FilterList())

    override fun popularMangaParse(response: Response) = MangasPage(
        mangas = apiResponseParse<PixivSearchResults>(response)
            .popular?.run { recent.orEmpty() + permanent.orEmpty() }
            ?.map(::parseSearchResult)
            .orEmpty(),

        hasNextPage = false,
    )

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val word = urlEncode(query.ifBlank { "漫画" })

        val parameters = mutableMapOf(
            "word" to query,
            "order" to "date_d",
            "mode" to "all",
            "p" to page.toString(),
            "s_mode" to "s_tag_full",
            "type" to "manga",
        )

        filters.forEach { filter ->
            when (filter) {
                is FilterType -> parameters["type"] = filter.value
                is FilterRating -> parameters["mode"] = filter.value
                is FilterSearchMode -> parameters["s_mode"] = filter.value
                is FilterOrder -> parameters["order"] = filter.value
                is FilterDateBefore -> filter.value?.let { parameters["ecd"] = it }
                is FilterDateAfter -> filter.value?.let { parameters["scd"] = it }
                else -> {}
            }
        }

        val endpoint = when (parameters["type"]) {
            "all" -> "artworks"
            "illust" -> "illustrations"
            "manga" -> "manga"
            else -> ""
        }

        return apiRequest("GET", "/search/$endpoint/$word", parameters)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val mangas = apiResponseParse<PixivSearchResults>(response)
            .run { illustManga ?: illust ?: manga }?.data
            ?.filter { it.isAdContainer != true }
            ?.map(::parseSearchResult)
            .orEmpty()

        return MangasPage(mangas, hasNextPage = mangas.isNotEmpty())
    }

    override fun latestUpdatesRequest(page: Int): Request =
        searchMangaRequest(page, "", FilterList())

    override fun latestUpdatesParse(response: Response): MangasPage =
        searchMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request =
        illustRequest(manga.url)

    override fun mangaDetailsParse(response: Response) = SManga.create().apply {
        val illust = illustParse(response)

        url = "/artworks/${illust.id!!}"
        title = illust.title ?: ""
        artist = illust.userName
        author = illust.userName
        description = illust.description?.let { Jsoup.parseBodyFragment(it).wholeText() }
        genre = illust.tags?.tags?.mapNotNull { it.tag }?.joinToString()
        thumbnail_url = illust.urls?.thumb
        update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> =
        fetchIllust(manga.url).map { illust ->
            listOf(
                SChapter.create().apply {
                    url = manga.url
                    name = "Oneshot"
                    date_upload = illust.uploadDate?.let(::parseTimestamp) ?: 0
                    chapter_number = 0F
                },
            )
        }

    override fun chapterListRequest(manga: SManga): Request =
        throw IllegalStateException("Not used")

    override fun chapterListParse(response: Response): List<SChapter> =
        throw IllegalStateException("Not used")

    override fun pageListRequest(chapter: SChapter): Request =
        apiRequest("GET", "/illust/${illustUrlToId(chapter.url)}/pages")

    override fun pageListParse(response: Response): List<Page> =
        apiResponseParse<List<PixivPage>>(response)
            .mapIndexed { i, it -> Page(index = i, imageUrl = it.urls?.original) }

    override fun imageUrlRequest(page: Page): Request =
        throw IllegalStateException("Not used")

    override fun imageUrlParse(response: Response): String =
        throw IllegalStateException("Not used")

    override fun getMangaUrl(manga: SManga): String =
        baseUrl + manga.url

    override fun getChapterUrl(chapter: SChapter): String =
        baseUrl + chapter.url

    override fun getFilterList() = FilterList(
        listOf(
            FilterType(),
            FilterRating(),
            FilterSearchMode(),
            FilterOrder(),
            FilterDateBefore(),
            FilterDateAfter(),
        ),
    )
}
