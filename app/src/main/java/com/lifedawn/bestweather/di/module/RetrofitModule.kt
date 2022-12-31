package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideRainViewerRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.RAIN_VIEWER)

    @Provides
    @Singleton
    fun provideKmaWebCurrentConditionsRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)

    @Provides
    @Singleton
    fun provideKmaWebForecastsRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)

    @Provides
    @Singleton
    fun provideMetNorwayRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST)

    @Provides
    @Singleton
    fun provideAqicnRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED)


    @Provides
    @Singleton
    fun provideOwmOneCallRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_ONE_CALL)

    @Provides
    @Singleton
    fun provideFlickrRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR)

    @Provides
    @Singleton
    fun provideFreeTimeRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.FREE_TIME)

    @Provides
    @Singleton
    fun provideNominatimRestApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.NOMINATIM)
}