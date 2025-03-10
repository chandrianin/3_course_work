package com.example.bfuhelper.model.schedule.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "professor_table",
    indices = [Index(value = ["lastName", "firstName", "patronymic"], unique = true)]
)
data class Professor(

    /**
     * Фамилия
     * */
    val lastName: String,

    /**
     * Имя
     * */
    val firstName: String,

    /**
     * Отчество
     * */
    val patronymic: String,

    @PrimaryKey(autoGenerate = true)
    val id: Short = 0
)