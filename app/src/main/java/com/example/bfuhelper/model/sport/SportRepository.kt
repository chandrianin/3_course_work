package com.example.bfuhelper.model.sport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SportRepository(private val dao: SportDao) {

    suspend fun getAll(): List<SportItem> {
        return withContext(Dispatchers.IO) {
            return@withContext dao.getAll()
        }
    }

    suspend fun getByMonth(targetMonth: Month): List<SportItem> {
        return withContext(Dispatchers.IO) {
            return@withContext dao.getByMonth(targetMonth)
        }
    }

    suspend fun getByMonthandDay(targetMonth: Month, targetDay: Byte): SportItem {
        return withContext(Dispatchers.IO) {
            return@withContext dao.getByMonthAndDay(targetMonth, targetDay)
        }
    }

    suspend fun isEmpty(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dao.isEmpty()
        }
    }

    suspend fun delete(item: SportItem) {
        return withContext(Dispatchers.IO) {
            dao.delete(item)
        }
    }

    suspend fun insert(item: SportItem) {
        return withContext(Dispatchers.IO) {
            dao.insert(item)
        }
    }

    suspend fun update(item: SportItem) {
        return withContext(Dispatchers.IO) {
            dao.update(item)
        }
    }

    suspend fun insertAll(items: List<SportItem>) {
        return withContext(Dispatchers.IO) {
            insertAll(items)
        }
    }
}