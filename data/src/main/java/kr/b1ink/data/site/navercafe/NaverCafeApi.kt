package kr.b1ink.data.site.navercafe

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NaverCafeApi {
    @GET("cafe-home-web/cafe-home/v1/cafes/join")
    suspend fun cafes(@Query("perPage") perPage: Long = 300): CafesRes

    @GET("cafe-web/cafe2/ArticleListV2dot1.json")
    suspend fun list(
        @Query("search.clubid") clubId: Long,
        @Query("search.queryType") queryType: String = "lastArticle",
        @Query("search.perPage") perPage: Int = 20,
        @Query("uuid") uuid: String = "6dd62de1-7279-49f0-b009-6ccc554ac679",
        @Query("search.page") page: Int = 1,
        @Query("ad") ad: Boolean = false,
    ): CafeListRes

    @GET("cafe-web/cafe-articleapi/v3/cafes/{clubId}/articles/{articleId}")
    suspend fun detail(
        @Path("clubId") clubId: String,
        @Path("articleId") articleId: Long,
    ): CafesDetailRes

    @GET("cafe-web/cafe-articleapi/v3/cafes/{clubId}/articles/{articleId}/comments")
    suspend fun comments(
        @Path("clubId") clubId: String,
        @Path("articleId") articleId: Long,
    ): CafesDetailCommentRes
}