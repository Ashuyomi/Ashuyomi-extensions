package eu.kanade.tachiyomi.extension.id.mangasusu

import app.cash.quickjs.QuickJs
import eu.kanade.tachiyomi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class Mangasusu : MangaThemesia("Mangasusu", "https://mangasusuku.xyz", "id", "/komik") {
    override val client by lazy {
        super.client.newBuilder().addInterceptor(::sucuriInterceptor).build()
    }

    // Taken from es/ManhwasNet
    private fun sucuriInterceptor(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            // Try to clear cookies and retry
            client.cookieJar.saveFromResponse(url, emptyList())
            val clearHeaders = request.headers.newBuilder().removeAll("Cookie").build()
            chain.proceed(request.newBuilder().headers(clearHeaders).build())
        }
        if (response.headers["x-sucuri-cache"].isNullOrEmpty() && response.headers["x-sucuri-id"] != null && url.toString().startsWith(baseUrl)) {
            val script = response.use { it.asJsoup() }.selectFirst("script")?.data()

            if (script != null) {
                val patchedScript = script.split("(r)")[0].dropLast(1) + "r=r.replace('document.cookie','cookie');"
                QuickJs.create().use {
                    val result = (it.evaluate(patchedScript) as String)
                        .replace("location.", "")
                        .replace("reload();", "")
                    val sucuriCookie = (it.evaluate(result) as String).split("=", limit = 2)
                    val cookieName = sucuriCookie.first()
                    val cookieValue = sucuriCookie.last().replace(";path", "")
                    client.cookieJar.saveFromResponse(url, listOf(Cookie.parse(url, "$cookieName=$cookieValue")!!))
                }
                val newResponse = chain.proceed(request)
                if (!newResponse.headers["x-sucuri-cache"].isNullOrEmpty()) return newResponse
            }
            throw IOException("Situs yang dilindungi - Buka di WebView untuk mencoba membuka blokir.")
        }
        return response
    }
}
