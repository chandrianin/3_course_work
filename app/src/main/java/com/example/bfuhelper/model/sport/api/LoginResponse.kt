package com.example.bfuhelper.model.sport.api

/**
 * Data class, представляющий структуру ответа сервера после успешной аутентификации.
 * Включает в себя токен авторизации и опциональные дополнительные поля, такие как ID пользователя,
 * срок действия токена и refresh-токен.
 *
 * @property token Основной токен авторизации (например, JWT), необходимый для доступа к защищенным ресурсам.
 * @property userId Опциональный идентификатор пользователя, связанный с токеном.
 * @property expiresIn Опциональный срок действия токена в секундах или миллисекундах.
 * @property refreshToken Опциональный токен, используемый для получения нового [token] после истечения его срока действия,
 * без необходимости повторной полной аутентификации.
 */
data class LoginResponse(
    val token: String,
    val userId: String? = null,
    val expiresIn: Long? = null,
    val refreshToken: String? = null
)