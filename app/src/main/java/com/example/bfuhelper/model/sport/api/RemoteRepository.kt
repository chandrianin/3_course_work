package com.example.bfuhelper.model.sport.api

import android.content.Context
import android.content.SharedPreferences
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
 * Управляет аутентификацией и получением данных о спортивных посещениях.
 *
 * Этот класс отвечает за взаимодействие с API для входа пользователя, получения CSRF-токенов
 * и парсинга HTML-страниц для извлечения информации о спортивной активности.
 *
 * @property _csrfToken Атомарная ссылка для потокобезопасного хранения CSRF-токена.
 * @property retrofitClient Лениво инициализируемый экземпляр [RetrofitClient] для создания сервисов API.
 * @property authApiService Лениво инициализируемый экземпляр [AuthApiService] для выполнения HTTP-запросов.
 */
class RemoteRepository private constructor(context: Context) {
    private val tag = "RemoteRepository"

    private val _csrfToken = AtomicReference<String?>(null)

    var loginResult: Result<Unit> = Result.failure(Exception())
        private set
        get() {
            getPasswordFromPrefs()?.let { }
                ?.let {
                    getUsernameFromPrefs()?.let {
                        return Result.success(Unit)
                    }
                }
            return field
        }

    private val retrofitClient: RetrofitClient by lazy { RetrofitClient { _csrfToken.get() } }
    private val authApiService: AuthApiService by lazy { retrofitClient.authApiService }

    private val INSECURE_PREFS_FILE = "my_app_prefs"
    private val PREF_USERNAME_INSECURE = "login"
    private val PREF_PASSWORD_INSECURE = "password"
    val PREF_CONTROL = "control"
    val PREF_LMS = "LMS"
    val PREF_SPORT_EVENT = "SE"
    val PREF_PHYSICAL_P = "PH_P"

    private val insecurePrefs: SharedPreferences by lazy {
        context.getSharedPreferences(INSECURE_PREFS_FILE, Context.MODE_PRIVATE)
    }

