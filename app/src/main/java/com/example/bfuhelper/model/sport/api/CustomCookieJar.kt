package com.example.bfuhelper.model.sport.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class CustomCookieJar : CookieJar {
    // ConcurrentHashMap для потокобезопасности и хранения по домену
    private val cookieStore: ConcurrentHashMap<String, MutableList<Cookie>> = ConcurrentHashMap()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val domain = url.host // Ключ - домен
        val existingCookies = cookieStore.getOrPut(domain) { mutableListOf() }
        // Удаляем старые куки с такими же именами и путями, добавляем новые
        val newCookies = cookies.filter { newCookie ->
            existingCookies.none { existingCookie ->
                existingCookie.name == newCookie.name && existingCookie.path == newCookie.path
            }
        }.toMutableList()
        existingCookies.removeAll { existingCookie ->
            cookies.any { newCookie ->
                existingCookie.name == newCookie.name && existingCookie.path == newCookie.path
            }
        }
        existingCookies.addAll(newCookies)
        cookieStore[domain] = existingCookies // Обновляем
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val domain = url.host
        val currentCookies = cookieStore[domain]?.filter { cookie ->
            cookie.matches(url) && !cookie.isExpired() // Проверяем, что куки подходит для URL и не истек
        }?.toMutableList() ?: mutableListOf()

        // Проверяем куки для родительских доменов
        val parts = domain.split(".")
        for (i in parts.indices) {
            val subDomain = parts.subList(i, parts.size).joinToString(".")
            if (subDomain != domain) {
                cookieStore[subDomain]?.filter { cookie ->
                    cookie.matches(url) && !cookie.isExpired() // Проверяем, что куки подходит для URL и не истек
                }?.forEach { currentCookies.add(it) }
            }
        }
        return currentCookies.distinctBy { it.name + it.path } // Убираем дубликаты
    }

    // Вспомогательная функция для Cookie, чтобы определить, подходит ли он для URL
    private fun Cookie.matches(url: HttpUrl): Boolean {
        return (this.domain == url.host || (this.domain.startsWith(".") && url.host.endsWith(this.domain))) && // Домен
                url.encodedPath.startsWith(this.path) // Путь
    }

    // Вспомогательная функция для проверки истечения срока действия куки
    private fun Cookie.isExpired(): Boolean {
        return expiresAt < System.currentTimeMillis()
    }
}