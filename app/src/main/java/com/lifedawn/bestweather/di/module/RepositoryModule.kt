package com.lifedawn.bestweather.di.module

import android.content.Context
import com.lifedawn.bestweather.data.local.favoriteaddress.repository.FavoriteAddressRepository
import com.lifedawn.bestweather.data.local.favoriteaddress.repository.FavoriteAddressRepositoryImpl
import com.lifedawn.bestweather.data.local.room.AppDb
import com.lifedawn.bestweather.data.local.room.dao.FavoriteAddressDao
import com.lifedawn.bestweather.data.local.room.queryinterfaces.KmaAreaCodesRepository
import com.lifedawn.bestweather.data.local.room.repository.KmaAreaCodesRepositoryImpl
import com.lifedawn.bestweather.data.remote.rainviewer.repository.RainViewerRepository
import com.lifedawn.bestweather.data.remote.rainviewer.repository.RainViewerRepositoryImpl
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.local.timezone.LocalTimeZoneRepository
import com.lifedawn.bestweather.data.local.timezone.LocalTimeZoneRepositoryImpl
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepository
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepositoryImpl
import com.lifedawn.bestweather.data.remote.nominatim.datasource.NominatimDataSource
import com.lifedawn.bestweather.data.remote.nominatim.repository.NominatimRepository
import com.lifedawn.bestweather.data.remote.nominatim.repository.NominatimRepositoryImpl
import com.lifedawn.bestweather.data.remote.timezone.RemoteTimeZoneRepository
import com.lifedawn.bestweather.data.remote.timezone.RemoteTimeZoneRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.aqicn.datasource.AqicnDataSource
import com.lifedawn.bestweather.data.remote.weather.aqicn.repository.AqicnRepository
import com.lifedawn.bestweather.data.remote.weather.aqicn.repository.AqicnRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.kma.datasource.KmaDataSource
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepository
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.metnorway.datasource.MetNorwayDataSource
import com.lifedawn.bestweather.data.remote.weather.metnorway.repository.MetNorwayRepository
import com.lifedawn.bestweather.data.remote.weather.metnorway.repository.MetNorwayRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.owm.datasource.OwmDataSource
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

    @Provides
    @Singleton
    fun provideMetNorwayRepository(
        metNorwayDataSource: MetNorwayDataSource,
        @ApplicationContext context: Context
    ): MetNorwayRepository = MetNorwayRepositoryImpl(metNorwayDataSource, context)

    @Provides
    @Singleton
    fun provideAqicnRepository(
        aqicnDataSource: AqicnDataSource
    ): AqicnRepository = AqicnRepositoryImpl(aqicnDataSource)

    @Provides
    @Singleton
    fun provideFavoriteAddressRepository(
        favoriteAddressDao: FavoriteAddressDao
    ): FavoriteAddressRepository = FavoriteAddressRepositoryImpl(favoriteAddressDao)

    @Provides
    @Singleton
    fun provideNominatimRepository(
        nominatimDataSource: NominatimDataSource
    ): NominatimRepository = NominatimRepositoryImpl(nominatimDataSource)
}