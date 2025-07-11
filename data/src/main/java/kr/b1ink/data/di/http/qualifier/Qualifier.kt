package kr.b1ink.data.di.http.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClienBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClienRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClienOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DamoangBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DamoangRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DamoangOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverCafeBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverCafeRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverCafeOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MeecoBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MeecoOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MeecoRetrofitClient
