package com.example.bfuhelper.model.schedule.tables

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.bfuhelper.model.schedule.LessonType

@Entity(
    tableName = "lesson_table",
    foreignKeys = [
        ForeignKey(
            entity = Address::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("addressId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Room::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("roomId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Professor::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("professorId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )]
)

data class Lesson(

    /**
     * Название занятия
     * */
    val name: String,

    /**
     * Внешний ключ на запись в таблице [address_table][Address]
     * */
    val addressId: Short?,

    /**
     * Внешний ключ на запись в таблице [room_table][Room]
     * */
    val roomId: Short?,

    /**
     * Тип занятия: лекция, практика или лабораторная
     * */
    val lessonType: LessonType,

    /**
     * Пара проходит *онлайн*?
     * */
    val online: Boolean,

    /**
     * Внешний ключ на запись в таблице [professor_table][Professor]
     * */
    val professorId: Short?,

    /**
     * Номер подгруппы
     * */
    val subgroupNumber: Byte?,

    @PrimaryKey(autoGenerate = true)
    val id: Short
)
