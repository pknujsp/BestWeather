package com.lifedawn.bestweather.di.module

import android.content.Context
import androidx.room.Room
import com.lifedawn.bestweather.data.local.datastore.AppSettingsDataStore
import com.lifedawn.bestweather.data.local.datastore.ValueUnitDataStore
import com.lifedawn.bestweather.data.local.room.AppDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    @Singleton
    fun provideRoom(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, "appdb")
            .createFromAsset("db/appdb.db").build()

    @Provides
    @Singleton
    fun provideFavoriteAddressDao(appDb: AppDb) = appDb.favoriteAddressDao()

    @Provides
    @Singleton
    fun provideValueUnitsDataStore(@ApplicationContext context: Context) = ValueUnitDataStore(context)

    @Provides
    @Singleton
    fun provideAppSettingsDataStore(@ApplicationContext context: Context) = AppSettingsDataStore(context)
}