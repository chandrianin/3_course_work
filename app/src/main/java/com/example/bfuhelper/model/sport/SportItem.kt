package com.example.bfuhelper.model.sport

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "sport_table")
/**
 * Шаблон элемента таблицы БД "Sport_table"
 *
 * Запись соответствует занятию по физкультуре.
 * Класс необходим для хранения данных о посещениях студента.
 * Использование типа Byte снижением веса БД на 45%
 *
 * Пример использования:
 * ```
 * val item = SportItem(
 *                 Month.Nov.ordinal.toByte(),
 *                 30,
 *                 Status.Future.ordinal.toByte()
 *                 )
 * val month = Month.entries[item.month.toInt]
 * val day = item.day.toInt
 * val status = item.status.toInt
 *```
 * */
data class SportItem(
    /**
     * Ordinal элемента enum-класса [Month]
     *
     * Например, значение 0 должно быть проассоциировано с [Month.Jan]
     * */
    var month: Byte,

    /**
     * Номер дня месяца [month]
     * */
    var day: Byte,

    /**
     * Ordinal элемента enum-класса [Status]
     *
     * Например, значение 0 должно быть проассоциировано с [Status.Absence]
     * */
    var status: Byte,

    @PrimaryKey(autoGenerate = true)
    val dayID: Long = 0
)