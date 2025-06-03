package com.example.bfuhelper.model.sport.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Интерфейс для взаимодействия с API аутентификации и получения данных о спортивной активности.
 * Определяет методы для выполнения HTTP-запросов, таких как получение страницы входа,
 * аутентификация пользователя и получение страницы статистики посещений.
 */
interface AuthApiService {
    /**
     * Выполняет GET-запрос для получения HTML-страницы входа.
     * Эта страница используется для извлечения CSRF-токена, необходимого для последующих POST-запросов.
     *
     * @return [Response] с [ResponseBody], содержащим HTML-код страницы входа.
     */
    @GET("/login") // TODO: Проверить и исправить эндпоинт, если он неверен.
    suspend fun getLoginPage(): Response<ResponseBody>

    /**
     * Выполняет POST-запрос для аутентификации пользователя.
     * Данные для входа (логин, пароль и CSRF-токен) отправляются в теле запроса в кодировке формы.
     *
     * @param username Логин пользователя.
     * @param password Пароль пользователя.
     * @param csrfToken CSRF-токен, полученный со страницы входа, для защиты от межсайтовой подделки запросов.
     * @return [Response] с [ResponseBody], который может содержать HTML или быть пустым
     * в случае успешной аутентификации (например, при редиректе).
     */
    @FormUrlEncoded
    @POST("login") // Эндпоинт для POST-запроса авторизации.
    suspend fun login(
        @Field("login") username: String,
        @Field("password") password: String,
        @Field("csrfmiddlewaretoken") csrfToken: String
    ): Response<ResponseBody>

    /**
     * Выполняет GET-запрос для получения HTML-страницы со статистикой спортивных посещений.
     * Этот запрос обычно выполняется после успешной аутентификации.
     *
     * @return [Response] с [ResponseBody], содержащим HTML-код страницы статистики.
     */
    @GET("/student/activity") // TODO: Проверить и исправить эндпоинт, если он неверен.
    suspend fun getSportStatsPage(): Response<ResponseBody>
}