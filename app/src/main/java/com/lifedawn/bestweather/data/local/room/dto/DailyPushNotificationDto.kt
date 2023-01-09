package com.lifedawn.bestweather.data.local.room.dto

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.ui.notification.daily.DailyPushNotificationType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import java.io.Serializable

@Entity(tableName = "daily_push_notifications_table")
class DailyPushNotificationDto  {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id = 0
    @ColumnInfo(name = "locationType") var locationType: LocationType? = null
    @ColumnInfo(name = "notificationType") var notificationType: DailyPushNotificationType? = null
    @ColumnInfo(name = "weatherProviderType") var weatherProviderType: WeatherProviderType? = null
    @ColumnInfo(name = "topPriorityKma") var isTopPriorityKma = false
    @ColumnInfo(name = "addressName") var addressName: String? = null
    @ColumnInfo(name = "admin") var admin: String? = null
    @ColumnInfo(name = "latitude") var latitude: Double? = null
    @ColumnInfo(name = "longitude") var longitude: Double? = null
    @ColumnInfo(name = "countryCode") var countryCode: String? = null
    @ColumnInfo(name = "alarmClock") var alarmClock: String? = null
    @ColumnInfo(name = "enabled") var isEnabled = false
    @ColumnInfo(name = "showAirQuality") var isShowAirQuality = false
    @ColumnInfo(name = "zoneId") var zoneId: String? = null
}