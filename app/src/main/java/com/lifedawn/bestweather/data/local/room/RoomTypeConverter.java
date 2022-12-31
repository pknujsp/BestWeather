package com.lifedawn.bestweather.data.local.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import com.lifedawn.bestweather.commons.constants.LocationType;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.constants.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.ui.notification.daily.DailyPushNotificationType;

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
	public String toString(Set<WeatherProviderType> weatherProviderTypeSet) {
		if (weatherProviderTypeSet == null) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		int i = 0;

		for (WeatherProviderType type : weatherProviderTypeSet) {
			stringBuilder.append(type.name());
			if (++i < weatherProviderTypeSet.size()) {
				stringBuilder.append(",");
			}
		}

		return stringBuilder.toString();
	}

	@TypeConverter
	public Set<WeatherProviderType> toSet(String value) {
		String[] types = value.split(",");
		Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();

		if (types.length > 0) {
			for (String type : types) {
				weatherProviderTypeSet.add(WeatherProviderType.valueOf(type));
			}
		}
		return weatherProviderTypeSet;
	}

	@TypeConverter
	public String toString(WeatherProviderType weatherProviderType) {
		if (weatherProviderType != null) {
			return weatherProviderType.name();
		} else {
			return null;
		}
	}

	@TypeConverter
	public WeatherProviderType toWeatherSourceType(String weatherSourceType) {
		if (weatherSourceType != null) {
			return WeatherProviderType.valueOf(weatherSourceType);
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


	@TypeConverter
	public String toString(RemoteViewsUtil.ErrorType errorType) {
		if (errorType == null) {
			return "";
		}
		return errorType.name();
	}

	@TypeConverter
	public RemoteViewsUtil.ErrorType toWidgetErrorType(String errorTypeStr) {
		if (errorTypeStr == null) {
			return null;
		} else if (errorTypeStr.isEmpty()) {
			return null;
		}
		return RemoteViewsUtil.ErrorType.valueOf(errorTypeStr);
	}


	@TypeConverter
	public String toString(WidgetNotiConstants.DataTypeOfIcon dataTypeOfIcon) {
		if (dataTypeOfIcon != null) {
			return dataTypeOfIcon.name();
		} else {
			return null;
		}
	}

	@TypeConverter
	public WidgetNotiConstants.DataTypeOfIcon toDataTypeOfIcon(String dataTypeOfIcon) {
		if (dataTypeOfIcon != null) {
			return WidgetNotiConstants.DataTypeOfIcon.valueOf(dataTypeOfIcon);
		} else {
			return null;
		}
	}
}
