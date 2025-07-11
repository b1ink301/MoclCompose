package kr.b1ink.data.di.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.db.AppDatabase
import kr.b1ink.data.db.MIGRATIONS
import kr.b1ink.data.db.dao.ListReadDao
import kr.b1ink.data.db.dao.MainDataDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase =
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(false)
            .addMigrations(*MIGRATIONS)
//            .addCallback(
//                object : RoomDatabase.Callback() {
//                    override fun onCreate(db: SupportSQLiteDatabase) {
//                        super.onCreate(db)
//                        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
//                            .build()
//                        WorkManager.getInstance(appContext).enqueue(request)
//                    }
//                }
//            )
            .build()

    @Provides
    fun provideMainDataDao(
        database: AppDatabase
    ): MainDataDao = database.mainDataDao()

    @Provides
    fun provideListReadDao(
        database: AppDatabase
    ): ListReadDao = database.listReadDao()
}