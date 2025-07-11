package kr.b1ink.data.di.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.b1ink.data.site.base.BaseParser
import kr.b1ink.data.site.clien.ClienParserImpl
import kr.b1ink.data.site.damoang.DamoangParserImpl
import kr.b1ink.data.site.meeco.MeecoParserImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ParserModule {
    @Singleton
    @Binds
    abstract fun bindDamoangParser(impl: DamoangParserImpl): BaseParser

    @Singleton
    @Binds
    abstract fun bindClienParser(impl: ClienParserImpl): BaseParser

    @Singleton
    @Binds
    abstract fun bindMeecoParser(impl: MeecoParserImpl): BaseParser
}