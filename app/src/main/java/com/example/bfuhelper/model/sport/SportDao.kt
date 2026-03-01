package com.example.bfuhelper.model.sport

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс доступа к записям БД [sport_table][SportItem]
 * */
@Dao
interface SportDao {

    @Query("SELECT * FROM sport_table ORDER BY month, day ASC")
    fun getAllSportItems(): Flow<List<SportItem>>

    @Insert
    /**
     * Функция для добавления нескольких записей в [sport_table][SportItem]
     * @param items список объектов класса [SportItem]
     * */
    suspend fun insertAll(items: List<SportItem>)

    @Query("SELECT * FROM sport_table ORDER BY month DESC")
            /**
             * Функция для получения всех записей БД [sport_table][SportItem]
             * @return объект `List<SportItem>`.
             * */
    fun getAll(): List<SportItem>


    @Query("DELETE FROM sport_table")
    fun deleteAll()
}