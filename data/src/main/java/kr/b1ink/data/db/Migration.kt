package kr.b1ink.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE clien_read;")
    }
}

val MIGRATIONS = arrayOf(
    MIGRATION_1_2,
)