package com.example.bfuhelper.model.sport

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter

@ProvidedTypeConverter
class SportConverters {
    @TypeConverter
    fun monthToByte(month: Month): Byte {
        return month.ordinal.toByte()
    }

    @TypeConverter
    fun byteToMonth(byte: Byte): Month {
        return Month.entries[byte.toInt()]
    }

    @TypeConverter
    fun statusToByte(status: Status): Byte {
        return status.ordinal.toByte()
    }

    @TypeConverter
    fun byteToStatus(byte: Byte): Status {
        return Status.entries[byte.toInt()]
    }
}