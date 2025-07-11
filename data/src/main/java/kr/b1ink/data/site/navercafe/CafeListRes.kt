package kr.b1ink.data.site.navercafe

import com.google.gson.annotations.SerializedName
import kr.b1ink.data.site.dto.ListItemDto
import kr.b1ink.data.util.toTimeAgo
import kr.b1ink.domain.model.UserInfo
import java.util.Date

data class CafeListRes(
    @SerializedName("message") val message: Message,
) {
    data class Message(
        @SerializedName("status") val status: String,
        @SerializedName("error") val error: Error,
        @SerializedName("result") val result: Result? = null,
    ) {
        fun isSuccess() = status == "200" && result != null
        fun getList() = result?.articleList ?: emptyList()
    }

    data class Error(
        @SerializedName("code") val code: String,
        @SerializedName("msg") val msg: String,
    ) {
        override fun toString(): String = "ListError(code='$code', msg='$msg')"
    }

    data class Result(
        @SerializedName("cafeId") val cafeId: Long,
        @SerializedName("cafeName") val cafeName: String,
        @SerializedName("cafeStaff") val cafeStaff: Boolean,
        @SerializedName("cafeMember") val cafeMember: Boolean,
        @SerializedName("hasNext") val hasNext: Boolean,
        @SerializedName("articleList") val articleList: List<CafeList> = emptyList(),
    )

    data class CafeList(
        @SerializedName("menuName") val category: String,
        @SerializedName("subject") val title: String,
        @SerializedName("memberKey") val userId: String,
        @SerializedName("writerNickname") val nickName: String,
        @SerializedName("writeDateTimestamp") val time: Long,
        @SerializedName("profileImage") val nickImage: String,
        @SerializedName("cafeId") val cafeId: Long,
        @SerializedName("articleId") val articleId: Long,
        @SerializedName("menuId") val menuId: Long,
        @SerializedName("commentCount") val reply: String,
        @SerializedName("readCount") val hit: String,
        @SerializedName("likeItCount") val like: String,
        @SerializedName("attachImage") var hasImage: Boolean = false,
        @Transient var isRead: Boolean = false
    ) {
        fun buildUrl() = "https://m.cafe.naver.com/ca-fe/web/cafes/$cafeId/articles/$articleId?boardtype=L"
    }
}

fun CafeListRes.CafeList.toListItemDto() = ListItemDto(
    id = articleId,
    title = title,
    url = buildUrl(),
    reply = reply,
    userInfo = UserInfo(
        userId,
        nickName,
        nickImage,
    ),
    info = "${nickName}ㆍ${Date(time).toTimeAgo()}ㆍ${hit} 읽음",
    category = category,
    time = time.toString(),
    board = cafeId.toString(),
    hasImage = hasImage,
    like = like,
    hit = hit,
)