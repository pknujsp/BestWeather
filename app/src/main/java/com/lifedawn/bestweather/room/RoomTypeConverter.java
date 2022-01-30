package com.lifedawn.bestweather.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.notification.daily.DailyPushNotificationType;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

public class RoomTypeConverter {

	@TypeConverter
	public byte[] toByteArr(Bitmap bitmap) {
		if (bitmap == null) {
			return new byte[0];
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	@TypeConverter
	public Bitmap toBitmap(byte[] byteArr) {
		if (byteArr == null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
	}

	@TypeConverter
	public String toString(Set<WeatherDataSourceType> weatherDataSourceTypeSet) {
		if (weatherDataSourceTypeSet == null) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		int i = 0;

		for (WeatherDataSourceType type : weatherDataSourceTypeSet) {
			stringBuilder.append(type.name());
			if (++i < weatherDataSourceTypeSet.size()) {
				stringBuilder.append(",");
			}
		}

		return stringBuilder.toString();
	}

	@TypeConverter
	public Set<WeatherDataSourceType> toSet(String value) {
		String[] types = value.split(",");
		Set<WeatherDataSourceType> weatherDataSourceTypeSet = new HashSet<>();

		if (types.length > 0) {
			for (String type : types) {
				weatherDataSourceTypeSet.add(WeatherDataSourceType.valueOf(type));
			}
		}
		return weatherDataSourceTypeSet;
	}

	@TypeConverter
	public String toString(WeatherDataSourceType weatherDataSourceType) {
		if (weatherDataSourceType != null) {
			return weatherDataSourceType.name();
		} else {
			return null;
		}
	}

	@TypeConverter
	public WeatherDataSourceType toWeatherSourceType(String weatherSourceType) {
		if (weatherSourceType != null) {
			return WeatherDataSourceType.valueOf(weatherSourceType);
		} else {
			return null;
		}
	}

	@TypeConverter
	public String toString(LocationType locationType) {
		if (locationType != null) {
			return locationType.name();
		} else {
			return null;
		}
	}

	@TypeConverter
	public LocationType toLocationType(String locationType) {
		if (locationType != null) {
			return LocationType.valueOf(locationType);
		} else {
			return null;
		}
	}

	@TypeConverter
	public String toString(DailyPushNotificationType dailyPushNotificationType) {
		if (dailyPushNotificationType == null) {
			return null;
		} else {
			return dailyPushNotificationType.name();
		}
	}

	@TypeConverter
	public DailyPushNotificationType toDailyPushNotificationType(String dailyPushNotificationType) {
		if (dailyPushNotificationType != null) {
			return DailyPushNotificationType.valueOf(dailyPushNotificationType);
		} else {
			return null;
		}
	}
}
