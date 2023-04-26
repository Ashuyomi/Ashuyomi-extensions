package eu.kanade.tachiyomi.extension.all.comickfun

import kotlinx.serialization.Serializable

@Serializable
data class Manga(
    val hid: String,
    val title: String,
    val md_covers: List<MDcovers>,
)

@Serializable
data class MDcovers(
    val b2key: String?,
)

@Serializable
data class MangaDetails(
    val comic: Comic,
    val artists: Array<Artist>,
    val authors: Array<Author>,
    val genres: Array<Genre>,
)

@Serializable
data class Comic(
    val hid: String,
    val title: String,
    val desc: String = "N/A",
    val status: Int,
    val md_covers: List<MDcovers>,
)

@Serializable
data class Artist(
    val name: String,
    val slug: String,
)

@Serializable
data class Author(
    val name: String,
    val slug: String,
)

@Serializable
data class Genre(
    val slug: String,
    val name: String,
)

@Serializable
data class ChapterList(
    val chapters: MutableList<Chapter>,
    val total: Int,
)

@Serializable
data class Chapter(
    val hid: String,
    val title: String = "",
    val created_at: String = "",
    val chap: String = "",
    val vol: String = "",
    val group_name: Array<String> = arrayOf(""),
)

@Serializable
data class PageList(
    val chapter: ChapterPageData,
)

@Serializable
data class ChapterPageData(
    val images: Array<Page>,
)

@Serializable
data class Page(
    val url: String? = null,
)
