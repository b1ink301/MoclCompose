package kr.b1ink.data.site.navercafe

import com.google.gson.annotations.SerializedName
import kr.b1ink.data.util.toTimeAgo
import kr.b1ink.domain.model.DetailComment
import kr.b1ink.domain.model.UserInfo
import java.util.Date

data class CafesDetailRes(
    @SerializedName("result") val result: Result,
) {
    data class Result(
        @SerializedName("errorCode") val errorCode: String? = null,
        @SerializedName("reason") val reason: String? = null,
        @SerializedName("message") val message: String? = null,
        @SerializedName("cafeId") val cafeId: Long,
        @SerializedName("articleId") val articleId: Long,
        @SerializedName("menuId") val menuId: Long = 0,
        @SerializedName("cafe") val cafe: Cafe,
        @SerializedName("user") val user: User,
        @SerializedName("article") val article: Article,
        @SerializedName("comments") val comments: Comments,
//        @Transient
//        val attaches: List<String> = listOf(),
//        @Transient
//        val tag: List<String> = listOf(),
        @SerializedName("isReadOnlyMode") val isReadOnlyMode: Boolean = false,
    ) {
        fun isSuccess(): Boolean = errorCode == null

        data class User(
            @SerializedName("memberKey") val memberKey: String,
            @SerializedName("memberLevelName") val memberLevelName: String,
            @SerializedName("nick") val nick: String,
            @SerializedName("image") val image: Image,
            @SerializedName("isCafeMember") val isCafeMember: Boolean = false,
            @SerializedName("isReadisLoginOnlyMode") val isLogin: Boolean = false,
        )

        data class Cafe(
            @SerializedName("id") val id: Long,
            @SerializedName("name") val name: String,
            @SerializedName("image") val image: Image,
        )

        data class Article(
            @SerializedName("id") val id: Long,
            @SerializedName("refArticleId") val refArticleId: Long,
            @SerializedName("subject") val subject: String,
            @SerializedName("writer") val writer: Writer,
            @SerializedName("writeDate") val writeDate: Long,
            @SerializedName("readCount") val readCount: Long,
            @SerializedName("commentCount") val commentCount: Long,
            @SerializedName("contentHtml") val contentHtml: String,
            @SerializedName("isNotice") val isNotice: Boolean = false,
            @SerializedName("isNewComment") val isNewComment: Boolean = false,
            @SerializedName("isDeleteParent") val isDeleteParent: Boolean = false,
            @SerializedName("isReadable") val isReadable: Boolean = false,
            @SerializedName("isBlind") val isBlind: Boolean = false,
            @SerializedName("isOpen") val isOpen: Boolean = false,
            @SerializedName("isWriteComment") val isWriteComment: Boolean = false,
        )
    }

    data class Writer(
        @SerializedName("memberKey") val memberKey: String,
        @SerializedName("nick") val nick: String,
        @SerializedName("image") val image: Image? = null,
        @SerializedName("memberLevelName") val memberLevelName: String,
        @SerializedName("memberLevelIconUrl") val memberLevelIconUrl: String,
    )

    data class Image(
        @SerializedName("url") val url: String,
        @SerializedName("service") val service: String,
        @SerializedName("type") val type: String,
        @SerializedName("isAnimated") val isAnimated: Boolean = false
    )
}

data class Comments(
    @SerializedName("items") val items: List<Comment>,
    @SerializedName("next") val next: Next,
    @SerializedName("last") val last: Last,
) {
    data class Next(
        @SerializedName("id") val id: Long,
        @SerializedName("refId") val refId: Long,
    )

    data class Last(
        @SerializedName("id") val id: Long,
        @SerializedName("refId") val refId: Long,
    )

    data class Comment(
        @SerializedName("id") val id: Long,
        @SerializedName("refId") val refId: Long,
        @SerializedName("writer") val writer: CafesDetailRes.Writer,
        @SerializedName("content") val content: String,
        @SerializedName("image") val image: CafesDetailRes.Image? = null,
        @SerializedName("originalImage") val originalImage: CafesDetailRes.Image,
        @SerializedName("sticker") val sticker: Sticker? = null,
        @SerializedName("updateDate") val updateDate: Long,
        @SerializedName("isRef") val isRef: Boolean = false,
        @SerializedName("isArticleWriter") val isArticleWriter: Boolean = false,
        @SerializedName("isNew") val isNew: Boolean = false,
        @SerializedName("isRemovable") val isRemovable: Boolean = false,
    ) {
        data class Sticker(
            @SerializedName("id") val id: String,
            @SerializedName("url") val url: String,
            @SerializedName("type") val type: String,
            @SerializedName("width") val width: Int,
            @SerializedName("height") val height: Int,
        )
    }
}

data class CafesDetailCommentRes(
    @SerializedName("hasPrev") val hasPrev: Boolean = false,
    @SerializedName("hasNext") val hasNext: Boolean = false,
    @SerializedName("result") val result: CafesDetailRes.Result,
)

fun Comments.Comment.toDetailComment(): DetailComment {
    var bodyHtml = content
    if (image != null) {
        bodyHtml += "<br><img src='${image.url}' width=\"240\" >"
    }
    if (sticker != null) {
        bodyHtml +=
            "<br><img src='${sticker.url}?type=${sticker.type}' width=\"129\" >"
    }
    val time = Date(updateDate).toTimeAgo()
    return DetailComment(
        id = id,
        bodyHtml = bodyHtml,
        time = updateDate.toString(),
        info = "${writer.nick}ㆍ$time",
        likeCount = "",
        userInfo = UserInfo(
            id = writer.memberKey,
            nickName = writer.nick,
            nickImage = writer.image?.url ?: ""
        ),
        isReply = isRef,
    )
}