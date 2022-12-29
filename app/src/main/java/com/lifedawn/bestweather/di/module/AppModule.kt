package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.models.rainviewer.repository.RainViewerRepository
import com.lifedawn.bestweather.data.models.rainviewer.repository.RainViewerRepositoryImpl
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRainViewerApi() =
        RetrofitClient.getApiService(RetrofitClient.ServiceType.RAIN_VIEWER)


    @Provides
    @Singleton
    fun provideRainViewerRepository(rainViewerApi: RestfulApiQuery): RainViewerRepository {
        return RainViewerRepositoryImpl(rainViewerApi)
    }

}