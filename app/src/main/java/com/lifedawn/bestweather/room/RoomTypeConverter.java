package com.lifedawn.bestweather.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;

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
	public String toString(Set<WeatherSourceType> weatherSourceTypeSet) {
		if (weatherSourceTypeSet == null) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		int i = 0;

		for (WeatherSourceType type : weatherSourceTypeSet) {
			stringBuilder.append(type.name());
			if (++i < weatherSourceTypeSet.size()) {
				stringBuilder.append(",");
			}
		}

		return stringBuilder.toString();
	}

	@TypeConverter
	public Set<WeatherSourceType> toSet(String value) {
		String[] types = value.split(",");
		Set<WeatherSourceType> weatherSourceTypeSet = new HashSet<>();
		for (String type : types) {
			weatherSourceTypeSet.add(WeatherSourceType.valueOf(type));
		}
		return weatherSourceTypeSet;
	}

	@TypeConverter
	public String toString(WeatherSourceType weatherSourceType) {
		if (weatherSourceType != null) {
			return weatherSourceType.name();
		} else {
			return null;
		}
	}

	@TypeConverter
	public WeatherSourceType toWeatherSourceType(String weatherSourceType) {
		if (weatherSourceType != null) {
			return WeatherSourceType.valueOf(weatherSourceType);
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
}
