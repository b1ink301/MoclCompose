package kr.b1ink.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.b1ink.data.db.dao.MainDataDao
import kr.b1ink.data.db.dao.ListReadDao
import kr.b1ink.data.db.entity.MainItemEntity
import kr.b1ink.data.db.entity.ListReadEntity

@Database(
    entities = [MainItemEntity::class, ListReadEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun mainDataDao() : MainDataDao
    abstract fun listReadDao() : ListReadDao

    companion object {
        const val DATABASE_NAME = "mocl-new.db"
        const val DEFAULT_BOARD_LINK = "default_board_link.json"
        const val ALL_BOARD_LINK = "board_link.json"
    }
}