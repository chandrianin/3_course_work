package com.example.bfuhelper.model.sport.api

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference // Для потокобезопасного хранения токена

/**
 * [CsrfInterceptor] — это перехватчик OkHttp, предназначенный для добавления CSRF-токена
 * в заголовки исходящих HTTP-запросов. Это особенно полезно для защиты от CSRF-атак
 * в веб-приложениях, где токен требуется для каждого запроса, изменяющего состояние.
 *
 * @property csrfTokenProvider Лямбда-функция, которая предоставляет текущий CSRF-токен.
 * Эта функция будет вызываться для получения токена перед каждым запросом.
 */
class CsrfInterceptor(private val csrfTokenProvider: () -> String?) : Interceptor {
    /**
     * Перехватывает HTTP-запрос и при необходимости добавляет заголовок с CSRF-токеном.
     *
     * @param chain Цепочка перехватчиков, через которую проходит запрос.
     * @return [Response] — ответ на модифицированный или оригинальный запрос.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val csrfToken = csrfTokenProvider()

        // Если CSRF-токен доступен, создаем новый запрос с добавленным заголовком.
        // Заголовок может быть "Ajax-Token" или "X-CSRFToken" в зависимости от требований сервера.
        return if (csrfToken != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Ajax-Token", csrfToken) // TODO: Проверить и исправить имя заголовка, например, на "X-CSRFToken", если это необходимо для сервера.
                .build()
            chain.proceed(newRequest)
        } else {
            // Если токен не доступен, продолжаем выполнение оригинального запроса без изменений.
            chain.proceed(originalRequest)
        }
    }
}