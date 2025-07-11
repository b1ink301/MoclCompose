package kr.b1ink.data.site

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kr.b1ink.data.base.safeApiCall
import kr.b1ink.data.base.safeApiCallList
import kr.b1ink.data.db.dao.ListReadDao
import kr.b1ink.data.di.coroutine.IoDispatcher
import kr.b1ink.data.site.base.ApiFactory
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.DetailData
import kr.b1ink.domain.model.ListItem
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import timber.log.Timber
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationContext private val context: Context,
    private val listReadDao: ListReadDao,
    private val apiFactory: ApiFactory
) {
    private var lastId = -1L

    private fun getPage(siteType: SiteType, page: Int) = when (siteType) {
        SiteType.Clien -> page
        else -> page + 1
    }

    private fun ListItemDto.checkRead(siteType: SiteType, id: Long): ListItemDto =
        if (listReadDao.getReadCount(siteType, id) > 0) {
            copy(isRead = true)
        } else {
            this
        }

    fun getListPager(siteType: SiteType, query: String): Pager<Int, ListItem> {
        lastId = -1L

        Timber.d("getListPager() siteType=$siteType, query=$query")

        return Pager(
            config = PagingConfig(pageSize = 7, prefetchDistance = 5, initialLoadSize = 20, enablePlaceholders = false)
        ) {
            val api = apiFactory.createApi(siteType)

            ListPagingSource(query = query, coroutineDispatcher = dispatcher) { query, index ->
                if (index == 0) lastId = -1L

                val data =
                    api.getList(board = query, page = getPage(siteType = siteType, page = index), lastId = lastId)
                        .map {
                            it.checkRead(siteType = siteType, id = it.id)
                                .run { mapping() }
                        }
                        .toImmutableList()

                data.lastOrNull()
                    ?.let {
                        lastId = it.id
                    }

                return@ListPagingSource data to api.hasNoPage(query)
            }
        }
    }

    suspend fun getDetail(siteType: SiteType, board: String, id: Long): Result<DetailData> =
        safeApiCall(context = context, coroutineDispatcher = dispatcher) {
            apiFactory.createApi(siteType)
                .getDetail(board, id)
        }

    suspend fun getMain(siteType: SiteType): Result<List<MainItem>> =
        safeApiCallList(context = context, coroutineDispatcher = dispatcher) {
            apiFactory.createApi(siteType)
                .getMain()
        }
}