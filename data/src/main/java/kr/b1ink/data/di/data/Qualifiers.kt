package kr.b1ink.data.di.data

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ClienApiQualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DamoangApiQualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NaverCafeApiQualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MeecoApiQualifier
