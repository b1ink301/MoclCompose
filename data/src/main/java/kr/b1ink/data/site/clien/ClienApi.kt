package kr.b1ink.data.site.clien

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClienApi {
    @GET("api/board/under/list")
    suspend fun list(
        @Query("boardCd") boardCd: String,
        @Query("boardSn") boardSn: Int = 0,
        @Query("po") po: Int = 0,           // page
        @Query("od") od: String = "T31",    // sort type
    ): String

    @GET("api/search/board/under/list")
    suspend fun search(
        @Query("boardCd") boardCd: String,
        @Query("boardSn") boardSn: Long = 0,
        @Query("po") po: Long = 0,          // page
        @Query("pt") pt: Int = 0,           // pt next search
        @Query("od") od: String = "T31",    // sort type
        @Query("sv") sv: String? = null,    // keyword
        @Query("sk") sk: String? = null,    // title type
    ): String

    @GET("board/{boardCd}/{boardSn}")
    suspend fun detail(
        @Path("boardCd") boardCd: String,
        @Path("boardSn") boardSn: Long = 0,
        @Query("po") po: Long = 0,
        @Query("od") od: String = "T31",
        @Query("sk") sk: String? = null,    // title
        @Query("sv") sv: String? = null     // keyword
    ): String

    @GET("board/{boardCd}/{boardSn}/comment")
    suspend fun comments(
        @Path("boardCd") boardCd: String,
        @Path("boardSn") boardSn: Long = 0,
        @Query("po") po: Long = 0,
        @Query("ps") od: Long = 100,
        @Query("order") order: String? = "date"
    ): String

    @GET("/service/recommend/list")
    suspend fun recommend(): String

    @GET("auth/login")
    suspend fun csrf(): String

    @GET("popup/userInfo/basic/{userId}")
    suspend fun userInfo(
        @Path("userId") userId: String,
    ): String

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("userId") userId: String,
        @Field("userPassword") userPassword: String,
        @Field("_csrf") _csrf: String,
        @Field("remember-me") remember: String = "on",
        @Field("deviceId") deviceId: String = "",
        @Field("totpcode") totpCode: String = "",
    ): String

    @FormUrlEncoded
    @POST("api/board/{boardCd}/{boardSn}/comment/regist")
    suspend fun comment(
        @Path("boardCd") boardCd: String,
        @Path("boardSn") boardSn: Long,
        @Field("boardSn") id: Long,
        @Field("param") param: String,
        @Header("X-CSRF-TOKEN") srf: String,
    ): String
}
