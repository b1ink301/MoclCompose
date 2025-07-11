package kr.b1ink.data.site.meeco

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MeecoApi {
    @GET("{board}")
    suspend fun list(
        @Path("board") board: String,
        @Query("page") page: Int,
    ): String

    @GET("{board}/{id}")
    suspend fun detail(
        @Path("board") board: String,
        @Path("id") id: Long
    ): String
}