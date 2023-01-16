package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.local.room.queryinterfaces.KmaAreaCodesRepository
import com.lifedawn.bestweather.data.remote.weather.aqicn.repository.AqicnRepository
import com.lifedawn.bestweather.data.remote.weather.aqicn.repository.AqicnRepositoryImpl
import com.lifedawn.bestweather.data.remote.weather.aqicn.usecase.GetAqicnUseCase
import com.lifedawn.bestweather.data.remote.weather.aqicn.usecase.GetAqicnUseCaseImpl
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetKmaWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.kma.repository.KmaWeatherRepository
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetKmaWeatherUseCaseImpl
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetAreaCodeUseCase
import com.lifedawn.bestweather.data.remote.weather.kma.usecase.GetAreaCodeUseCaseImpl
import com.lifedawn.bestweather.data.remote.weather.metnorway.repository.MetNorwayRepository
import com.lifedawn.bestweather.data.remote.weather.metnorway.usecase.GetMetNorwayWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.metnorway.usecase.GetMetNorwayWeatherUseCaseImpl
import com.lifedawn.bestweather.data.remote.weather.owm.repository.OwmWeatherRepository
import com.lifedawn.bestweather.data.remote.weather.owm.usecase.GetOwmWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.owm.usecase.GetOwmWeatherUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetKmaWeatherUseCase(weatherRepository: KmaWeatherRepository): GetKmaWeatherUseCase =
        GetKmaWeatherUseCaseImpl(weatherRepository)

    @Provides
    @Singleton
    fun provideGetOwmWeatherUseCase(weatherRepository: OwmWeatherRepository): GetOwmWeatherUseCase =
        GetOwmWeatherUseCaseImpl(weatherRepository)

    @Provides
    @Singleton
    fun provideGetMetNorwayWeatherUseCase(weatherRepository: MetNorwayRepository): GetMetNorwayWeatherUseCase =
        GetMetNorwayWeatherUseCaseImpl(weatherRepository)

    @Provides
    @Singleton
    fun provideGetAqicnUseCase(aqicnRepository: AqicnRepository): GetAqicnUseCase =
        GetAqicnUseCaseImpl(aqicnRepository)

    @Provides
    @Singleton
    fun provideGetAreaCodeUseCase(kmaAreaCodesRepository: KmaAreaCodesRepository): GetAreaCodeUseCase =
        GetAreaCodeUseCaseImpl(kmaAreaCodesRepository)
}