    companion object {
        @Volatile
        private var INSTANCE: RemoteRepository? = null

        fun getInstance(context: Context): RemoteRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RemoteRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Сохраняет предоставленные баллы за спортивную активность в [SharedPreferences].
     *
     * @param control Балл за контрольные упражнения.
     * @param lms Балл за LMS3.
     * @param events Балл за спортивные мероприятия.
     * @param physical Балл за физическую подготовку.
     */
    fun setInfo(control: Int, lms: Int, events: Int, physical: Int) {
        insecurePrefs.edit()
            .putString(PREF_CONTROL, control.toString())
            .putString(PREF_LMS, lms.toString())
            .putString(PREF_SPORT_EVENT, events.toString())
            .putString(PREF_PHYSICAL_P, physical.toString())
            .apply()
    }

    /**
     * Извлекает сохраненные баллы за спортивную активность из [SharedPreferences].
     *
     * @return [Map], где ключи — это категории баллов, а значения — соответствующие им целочисленные баллы.
     * Возвращает 0 для любого ненайденного балла.
     */
    fun getInfo(): Map<String, Int> {
        val scoresMap = mutableMapOf<String, Int>()
        scoresMap[PREF_CONTROL] = insecurePrefs.getString(PREF_CONTROL, "0")?.toInt() ?: 0
        scoresMap[PREF_LMS] = insecurePrefs.getString(PREF_LMS, "0")?.toInt() ?: 0
        scoresMap[PREF_SPORT_EVENT] = insecurePrefs.getString(PREF_SPORT_EVENT, "0")?.toInt() ?: 0
        scoresMap[PREF_PHYSICAL_P] = insecurePrefs.getString(PREF_PHYSICAL_P, "0")?.toInt() ?: 0
        Log.d(tag, "Specific scores map retrieved: $scoresMap")
        return scoresMap
    }

    private fun setCredentialsInPrefs(username: String, password: String) {
        Log.w(tag, "SECURITY WARNING: Saving credentials in unencrypted SharedPreferences.")
        insecurePrefs.edit()
            .putString(PREF_USERNAME_INSECURE, username)
            .putString(PREF_PASSWORD_INSECURE, password)
            .apply()
    }

    private fun getUsernameFromPrefs(): String? {
        Log.w(tag, "SECURITY WARNING: Retrieving username from unencrypted SharedPreferences.")
        return insecurePrefs.getString(PREF_USERNAME_INSECURE, null)
    }

    private fun getPasswordFromPrefs(): String? {
        Log.w(tag, "SECURITY WARNING: Retrieving password from unencrypted SharedPreferences.")
        return insecurePrefs.getString(PREF_PASSWORD_INSECURE, null)
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
                    Log.i(tag, "Get login page response: $response")
                    html?.let {
                        val document = Jsoup.parse(it)
                        val csrfInput = document.select("input[name=csrfmiddlewaretoken]").first()
                        val token = csrfInput?.attr("value")
                        _csrfToken.set(token)
                        Log.i(tag, "CSRF-Token fetched: $token")
                        token
                    }
                } else {
                    Log.e(tag, "Failed to get login page: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching CSRF token: ${e.message}", e)
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
    suspend fun login(username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val csrfToken = _csrfToken.get() ?: fetchCsrfToken()

            if (csrfToken == null) {
                loginResult = Result.failure(Exception("Failed to obtain CSRF token."))
                return@withContext Result.failure(Exception("Failed to obtain CSRF token."))
            }

            try {
                val response = authApiService.login(username, password, csrfToken)
                Log.i(tag, "Sent login POST-request")
                val body = response.body()?.string()

                if (body != null) {
                    val document = Jsoup.parse(body)
                    val errorMessageElement = document.select("h3[style*=color: #f00]")
                        .firstOrNull() // Ищем h3 с красным цветом
                    val specificErrorMessage = errorMessageElement?.text()

                    if (specificErrorMessage != null && specificErrorMessage.contains("Неверный логин или пароль")) {
                        Log.e(
                            tag,
                            "Specific login error: Неверный логин или пароль"
                        )
                        loginResult = Result.failure(Exception("Неверный логин или пароль."))
                        return@withContext Result.failure(Exception("Неверный логин или пароль."))
                    }
                }

                if (response.isSuccessful) {
                    Log.i(tag, "Login successful!")
                    loginResult = Result.success(Unit)
                    setCredentialsInPrefs(username, password)
                    Result.success(Unit)
                } else {
                    loginResult =
                        Result.failure(Exception("Login failed: ${response.message()} (Code: ${response.code()})"))
                    Result.failure(Exception("Login failed: ${response.message()} (Code: ${response.code()})"))
                }
            } catch (e: Exception) {
                Log.e(tag, "Login exception: ${e.message}", e)
                loginResult = Result.failure(Exception("Network error: ${e.message}"))
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    suspend fun login(): Result<Unit> {
        if (loginResult.isSuccess) {
            val username = getUsernameFromPrefs()
            val password = getPasswordFromPrefs()
            if (username != null && password != null) {
                return login(username, password)
            }
        }
        return loginResult
    }

    /**
     * Асинхронно получает и парсит данные о спортивных посещениях.
     *
     * Он запрашивает HTML-страницу со статистикой и извлекает из нее список [SportItem],
     * а также карту конкретных баллов.
     *
     * @return [Result.success] с [Pair], содержащим список [SportItem] и карту
     * баллов, если получение и парсинг данных прошли успешно.
     * Возвращает [Result.failure] с [Exception] в случае ошибки (например, сетевая ошибка,
     * пустой ответ или сбой парсинга).
     */
    suspend fun getSportVisits(): Result<Pair<List<SportItem>, Map<String, Int>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.getSportStatsPage()
                Log.i(tag, "Get statistic page response: $response")
                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    html?.let {
                        val sportItems = parseSportStatsHtml(it)
                        Result.success(sportItems)
                    } ?: Result.failure(Exception("Empty response body for sport stats."))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        tag,
                        "Failed to get sport stats: ${response.code()} - $errorBody"
                    )
                    Result.failure(Exception("Failed to get sport stats: ${response.message()} - ${errorBody}"))
                }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching sport stats: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }

    /**
     * Парсит HTML-строку, извлекая из нее информацию о спортивных посещениях и конкретные баллы,
     * преобразуя ее в список [SportItem] и карту баллов.
     *
     * Метод ищет таблицу на странице и извлекает данные из ее строк, пропуская заголовок.
     * Он также пытается проанализировать баллы из определенного тега `<p>` в HTML.
     *
     * @param html HTML-код страницы со статистикой посещений.
     * @return [Pair], где первый элемент — это список объектов [SportItem], представляющих
     * данные о спортивных посещениях, а второй элемент — это карта конкретных баллов.
     * Возвращает пустой список для [SportItem], если таблица не найдена или данные не могут быть проанализированы.
     * Возвращает пустую карту для баллов, если тег, содержащий баллы, не найден или баллы не могут быть проанализированы.
     */
    private fun parseSportStatsHtml(html: String): Pair<List<SportItem>, Map<String, Int>> {
        val document = Jsoup.parse(html)
        val sportItems = mutableListOf<SportItem>()

        val dateFormatWithDot = SimpleDateFormat("MMM. d,yyyy", Locale("en", "US"))
        val dateFormatWithoutDot = SimpleDateFormat("MMM d,yyyy", Locale("en", "US"))

        val scoresMap = mutableMapOf<String, Int>()
        val infoMap = mutableMapOf<String, Int>()

        val pTagWithScores =
            document.select("p").firstOrNull { it.text().contains("Количество баллов за занятия:") }

        if (pTagWithScores != null) {
            val rawText = pTagWithScores.html().replace("<br>", "|").replace("<BR>", "|")
            val lines = rawText.split("|").map { it.trim() }.filter { it.isNotBlank() }

            val regex = "(.+?):\\s*(\\d+)".toRegex()

            for (line in lines) {
                val matchResult = regex.find(line)
                matchResult?.let {
                    val (key, valueStr) = it.destructured
                    scoresMap[key.trim()] = valueStr.toIntOrNull() ?: 0
                }
            }

            infoMap[PREF_CONTROL] = scoresMap["Количество баллов за контрольные упражнения"] ?: 0
            infoMap[PREF_LMS] = scoresMap["Количество баллов за LMS3"] ?: 0
            infoMap[PREF_SPORT_EVENT] = scoresMap["Количество баллов за СМ"] ?: 0
            infoMap[PREF_PHYSICAL_P] = scoresMap["Количество баллов за ФП"] ?: 0

        } else {
            Log.e(tag, "parseScoresHtml: Could not find the <p> tag containing scores.")
        }

        val table = document.select("table").firstOrNull() ?: return Pair(emptyList(), infoMap)

        val rows = table.select("tr").drop(1)
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
                        Log.i(tag, "Parsed date: $month $day, status: $status")
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
        return Pair(sportItems, infoMap)
    }
}