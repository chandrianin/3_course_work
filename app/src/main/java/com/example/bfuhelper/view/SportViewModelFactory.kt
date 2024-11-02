package com.example.bfuhelper.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bfuhelper.model.sport.SportDao
import com.example.bfuhelper.viewModel.SportViewModel

class SportViewModelFactory(private val dao: SportDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SportViewModel::class.java)){
            return  SportViewModel(dao) as  T
        }
        throw IllegalArgumentException("Неизвестная ViewModel")
    }
}