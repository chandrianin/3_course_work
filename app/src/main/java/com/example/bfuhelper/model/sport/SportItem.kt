package com.example.bfuhelper.model.sport

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "sport_table")
/**
 * Шаблон элемента таблицы БД "Sport_table"
 *
 * Запись соответствует занятию по физкультуре.
 * Класс необходим для хранения данных о посещениях студента.
 *
 * Пример использования:
 * ```
 * val item = SportItem(
 *                 Month.Nov,
 *                 30,
 *                 Status.Future
 *                 )
 * val month = item.month
 * val day = item.day.toInt
 * val status = item.status
 *```
 * */
data class SportItem(

    var month: Month,

    var day: Byte,

    var status: Status,

    @PrimaryKey(autoGenerate = true)
    val dayID: Long = 0
)