package com.lifedawn.bestweather.ui.notification.daily.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import java.time.LocalTime

class DailyNotificationViewModel constructor(application: Application) : AndroidViewModel(application) {
    private val repository: DailyPushNotificationRepository = DailyPushNotificationRepository.getINSTANCE()
    val listLiveData: LiveData<List<DailyPushNotificationDto>>
    private var dailyNotificationHelper: DailyNotificationHelper
    private var savedNotificationDto: DailyPushNotificationDto? = null
    private var newNotificationDto: DailyPushNotificationDto? = null
    private var editingNotificationDto: DailyPushNotificationDto? = null
    var isNewNotificationSession: Boolean = false
        private set
    var isSelectedFavoriteLocation: Boolean = false
        private set
    var bundle: Bundle? = null
        private set
    private var mainWeatherProviderType: WeatherProviderType? = null
    private var selectedFavoriteAddressDto: FavoriteAddressDto? = null

    init {
        listLiveData = repository.listLiveData()
        dailyNotificationHelper = DailyNotificationHelper(application.getApplicationContext())
    }

    fun getDailyNotificationHelper(): DailyNotificationHelper {
        return dailyNotificationHelper
    }

    fun setDailyNotificationHelper(dailyNotificationHelper: DailyNotificationHelper): DailyNotificationViewModel {
        this.dailyNotificationHelper = dailyNotificationHelper
        return this
    }

    fun getSavedNotificationDto(): DailyPushNotificationDto? {
        return savedNotificationDto
    }

    fun setSavedNotificationDto(savedNotificationDto: DailyPushNotificationDto?): DailyNotificationViewModel {
        this.savedNotificationDto = savedNotificationDto
        return this
    }

    fun getNewNotificationDto(): DailyPushNotificationDto? {
        return newNotificationDto
    }

    fun setNewNotificationDto(newNotificationDto: DailyPushNotificationDto?): DailyNotificationViewModel {
        this.newNotificationDto = newNotificationDto
        return this
    }

    fun getEditingNotificationDto(): DailyPushNotificationDto? {
        return editingNotificationDto
    }

    fun setEditingNotificationDto(editingNotificationDto: DailyPushNotificationDto?): DailyNotificationViewModel {
        this.editingNotificationDto = editingNotificationDto
        return this
    }

    fun setNotificationSession(newNotificationSession: Boolean) {
        isNewNotificationSession = newNotificationSession
        if (newNotificationSession) {
            newNotificationDto = DailyPushNotificationDto()
            newNotificationDto.isEnabled = true
            newNotificationDto.alarmClock = LocalTime.of(8, 0).toString()
            newNotificationDto.weatherProviderType = getMainWeatherProviderType()
            newNotificationDto.notificationType = DailyPushNotificationType.First
            newNotificationDto.locationType = LocationType.CurrentLocation
            editingNotificationDto = newNotificationDto
        } else {
            savedNotificationDto = bundle!!.getSerializable("dto") as DailyPushNotificationDto?
            editingNotificationDto = savedNotificationDto
            if (editingNotificationDto.locationType === LocationType.SelectedAddress) {
                selectedFavoriteAddressDto = FavoriteAddressDto()
                selectedFavoriteAddressDto.displayName = editingNotificationDto.addressName
                selectedFavoriteAddressDto.countryCode = editingNotificationDto.countryCode
                selectedFavoriteAddressDto.latitude = editingNotificationDto.latitude.toString()
                selectedFavoriteAddressDto.longitude = editingNotificationDto.longitude.toString()
                isSelectedFavoriteLocation = true
            }
        }
    }

    fun setSelectedFavoriteLocation(selectedFavoriteLocation: Boolean): DailyNotificationViewModel {
        isSelectedFavoriteLocation = selectedFavoriteLocation
        return this
    }

    fun setBundle(bundle: Bundle?): DailyNotificationViewModel {
        this.bundle = bundle
        return this
    }

    fun getMainWeatherProviderType(): WeatherProviderType? {
        return mainWeatherProviderType
    }

    fun setMainWeatherProviderType(mainWeatherProviderType: WeatherProviderType?): DailyNotificationViewModel {
        this.mainWeatherProviderType = mainWeatherProviderType
        return this
    }

    fun getSelectedFavoriteAddressDto(): FavoriteAddressDto? {
        return selectedFavoriteAddressDto
    }

    fun setSelectedFavoriteAddressDto(selectedFavoriteAddressDto: FavoriteAddressDto?): DailyNotificationViewModel {
        this.selectedFavoriteAddressDto = selectedFavoriteAddressDto
        return this
    }

    fun getAll(callback: DbQueryCallback<List<DailyPushNotificationDto?>?>?) {
        repository.getAll(callback)
    }

    fun size(callback: DbQueryCallback<Int?>?) {
        repository.size(callback)
    }

    operator fun get(id: Int, callback: DbQueryCallback<DailyPushNotificationDto?>?) {
        repository.get(id, callback)
    }

    fun delete(dailyPushNotificationDto: DailyPushNotificationDto?, callback: DbQueryCallback<Boolean?>?) {
        repository.delete(dailyPushNotificationDto, callback)
    }

    fun add(dailyPushNotificationDto: DailyPushNotificationDto?, callback: DbQueryCallback<DailyPushNotificationDto?>?) {
        repository.add(dailyPushNotificationDto, callback)
    }

    fun update(dailyPushNotificationDto: DailyPushNotificationDto?, callback: DbQueryCallback<DailyPushNotificationDto?>?) {
        repository.update(dailyPushNotificationDto, callback)
    }
}