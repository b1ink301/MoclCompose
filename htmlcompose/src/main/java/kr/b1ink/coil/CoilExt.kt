package kr.b1ink.coil

import coil3.Extras
import coil3.ImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import okhttp3.OkHttpClient

fun interface ProgressListener {
    fun onProgress(progress: Float)
}

// 파일의 최상단이나 관련 객체 내부에 정의합니다.
val progressListenerKey = Extras.Key<ProgressListener?>(
    default = null
)

/**
 * ImageRequest에 진행률 리스너를 설정하는 확장 함수입니다.
 *
 * @param listener 진행률 업데이트를 수신할 리스너.
 */
fun ImageRequest.Builder.initProgress(progressId: String): ImageRequest.Builder {
    val headers = NetworkHeaders.Builder()
        .set(HEADER_X_PROGRESS_ID, progressId)
        .build()
    httpHeaders(headers)
//    extras[progressListenerKey] = listener
//    Timber.d("[progressListener] extras=$extras, progressListenerKey=$progressListenerKey, listener=$listener")
    return this
}

fun ImageLoader.Builder.useProgressInterceptor(): ImageLoader.Builder {
    val progressOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ProgressInterceptor())
        .build()

    return this
        .components {
            add(OkHttpNetworkFetcherFactory(progressOkHttpClient))
        }
}