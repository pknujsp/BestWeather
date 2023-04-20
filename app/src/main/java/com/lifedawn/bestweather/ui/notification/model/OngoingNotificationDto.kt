package com.lifedawn.bestweather.ui.notification.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.constants.WidgetNotiConstants.DataTypeOfIcon

open class OngoingNotificationDto constructor() {
    @Expose @SerializedName("on") var isOn: Boolean = false
    @Expose @SerializedName("locationType") var locationType: LocationType? = null
    @Expose @SerializedName("weatherProviderType") var weatherSourceType: WeatherProviderType? = null
    @Expose @SerializedName("topPriorityKma") var isTopPriorityKma: Boolean = false
    @Expose @SerializedName("updateIntervalMillis") var updateIntervalMillis: Long = 0
    @Expose @SerializedName("dataTypeOfIcon") var dataTypeOfIcon: DataTypeOfIcon? = null
    @Expose @SerializedName("displayName") var displayName: String? = null
    @Expose @SerializedName("latitude") var latitude: Double = 0.0
    @Expose @SerializedName("longitude") var longitude: Double = 0.0
    @Expose @SerializedName("countryCode") var countryCode: String? = null
    @Expose @SerializedName("zoneId") var zoneId: String? = null
}