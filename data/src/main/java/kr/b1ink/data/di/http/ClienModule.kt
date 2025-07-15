package kr.b1ink.data.di.http

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.BuildConfig
import kr.b1ink.data.di.http.qualifier.ClienBaseUrl
import kr.b1ink.data.di.http.qualifier.ClienOkHttpClient
import kr.b1ink.data.di.http.qualifier.ClienRetrofitClient
import kr.b1ink.data.site.clien.ClienApi
import kr.b1ink.data.util.WebViewPersistentCookieJar
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ClienModule {

    @ClienBaseUrl
    @Provides
    fun providesBaseUrl(): String = BuildConfig.CLIEN_API_URL

    @Provides
    fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        val level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.HEADERS // debug log level
        else
            HttpLoggingInterceptor.Level.NONE
        return HttpLoggingInterceptor().setLevel(level)
    }

    @Provides
    @Singleton
    fun provideCookieJar(
        @ApplicationContext context: Context
    ) = WebViewPersistentCookieJar(context)
//        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

    @ClienOkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        cookieJar: WebViewPersistentCookieJar
    ): OkHttpClient {
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
            )
            .build()

        return OkHttpClient.Builder()
            .followSslRedirects(true)
            .followRedirects(true)
//            .retryOnConnectionFailure(true)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS))
            .addInterceptor(loggingInterceptor)
            .hostnameVerifier { hostname, session ->
                Timber.d("hostname: $hostname")
                if (hostname == "m.clien.net") {
                    true // 이 호스트 이름만 신뢰
                } else {
                    OkHostnameVerifier.verify(hostname, session)
                }
            }
//            .addInterceptor { chain ->
//                val request = chain.request()
//                    .newBuilder()
//                    .header(
//                        "User-Agent",
//                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:126.0) Gecko/20100101 Firefox/126.0"
//                    )
//                    .build()
//                chain.proceed(request)
//            }
            .build()
    }

    @ClienRetrofitClient
    @Provides
    @Singleton
    fun provideRetrofitClient(
        @ClienOkHttpClient okHttpClient: OkHttpClient,
        @ClienBaseUrl baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideClienApi(
        @ClienRetrofitClient retrofit: Retrofit
    ): ClienApi = retrofit.create(ClienApi::class.java)
}