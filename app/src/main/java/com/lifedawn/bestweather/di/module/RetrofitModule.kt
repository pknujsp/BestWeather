package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideRainViewerApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.RAIN_VIEWER)

    @Provides
    @Singleton
    fun provideKmaWebCurrentConditionsApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)

    @Provides
    @Singleton
    fun provideKmaWebForecastsApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)

    @Provides
    @Singleton
    fun provideMetNorwayApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST)

    @Provides
    @Singleton
    fun provideAqicnApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED)


    @Provides
    @Singleton
    fun provideOwmOneCallApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_ONE_CALL)

    @Provides
    @Singleton
    fun provideFlickrApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR)

    @Provides
    @Singleton
    fun provideFreeTimeApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.FREE_TIME)

    @Provides
    @Singleton
    fun provideNominatimApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.NOMINATIM)
}