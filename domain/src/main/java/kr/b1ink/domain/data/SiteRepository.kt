package kr.b1ink.domain.data

import androidx.paging.Pager
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.DetailData
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType

interface SiteRepository {
    suspend fun getMainData(siteType: SiteType): Result<List<MainItem>>

    suspend fun getMainDataFromJson(siteType: SiteType): Result<List<MainItem>>

    suspend fun setMainData(siteType: SiteType, list: List<MainItem>)

    fun getListPager(siteType: SiteType, query: String): Pager<Int, ListItem>

    suspend fun getDetail(siteType: SiteType, board: String, id: Long): Result<DetailData>

    suspend fun markRead(siteType: SiteType, id: Long)
}