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
        private val coroutineScope = CoroutineScope(Dispatchers.Default)


        private suspend fun refreshWidgetsAndOngoingNotification(context: Context, weatherProviderTypeSet: Set<WeatherProviderType>,
                                                                 weatherRestApiDownloader: WeatherRestApiDownloader,
                                                                 latitude: Double, longitude: Double, currentLocation: Boolean):
                Deferred<String> =
                coroutineScope.async {
                    val enableOngoingNotification = PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean(NotificationType.Ongoing.preferenceName, false)

                    if (enableOngoingNotification) {
                        val notiViewCreator = OngoingNotiViewCreator(context, null)
                        notiViewCreator.loadSavedPreferences()

                        val notiData = notiViewCreator.notificationDataObj

                        if (currentLocation) {
                            if (notiData.locationType == LocationType.CurrentLocation) {
                                if (weatherProviderTypeSet.contains(notiData.weatherSourceType)) {

                                }
                            }
                        } else {

                        }

                    }

                    val widgetRepository = WidgetRepository(context)

                    widgetRepository.getAll(object : DbQueryCallback<List<WidgetDto>> {
                        override fun onResultSuccessful(result: List<WidgetDto>?) {
                            if (result!!.isNotEmpty()) {

                            }
                        }

                        override fun onResultNoData() {

                        }
                    })

                    return@async "Finished"
                }
    }
}