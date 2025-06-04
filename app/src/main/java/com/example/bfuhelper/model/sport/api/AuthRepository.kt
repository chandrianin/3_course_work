package com.example.bfuhelper.model.sport.api

import android.util.Log
import androidx.core.net.ParseException
import com.example.bfuhelper.model.sport.Month
import com.example.bfuhelper.model.sport.SportItem
import com.example.bfuhelper.model.sport.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/**
 * Репозиторий для управления аутентификацией и получения данных о спортивных посещениях.
 * Этот класс отвечает за взаимодействие с API для входа в систему, получения CSRF-токенов
 * и парсинга HTML-страниц для извлечения информации о спортивной активности.
 *
 * @property _username Логин пользователя, может быть null до установки.
 * @property _password Пароль пользователя, может быть null до установки.
 * @property _csrfToken Атомарная ссылка для потокобезопасного хранения CSRF-токена.
 * @property _authToken Атомарная ссылка для потокобезопасного хранения токена сессии после авторизации.
 * @property retrofitClient Лениво инициализируемый экземпляр [RetrofitClient] для создания сервисов API.
 * @property authApiService Лениво инициализируемый экземпляр [AuthApiService] для выполнения HTTP-запросов.
 */
class AuthRepository {

    private var _username: String? = null
    private var _password: String? = null

    private val _csrfToken = AtomicReference<String?>(null)
    private val _authToken = AtomicReference<String?>(null)

    private val retrofitClient: RetrofitClient by lazy { RetrofitClient { _csrfToken.get() } }
    private val authApiService: AuthApiService by lazy { retrofitClient.authApiService }

    /**
     * Конструктор класса [AuthRepository].
     *
     * @param username Начальное значение логина пользователя. По умолчанию null.
     * @param password Начальное значение пароля пользователя. По умолчанию null.
     */
    constructor(username: String? = null, password: String? = null) {
        _username = username
        _password = password
    }

    /**
     * Устанавливает или обновляет учетные данные пользователя (логин и пароль).
     *
     * @param username Новый логин пользователя.
     * @param password Новый пароль пользователя.
     */
    fun setCredentials(username: String, password: String) {
        _username = username
        _password = password
        // TODO save in memory
    }

