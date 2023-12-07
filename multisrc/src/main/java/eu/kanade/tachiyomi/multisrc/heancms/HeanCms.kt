package eu.kanade.tachiyomi.multisrc.heancms

<<<<<<< HEAD
=======
import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
>>>>>>> upstream/master
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import rx.Observable
<<<<<<< HEAD
=======
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException
>>>>>>> upstream/master
import java.text.SimpleDateFormat
import java.util.Locale

abstract class HeanCms(
    override val name: String,
    override val baseUrl: String,
    override val lang: String,
    protected val apiUrl: String = baseUrl.replace("://", "://api."),
) : ConfigurableSource, HttpSource() {

<<<<<<< HEAD
=======
    private val preferences: SharedPreferences by lazy {
        Injekt.get<Application>().getSharedPreferences("source_$id", 0x0000)
    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        SwitchPreferenceCompat(screen.context).apply {
            key = SHOW_PAID_CHAPTERS_PREF
            title = intl.prefShowPaidChapterTitle
            summaryOn = intl.prefShowPaidChapterSummaryOn
            summaryOff = intl.prefShowPaidChapterSummaryOff
            setDefaultValue(SHOW_PAID_CHAPTERS_DEFAULT)

            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit()
                    .putBoolean(SHOW_PAID_CHAPTERS_PREF, newValue as Boolean)
                    .commit()
            }
        }.also(screen::addPreference)
    }

