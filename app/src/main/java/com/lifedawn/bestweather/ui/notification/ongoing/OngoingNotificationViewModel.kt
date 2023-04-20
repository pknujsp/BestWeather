package com.lifedawn.bestweather.ui.notification.ongoing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback

class OngoingNotificationViewModel constructor(application: Application) : AndroidViewModel(application) {
    private val repository: OngoingNotificationRepository?

    init {
        repository = OngoingNotificationRepository.Companion.getINSTANCE()
    }

    fun getOngoingNotificationDto(callback: DbQueryCallback<OngoingNotificationDto?>) {
        repository!!.getOngoingNotificationDto(callback)
    }

    fun save(ongoingNotificationDto: OngoingNotificationDto?, callback: BackgroundWorkCallback?) {
        repository!!.save(ongoingNotificationDto, callback)
    }

    fun remove() {
        repository!!.remove()
    }
}