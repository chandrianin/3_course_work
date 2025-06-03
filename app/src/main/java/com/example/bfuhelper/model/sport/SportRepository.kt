package com.example.bfuhelper.model.sport

import android.util.Log
import com.example.bfuhelper.model.sport.api.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SportRepository(private val sportDao: SportDao) {
    private val authRepository: AuthRepository by lazy { AuthRepository() }

    // Метод для получения данных из локальной БД реактивно
    fun getLocalSportItems(): Flow<List<SportItem>> {
        return sportDao.getAllFlow()
    }

    // Метод для запуска синхронизации с сетью и обновления БД
    suspend fun syncSportData() {
        return withContext(Dispatchers.IO) {
            Log.d("SportRepository", "Starting sport data synchronization...")
            val remoteResult = authRepository.getSportVisits()

            if (remoteResult.isSuccess) {
                val remoteItems = remoteResult.getOrThrow()
                val localItems = sportDao.getAllFlow().firstOrNull() ?: emptyList() // Получаем текущие локальные элементы один раз
                // Используем .firstOrNull() чтобы получить текущее состояние
                // без ожидания следующих эмиссий Flow

                val itemsToInsert = mutableListOf<SportItem>()
                val itemsToUpdate = mutableListOf<SportItem>()

                val localItemsMap = localItems.associateBy { Pair(it.month, it.day) }
                val remoteItemsMap = remoteItems.associateBy { Pair(it.month, it.day) } // Для обнаружения удаленных

                // 1. Проверяем новые или изменившиеся записи из сети
                for (remoteItem in remoteItems.sortedBy { it.month.ordinal * 100 + it.day }) {
                    val localItem = localItemsMap[Pair(remoteItem.month, remoteItem.day)]

                    if (localItem == null) {
                        itemsToInsert.add(remoteItem)
                        // delay(50) // Имитация задержки для поэтапного обновления
                    } else if (localItem.status != remoteItem.status) {
                        itemsToUpdate.add(remoteItem.copy()) // Копия для обновления
                        // delay(50) // Имитация задержки
                    }
                }

                // 2. Обработка записей, которые могли быть удалены на сервере
                val itemsToDelete = localItems.filter { Pair(it.month, it.day) !in remoteItemsMap }

                // Выполняем операции с БД
                if (itemsToDelete.isNotEmpty()) {
                    // sportDao.delete(itemsToDelete) // Вам понадобится такой метод в DAO
                    Log.d("SportRepository", "Detected ${itemsToDelete.size} items to delete (not yet implemented).")
                }
                if (itemsToInsert.isNotEmpty()) {
                    sportDao.insertAll(itemsToInsert) // Room с OnConflictStrategy.REPLACE обработает и вставку, и обновление, если ключи совпадут.
                    Log.d("SportRepository", "Inserted ${itemsToInsert.size} new items.")
                }
                if (itemsToUpdate.isNotEmpty()) {
                    sportDao.updateAll(itemsToUpdate) // Обновляем только те, что изменились
                    Log.d("SportRepository", "Updated ${itemsToUpdate.size} items.")
                }

                // Если вы хотите, чтобы после синхронизации список был точно как на сервере,
                // можно сначала deleteAll, а потом insertAll(remoteItems) - это самый простой путь,
                // но он не демонстрирует поэтапное обновление через Room Flow.
                // Для поэтапного обновления, как сделано выше, Room Flow будет сам триггерить
                // изменения по мере вставки/обновления.

            } else {
                val exception = remoteResult.exceptionOrNull()
                Log.e("SportRepository", "Failed to fetch from network during sync: ${exception?.message}")
                // В этом случае UI просто продолжит показывать локальные данные.
            }
            Log.d("SportRepository", "Sport data synchronization finished.")
        }
    }

    suspend fun getAll(): List<SportItem> {
        return withContext(Dispatchers.IO) {
            val remoteResult = authRepository.getSportVisits()

            if (remoteResult.isSuccess) {
                val remoteItems = remoteResult.getOrThrow()
                val localItems = sportDao.getAll()

                val itemsToInsert = mutableListOf<SportItem>()
                val itemsToUpdate = mutableListOf<SportItem>()
                val syncedItems = mutableListOf<SportItem>() // Список для возврата после синхронизации

                val localItemsMap = localItems.associateBy { Pair(it.month, it.day) }

                for (remoteItem in remoteItems.sortedBy { it.month.ordinal * 100 + it.day }) {
                    val localItem = localItemsMap[Pair(remoteItem.month, remoteItem.day)]

                    if (localItem == null) {
                        itemsToInsert.add(remoteItem)
                        syncedItems.add(remoteItem) // Добавляем в список для возврата
                    } else if (localItem.status != remoteItem.status) {
                        // Только если статус изменился, помечаем для обновления
                        // Важно: создаем копию, так как Room может работать лучше с отдельными объектами
                        val updated = remoteItem.copy()
                        itemsToUpdate.add(updated)
                        syncedItems.add(updated) // Добавляем в список для возврата
                    } else {
                        // Элемент не изменился, используем локальную версию, если она есть
                        syncedItems.add(localItem)
                    }
                }

                // ВАЖНО: Если данные могут быть удалены на сервере, вам также понадобится логика удаления
                // val remoteItemKeys = remoteItems.map { Pair(it.month, it.day) }.toSet()
                // val itemsToDelete = localItems.filter { Pair(it.month, it.day) !in remoteItemKeys }
                // if (itemsToDelete.isNotEmpty()) {
                //     sportDao.delete(itemsToDelete) // Нужен метод delete в DAO
                //     Log.d("SportRepository", "Deleted ${itemsToDelete.size} items from local DB.")
                // }


                // Выполняем операции с БД
                if (itemsToInsert.isNotEmpty()) {
                    sportDao.insertAll(itemsToInsert) // Вставка новых
                    Log.d("SportRepository", "Inserted ${itemsToInsert.size} new items.")
                }
                if (itemsToUpdate.isNotEmpty()) {
                    sportDao.updateAll(itemsToUpdate) // Обновление измененных
                    Log.d("SportRepository", "Updated ${itemsToUpdate.size} items.")
                }

                return@withContext syncedItems.sortedBy { it.month.ordinal * 100 + it.day } // Возвращаем отсортированный список после синхронизации
            } else {
                val exception = remoteResult.exceptionOrNull()
                Log.e("SportRepository", "Failed to fetch from network: ${exception?.message}. Attempting to load from DB.")
                return@withContext sportDao.getAll() // Возвращаем данные из локальной БД в случае ошибки сети
            }
        }
    }

    suspend fun getByMonth(targetMonth: Month): List<SportItem> {
        return withContext(Dispatchers.IO) {
            return@withContext sportDao.getByMonth(targetMonth)
        }
    }

    suspend fun getByMonthandDay(targetMonth: Month, targetDay: Byte): SportItem {
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