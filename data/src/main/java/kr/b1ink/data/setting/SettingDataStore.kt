package kr.b1ink.data.setting

import android.content.Context
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.b1ink.domain.model.SiteType
import javax.inject.Inject

class SettingDataStore @Inject constructor(
    @ApplicationContext context: Context,
) : KotprefModel(context) {
    var siteType by enumValuePref(default = SiteType.Damoang)
}