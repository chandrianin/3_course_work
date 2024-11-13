package com.example.bfuhelper.model.sport

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SportItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
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
                ).addTypeConverter(Converters()).build()
                INSTANCE = instance
            }
            return instance
        }
    }
}