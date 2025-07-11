package kr.b1ink.data.site.damoang

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DamoangApi {
    //    @Headers("User-Agent: Mozilla/5.0 (Linux; U; Android 13; en-US; SM-F907B Build/TPB3.22.01-01) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.124 Mobile Safari/537.36")
    @GET("/{board}")
    suspend fun list(
        @Path("board") board: String,
        @Query("page") page: Int = 1,
    ): String

    @GET("/{board}/{id}")
    suspend fun detail(
        @Path("board") board: String,
        @Path("id") id: Long
    ): String

    @GET("board/{boardCd}/{boardSn}/comment")
    suspend fun comments(
        @Path("boardCd") boardCd: String,
        @Path("boardSn") boardSn: Long = 0,
        @Query("po") po: Long = 0,
        @Query("ps") od: Long = 100,
        @Query("order") order: String? = "date"
    ): String
}
