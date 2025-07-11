package kr.b1ink.common.ui.base

import android.webkit.JavascriptInterface
import kr.b1ink.common.util.decodeBase64String
import kr.b1ink.common.util.encodeBase64String
import org.json.JSONObject

abstract class BaseJavaInterface {
    companion object {
        const val EXTRA_DATA = "data"
        const val EXTRA_ACTION = "action"
    }

    enum class DefineFunc {
        Login,
        Logout,
        OpenWindow,
        OpenBrowser,
        ShareText,
        CloseWindow,
        GoHome,
        Clipboard,
        SystemAppSetting,
        FinishApp,
        TopMainBack,
        SetUserInfo,
        GetDeviceInfo,
        DeleteDeviceInfo,
        SetDeviceInfo,
        PopupWindowClose,
        CheckAppVersion,
        ToastMessage,
        ClearCache,
        AppVersion,
    }

    abstract fun close()

    abstract fun onCallback(defineFunc: DefineFunc, params: JSONObject? = null)

    @JavascriptInterface
    fun postMessage(params: String) = try {
        val decodeParams = params.decodeBase64String()
        println("[postMessage] decodeParams: $decodeParams")

        val jObject = JSONObject(decodeParams)
        val action = jObject.getString(EXTRA_ACTION)
            .toDefFunc()
        val data = jObject.optJSONObject(EXTRA_DATA)
        onCallback(action, data)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun String.toDefFunc(): DefineFunc = when (this) {
        "SAVE_TO_STORAGE" -> DefineFunc.SetDeviceInfo
        "LOAD_FROM_STORAGE" -> DefineFunc.GetDeviceInfo
        "GET_APP_VERSION" -> DefineFunc.CheckAppVersion
        "CLEAR_STORAGE" -> DefineFunc.ClearCache
        "DELETE_FROM_STORAGE" -> DefineFunc.DeleteDeviceInfo
        "FETCH_LATEST_VERSION" -> DefineFunc.AppVersion
        else -> throw IllegalArgumentException("$this is not a valid action")
    }

    private fun DefineFunc.toAction(): String = when (this) {
        DefineFunc.SetDeviceInfo -> "SAVE_TO_STORAGE"
        DefineFunc.GetDeviceInfo -> "LOAD_FROM_STORAGE"
        DefineFunc.CheckAppVersion -> "GET_APP_VERSION"
        DefineFunc.ClearCache -> "CLEAR_STORAGE"
        DefineFunc.DeleteDeviceInfo -> "DELETE_FROM_STORAGE"
        DefineFunc.AppVersion -> "FETCH_LATEST_VERSION"
        else -> throw IllegalArgumentException("$this is not a valid action")
    }

    fun DefineFunc.encodeBase64String(params: List<Pair<String, Any>>): String {
        val jsonOBJ = JSONObject()
        params.forEach {
            jsonOBJ.put(it.first, it.second)
        }
        val jsonOBJ2 = JSONObject()
        jsonOBJ2.put(EXTRA_ACTION, toAction())
        jsonOBJ2.put(EXTRA_DATA, jsonOBJ)
        val originData: String = jsonOBJ2.toString()
        return originData.encodeBase64String()
    }
}