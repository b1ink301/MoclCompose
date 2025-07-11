package kr.b1ink.coil

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

internal class ProgressInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
//        val listener = request.tag(Extras::class.java)?.get(progressListenerKey)
        val progressId = request.header(HEADER_X_PROGRESS_ID)

        Timber.d("[ProgressInterceptor] progressId=$progressId")

        val originalResponse = chain.proceed(request)

        // 리스너가 있고, 실제 네트워크 응답이 있는 경우에만 진행률 추적을 위한 ResponseBody로 교체합니다.
        // originalResponse.networkResponse가 null이면 캐시된 응답입니다.
        return if (progressId != null && originalResponse.networkResponse != null) {
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!, progressId))
                .build()
        } else {
            originalResponse
        }
    }
}