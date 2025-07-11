package kr.b1ink.data.site.navercafe

import com.google.gson.annotations.SerializedName
import kr.b1ink.data.site.dto.MainItemDto
import kr.b1ink.domain.model.SiteType

data class CafesRes(
    @SerializedName("message") val message: Message,
) {
    data class Message(
        @SerializedName("status") val status: String,
        @SerializedName("error") val error: Error,
        @SerializedName("result") val result: Result? = null,
    ) {
        fun isSuccess() = status == "200" && result != null
        fun getList(): List<Cafe> = result?.cafe ?: emptyList()
    }

    data class Error(
        @SerializedName("code") val code: String = "",
        @SerializedName("msg") val msg: String = "",
    )

    data class Result(
        @SerializedName("pageInfo") val pageInfo: PageInfo,
        @SerializedName("cafes") val cafe: ArrayList<Cafe> = ArrayList(),
    ) {
        data class PageInfo(
            @SerializedName("page") val page: Int = 1,
            @SerializedName("perPage") val perPage: Int = 20,
            @SerializedName("totalCount") val totalCount: Int = 0,
            @SerializedName("lastPage") val lastPage: Boolean = false,
        )
    }

    data class Cafe(
        @SerializedName("cafeId") val cafeId: Long,
        @SerializedName("mobileCafeName") val cafeName: String,
        @SerializedName("cafeUrl") val cafeUrl: String,
        @SerializedName("cafeThumbnailMobileUrl") val cafeThumbnailMobileUrl: String
    )
}

fun CafesRes.Cafe.toMainItemDto(): MainItemDto = MainItemDto(
    id = cafeId,
    title = cafeName,
    url = cafeUrl,
    board = cafeId.toString(),
    siteType = SiteType.NaverCafe,
)