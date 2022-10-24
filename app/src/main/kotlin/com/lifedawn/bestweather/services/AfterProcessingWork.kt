package com.lifedawn.bestweather.services

import android.content.Context
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.commons.enums.LocationType
import com.lifedawn.bestweather.commons.enums.WeatherProviderType
import com.lifedawn.bestweather.notification.NotificationType
import com.lifedawn.bestweather.notification.ongoing.OngoingNotiViewCreator
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader
import com.lifedawn.bestweather.room.callback.DbQueryCallback
import com.lifedawn.bestweather.room.dto.WidgetDto
import com.lifedawn.bestweather.room.repository.WidgetRepository
import kotlinx.coroutines.*

class AfterProcessingWork {

    companion object {
     
    }
}