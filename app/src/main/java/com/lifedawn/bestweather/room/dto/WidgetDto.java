package com.lifedawn.bestweather.room.dto;


import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "widget_table")
public class WidgetDto {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private long id;

	@ColumnInfo(name = "appWidgetId")
	private int appWidgetId;

	@ColumnInfo(name = "backgroundAlpha")
	private int backgroundAlpha;

	@ColumnInfo(name = "displayClock")
	private boolean displayClock;

	@ColumnInfo(name = "displayLocalClock")
	private boolean displayLocalClock;

	@ColumnInfo(name = "locationType")
	private String locationType;

	@ColumnInfo(name = "weatherSourceType")
	private String weatherSourceType;

	@ColumnInfo(name = "topPriorityKma")
	private boolean topPriorityKma;

	@ColumnInfo(name = "updateIntervalMillis")
	private long updateIntervalMillis;

	@ColumnInfo(name = "selectedAddressDtoId")
	private int selectedAddressDtoId;

	@ColumnInfo(name = "textSizeAmount")
	private int textSizeAmount;

	@ColumnInfo(name = "addressName")
	private String addressName;

	@ColumnInfo(name = "latitude")
	private double latitude;

	@ColumnInfo(name = "longitude")
	private double longitude;

	@ColumnInfo(name = "countryCode")
	private String countryCode;

	@ColumnInfo(name = "timeZoneId")
	private String timeZoneId;

	@ColumnInfo(name = "lastRefreshDateTime")
	private String lastRefreshDateTime;

	@ColumnInfo(name = "loadSuccessful")
	private boolean loadSuccessful;

	@ColumnInfo(name = "bitmap")
	private Bitmap bitmap;

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getAppWidgetId() {
		return appWidgetId;
	}

	public void setAppWidgetId(int appWidgetId) {
		this.appWidgetId = appWidgetId;
	}

	public int getBackgroundAlpha() {
		return backgroundAlpha;
	}

	public void setBackgroundAlpha(int backgroundAlpha) {
		this.backgroundAlpha = backgroundAlpha;
	}

	public boolean isDisplayClock() {
		return displayClock;
	}

	public void setDisplayClock(boolean displayClock) {
		this.displayClock = displayClock;
	}

	public boolean isDisplayLocalClock() {
		return displayLocalClock;
	}

	public void setDisplayLocalClock(boolean displayLocalClock) {
		this.displayLocalClock = displayLocalClock;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public String getWeatherSourceType() {
		return weatherSourceType;
	}

	public void setWeatherSourceType(String weatherSourceType) {
		this.weatherSourceType = weatherSourceType;
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

	public int getSelectedAddressDtoId() {
		return selectedAddressDtoId;
	}

	public void setSelectedAddressDtoId(int selectedAddressDtoId) {
		this.selectedAddressDtoId = selectedAddressDtoId;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
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

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	public int getTextSizeAmount() {
		return textSizeAmount;
	}

	public void setTextSizeAmount(int textSizeAmount) {
		this.textSizeAmount = textSizeAmount;
	}

	public String getLastRefreshDateTime() {
		return lastRefreshDateTime;
	}

	public void setLastRefreshDateTime(String lastRefreshDateTime) {
		this.lastRefreshDateTime = lastRefreshDateTime;
	}

	public boolean isLoadSuccessful() {
		return loadSuccessful;
	}

	public void setLoadSuccessful(boolean loadSuccessful) {
		this.loadSuccessful = loadSuccessful;
	}

}
