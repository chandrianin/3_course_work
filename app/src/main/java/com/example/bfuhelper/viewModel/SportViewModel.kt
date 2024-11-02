package com.example.bfuhelper.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bfuhelper.model.sport.SportDao
import com.example.bfuhelper.model.sport.SportItem
import com.example.bfuhelper.model.sport.Status
import kotlinx.coroutines.launch

class SportViewModel(private val dao: SportDao) : ViewModel() {
    var newDay = ""
    fun testFun() {
        viewModelScope.launch {
            val item = SportItem("Sept", newDay.toByte(), Status.Future.text())
            dao.insert(item)
        }
    }
}