>>>>>>> upstream/master
    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient

    /**
     * Custom Json instance to make usage of `encodeDefaults`,
     * which is not enabled on the injected instance of the app.
     */
    protected val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    protected val intl by lazy { HeanCmsIntl(lang) }

    protected open val fetchAllTitles: Boolean = false

    protected open val coverPath: String = "cover/"

    protected open val mangaSubDirectory: String = "series"

    protected open val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US)

    private var seriesSlugMap: Map<String, HeanCmsTitle>? = null

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("Origin", baseUrl)
        .add("Referer", "$baseUrl/")

    override fun popularMangaRequest(page: Int): Request {
        val payloadObj = HeanCmsQuerySearchPayloadDto(
            page = page,
            order = "desc",
            orderBy = "total_views",
            status = "Ongoing",
            type = "Comic",
        )

        val payload = json.encodeToString(payloadObj).toRequestBody(JSON_MEDIA_TYPE)

        val apiHeaders = headersBuilder()
            .add("Accept", ACCEPT_JSON)
            .add("Content-Type", payload.contentType().toString())
            .build()

        return POST("$apiUrl/series/querysearch", apiHeaders, payload)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val json = response.body.string()

        if (json.startsWith("{")) {
            val result = json.parseAs<HeanCmsQuerySearchDto>()
<<<<<<< HEAD
            val mangaList = result.data.map { it.toSManga(apiUrl, coverPath) }
=======
            val mangaList = result.data.map {
                if (slugStrategy != SlugStrategy.NONE) {
                    preferences.slugMap = preferences.slugMap.toMutableMap()
                        .also { map -> map[it.slug.toPermSlugIfNeeded()] = it.slug }
                }
                it.toSManga(apiUrl, coverPath, mangaSubDirectory, slugStrategy)
            }
>>>>>>> upstream/master

            fetchAllTitles()

            return MangasPage(mangaList, result.meta?.hasNextPage ?: false)
        }

        val mangaList = json.parseAs<List<HeanCmsSeriesDto>>()
<<<<<<< HEAD
            .map { it.toSManga(apiUrl, coverPath) }
=======
            .map {
                if (slugStrategy != SlugStrategy.NONE) {
                    preferences.slugMap = preferences.slugMap.toMutableMap()
                        .also { map -> map[it.slug.toPermSlugIfNeeded()] = it.slug }
                }
                it.toSManga(apiUrl, coverPath, mangaSubDirectory, slugStrategy)
            }
>>>>>>> upstream/master

        fetchAllTitles()

        return MangasPage(mangaList, hasNextPage = false)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val payloadObj = HeanCmsQuerySearchPayloadDto(
            page = page,
            order = "desc",
            orderBy = "latest",
            status = "Ongoing",
            type = "Comic",
        )

        val payload = json.encodeToString(payloadObj).toRequestBody(JSON_MEDIA_TYPE)

        val apiHeaders = headersBuilder()
            .add("Accept", ACCEPT_JSON)
            .add("Content-Type", payload.contentType().toString())
            .build()

        return POST("$apiUrl/series/querysearch", apiHeaders, payload)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        if (!query.startsWith(SEARCH_PREFIX)) {
            return super.fetchSearchManga(page, query, filters)
        }

        val slug = query.substringAfter(SEARCH_PREFIX)
<<<<<<< HEAD
        val manga = SManga.create().apply { url = "/series/$slug" }
=======
        val manga = SManga.create().apply {
            url = if (slugStrategy != SlugStrategy.NONE) {
                val mangaId = getIdBySlug(slug)
                "/$mangaSubDirectory/${slug.toPermSlugIfNeeded()}#$mangaId"
            } else {
                "/$mangaSubDirectory/$slug"
            }
        }
>>>>>>> upstream/master

        return fetchMangaDetails(manga).map { MangasPage(listOf(it), false) }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        /**
         * Their query search endpoint doesn't return the thumbnails, so we need to do
         * later an special parsing to get the thumbnails as well from the slug map.
         */
        if (query.isNotBlank()) {
            val searchPayloadObj = HeanCmsSearchPayloadDto(query)
            val searchPayload = json.encodeToString(searchPayloadObj)
                .toRequestBody(JSON_MEDIA_TYPE)

            val apiHeaders = headersBuilder()
                .add("Accept", ACCEPT_JSON)
                .add("Content-Type", searchPayload.contentType().toString())
                .build()

            return POST("$apiUrl/series/search", apiHeaders, searchPayload)
        }

        val sortByFilter = filters.firstInstanceOrNull<SortByFilter>()

        val payloadObj = HeanCmsQuerySearchPayloadDto(
            page = page,
            order = if (sortByFilter?.state?.ascending == true) "asc" else "desc",
            orderBy = sortByFilter?.selected ?: "total_views",
            status = filters.firstInstanceOrNull<StatusFilter>()?.selected?.value ?: "Ongoing",
            type = "Comic",
            tagIds = filters.firstInstanceOrNull<GenreFilter>()?.state
                ?.filter(Genre::state)
                ?.map(Genre::id)
                .orEmpty(),
        )

        val payload = json.encodeToString(payloadObj).toRequestBody(JSON_MEDIA_TYPE)

        val apiHeaders = headersBuilder()
            .add("Accept", ACCEPT_JSON)
            .add("Content-Type", payload.contentType().toString())
            .build()

        return POST("$apiUrl/series/querysearch", apiHeaders, payload)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val json = response.body.string()

        if (response.request.url.pathSegments.last() == "search") {
            fetchAllTitles()

            val result = json.parseAs<List<HeanCmsSearchDto>>()
            val mangaList = result
                .filter { it.type == "Comic" }
<<<<<<< HEAD
                .map { it.toSManga(apiUrl, coverPath, seriesSlugMap.orEmpty()) }
=======
                .map {
                    it.slug = it.slug.toPermSlugIfNeeded()
                    it.toSManga(apiUrl, coverPath, mangaSubDirectory, seriesSlugMap.orEmpty(), slugStrategy)
                }
>>>>>>> upstream/master

            return MangasPage(mangaList, false)
        }

        if (json.startsWith("{")) {
            val result = json.parseAs<HeanCmsQuerySearchDto>()
<<<<<<< HEAD
            val mangaList = result.data.map { it.toSManga(apiUrl, coverPath) }
=======
            val mangaList = result.data.map {
                if (slugStrategy != SlugStrategy.NONE) {
                    preferences.slugMap = preferences.slugMap.toMutableMap()
                        .also { map -> map[it.slug.toPermSlugIfNeeded()] = it.slug }
                }
                it.toSManga(apiUrl, coverPath, mangaSubDirectory, slugStrategy)
            }
>>>>>>> upstream/master

            fetchAllTitles()

            return MangasPage(mangaList, result.meta?.hasNextPage ?: false)
        }

        val mangaList = json.parseAs<List<HeanCmsSeriesDto>>()
<<<<<<< HEAD
            .map { it.toSManga(apiUrl, coverPath) }
=======
            .map {
                if (slugStrategy != SlugStrategy.NONE) {
                    preferences.slugMap = preferences.slugMap.toMutableMap()
                        .also { map -> map[it.slug.toPermSlugIfNeeded()] = it.slug }
                }
                it.toSManga(apiUrl, coverPath, mangaSubDirectory, slugStrategy)
            }
>>>>>>> upstream/master

        fetchAllTitles()

        return MangasPage(mangaList, hasNextPage = false)
    }

    override fun getMangaUrl(manga: SManga): String {
        val seriesSlug = manga.url
            .substringAfterLast("/")
            .replace(TIMESTAMP_REGEX, "")

        val currentSlug = seriesSlugMap?.get(seriesSlug)?.slug ?: seriesSlug

        return "$baseUrl/$mangaSubDirectory/$currentSlug"
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        val seriesSlug = manga.url
            .substringAfterLast("/")
            .replace(TIMESTAMP_REGEX, "")

        fetchAllTitles()

        val seriesDetails = seriesSlugMap?.get(seriesSlug)
        val currentSlug = seriesDetails?.slug ?: seriesSlug
        val currentStatus = seriesDetails?.status ?: manga.status

        val apiHeaders = headersBuilder()
            .add("Accept", ACCEPT_JSON)
            .build()

        return GET("$apiUrl/series/$currentSlug#$currentStatus", apiHeaders)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val result = runCatching { response.parseAs<HeanCmsSeriesDto>() }
<<<<<<< HEAD
        val seriesDetails = result.getOrNull()?.toSManga(apiUrl, coverPath)
            ?: throw Exception(intl.urlChangedError(name))
=======

        val seriesResult = result.getOrNull() ?: throw Exception(intl.urlChangedError(name))

        if (slugStrategy != SlugStrategy.NONE) {
            preferences.slugMap = preferences.slugMap.toMutableMap()
                .also { it[seriesResult.slug.toPermSlugIfNeeded()] = seriesResult.slug }
        }

        val seriesDetails = seriesResult.toSManga(apiUrl, coverPath, mangaSubDirectory, slugStrategy)
>>>>>>> upstream/master

        return seriesDetails.apply {
            status = status.takeUnless { it == SManga.UNKNOWN }
                ?: response.request.url.fragment?.toIntOrNull() ?: SManga.UNKNOWN
        }
    }

    override fun chapterListRequest(manga: SManga): Request = mangaDetailsRequest(manga)

    override fun chapterListParse(response: Response): List<SChapter> {
        val result = response.parseAs<HeanCmsSeriesDto>()
        val seriesSlug = response.request.url.pathSegments.last()
        val currentTimestamp = System.currentTimeMillis()

<<<<<<< HEAD
        return result.chapters.orEmpty()
            .filterNot { it.price == 1 }
            .map { it.toSChapter(seriesSlug, dateFormat) }
=======
        val showPaidChapters = preferences.showPaidChapters

        if (useNewQueryEndpoint) {
            return result.seasons.orEmpty()
                .flatMap { it.chapters.orEmpty() }
                .filter { it.price == 0 || showPaidChapters }
                .map { it.toSChapter(result.slug, mangaSubDirectory, dateFormat, slugStrategy) }
                .filter { it.date_upload <= currentTimestamp }
        }

        return result.chapters.orEmpty()
            .filter { it.price == 0 || showPaidChapters }
            .map { it.toSChapter(result.slug, mangaSubDirectory, dateFormat, slugStrategy) }
>>>>>>> upstream/master
            .filter { it.date_upload <= currentTimestamp }
            .reversed()
    }

