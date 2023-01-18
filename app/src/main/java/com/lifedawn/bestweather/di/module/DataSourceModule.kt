package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.remote.nominatim.datasource.NominatimDataSource
import com.lifedawn.bestweather.data.remote.nominatim.datasource.NominatimDataSourceImpl
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.weather.aqicn.datasource.AqicnDataSource
import com.lifedawn.bestweather.data.remote.weather.aqicn.datasource.AqicnDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSource
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSource
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSource
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
    ): KmaDataSource = KmaDataSourceImpl(kmaWebCurrentConditionsRestApi, kmaWebForecastsRestApi)

    @Provides
    @Singleton
    fun provideOwmDataSource(owmOneCallRestApi: RestfulApiQuery): OwmDataSource = OwmDataSourceImpl(owmOneCallRestApi)

    @Provides
    @Singleton
    fun provideMetNorwayDataSource(metNorwayRestApi: RestfulApiQuery): MetNorwayDataSource = MetNorwayDataSourceImpl(metNorwayRestApi)

    @Provides
    @Singleton
    fun provideAqicnDataSource(aqicnRestApi: RestfulApiQuery): AqicnDataSource = AqicnDataSourceImpl(aqicnRestApi)

    @Provides
    @Singleton
    fun provideNominatimDataSource(nominatimRestApi:RestfulApiQuery) : NominatimDataSource =
        NominatimDataSourceImpl(nominatimRestApi)
}