package kr.b1ink.mocl.ui.site.login

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.EXTRAS_SITE_TYPE
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
//    private val saveCookies: SaveCookies,
//    private val clearCookies: ClearCookies,
) : ViewModel() {
    private val siteType =
        savedStateHandle.get<SiteType>(EXTRAS_SITE_TYPE) ?: throw IllegalArgumentException("siteType is null")

    val url: String = when (siteType) {
        SiteType.Clien -> "https://m.clien.net/service/mypage/myInfo"
        SiteType.Damoang -> "https://damoang.net/bbs/login.php?url=/bbs/memo.php"
        SiteType.NaverCafe -> "https://nid.naver.com/mobile/user/help/naverProfile.nhn?lang=ko_KR"
        SiteType.Meeco -> "https://meeco.kr/index.php?mid=index&act=dispMemberLoginForm"
        SiteType.None -> ""
    }

    val headers: Map<String, String> = when (siteType) {
        SiteType.Clien -> mapOf(
            "Referer" to
                    "https://m.clien.net/service/mypage/myInfo",
            "ContentType" to "application/x-www-form-urlencoded"
        )

        SiteType.Damoang -> mapOf(
            "Referer" to
                    "https://damoang.net/bbs/memo.php",
            "ContentType" to "application/x-www-form-urlencoded"
        )

        SiteType.NaverCafe -> mapOf(
            "Referer" to
                    "https://nid.naver.com/mobile/user/help/naverProfile.nhn?lang=ko_KR",
            "ContentType" to "application/x-www-form-urlencoded"
        )

        SiteType.Meeco -> emptyMap()

        SiteType.None -> emptyMap()
    }

//    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.Loading)
//    val uiState: StateFlow<LoginUiState> = _uiState

    fun isLogin(url: String?): Boolean {
        val path = url?.toUri()?.path ?: return false

        return when (siteType) {
            SiteType.Clien -> path == "/service/mypage/myInfo"
            SiteType.Damoang -> path == "/bbs/memo.php"
            SiteType.NaverCafe -> path == "/user2/help/myInfoV2"
            SiteType.Meeco -> path == "/"
            SiteType.None -> false
        }
    }
}