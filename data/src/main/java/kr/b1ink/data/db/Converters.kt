package kr.b1ink.data.db

import androidx.room.TypeConverter
import kr.b1ink.domain.model.SiteType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date = Date(value)
    @TypeConverter
    fun dateToTimestamp(date: Date): Long = date.time
    @TypeConverter
    fun siteTypeToString(type: SiteType) :String = type.name
    @TypeConverter
    fun stringToSiteType(name:String): SiteType = SiteType.valueOf(name)
}