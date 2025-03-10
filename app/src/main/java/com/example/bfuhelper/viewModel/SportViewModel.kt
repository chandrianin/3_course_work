package com.example.bfuhelper.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bfuhelper.model.sport.Month
import com.example.bfuhelper.model.sport.SportDao
import com.example.bfuhelper.model.sport.SportItem
import com.example.bfuhelper.model.sport.SportRepository
import com.example.bfuhelper.model.sport.Status
import kotlinx.coroutines.launch
import kotlin.random.Random

class SportViewModel(private val dao: SportDao) : ViewModel() {

    private val repository = SportRepository(dao)

    private var allItems: List<SportItem> = listOf()
    private var allItemsString = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            allItems = repository.getAll()
            allItemsString.value = formatData()
            Log.d("SportViewModel", allItemsString.value.toString())
        }
    }

//    suspend fun getAll(): LiveData<List<SportItem>> {
//        return dao.getAll()
//    }
//    private val itemsInMonths = List(12) { dao.getByMonth(Month.entries[it].toString()) }

//    private val allItemsInMonths = List(12) { index: Int ->
//        itemsInMonths.value?.get(index)?.map {
//            itemsInMonths.value?.get(index).value?.fold(Month.entries[index].toString()) { str, item ->
//                str + ", ${item.day}"
//            } ?: " "
//        }
//    }
//    val allItemsInMonthsString =
//        allItemsInMonths.map { allItemsInMonths.joinToString(separator = "\n") }

    private fun formatData(): String {
        return allItems.fold(" ") { str, item ->
            str + '\n' + "${item.month} ${item.day} ${item.status}"
        }
    }

    fun testFun() {
        viewModelScope.launch {
            val item =
                SportItem(
                    Month.entries[Random.nextInt(Month.entries.size)],
                    Random.nextInt(1, 30).toByte(),
                    Status.entries[Random.nextInt(Status.entries.size)]
                )
            dao.insert(item)
            allItems = repository.getAll()
            allItemsString.value = formatData()
        }
    }

}