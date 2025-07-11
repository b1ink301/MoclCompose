package kr.b1ink.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.b1ink.data.base.DtoMapper
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType

@Entity(
    tableName = "board_link",
    primaryKeys = ["board", "siteType"]
)
data class MainItemEntity(
    val board: String,
    val title: String,
    val url: String,
    val type: Int,
    val siteType: SiteType = SiteType.None,
    val no: Int = 0,
): DtoMapper<MainItemEntity, MainItem> {
    override fun MainItemEntity.mapping(): MainItem = toDto()
}

fun MainItemEntity.toDto() =
    MainItem(
        id = no.toLong(),
        title = title,
        url = url,
        board = board,
        type = type,
        siteType = siteType,
        no = no
    )

fun MainItem.toEntity() =
    MainItemEntity(
        board = board,
        title = title,
        url = url,
        type = type,
        siteType = siteType,
        no = no
    )