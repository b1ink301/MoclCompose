package kr.b1ink.data.site

import androidx.paging.Pager
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.DetailData
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class SiteRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : SiteRepository {

    override suspend fun getMainData(siteType: SiteType): Result<List<MainItem>> = when (siteType) {
        SiteType.NaverCafe -> remoteDataSource.getMain(siteType)

        else -> localDataSource.getMainData(siteType)
    }

    override suspend fun setMainData(siteType: SiteType, list: List<MainItem>) =
        localDataSource.setMainData(siteType, list)

    override fun getListPager(siteType: SiteType, query: String): Pager<Int, ListItem> =
        remoteDataSource.getListPager(siteType, query)

    override suspend fun getDetail(siteType: SiteType, board: String, id: Long): Result<DetailData> =
        remoteDataSource.getDetail(siteType, board, id)

    override suspend fun getMainDataFromJson(siteType: SiteType): Result<List<MainItem>> =
        localDataSource.getMainDataFromJson(siteType)

    override suspend fun markRead(siteType: SiteType, id: Long) = localDataSource.markRead(siteType, id)
}