    /**
     * Асинхронно получает CSRF-токен из HTML-страницы входа.
     * Этот токен необходим для отправки авторизационных данных.
     *
     * @return Полученный CSRF-токен в виде [String] или null, если произошла ошибка или токен не найден.
     */
    private suspend fun fetchCsrfToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.getLoginPage()
                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    Log.i("AuthRepository", "Get login page response: $response")
                    html?.let {
                        val document = Jsoup.parse(it)
                        val csrfInput = document.select("input[name=csrfmiddlewaretoken]").first()
                        val token = csrfInput?.attr("value")
                        _csrfToken.set(token)
                        Log.i("AuthRepository", "CSRF-Token fetched: $token")
                        token
                    }
                } else {
                    Log.e("AuthRepository", "Failed to get login page: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error fetching CSRF token: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Выполняет асинхронную авторизацию пользователя.
     * Сначала пытается получить CSRF-токен, если он еще не установлен,
     * затем отправляет логин, пароль и токен на сервер.
     *
     * @return [Result.success] с [Unit] в случае успешной авторизации,
     * или [Result.failure] с [Exception] в случае ошибки (например, неверные учетные данные,
     * отсутствие токена или сетевая ошибка).
     */
    suspend fun login(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val username = _username
            val password = _password

            if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Username or password not set."))
            }

            val csrfToken = _csrfToken.get() ?: fetchCsrfToken()

            if (csrfToken == null) {
                return@withContext Result.failure(Exception("Failed to obtain CSRF token."))
            }

            try {
                val response = authApiService.login(username, password, csrfToken)
                Log.i("AuthRepository", "Sent login POST-request")
                if (response.isSuccessful) {
                    Log.i("AuthRepository", "Login successful!")
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthRepository", "Login failed: ${response.code()} - $errorBody")
                    Result.failure(Exception("Login failed: ${response.message()} - $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Login exception: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    /**
     * Асинхронно получает и парсит данные о спортивных посещениях.
     * Запрашивает HTML-страницу статистики и извлекает из нее список [SportItem].
     *
     * @return [Result.success] со списком [SportItem] в случае успешного получения и парсинга данных,
     * или [Result.failure] с [Exception] в случае ошибки (например, сетевая ошибка,
     * пустой ответ или неудача парсинга).
     */
    suspend fun getSportVisits(): Result<List<SportItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.getSportStatsPage()
                Log.i("AuthRepository", "Get statistic page response: $response")
                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    html?.let {
                        val sportItems = parseSportStatsHtml(it)
                        Result.success(sportItems)
                    } ?: Result.failure(Exception("Empty response body for sport stats."))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "AuthRepository",
                        "Failed to get sport stats: ${response.code()} - $errorBody"
                    )
                    Result.failure(Exception("Failed to get sport stats: ${response.message()} - ${errorBody}"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error fetching sport stats: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    /**
     * Парсит HTML-строку, извлекая из нее информацию о спортивных посещениях и преобразуя ее в список [SportItem].
     * Метод ищет таблицу на странице и извлекает данные из ее строк, пропуская заголовок.
     *
     * @param html HTML-код страницы со статистикой посещений.
     * @return Список объектов [SportItem], представляющих данные о спортивных посещениях.
     * Возвращает пустой список, если таблица не найдена или данные не могут быть распарсены.
     */
    private fun parseSportStatsHtml(html: String): List<SportItem> {
        val document = Jsoup.parse(html)
        val sportItems = mutableListOf<SportItem>()

        val dateFormatWithDot = SimpleDateFormat("MMM. d,yyyy", Locale("en", "US"))
        val dateFormatWithoutDot = SimpleDateFormat("MMM d,yyyy", Locale("en", "US"))

        val table = document.select("table").firstOrNull() ?: return emptyList()

        val rows = table.select("tr").drop(1)
//        Log.d("AuthRepository", rows.toString())
        for (row in rows) {
            val columns = row.select("td")
            if (columns.size >= 5) {
                val dateStr = columns[1].text().trim()
                val statusStr = columns[4].text().trim()

                if (dateStr.isBlank() && statusStr == "Будущее занятие") {
                    Log.d("Parser", "Skipping empty future item")
                    continue // Переходим к следующей строке
                }

                try {
                    var parsedDate: java.util.Date? = null
                    try {
                        parsedDate = dateFormatWithDot.parse(dateStr)
                    } catch (e: Exception) {
                        try {
                            parsedDate = dateFormatWithoutDot.parse(dateStr)
                        } catch (e2: ParseException) {
                            // Если и без точки не получилось, parsedDate останется null
                            Log.e(
                                "Parser",
                                "Both date formats failed for: \"$dateStr\". Error: ${e2.message}"
                            )
                        }
                    }
//                    parsedDate = try {
//                        // Попытка парсинга с точкой
//                        dateFormatWithDot.parse(dateStr)
//                    } catch (e: ParseException) {
//                        // Если с точкой не получилось, пробуем без точки
//                        dateFormatWithoutDot.parse(dateStr)
//                    }
                    parsedDate?.let {
                        val calendar = java.util.Calendar.getInstance().apply {
                            time = parsedDate
                        }
                        val month = Month.entries[calendar.get(java.util.Calendar.MONTH)]
                        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH).toByte()

                        val status = when (statusStr) {
                            "Присутствовал" -> Status.Visit
                            "Отсутствовал на занятии по уважительной причине" -> Status.Disease
                            "Будущее занятие" -> Status.Future
                            "Отсутствовал на занятии" -> Status.Absence
                            else -> Status.Future
                        }
                        sportItems.add(SportItem(month, day, status))
                        Log.i("AuthRepository", "Parsed date: $month $day, status: $status")
                    } ?: run {
                        Log.e(
                            "Parser",
                            "Error parsing row: $dateStr, $statusStr. Error: Date parsing failed after trying both formats."
                        )
                    }

                } catch (e: Exception) {
                    Log.e("Parser", "Error parsing row: $dateStr, $statusStr. Error: ${e.message}")
                }
            }
        }
        return sportItems
    }
}