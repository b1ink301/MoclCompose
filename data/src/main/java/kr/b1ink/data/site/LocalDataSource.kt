package kr.b1ink.data.site

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kr.b1ink.data.db.AppDatabase
import kr.b1ink.data.db.dao.ListReadDao
import kr.b1ink.data.db.dao.MainDataDao
import kr.b1ink.data.db.entity.ListReadEntity
import kr.b1ink.data.db.entity.MainItemEntity
import kr.b1ink.data.db.entity.toDto
import kr.b1ink.data.db.entity.toEntity
import kr.b1ink.data.di.coroutine.DefaultDispatcher
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import timber.log.Timber
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationContext private val context: Context,
    private val mainDataDao: MainDataDao,
    private val listReadDao: ListReadDao,
) {
    suspend fun getMainData(siteType: SiteType): Result<List<MainItem>> = withContext(dispatcher) {
        try {
            Result.Success(
                mainDataDao
                    .getAll(siteType)
                    .map(MainItemEntity::toDto)
                    .toImmutableList()
            )
        } catch (e: Exception) {
            Result.Error(error = e.message ?: "Unknown Error")
        }
    }

    suspend fun getMainDataFromJson(siteType: SiteType): Result<List<MainItem>> = withContext(dispatcher) {
        try {
            val jsonString = context.assets
                .open("${siteType.name.lowercase()}/${AppDatabase.ALL_BOARD_LINK}")
                .bufferedReader()
                .use(BufferedReader::readText)

            val plantType = object : TypeToken<List<MainItemEntity>>() {}.type
            val plantList: List<MainItemEntity> = Gson().fromJson(jsonString, plantType)
            val result = plantList.map { plant ->
                plant
                    .copy(siteType = siteType)
                    .toDto()
                    .copy(hasRoom = mainDataDao.getCountLink(siteType, plant.board) > 0)
            }
                .toImmutableList()
            Result.Success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(error = e.message ?: "Unknown Error")
        }
    }

    suspend fun setMainData(siteType: SiteType, list: List<MainItem>) = withContext(dispatcher) {
        mainDataDao.deleteAll(siteType)
        delay(200)
        mainDataDao.insertAll(list.map {
            Timber.d("addLinks it=$it")
            it.toEntity()
        })
    }

    suspend fun markRead(
        siteType: SiteType,
        id: Long,
    ) = withContext(dispatcher) {
        listReadDao.insert(ListReadEntity(id, siteType))
    }
}