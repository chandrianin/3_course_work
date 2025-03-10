package com.example.bfuhelper.model.schedule.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "address_table")
data class Address(
    val addressName: String,

    @PrimaryKey(autoGenerate = true)
    val id: Short = 0
)