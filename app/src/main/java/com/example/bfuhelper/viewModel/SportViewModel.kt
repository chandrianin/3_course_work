package com.example.bfuhelper.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.bfuhelper.model.sport.Month
import com.example.bfuhelper.model.sport.SportDao
import com.example.bfuhelper.model.sport.SportItem
import com.example.bfuhelper.model.sport.Status
import kotlinx.coroutines.launch

class SportViewModel(private val dao: SportDao) : ViewModel() {
    var newDay = MutableLiveData("")

    private val allItems = dao.getAll()
    val allItemsString = allItems.map { formatData() }

    private val septItems = dao.getByMonth(Month.Oct)
    val septItemsString = septItems.map {
        it.fold("${Month.Oct.text()}: ") { str, item ->
            str + " ${item.day}"
        }
    }
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
        return allItems.value?.fold(" ") { str, item ->
            str + '\n' + "${item.month} ${item.day} ${item.status}"
        } ?: "Нечего показывать"
    }

    fun testFun() {
        viewModelScope.launch {
            val item =
                SportItem(
                    Month.Nov,
                    newDay.value?.toByte() ?: 0,
                    Status.Future
                )
            dao.insert(item)
        }
    }

}