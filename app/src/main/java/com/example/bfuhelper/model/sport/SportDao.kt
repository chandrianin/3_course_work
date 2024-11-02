package com.example.bfuhelper.model.sport

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface SportDao {
    @Insert
    suspend fun insert(item: SportItem)

    @Insert
    suspend fun insertAll(items: List<SportItem>)

    @Update
    suspend fun update(item: SportItem)

    @Delete
    suspend fun delete(item: SportItem)

//    @Query("SELECT * FROM sport_table WHERE month =:inputMonth & day=:inputDay")
//    fun get(inputMonth: String, inputDay: Byte): LiveData<SportItem>
}