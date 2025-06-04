package com.example.bfuhelper.model.sport.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * [RetrofitClient] — это класс, ответственный за настройку и предоставление экземпляров
 * [Retrofit] и связанных с ним API-сервисов.
 * Он конфигурирует [OkHttpClient] для логирования сетевых запросов и ответов,
 * а также настраивает [Retrofit] для работы с базовым URL и конвертером JSON.
 */
class RetrofitClient(private val _csrfProvider: () -> String?) {

    /**
     * Лениво инициализируемый экземпляр [OkHttpClient].
     * Настраивает HTTP-клиент с интерцептором для логирования сетевой активности.
     *
     * Включает [HttpLoggingInterceptor] с уровнем логирования [HttpLoggingInterceptor.Level.BODY]
     * для вывода детальной информации о запросах и ответах в логcat.
     */
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(CsrfInterceptor { _csrfProvider() })
            .cookieJar(CustomCookieJar())
            .build()
    }

    /**
     * Лениво инициализируемый экземпляр [Retrofit].
     * Конфигурирует Retrofit с базовым URL, настроенным [OkHttpClient] и
     * [GsonConverterFactory] для автоматической сериализации/десериализации JSON.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://fc.kantiana.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Лениво инициализируемый экземпляр [AuthApiService].
     * Создает реализацию [AuthApiService] с помощью сконфигурированного [Retrofit] клиента.
     * Этот сервис используется для выполнения запросов, связанных с аутентификацией.
     */
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}