package com.lifedawn.bestweather.di.module

import android.content.Context
import com.lifedawn.bestweather.data.local.room.AppDb
import com.lifedawn.bestweather.data.local.room.queryinterfaces.KmaAreaCodesRepository
import com.lifedawn.bestweather.data.local.room.repository.KmaAreaCodesRepositoryImpl
import com.lifedawn.bestweather.data.remote.rainviewer.repository.RainViewerRepository
import com.lifedawn.bestweather.data.remote.rainviewer.repository.RainViewerRepositoryImpl
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.local.timezone.LocalTimeZoneRepository
import com.lifedawn.bestweather.data.local.timezone.LocalTimeZoneRepositoryImpl
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepository
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepositoryImpl
import com.lifedawn.bestweather.data.remote.timezone.RemoteTimeZoneRepository
import com.lifedawn.bestweather.data.remote.timezone.RemoteTimeZoneRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSource
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepository
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSource
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSourceImpl
import com.lifedawn.bestweather.data.remote.weather.owm.repository.OwmWeatherRepository
import com.lifedawn.bestweather.data.remote.weather.owm.repository.OwmWeatherRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideRainViewerRepository(rainViewerApi: RestfulApiQuery): RainViewerRepository = RainViewerRepositoryImpl(rainViewerApi)


    @Provides
    @Singleton
    fun provideTimeZoneIdRepository(room: AppDb): LocalTimeZoneRepository =
        LocalTimeZoneRepositoryImpl(room)


    @Provides
    @Singleton
    fun provideRemoteTimeZoneRepository(freeTimeRestApi: RestfulApiQuery): RemoteTimeZoneRepository =
        RemoteTimeZoneRepositoryImpl(freeTimeRestApi)

    @Provides
    @Singleton
    fun provideFlickrRepository(@ApplicationContext context: Context): FlickrRepository =
        FlickrRepositoryImpl(context)


    @Provides
    @Singleton
    fun provideKmaAreaCodesRepository(room: AppDb): KmaAreaCodesRepository = KmaAreaCodesRepositoryImpl(room)

    @Provides
    @Singleton
    fun provideKmaWeatherRepository(
        kmaDataSource: KmaDataSource,
        @ApplicationContext context: Context
    ): KmaWeatherRepository = KmaWeatherRepositoryImpl(kmaDataSource, context)

    @Provides
    @Singleton
    fun provideOwmWeatherRepository(
        owmDataSource: OwmDataSource,
        @ApplicationContext context: Context
    ): OwmWeatherRepository = OwmWeatherRepositoryImpl(owmDataSource, context)
}