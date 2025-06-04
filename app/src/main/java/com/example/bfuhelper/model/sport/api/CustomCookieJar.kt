package com.example.bfuhelper.model.sport.api

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

// CustomCookieJar.kt
class CustomCookieJar : CookieJar {
    private val TAG = "CustomCookieJar"
    private val cookieStore: ConcurrentHashMap<String, MutableList<Cookie>> = ConcurrentHashMap()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val domain = url.host
        Log.d(TAG, "Saving cookies from response for domain: $domain, URL: $url")
        val existingCookies = cookieStore.getOrPut(domain) { mutableListOf() }

        // Создаем временный список для добавления новых кук, чтобы избежать ConcurrentModificationException
        val cookiesToAdd = mutableListOf<Cookie>()

        cookies.forEach { newCookie ->
            // Удаляем старые куки с тем же именем и путем, если они есть
            existingCookies.removeAll { existingCookie ->
                existingCookie.name == newCookie.name && existingCookie.path == newCookie.path
            }
            // Добавляем новую куку
            cookiesToAdd.add(newCookie)
        }
        existingCookies.addAll(cookiesToAdd)

        // Можно убрать эту строку, так как existingCookies уже является ссылкой на список в ConcurrentHashMap
        // cookieStore[domain] = existingCookies
        cookies.forEach {
            Log.d(TAG, "  Saved: Name=${it.name}, Value=${it.value}, Domain=${it.domain}, Path=${it.path}, Expires=${it.expiresAt}")
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val allMatchingCookies = mutableListOf<Cookie>()
        Log.d(TAG, "Loading cookies for request to domain: ${url.host}, URL: $url")

        // Итерируем по всем доменам, для которых у нас есть куки
        cookieStore.forEach { (domainInStore, storedCookies) ->
            // Теперь фильтруем куки, проверяя, подходят ли они для текущего URL
            storedCookies.filter { cookie ->
                cookie.matches(url) && !cookie.isExpired()
            }.forEach {
                allMatchingCookies.add(it)
            }
        }

        // Убираем дубликаты (например, если куки с одним именем были установлены для разных, но подходящих путей/доменов)
        val distinctCookies = allMatchingCookies.distinctBy { it.name + it.path }

        distinctCookies.forEach {
            Log.d(TAG, "  Loaded: Name=${it.name}, Value=${it.value}, Domain=${it.domain}, Path=${it.path}, Expires=${it.expiresAt}")
        }
        return distinctCookies
    }

    // Вспомогательная функция для Cookie, чтобы определить, подходит ли он для URL
    // Эта функция OkHttp Cookie.matches(HttpUrl url) уже учитывает домен, поддомены и пути
    // private fun Cookie.matches(url: HttpUrl): Boolean {
    //    return (this.domain == url.host || (this.domain.startsWith(".") && url.host.endsWith(this.domain))) && // Домен
    //            url.encodedPath.startsWith(this.path) // Путь
    // }

    // Вспомогательная функция для проверки истечения срока действия куки
    private fun Cookie.isExpired(): Boolean {
        return expiresAt < System.currentTimeMillis()
    }
}