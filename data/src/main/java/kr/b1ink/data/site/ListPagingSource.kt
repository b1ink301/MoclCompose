package kr.b1ink.data.site

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kr.b1ink.domain.model.ListItem

class ListPagingSource(
    private val query: String,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val getListCallback: suspend (query: String, position: Int) -> Pair<List<ListItem>, Boolean>,
) : PagingSource<Int, ListItem>() {

    override fun getRefreshKey(state: PagingState<Int, ListItem>): Int? = 0

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, ListItem> = withContext(coroutineDispatcher) {
        try {
            val position: Int = when (params) {
                is LoadParams.Refresh -> params.key ?: 0

                is LoadParams.Append -> params.key

                is LoadParams.Prepend -> params.key
            }

            if (query.isBlank()) {
                LoadResult.Error(Error("게시판을 선택해 주세요!"))
            } else {
                val (list, hasNoPage) = getListCallback(query, position)

                val prevKey = if (position == 0) {
                    null
                } else {
                    position - 1
                }

                val nextKey = if (hasNoPage || list.isEmpty()) {
                    null
                } else {
                    position + 1
                }

                LoadResult.Page(data = list, prevKey = prevKey, nextKey = nextKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}