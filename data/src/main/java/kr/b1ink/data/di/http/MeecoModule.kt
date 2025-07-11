package kr.b1ink.data.di.http

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.BuildConfig
import kr.b1ink.data.di.http.qualifier.MeecoBaseUrl
import kr.b1ink.data.di.http.qualifier.MeecoOkHttpClient
import kr.b1ink.data.di.http.qualifier.MeecoRetrofitClient
import kr.b1ink.data.site.meeco.MeecoApi
import kr.b1ink.data.util.WebViewPersistentCookieJar
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object MeecoModule {
    @MeecoBaseUrl
    @Provides
    fun providesBaseUrl(): String = BuildConfig.MEECO_API_URL

    @MeecoOkHttpClient
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
                        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/AP2A.240905.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.58 Mobile Safari/537.36 Yappli/1673b203.20240919 (Linux; Android 14; Google Build/Pixel 8)"
                    )
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @MeecoRetrofitClient
    @Provides
    @Singleton
    fun provideRetrofitClient(
        @MeecoOkHttpClient okHttpClient: OkHttpClient,
        @MeecoBaseUrl baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideMeecoApi(
        @MeecoRetrofitClient retrofit: Retrofit
    ): MeecoApi = retrofit.create(MeecoApi::class.java)
}