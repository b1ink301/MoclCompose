package kr.b1ink.data.di.http

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.BuildConfig
import kr.b1ink.data.di.http.qualifier.NaverCafeBaseUrl
import kr.b1ink.data.di.http.qualifier.NaverCafeOkHttpClient
import kr.b1ink.data.di.http.qualifier.NaverCafeRetrofitClient
import kr.b1ink.data.site.navercafe.NaverCafeApi
import kr.b1ink.data.util.WebViewPersistentCookieJar
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NaverCafeModule {
    @NaverCafeBaseUrl
    @Provides
    fun providesBaseUrl(): String = BuildConfig.NAVER_CAFE_API_URL

    @NaverCafeOkHttpClient
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
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:126.0) Gecko/20100101 Firefox/126.0"
                    )
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @NaverCafeRetrofitClient
    @Provides
    @Singleton
    fun provideRetrofitClient(
        @NaverCafeOkHttpClient okHttpClient: OkHttpClient,
        @NaverCafeBaseUrl baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideNaverCafeApi(
        @NaverCafeRetrofitClient retrofit: Retrofit
    ): NaverCafeApi = retrofit.create(NaverCafeApi::class.java)
}