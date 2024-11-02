package com.example.bfuhelper.model.sport

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SportItem::class], version = 3, exportSchema = false)
abstract class SportDataBase : RoomDatabase() {
    abstract val sportDao: SportDao

    companion object {
        @Volatile
        private var INSTANCE: SportDataBase? = null

        fun getInstance(context: Context): SportDataBase {
            var instance = INSTANCE
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportDataBase::class.java,
                    "sport_database"
                ).build()
                INSTANCE = instance
            }
            return instance
        }
    }
}