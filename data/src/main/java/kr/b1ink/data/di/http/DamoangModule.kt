package kr.b1ink.data.di.http

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.BuildConfig
import kr.b1ink.data.di.http.qualifier.ClienOkHttpClient
import kr.b1ink.data.di.http.qualifier.DamoangBaseUrl
import kr.b1ink.data.di.http.qualifier.DamoangRetrofitClient
import kr.b1ink.data.site.damoang.DamoangApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DamoangModule {

    @DamoangBaseUrl
    @Provides
    fun providesBaseUrl(): String = BuildConfig.DAMOANG_API_URL

//    @DamoangOkHttpClient
//    @Provides
//    @Singleton
//    fun provideOkHttpClient(
//        @ApplicationContext context: Context,
//        loggingInterceptor: HttpLoggingInterceptor
//    ): OkHttpClient {
//        val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
//
//        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//            .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
//            .cipherSuites(
//                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
//                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
//                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
//            )
//            .build()
//
//        return OkHttpClient.Builder()
//            .followSslRedirects(true)
//            .followRedirects(true)
////            .retryOnConnectionFailure(true)
//            .writeTimeout(10, TimeUnit.SECONDS)
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(10, TimeUnit.SECONDS)
//            .cookieJar(cookieJar)
//            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS))
//            .addInterceptor(loggingInterceptor)
//            .build()
//    }

    @DamoangRetrofitClient
    @Provides
    @Singleton
    fun provideRetrofitClient(
        @ClienOkHttpClient okHttpClient: OkHttpClient,
        @DamoangBaseUrl baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
//        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideDamoangApi(
        @DamoangRetrofitClient retrofit: Retrofit
    ): DamoangApi = retrofit.create(DamoangApi::class.java)
}