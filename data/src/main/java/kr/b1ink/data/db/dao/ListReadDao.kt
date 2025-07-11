package kr.b1ink.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.b1ink.data.db.entity.ListReadEntity
import kr.b1ink.domain.model.SiteType

@Dao
interface ListReadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ListReadEntity)

    @Query("SELECT COUNT(*) FROM board_read WHERE id = :id AND type = :type")
    fun getReadCount(type: SiteType, id:Long): Long

    @Query("DELETE FROM board_read")
    fun deleteAll()
}