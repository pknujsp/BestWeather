package com.lifedawn.bestweather

import android.app.Application
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.data.local.datastore.AppSettingsDataStore
import com.lifedawn.bestweather.data.local.datastore.ValueUnitDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class AppApplication : Application() {
    @Inject
    lateinit var valueUnitDataStore: ValueUnitDataStore

    @Inject
    lateinit var appSettingsDataStore: AppSettingsDataStore

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        lateinit var locale: Locale
        lateinit var localeCountryCode: String

        lateinit var tempUnit: ValueUnits
        lateinit var windSpeedUnit: ValueUnits
        lateinit var visibilityUnit: ValueUnits
        lateinit var clockUnit: ValueUnits

        var kmaIsTopPriority = true
        var enabledCurrentLocation = true
        var neverAskAgainAccessLocationPermission = true
        var enabledBackgroundAnimation = true
        var widgetRefreshInterval = 0L
    }

    override fun onCreate() {
        super.onCreate()
        locale = resources.configuration.locales[0]
        localeCountryCode = locale.country

        applicationScope.launch {
            // 앱 설정 값 로드
            // 대한 민국 기상청 최우선, 현재 위치 사용, 위치 권한 다시 묻지않음, 배경 애니메이션, 위젯 새로고침 주기
            appSettingsDataStore.kmaTopPriority.collect {
                kmaIsTopPriority = it
            }
            appSettingsDataStore.enableCurrentLocation.collect {
                enabledCurrentLocation = it
            }
            appSettingsDataStore.neverAskAgainAccessLocationPermission.collect {
                neverAskAgainAccessLocationPermission = it
            }
            appSettingsDataStore.enabledBackgroundAnimation.collect {
                enabledBackgroundAnimation = it
            }
            appSettingsDataStore.widgetRefreshInterval.collect {
                widgetRefreshInterval = it
            }

            // 날씨 데이터 값 단위 로드
            // 기온, 풍속, 시야, 시간 단위
            valueUnitDataStore.tempUnit.collect {
                tempUnit = it
            }
            valueUnitDataStore.windUnit.collect {
                windSpeedUnit = it
            }
            valueUnitDataStore.visibilityUnit.collect {
                visibilityUnit = it
            }
            valueUnitDataStore.clockUnit.collect {
                clockUnit = it
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

}