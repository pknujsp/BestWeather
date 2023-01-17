package com.lifedawn.bestweather.di.module

import android.content.Context
import com.lifedawn.bestweather.commons.classes.location.FusedLocation
import com.lifedawn.bestweather.commons.classes.NetworkStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedModule {

    @Provides
    @Singleton
    fun provideFusedLocation(
        @ApplicationContext context: Context,
        networkStatus: NetworkStatus
    ) = FusedLocation(context, networkStatus)

    @Provides
    @Singleton
    fun provideNetworkStatus(@ApplicationContext context: Context) = NetworkStatus(context)
}