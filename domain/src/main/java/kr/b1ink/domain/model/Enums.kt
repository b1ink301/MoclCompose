package kr.b1ink.domain.model

enum class SiteType {
    Clien,
    Damoang,
    NaverCafe,
    Meeco,
    None,
}

fun SiteType.title() = when (this) {
    SiteType.Clien -> "클리앙"
    SiteType.Damoang -> "다모앙"
    SiteType.NaverCafe -> "네이버카페"
    SiteType.Meeco -> "미코"
    SiteType.None -> ""
}

enum class MainNavOption {
    AboutScreen,
    SettingsScreen,
    SiteScreen,
}

enum class NavScreen {
    Main,
    List,
    Detail,
    Login,
}
