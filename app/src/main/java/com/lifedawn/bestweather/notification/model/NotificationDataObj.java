package com.lifedawn.bestweather.notification.model;

import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;

public class NotificationDataObj {
	private LocationType locationType;
	private WeatherDataSourceType weatherDataSourceType;
	private boolean topPriorityKma;
	private long updateIntervalMillis;
	private int selectedAddressDtoId;

	private String addressName;
	private double latitude;
	private double longitude;
	private String countryCode;

	public LocationType getLocationType() {
		return locationType;
	}

	public NotificationDataObj setLocationType(LocationType locationType) {
		this.locationType = locationType;
		return this;
	}

	public WeatherDataSourceType getWeatherSourceType() {
		return weatherDataSourceType;
	}

	public NotificationDataObj setWeatherSourceType(WeatherDataSourceType weatherDataSourceType) {
		this.weatherDataSourceType = weatherDataSourceType;
		return this;
	}

	public boolean isTopPriorityKma() {
		return topPriorityKma;
	}

	public NotificationDataObj setTopPriorityKma(boolean topPriorityKma) {
		this.topPriorityKma = topPriorityKma;
		return this;
	}

	public long getUpdateIntervalMillis() {
		return updateIntervalMillis;
	}

	public NotificationDataObj setUpdateIntervalMillis(long updateIntervalMillis) {
		this.updateIntervalMillis = updateIntervalMillis;
		return this;
	}

	public int getSelectedAddressDtoId() {
		return selectedAddressDtoId;
	}

	public NotificationDataObj setSelectedAddressDtoId(int selectedAddressDtoId) {
		this.selectedAddressDtoId = selectedAddressDtoId;
		return this;
	}

	public String getAddressName() {
		return addressName;
	}

	public NotificationDataObj setAddressName(String addressName) {
		this.addressName = addressName;
		return this;
	}

	public double getLatitude() {
		return latitude;
	}

	public NotificationDataObj setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}

	public double getLongitude() {
		return longitude;
	}

	public NotificationDataObj setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public NotificationDataObj setCountryCode(String countryCode) {
		this.countryCode = countryCode;
		return this;
	}
}
