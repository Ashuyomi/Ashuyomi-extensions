package eu.kanade.tachiyomi.extension.ar.gmanga

import android.annotation.SuppressLint
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.text.ParseException
import java.text.SimpleDateFormat

class GmangaFilters() {

    companion object {

        fun getFilterList() = FilterList(
            MangaTypeFilter(),
            OneShotFilter(),
            StoryStatusFilter(),
            TranslationStatusFilter(),
            ChapterCountFilter(),
            DateRangeFilter(),
            CategoryFilter(),
        )

        fun buildSearchPayload(page: Int, query: String = "", filters: FilterList): JsonObject {
            val mangaTypeFilter = filters.findInstance<MangaTypeFilter>()!!
            val oneShotFilter = filters.findInstance<OneShotFilter>()!!
            val storyStatusFilter = filters.findInstance<StoryStatusFilter>()!!
            val translationStatusFilter = filters.findInstance<TranslationStatusFilter>()!!
            val chapterCountFilter = filters.findInstance<ChapterCountFilter>()!!
            val dateRangeFilter = filters.findInstance<DateRangeFilter>()!!
            val categoryFilter = filters.findInstance<CategoryFilter>()!!

            return buildJsonObject {
                oneShotFilter.state.first().let {
                    putJsonObject("oneshot") {
                        when {
                            it.isIncluded() -> put("value", true)
                            it.isExcluded() -> put("value", false)
                            else -> put("value", JsonNull)
                        }
                    }
                }

                put("title", query)
                put("page", page)
                putJsonObject("manga_types") {
                    putJsonArray("include") {
                        mangaTypeFilter.state.filter { it.isIncluded() }.map { it.id }.forEach { add(it) }
                    }

                    putJsonArray("exclude") {
                        mangaTypeFilter.state.filter { it.isExcluded() }.map { it.id }.forEach { add(it) }
                    }
                }
                putJsonObject("story_status") {
                    putJsonArray("include") {
                        storyStatusFilter.state.filter { it.isIncluded() }.map { it.id }.forEach { add(it) }
                    }

                    putJsonArray("exclude") {
                        storyStatusFilter.state.filter { it.isExcluded() }.map { it.id }.forEach { add(it) }
                    }
                }
                putJsonObject("translation_status") {
                    putJsonArray("include") {
                        translationStatusFilter.state.filter { it.isIncluded() }.map { it.id }.forEach { add(it) }
                    }

                    putJsonArray("exclude") {
                        translationStatusFilter.state.filter { it.isExcluded() }.map { it.id }.forEach { add(it) }
                    }
                }
                putJsonObject("categories") {
                    putJsonArray("include") {
                        add(JsonNull) // always included, maybe to avoid shifting index in the backend
                        categoryFilter.state.filter { it.isIncluded() }.map { it.id }.forEach { add(it) }
                    }

                    putJsonArray("exclude") {
                        categoryFilter.state.filter { it.isExcluded() }.map { it.id }.forEach { add(it) }
                    }
                }
                putJsonObject("chapters") {
                    putFromValidatingTextFilter(
                        chapterCountFilter.state.first {
                            it.id == FILTER_ID_MIN_CHAPTER_COUNT
                        },
                        "min",
                        ERROR_INVALID_MIN_CHAPTER_COUNT,
                        "",
                    )

                    putFromValidatingTextFilter(
                        chapterCountFilter.state.first {
                            it.id == FILTER_ID_MAX_CHAPTER_COUNT
                        },
                        "max",
                        ERROR_INVALID_MAX_CHAPTER_COUNT,
                        "",
                    )
                }
                putJsonObject("dates") {
                    putFromValidatingTextFilter(
                        dateRangeFilter.state.first {
                            it.id == FILTER_ID_START_DATE
                        },
                        "start",
                        ERROR_INVALID_START_DATE,
                    )

                    putFromValidatingTextFilter(
                        dateRangeFilter.state.first {
                            it.id == FILTER_ID_END_DATE
                        },
                        "end",
                        ERROR_INVALID_END_DATE,
                    )
                }
            }
        }

        // filter IDs
        private const val FILTER_ID_ONE_SHOT = "oneshot"
        private const val FILTER_ID_START_DATE = "start"
        private const val FILTER_ID_END_DATE = "end"
        private const val FILTER_ID_MIN_CHAPTER_COUNT = "min"
        private const val FILTER_ID_MAX_CHAPTER_COUNT = "max"

        // error messages
        private const val ERROR_INVALID_START_DATE = "?????????? ?????????? ?????? ????????"
        private const val ERROR_INVALID_END_DATE = " ?????????? ?????????? ?????? ????????"
        private const val ERROR_INVALID_MIN_CHAPTER_COUNT = "???????? ???????????? ???????? ???????????? ?????? ????????"
        private const val ERROR_INVALID_MAX_CHAPTER_COUNT = "???????? ???????????? ???????? ???????????? ?????? ????????"

        private class MangaTypeFilter() : Filter.Group<TagFilter>(
            "??????????",
            listOf(
                TagFilter("1", "??????????????", TriState.STATE_INCLUDE),
                TagFilter("2", "??????????", TriState.STATE_INCLUDE),
                TagFilter("3", "??????????", TriState.STATE_INCLUDE),
                TagFilter("4", "??????????", TriState.STATE_INCLUDE),
                TagFilter("5", "??????????", TriState.STATE_INCLUDE),
                TagFilter("6", "????????", TriState.STATE_INCLUDE),
                TagFilter("7", "??????????????????", TriState.STATE_INCLUDE),
                TagFilter("8", "??????????", TriState.STATE_INCLUDE),
            ),
        )

        private class OneShotFilter() : Filter.Group<TagFilter>(
            "????????????",
            listOf(
                TagFilter(FILTER_ID_ONE_SHOT, "??????", TriState.STATE_EXCLUDE),
            ),
        )

        private class StoryStatusFilter() : Filter.Group<TagFilter>(
            "???????? ??????????",
            listOf(
                TagFilter("2", "????????????"),
                TagFilter("3", "????????????"),
            ),
        )

        private class TranslationStatusFilter() : Filter.Group<TagFilter>(
            "???????? ??????????????",
            listOf(
                TagFilter("0", "????????????"),
                TagFilter("1", "????????????"),
                TagFilter("2", "????????????"),
                TagFilter("3", "?????? ????????????", TriState.STATE_EXCLUDE),
            ),
        )

        private class ChapterCountFilter() : Filter.Group<IntFilter>(
            "?????? ????????????",
            listOf(
                IntFilter(FILTER_ID_MIN_CHAPTER_COUNT, "?????? ??????????"),
                IntFilter(FILTER_ID_MAX_CHAPTER_COUNT, "?????? ????????????"),
            ),
        )

        private class DateRangeFilter() : Filter.Group<DateFilter>(
            "?????????? ??????????",
            listOf(
                DateFilter(FILTER_ID_START_DATE, "?????????? ??????????"),
                DateFilter(FILTER_ID_END_DATE, "?????????? ????????????????"),
            ),
        )

        private class CategoryFilter() : Filter.Group<TagFilter>(
            "??????????????????",
            listOf(
                TagFilter("1", "??????????"),
                TagFilter("2", "????????"),
                TagFilter("3", "???????????? ????????????????"),
                TagFilter("4", "???????????? ??????????????"),
                TagFilter("5", "??????????"),
                TagFilter("6", "????????????"),
                TagFilter("7", "??????????????"),
                TagFilter("8", "??????????"),
                TagFilter("9", "????????"),
                TagFilter("10", "????????"),
                TagFilter("11", "???????? ????????"),
                TagFilter("12", "??????????"),
                TagFilter("13", "??????"),
                TagFilter("14", "??????????????"),
                TagFilter("15", "??????????"),
                TagFilter("16", "??????????????"),
                TagFilter("17", "??????"),
                TagFilter("18", "??????????"),
                TagFilter("19", "????????"),
                TagFilter("20", "??????????"),
                TagFilter("21", "??????"),
                TagFilter("22", "????????"),
                TagFilter("23", "???????? ????????"),
                TagFilter("24", "?????? ??????????"),
                TagFilter("25", "????????????"),
                TagFilter("26", "????????"),
                TagFilter("27", "????????????"),
                TagFilter("28", "?????????? ????????????"),
                TagFilter("29", "??????????????"),
                TagFilter("30", "????????????"),
                TagFilter("31", "????????"),
                TagFilter("32", "??????????"),
                TagFilter("33", "????????"),
                TagFilter("34", "????????"),
                TagFilter("35", "????????"),
                TagFilter("38", "??????-??????"),
                TagFilter("39", "??????????????"),
            ),
        )

        private const val DATE_FILTER_PATTERN = "yyyy/MM/dd"

        @SuppressLint("SimpleDateFormat")
        private val DATE_FITLER_FORMAT = SimpleDateFormat(DATE_FILTER_PATTERN).apply {
            isLenient = false
        }

        private fun SimpleDateFormat.isValid(date: String): Boolean {
            return try {
                this.parse(date)
                true
            } catch (e: ParseException) {
                false
            }
        }

        private fun JsonObjectBuilder.putFromValidatingTextFilter(
            filter: ValidatingTextFilter,
            property: String,
            invalidErrorMessage: String,
            default: String? = null,
        ) {
            filter.let {
                when {
                    it.state == "" -> if (default == null) {
                        put(property, JsonNull)
                    } else {
                        put(property, default)
                    }
                    it.isValid() -> put(property, it.state)
                    else -> throw Exception(invalidErrorMessage)
                }
            }
        }

        private inline fun <reified T> Iterable<*>.findInstance() = find { it is T } as? T

        private class TagFilter(val id: String, name: String, state: Int = STATE_IGNORE) : Filter.TriState(name, state)

        private abstract class ValidatingTextFilter(name: String) : Filter.Text(name) {
            abstract fun isValid(): Boolean
        }

        private class DateFilter(val id: String, name: String) : ValidatingTextFilter("($DATE_FILTER_PATTERN) $name)") {
            override fun isValid(): Boolean = DATE_FITLER_FORMAT.isValid(this.state)
        }

        private class IntFilter(val id: String, name: String) : ValidatingTextFilter(name) {
            override fun isValid(): Boolean = state.toIntOrNull() != null
        }
    }
}
