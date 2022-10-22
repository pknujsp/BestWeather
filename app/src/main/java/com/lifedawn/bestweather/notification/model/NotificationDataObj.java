package com.lifedawn.bestweather.notification.model;

import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;

public class NotificationDataObj {
	private LocationType locationType;
	private WeatherProviderType weatherProviderType;
	private boolean topPriorityKma;
	private long updateIntervalMillis;
	private int selectedAddressDtoId;
	private WidgetNotiConstants.DataTypeOfIcon dataTypeOfIcon;

	private String displayName;
	private double latitude;
	private double longitude;
	private String countryCode;
	private String zoneId;

	public NotificationDataObj setZoneId(String zoneId) {
		this.zoneId = zoneId;
		return this;
	}

	public String getZoneId() {
		return zoneId;
	}

	public NotificationDataObj setDataTypeOfIcon(WidgetNotiConstants.DataTypeOfIcon dataTypeOfIcon) {
		this.dataTypeOfIcon = dataTypeOfIcon;
		return this;
	}

	public WidgetNotiConstants.DataTypeOfIcon getDataTypeOfIcon() {
		return dataTypeOfIcon;
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public NotificationDataObj setLocationType(LocationType locationType) {
		this.locationType = locationType;
		return this;
	}

	public WeatherProviderType getWeatherSourceType() {
		return weatherProviderType;
	}

	public NotificationDataObj setWeatherSourceType(WeatherProviderType weatherProviderType) {
		this.weatherProviderType = weatherProviderType;
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

	public String getDisplayName() {
		return displayName;
	}

	public NotificationDataObj setDisplayName(String displayName) {
		this.displayName = displayName;
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