<<<<<<< HEAD
    override fun getChapterUrl(chapter: SChapter): String = baseUrl + chapter.url

    override fun pageListRequest(chapter: SChapter): Request {
        val chapterId = chapter.url.substringAfterLast("#")
=======
    override fun getChapterUrl(chapter: SChapter): String {
        if (slugStrategy == SlugStrategy.NONE) return baseUrl + chapter.url

        val seriesSlug = chapter.url
            .substringAfter("/$mangaSubDirectory/")
            .substringBefore("/")
            .toPermSlugIfNeeded()

        val currentSlug = preferences.slugMap[seriesSlug] ?: seriesSlug
        val chapterUrl = chapter.url.replaceFirst(seriesSlug, currentSlug)

        return baseUrl + chapterUrl
    }

    override fun pageListRequest(chapter: SChapter): Request {
        if (useNewQueryEndpoint) {
            if (slugStrategy != SlugStrategy.NONE) {
                val seriesPermSlug = chapter.url.substringAfter("/$mangaSubDirectory/").substringBefore("/")
                val seriesSlug = preferences.slugMap[seriesPermSlug] ?: seriesPermSlug
                val chapterUrl = chapter.url.replaceFirst(seriesPermSlug, seriesSlug)
                return GET(baseUrl + chapterUrl, headers)
            }
            return GET(baseUrl + chapter.url, headers)
        }

        val chapterId = chapter.url.substringAfterLast("#").substringBefore("-paid")
>>>>>>> upstream/master

        val apiHeaders = headersBuilder()
            .add("Accept", ACCEPT_JSON)
            .build()

        return GET("$apiUrl/series/chapter/$chapterId", apiHeaders)
    }

    override fun pageListParse(response: Response): List<Page> {
<<<<<<< HEAD
        return response.parseAs<HeanCmsReaderDto>().content?.images.orEmpty()
            .filterNot { imageUrl ->
                // Their image server returns HTTP 403 for hidden files that starts
                // with a dot in the file name. To avoid download errors, these are removed.
                imageUrl
                    .removeSuffix("/")
                    .substringAfterLast("/")
                    .startsWith(".")
            }
=======
        if (useNewQueryEndpoint) {
            val paidChapter = response.request.url.fragment?.contains("-paid")

            val document = response.asJsoup()

            val images = document.selectFirst("div.min-h-screen > div.container > p.items-center")

            if (images == null && paidChapter == true) {
                throw IOException(intl.paidChapterError)
            }

            return images?.select("img").orEmpty().mapIndexed { i, img ->
                val imageUrl = if (img.hasClass("lazy")) img.absUrl("data-src") else img.absUrl("src")
                Page(i, "", imageUrl)
            }
        }

        val images = response.parseAs<HeanCmsReaderDto>().content?.images.orEmpty()
        val paidChapter = response.request.url.fragment?.contains("-paid")

        if (images.isEmpty() && paidChapter == true) {
            throw IOException(intl.paidChapterError)
        }

        return images.filterNot { imageUrl ->
            // Their image server returns HTTP 403 for hidden files that starts
            // with a dot in the file name. To avoid download errors, these are removed.
            imageUrl
                .removeSuffix("/")
                .substringAfterLast("/")
                .startsWith(".")
        }
>>>>>>> upstream/master
            .mapIndexed { i, url ->
                Page(i, imageUrl = if (url.startsWith("http")) url else "$apiUrl/$url")
            }
    }

    override fun fetchImageUrl(page: Page): Observable<String> = Observable.just(page.imageUrl!!)

    override fun imageUrlParse(response: Response): String = ""

    override fun imageRequest(page: Page): Request {
        val imageHeaders = headersBuilder()
            .add("Accept", ACCEPT_IMAGE)
            .build()

        return GET(page.imageUrl!!, imageHeaders)
    }

    protected open fun getStatusList(): List<Status> = listOf(
        Status(intl.statusOngoing, "Ongoing"),
        Status(intl.statusOnHiatus, "Hiatus"),
        Status(intl.statusDropped, "Dropped"),
    )

    protected open fun getSortProperties(): List<SortProperty> = listOf(
        SortProperty(intl.sortByTitle, "title"),
        SortProperty(intl.sortByViews, "total_views"),
        SortProperty(intl.sortByLatest, "latest"),
        SortProperty(intl.sortByRecentlyAdded, "recently_added"),
    )

    protected open fun getGenreList(): List<Genre> = emptyList()

    protected open fun fetchAllTitles() {
        if (!seriesSlugMap.isNullOrEmpty() || !fetchAllTitles) {
            return
        }

        val result = runCatching {
            var hasNextPage = true
            var page = 1
            val tempMap = mutableMapOf<String, HeanCmsTitle>()

            while (hasNextPage) {
                val response = client.newCall(allTitlesRequest(page)).execute()
                val json = response.body.string()

                if (json.startsWith("{")) {
                    val result = json.parseAs<HeanCmsQuerySearchDto>()
                    tempMap.putAll(parseAllTitles(result.data))
                    hasNextPage = result.meta?.hasNextPage ?: false
                    page++
                } else {
                    val result = json.parseAs<List<HeanCmsSeriesDto>>()
                    tempMap.putAll(parseAllTitles(result))
                    hasNextPage = false
                }
            }

            tempMap.toMap()
        }

        seriesSlugMap = result.getOrNull()
    }

    protected open fun allTitlesRequest(page: Int): Request {
        val payloadObj = HeanCmsQuerySearchPayloadDto(
            page = page,
            order = "desc",
            orderBy = "total_views",
            type = "Comic",
        )

        val payload = json.encodeToString(payloadObj).toRequestBody(JSON_MEDIA_TYPE)

        val apiHeaders = headersBuilder()
            .add("Accept", ACCEPT_JSON)
            .add("Content-Type", payload.contentType().toString())
            .build()

        return POST("$apiUrl/series/querysearch", apiHeaders, payload)
    }

    protected open fun parseAllTitles(result: List<HeanCmsSeriesDto>): Map<String, HeanCmsTitle> {
        return result
            .filter { it.type == "Comic" }
            .associateBy(
                keySelector = { it.slug.replace(TIMESTAMP_REGEX, "") },
                valueTransform = {
                    HeanCmsTitle(
                        slug = it.slug,
                        thumbnailFileName = it.thumbnail,
                        status = it.status?.toStatus() ?: SManga.UNKNOWN,
                    )
                },
            )
    }

<<<<<<< HEAD
=======
    /**
     * Used to store the current slugs for sources that change it periodically and for the
     * search that doesn't return the thumbnail URLs.
     */
    data class HeanCmsTitle(val slug: String, val thumbnailFileName: String, val status: Int)

    /**
     * Used to specify the strategy to use when fetching the slug for a manga.
     * This is needed because some sources change the slug periodically.
     * [NONE]: Use series_slug without changes.
     * [ID]: Use series_id to fetch the slug from the API.
     * IMPORTANT: [ID] is only available in the new query endpoint.
     * [FETCH_ALL]: Convert the slug to a permanent slug by removing the timestamp.
     * At extension start, all the slugs are fetched and stored in a map.
     */
    enum class SlugStrategy {
        NONE, ID, FETCH_ALL
    }

    private fun String.toPermSlugIfNeeded(): String {
        return if (slugStrategy != SlugStrategy.NONE) {
            this.replace(TIMESTAMP_REGEX, "")
        } else {
            this
        }
    }

    protected open fun getStatusList(): List<Status> = listOf(
        Status(intl.statusAll, "All"),
        Status(intl.statusOngoing, "Ongoing"),
        Status(intl.statusOnHiatus, "Hiatus"),
        Status(intl.statusDropped, "Dropped"),
    )

    protected open fun getSortProperties(): List<SortProperty> = listOf(
        SortProperty(intl.sortByTitle, "title"),
        SortProperty(intl.sortByViews, "total_views"),
        SortProperty(intl.sortByLatest, "latest"),
        SortProperty(intl.sortByCreatedAt, "created_at"),
    )

    protected open fun getGenreList(): List<Genre> = emptyList()

>>>>>>> upstream/master
    override fun getFilterList(): FilterList {
        val genres = getGenreList()

        val filters = listOfNotNull(
            Filter.Header(intl.filterWarning),
            StatusFilter(intl.statusFilterTitle, getStatusList()),
            SortByFilter(intl.sortByFilterTitle, getSortProperties()),
            GenreFilter(intl.genreFilterTitle, genres).takeIf { genres.isNotEmpty() },
        )

        return FilterList(filters)
    }

    private inline fun <reified T> Response.parseAs(): T = use {
        it.body.string().parseAs()
    }

    private inline fun <reified T> String.parseAs(): T = json.decodeFromString(this)

    private inline fun <reified R> List<*>.firstInstanceOrNull(): R? =
        filterIsInstance<R>().firstOrNull()

<<<<<<< HEAD
    /**
     * Used to store the current slugs for sources that change it periodically and for the
     * search that doesn't return the thumbnail URLs.
     */
    data class HeanCmsTitle(val slug: String, val thumbnailFileName: String, val status: Int)
=======
    protected var SharedPreferences.slugMap: MutableMap<String, String>
        get() {
            val jsonMap = getString(PREF_URL_MAP_SLUG, "{}")!!
            val slugMap = runCatching { json.decodeFromString<Map<String, String>>(jsonMap) }
            return slugMap.getOrNull()?.toMutableMap() ?: mutableMapOf()
        }
        set(newSlugMap) {
            edit()
                .putString(PREF_URL_MAP_SLUG, json.encodeToString(newSlugMap))
                .apply()
        }
>>>>>>> upstream/master

    private val SharedPreferences.showPaidChapters: Boolean
        get() = getBoolean(SHOW_PAID_CHAPTERS_PREF, SHOW_PAID_CHAPTERS_DEFAULT)

    companion object {
        private const val ACCEPT_IMAGE = "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
        private const val ACCEPT_JSON = "application/json, text/plain, */*"

        private val JSON_MEDIA_TYPE = "application/json".toMediaType()

        val TIMESTAMP_REGEX = "-\\d+$".toRegex()

        const val SEARCH_PREFIX = "slug:"
<<<<<<< HEAD
=======

        private const val PREF_URL_MAP_SLUG = "pref_url_map"

        private const val SHOW_PAID_CHAPTERS_PREF = "pref_show_paid_chap"
        private const val SHOW_PAID_CHAPTERS_DEFAULT = false
>>>>>>> upstream/master
    }
}
