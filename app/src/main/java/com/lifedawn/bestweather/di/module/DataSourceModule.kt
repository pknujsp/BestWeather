package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnDataSource
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideKmaDataSource(
        kmaWebCurrentConditionsRestApi: RestfulApiQuery,
        kmaWebForecastsRestApi: RestfulApiQuery
    ) = KmaDataSourceImpl(kmaWebCurrentConditionsRestApi, kmaWebForecastsRestApi)

    @Provides
    @Singleton
    fun provideOwmDataSource(owmOneCallRestApi: RestfulApiQuery) = OwmDataSourceImpl(owmOneCallRestApi)

    @Provides
    @Singleton
    fun provideMetNorwayDataSource(metNorwayRestApi: RestfulApiQuery) = MetNorwayDataSourceImpl(metNorwayRestApi)

    @Provides
    @Singleton
    fun provideAqicnDataSource(aqicnRestApi: RestfulApiQuery) = AqicnDataSourceImpl(aqicnRestApi)
}