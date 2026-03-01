//package com.example.bfuhelper.view
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.bfuhelper.model.sport.AuthManager
//import com.example.bfuhelper.model.sport.SportDao
//import com.example.bfuhelper.viewModel.SportViewModel
//
//class SportViewModelFactory(private val dao: SportDao/*, private val authManager: AuthManager*/) :
//    ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(SportViewModel::class.java)) {
//            return SportViewModel(dao/*, authManager*/) as T
//        }
//        throw IllegalArgumentException("Неизвестная ViewModel")
//    }
//}


package com.example.bfuhelper.view

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bfuhelper.model.sport.LocalRepository
import com.example.bfuhelper.model.sport.SportDao
import com.example.bfuhelper.model.sport.api.RemoteRepository
import com.example.bfuhelper.viewModel.SportViewModel

class SportViewModelFactory(
    private val dao: SportDao, // DAO все еще нужен здесь для создания SportRepository
    private val application: Application // Application нужен для SportViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SportViewModel::class.java)) {
            // Создаем RetrofitClient, который будет использоваться для AuthApiService
            // и для SportRepository (через AuthApiService)
//            val retrofitClient = RetrofitClient() // RetrofitClient теперь создается здесь

            // Создаем AuthRepository
            val remoteRepository = RemoteRepository.getInstance(application)

            // Создаем SportRepository, передавая ему SportDao и AuthApiService
            val localRepository = LocalRepository(dao)

            @Suppress("UNCHECKED_CAST")
            // Передаем SportRepository, AuthRepository и Application в конструктор SportViewModel
            return SportViewModel(localRepository, remoteRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
