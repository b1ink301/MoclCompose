package kr.b1ink.data.base

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

@SuppressLint("MissingPermission", "ObsoleteSdkInt")
fun isInternetConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork: Network? = connectivityManager.activeNetwork
        if (activeNetwork == null) {
            // 활성 네트워크가 없음 (인터넷 연결 안됨)
            return false
        }
        val networkCapabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities == null) {
            // 네트워크 기능 정보를 가져올 수 없음 (연결 상태 불확실, 안전하게 false 처리)
            return false
        }
        // 실제로 인터넷에 연결되어 있는지 확인
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        // API 레벨 23 (M) 미만 버전 (deprecated된 API 사용)
        @Suppress("DEPRECATION")
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
}