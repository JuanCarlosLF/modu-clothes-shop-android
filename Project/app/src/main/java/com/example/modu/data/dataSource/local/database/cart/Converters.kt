package com.example.modu.data.dataSource.local.database.cart

import androidx.room.TypeConverter
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String?  = value?.toPlainString()

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }
}