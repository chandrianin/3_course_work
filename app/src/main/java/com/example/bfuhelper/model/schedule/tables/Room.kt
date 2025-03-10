package com.example.bfuhelper.model.schedule.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_table")
data class Room(

    /**
     * Номер этажа
     * */
    val level: Byte,

    /**
     * Номер аудитории
     * */
    val room: Short,

    @PrimaryKey(autoGenerate = true)
    val id: Short = 0
)
