package com.example.bfuhelper.model.sport

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalRepository(private val sportDao: SportDao) {
    private val tag = "SportRepository"

    fun getAllSportItems(): Flow<List<SportItem>> {
        return sportDao.getAllSportItems()
    }

    suspend fun getAll(): List<SportItem> {
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.getAll()
        }
    }

    suspend fun deleteAll() {
        Log.d(tag, "deleteAll")
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.deleteAll()
        }
    }

    suspend fun insertAll(items: List<SportItem>) {
        return withContext(Dispatchers.IO) {
            sportDao.insertAll(items)
        }
    }

//    suspend fun getByMonth(targetMonth: Month): List<SportItem> {
//        return withContext(Dispatchers.IO) {
//            return@withContext sportDao.getByMonth(targetMonth)
//        }
//    }

//    suspend fun get(targetMonth: Month, targetDay: Byte): SportItem {
//        return withContext(Dispatchers.IO) {
//            return@withContext sportDao.getByMonthAndDay(targetMonth, targetDay)
//        }
//    }

//    suspend fun isEmpty(): Boolean {
//        return withContext(Dispatchers.IO) {
//            return@withContext sportDao.isEmpty()
//        }
//    }

//    suspend fun delete(item: SportItem) {
//        return withContext(Dispatchers.IO) {
//            sportDao.delete(item)
//        }
//    }

//    suspend fun insert(item: SportItem) {
//        return withContext(Dispatchers.IO) {
//            sportDao.insert(item)
//        }
//    }

//    suspend fun update(item: SportItem) {
//        return withContext(Dispatchers.IO) {
//            sportDao.update(item)
//        }
//    }
}