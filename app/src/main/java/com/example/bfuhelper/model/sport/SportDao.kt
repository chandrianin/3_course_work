package com.example.bfuhelper.model.sport

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Интерфейс доступа к записям БД [sport_table][SportItem]
 * */
@Dao
interface SportDao {
    @Insert
    /**
     * Функция для добавления записи в [sport_table][SportItem]
     * @param item объект класса [SportItem]
     * */
    suspend fun insert(item: SportItem)

    @Insert
    /**
     * Функция для добавления нескольких записей в [sport_table][SportItem]
     * @param items список объектов класса [SportItem]
     * */
    suspend fun insertAll(items: List<SportItem>)

    @Update
    /**
     * Функция для обновления записи БД [sport_table][SportItem]
     * @param item объект класса [SportItem]. Объект будет идентифицироваться по его [dayID][SportItem.dayID]
     * */
    suspend fun update(item: SportItem)

    @Delete
    /**
     * Функция для удаления записи БД [sport_table][SportItem]
     * @param item объект класса [SportItem]. Объект будет идентифицироваться по его [dayID][SportItem.dayID]
     * */
    suspend fun delete(item: SportItem)

    @Query("SELECT * FROM sport_table WHERE dayID=:targetId")
            /**
             * Функция для получения записи БД [sport_table][SportItem]
             * @param targetId Long.
             * @return `LiveData<SportItem>`. Функция будет искать запись с таким же [dayID][SportItem.dayID] *Не использовать его в suspend-функциях*
             * */
    fun getById(targetId: Long): LiveData<SportItem>

    @Query("SELECT * FROM sport_table WHERE month=:targetMonth ORDER BY day ASC")
            /**
             * Функция для получения списка записей БД [sport_table][SportItem]
             * @param targetMonth Month.
             * @return `LiveData<List<SportItem>>`. Функция будет искать объекты с таким же [month][SportItem.month]. Список будет составлен по убыванию [day][SportItem.day]. *Не использовать его в suspend-функциях*
             * */
    fun getByMonth(targetMonth: Month): LiveData<List<SportItem>>

    @Query("SELECT * FROM sport_table ORDER BY dayID DESC")
            /**
             * Функция для получения всех записей БД [sport_table][SportItem]
             * @return объект `LiveData<SportItem>`. Список будет составлен по убыванию [dayId][SportItem.dayID]. *Не использовать его в suspend-функциях*
             * */
    fun getAll(): LiveData<List<SportItem>>
}