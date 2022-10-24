package com.lifedawn.bestweather.notification.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;

public class OngoingNotificationDto {
	@Expose
	@SerializedName("on")
	private boolean on;
	@Expose
	@SerializedName("locationType")
	private LocationType locationType;
	@Expose
	@SerializedName("weatherProviderType")
	private WeatherProviderType weatherProviderType;
	@Expose
	@SerializedName("topPriorityKma")
	private boolean topPriorityKma;
	@Expose
	@SerializedName("updateIntervalMillis")
	private long updateIntervalMillis;
	@Expose
	@SerializedName("dataTypeOfIcon")
	private WidgetNotiConstants.DataTypeOfIcon dataTypeOfIcon;
	@Expose
	@SerializedName("displayName")
	private String displayName;
	@Expose
	@SerializedName("latitude")
	private double latitude;
	@Expose
	@SerializedName("longitude")
	private double longitude;
	@Expose
	@SerializedName("countryCode")
	private String countryCode;
	@Expose
	@SerializedName("zoneId")
	private String zoneId;

	public void setOn(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setDataTypeOfIcon(WidgetNotiConstants.DataTypeOfIcon dataTypeOfIcon) {
		this.dataTypeOfIcon = dataTypeOfIcon;
	}

	public WidgetNotiConstants.DataTypeOfIcon getDataTypeOfIcon() {
		return dataTypeOfIcon;
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}

	public WeatherProviderType getWeatherSourceType() {
		return weatherProviderType;
	}

	public void setWeatherSourceType(WeatherProviderType weatherProviderType) {
		this.weatherProviderType = weatherProviderType;
	}

	public boolean isTopPriorityKma() {
		return topPriorityKma;
	}

	public void setTopPriorityKma(boolean topPriorityKma) {
		this.topPriorityKma = topPriorityKma;
	}

	public long getUpdateIntervalMillis() {
		return updateIntervalMillis;
	}

	public void setUpdateIntervalMillis(long updateIntervalMillis) {
		this.updateIntervalMillis = updateIntervalMillis;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
