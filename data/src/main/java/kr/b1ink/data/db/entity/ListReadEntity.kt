package kr.b1ink.data.db.entity

import androidx.room.Entity
import kr.b1ink.domain.model.SiteType

@Entity(
    tableName = "board_read",
    primaryKeys = ["id", "type"]
)
data class ListReadEntity(
    val id: Long = 0,
    val type: SiteType
)
