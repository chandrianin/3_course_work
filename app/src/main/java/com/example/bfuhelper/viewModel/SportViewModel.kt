package com.example.bfuhelper.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bfuhelper.model.sport.SportDao
import com.example.bfuhelper.model.sport.SportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SportViewModel(private val dao: SportDao) : ViewModel() {

    private val repository = SportRepository(dao)

//    private var allItems: List<SportItem> = listOf()
//    private var allItemsString = MutableLiveData<String>()

    val scores = MutableLiveData(1)
    val maxScores = MutableLiveData(65)
    val visits = MutableLiveData(10)
    val maxVisits = MutableLiveData(10)

    init {
        viewModelScope.launch {
//            allItems = repository.getAll()
//            allItemsString.value = formatData()
//            Log.d("SportViewModel", allItemsString.value.toString())
            delay(1000)
            updateProgressValues(50, 65, 1, 10)
        }
    }


    // Метод для обновления значений прогресса
    private fun updateProgressValues(currentScores: Int, maxScores: Int, currentVisits: Int, maxVisits: Int) {
        this.scores.value = currentScores
        this.maxScores.value = maxScores
        this.visits.value = currentVisits
        this.maxVisits.value = maxVisits
    }

//    private fun formatData(): String {
//        return allItems.fold(" ") { str, item ->
//            str + '\n' + "${item.month} ${item.day} ${item.status}"
//        }
//    }
//
//    fun testFun() {
//        viewModelScope.launch {
//            val item =
//                SportItem(
//                    Month.entries[Random.nextInt(Month.entries.size)],
//                    Random.nextInt(1, 30).toByte(),
//                    Status.entries[Random.nextInt(Status.entries.size)]
//                )
//            dao.insert(item)
//            allItems = repository.getAll()
//            allItemsString.value = formatData()
//        }
//    }

}