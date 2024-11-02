package com.example.bfuhelper.model.sport

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "sport_table")
data class SportItem(
    var month: String,
    var day: Byte,
    var status: String,
    @PrimaryKey(autoGenerate = true)
    val dayID: Long = 0
)