package com.lifedawn.bestweather.data.local.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.ui.notification.daily.DailyPushNotificationType
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.ErrorType
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil
import com.lifedawn.bestweather.commons.constants.WidgetNotiConstants.DataTypeOfIcon
import com.lifedawn.bestweather.commons.constants.WidgetNotiConstants
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.util.HashSet

class RoomTypeConverter {
    @TypeConverter
    fun toByteArr(bitmap: Bitmap?): ByteArray = if (bitmap == null) {
        ByteArray(0)
    } else {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        byteArrayOutputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArr: ByteArray?): Bitmap? {
        return if (byteArr == null) {
            null
        } else BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
    }

    @TypeConverter
    fun toString(weatherProviderTypeSet: Set<WeatherProviderType>?): String? {
        if (weatherProviderTypeSet == null) {
            return null
        }
        val stringBuilder = StringBuilder()
        for ((i, type) in weatherProviderTypeSet.withIndex()) {
            stringBuilder.append(type.name)
            if (i + 1 < weatherProviderTypeSet.size) {
                stringBuilder.append(",")
            }
        }
        return stringBuilder.toString()
    }

    @TypeConverter
    fun toSet(value: String): Set<WeatherProviderType> {
        val types = value.split(",").toTypedArray()
        val weatherProviderTypeSet: MutableSet<WeatherProviderType> = HashSet()
        if (types.isNotEmpty()) {
            for (type in types) {
                weatherProviderTypeSet.add(WeatherProviderType.valueOf(type))
            }
        }
        return weatherProviderTypeSet
    }

    @TypeConverter
    fun toString(weatherProviderType: WeatherProviderType?): String? {
        return weatherProviderType?.name
    }

    @TypeConverter
    fun toWeatherSourceType(weatherSourceType: String?): WeatherProviderType? {
        return if (weatherSourceType != null) {
            WeatherProviderType.valueOf(weatherSourceType)
        } else {
            null
        }
    }

    @TypeConverter
    fun toString(locationType: LocationType?): String? {
        return locationType?.name
    }

    @TypeConverter
    fun toLocationType(locationType: String?): LocationType? {
        return if (locationType != null) {
            LocationType.valueOf(locationType)
        } else {
            null
        }
    }

    @TypeConverter
    fun toString(dailyPushNotificationType: DailyPushNotificationType?): String? {
        return dailyPushNotificationType?.name
    }

    @TypeConverter
    fun toDailyPushNotificationType(dailyPushNotificationType: String?): DailyPushNotificationType? {
        return if (dailyPushNotificationType != null) {
            DailyPushNotificationType.valueOf(dailyPushNotificationType)
        } else {
            null
        }
    }

    @TypeConverter
    fun toString(errorType: ErrorType?): String {
        return errorType?.name ?: ""
    }

    @TypeConverter
    fun toWidgetErrorType(errorTypeStr: String?): ErrorType? {
        if (errorTypeStr == null) {
            return null
        } else if (errorTypeStr.isEmpty()) {
            return null
        }
        return ErrorType.valueOf(errorTypeStr)
    }

    @TypeConverter
    fun toString(dataTypeOfIcon: DataTypeOfIcon?): String? {
        return dataTypeOfIcon?.name
    }

    @TypeConverter
    fun toDataTypeOfIcon(dataTypeOfIcon: String?): DataTypeOfIcon? {
        return if (dataTypeOfIcon != null) {
            DataTypeOfIcon.valueOf(dataTypeOfIcon)
        } else {
            null
        }
    }
}