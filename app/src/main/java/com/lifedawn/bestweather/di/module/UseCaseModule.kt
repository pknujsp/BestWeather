package com.lifedawn.bestweather.di.module

import com.lifedawn.bestweather.data.remote.weather.commons.GetWeatherUseCase
import com.lifedawn.bestweather.data.remote.weather.commons.WeatherRepository
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
    fun provideGetWeatherUseCase(weatherRepository: WeatherRepository): GetWeatherUseCase = GetWeatherUseCase(weatherRepository)
}