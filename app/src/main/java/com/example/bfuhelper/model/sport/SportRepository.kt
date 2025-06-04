package com.example.bfuhelper.model.sport

import com.example.bfuhelper.model.sport.api.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SportRepository(private val sportDao: SportDao) {
    private val authRepository: AuthRepository by lazy { AuthRepository() }

    suspend fun getAll(): List<SportItem> {
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.getAll()
        }
    }

    suspend fun getByMonth(targetMonth: Month): List<SportItem> {
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.getByMonth(targetMonth)
        }
    }

    suspend fun get(targetMonth: Month, targetDay: Byte): SportItem {
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.getByMonthAndDay(targetMonth, targetDay)
        }
    }

    suspend fun isEmpty(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.isEmpty()
        }
    }

    suspend fun delete(item: SportItem) {
        return withContext(Dispatchers.IO) {
            sportDao.delete(item)
        }
    }

    suspend fun insert(item: SportItem) {
        return withContext(Dispatchers.IO) {
            sportDao.insert(item)
        }
    }

    suspend fun update(item: SportItem) {
        return withContext(Dispatchers.IO) {
            sportDao.update(item)
        }
    }

    suspend fun insertAll(items: List<SportItem>) {
        return withContext(Dispatchers.IO) {
            insertAll(items)
        }
    }
}