package kr.b1ink.data.db

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kr.b1ink.data.db.entity.MainItemEntity
import timber.log.Timber

@HiltWorker
class SeedDatabaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: AppDatabase,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open("damoang/${AppDatabase.DEFAULT_BOARD_LINK}").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val plantType = object : TypeToken<List<MainItemEntity>>() {}.type
                    val plantList: List<MainItemEntity> = Gson().fromJson<List<MainItemEntity>>(jsonReader, plantType)
                        .filter { it.type == 0 }
                    database.mainDataDao().insertAll(plantList)
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error seeding database")
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SeedDatabaseWorker"
    }
}
