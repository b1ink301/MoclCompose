package kr.b1ink.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kr.b1ink.data.db.entity.MainItemEntity
import kr.b1ink.domain.model.SiteType

@Dao
interface MainDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MainItemEntity)

    @Query("SELECT COUNT(*) FROM board_link WHERE board = :board AND siteType = :siteType")
    suspend fun getCountLink(siteType: SiteType, board: String): Int

    @Query("SELECT * FROM board_link WHERE board = :board AND siteType = :siteType")
    fun getFlow(siteType: SiteType, board: String): Flow<MainItemEntity>

    @Query("SELECT * FROM board_link WHERE siteType = :siteType ORDER By `no` ASC")
    suspend fun getAll(siteType: SiteType): List<MainItemEntity>

    @Query("SELECT * FROM board_link WHERE siteType = :siteType ORDER By `no` ASC")
    fun getAllFlow(siteType: SiteType): Flow<List<MainItemEntity>>

    @Query("SELECT COUNT(*) FROM board_link WHERE board = :board")
    suspend fun count(board: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MainItemEntity>)

    @Delete
    suspend fun delete(list: List<MainItemEntity>)

    @Query("DELETE FROM board_link")
    suspend fun deleteAll()

    @Query("DELETE FROM board_link WHERE siteType = :siteType")
    suspend fun deleteAll(siteType: SiteType)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(vararg link: MainItemEntity)

    @Query("UPDATE board_link SET `no` = :no WHERE board = :board")
    suspend fun updateNo(board: String, no: Int)
}