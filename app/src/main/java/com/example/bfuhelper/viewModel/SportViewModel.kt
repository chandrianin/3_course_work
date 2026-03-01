package com.example.bfuhelper.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bfuhelper.model.sport.LocalRepository
import com.example.bfuhelper.model.sport.Month
import com.example.bfuhelper.model.sport.SportItem
import com.example.bfuhelper.model.sport.Status
import com.example.bfuhelper.model.sport.api.RemoteRepository
import com.example.bfuhelper.utils.Event
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar

class SportViewModel(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    application: Application
) : AndroidViewModel(application) {
    private val tag = "SportViewModel"

    // LiveData, указывающая, подходят ли данные для входа
    val isCredentialsMissing = MutableLiveData<Boolean>()

    private val _sportItems = MutableLiveData<List<SportItem>>()
    val sportItems: LiveData<List<SportItem>> get() = _sportItems

    // LiveData для сообщений Toast
    private val _showToastEvent = MutableLiveData<Event<String>>()
    val showToastEvent: LiveData<Event<String>> get() = _showToastEvent

    // LiveData для индикатора загрузки (SwipeRefreshLayout)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData для текущего выбранного месяца
    private val _selectedMonth = MutableLiveData<Month>()
    val selectedMonth: LiveData<Month> get() = _selectedMonth

    // LiveData для списка SportItem, отфильтрованных по выбранному месяцу
    private val _filteredSportItems = MutableLiveData<List<SportItem>>()
    val filteredSportItems: LiveData<List<SportItem>> get() = _filteredSportItems

    // LiveData для списка доступных месяцев на основе загруженных данных
    private val _availableMonths = MutableLiveData<List<Month>>()
    val availableMonths: LiveData<List<Month>> get() = _availableMonths

    val scores = MutableLiveData(0)
    val maxScores = MutableLiveData(0)
    val visits = MutableLiveData(0)
    val maxVisits = MutableLiveData(0)

    var control = 0
    var lms = 0
    var events = 0
    var physical = 0

    init {
        val calendar = Calendar.getInstance()
        val currentMonthIndex = calendar.get(Calendar.MONTH)
        _selectedMonth.value = Month.entries[currentMonthIndex]

        initObservers()
        checkLoginData(true)
    }

    private fun initObservers() {
        sportItems.observeForever { items ->
            updateAvailableMonths(items) // Вызов для обновления списка доступных месяцев
        }
        selectedMonth.observeForever { month ->
            filterSportItems(sportItems.value, month)
        }
    }

    private fun updateAvailableMonths(allItems: List<SportItem>?) {
        val monthsList =
            allItems?.map { it.month }?.distinct()?.sortedBy { it.ordinal } ?: emptyList()
        _availableMonths.postValue(monthsList)
        Log.d(tag, "updateAvailableMonths: Available months updated: ${monthsList.size}")

        if (_selectedMonth.value == null || !monthsList.contains(_selectedMonth.value)) {
            val calendar = Calendar.getInstance()
            val currentMonthEnum = Month.entries[calendar.get(Calendar.MONTH)]

            if (monthsList.contains(currentMonthEnum)) {
                // Выбираем текущий месяц, если он есть в доступных
                _selectedMonth.postValue(currentMonthEnum)
                Log.d(
                    tag,
                    "updateAvailableMonths: Set selected month to current month: $currentMonthEnum"
                )
            } else if (monthsList.isNotEmpty()) {
                // Иначе выбираем первый доступный месяц
                _selectedMonth.postValue(monthsList.first())
                Log.d(
                    tag,
                    "updateAvailableMonths: Set selected month to first available month: ${monthsList.first()}"
                )
            } else {
                // Если месяцев нет вообще, сбрасываем выбор
                _selectedMonth.postValue(Month.Jan)
                Log.d(
                    tag,
                    "updateAvailableMonths: No months available, selected month set to null."
                )
            }
        }
    }

    /**
     * Фильтрует список SportItem по выбранному месяцу.
     * @param allItems Все элементы SportItem.
     * @param month Выбранный месяц.
     */
    private fun filterSportItems(allItems: List<SportItem>?, month: Month?) {
        if (allItems == null || month == null) {
            _filteredSportItems.postValue(emptyList())
            return
        }
        val filteredList = allItems.filter { it.month == month }
        _filteredSportItems.postValue(filteredList)
    }

    /**
     * Устанавливает выбранный месяц.
     * @param month Новый выбранный месяц.
     */
    fun setSelectedMonth(month: Month) {
        _selectedMonth.value = month
    }

    fun checkLoginData(init: Boolean) {
        viewModelScope.launch {
            val isLoggedIn = remoteRepository.loginResult.isSuccess
            isCredentialsMissing.postValue(!isLoggedIn)
            Log.d(tag, "isLoggedIn: ${isLoggedIn}")
            if (isLoggedIn) {
                Log.d(tag, "isMissing: ${isCredentialsMissing.value}")
                loadLocalItems()
                if (init) {
                    remoteRepository.login()
                }
                loadRemoteItems()
                Log.d(tag, "isMissing: ${isCredentialsMissing.value}")
            }
        }
    }

    fun saveAndLogin(username: String, password: String) {
        Log.d(tag, "saveAndLogin $username, $password")
        viewModelScope.launch {
            val tempLoginResult = remoteRepository.login(username, password)
            if (tempLoginResult.isSuccess) {
                checkLoginData(false)
            } else if ((tempLoginResult.exceptionOrNull()?.message
                    ?: "") == "Неверный логин или пароль."
            ) {
                _showToastEvent.value = Event("Неверный логин или пароль.")
            } else {
                _showToastEvent.value = Event("Попробуйте позднее...")
            }
        }
    }

    private fun loadLocalItems() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            localRepository.getAllSportItems().collect { localData ->
                _sportItems.postValue(localData)
                Log.d(tag, "Отображены локальные данные: ${localData.size} элементов.")
                _showToastEvent.value = Event("Отображены локальные данные")
                // Останавливаем сбор Flow после первого получения,
                // чтобы не переопределять данные, которые придут из сети
                cancel() // отписываемся от Flow
            }
            updateProgressValues()
        }
    }


    private fun loadRemoteItems() {
        viewModelScope.launch {
            _showToastEvent.value = Event("Загрузка данных из сети...")
            val loginResult = remoteRepository.loginResult
            if (loginResult.isSuccess) {
                val remoteItemsResult = remoteRepository.getSportVisits()
                if (remoteItemsResult.isSuccess) {
                    val (newItems, info) = remoteItemsResult.getOrThrow()
                    // 2. Если сетевой запрос успешен, обновляем локальную БД
                    localRepository.deleteAll()

                    remoteRepository.setInfo(
                        info[remoteRepository.PREF_CONTROL]!!,
                        info[remoteRepository.PREF_LMS]!!,
                        info[remoteRepository.PREF_SPORT_EVENT]!!,
                        info[remoteRepository.PREF_PHYSICAL_P]!!
                    )

                    _sportItems.postValue(newItems)
                    try {
                        localRepository.insertAll(newItems)
                        Log.d(tag, "данные вставлены: ${localRepository.getAll()}")
                    } catch (e: Exception) {
                        Log.w(tag, "Ошибка вставки данных: $e")
                    }
                    _showToastEvent.value = Event("Данные успешно обновлены из сети!")
                    Log.d(tag, "Данные обновлены из сети. Элементов: ${newItems.size}")
                } else {
                    val errorMessage =
                        "Ошибка получения данных из сети: ${remoteItemsResult.exceptionOrNull()?.message ?: "Неизвестная ошибка"}"
                    _showToastEvent.value = Event(errorMessage)
                    Log.e(tag, errorMessage, remoteItemsResult.exceptionOrNull())
                }
            } else {
                val errorMessage =
                    "Ошибка входа, данные из сети не загружены: ${loginResult.exceptionOrNull()?.message ?: "Неизвестная ошибка"}"
                _showToastEvent.value = Event(errorMessage)
                Log.e(tag, errorMessage, loginResult.exceptionOrNull())
                // В случае ошибки, _sportItems сохранит предыдущие локальные данные
            }
            updateProgressValues()
            _isLoading.value = false // Скрываем индикатор загрузки
        }

    }


    private fun updateProgressValues() {
        viewModelScope.launch {
            val info = remoteRepository.getInfo()

            control = info[remoteRepository.PREF_CONTROL]!!
            lms = info[remoteRepository.PREF_LMS]!!
            events = info[remoteRepository.PREF_SPORT_EVENT]!!
            physical = info[remoteRepository.PREF_PHYSICAL_P]!!

            var currentScores =
                info[remoteRepository.PREF_CONTROL]!! + info[remoteRepository.PREF_LMS]!! + info[remoteRepository.PREF_SPORT_EVENT]!! + info[remoteRepository.PREF_PHYSICAL_P]!!
            var currentMaxScores =
                info[remoteRepository.PREF_CONTROL]!! + info[remoteRepository.PREF_LMS]!! + info[remoteRepository.PREF_SPORT_EVENT]!! + info[remoteRepository.PREF_PHYSICAL_P]!!
            var currentVisits = 0
            var currentMaxVisits = 0
            val currentItems = localRepository.getAll()
            currentItems.forEach {
                when (it.status) {
                    Status.Visit -> {
                        currentScores += 2
                        currentVisits += 1
                    }

                    Status.Disease -> {
                        currentScores += 1
                    }

                    else -> {

                    }
                }
                currentMaxVisits += 1
                currentMaxScores += 2
            }
            maxScores.postValue(currentMaxScores)
            maxVisits.postValue(currentMaxVisits)
            scores.postValue(currentScores)
            visits.postValue(currentVisits)
        }
    }
}