package com.lifedawn.bestweather.data.local.room.dto;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.lifedawn.bestweather.commons.constants.LocationType;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;

import java.util.HashSet;
import java.util.Set;

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
	private LocationType locationType;

	@ColumnInfo(name = "weatherSourceTypes")
	private Set<WeatherProviderType> weatherProviderTypeSet;

	@ColumnInfo(name = "topPriorityKma")
	private boolean topPriorityKma;

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


	@ColumnInfo(name = "responseText")
	private String responseText;

	@ColumnInfo(name = "initialized")
	private boolean initialized;

	@ColumnInfo(name = "multipleWeatherDataSource")
	private boolean multipleWeatherDataSource;

	@ColumnInfo(name = "widgetProviderClassName")
	private String widgetProviderClassName;

	@ColumnInfo(name = "lastErrorType")
	private RemoteViewsUtil.ErrorType lastErrorType;

	@ColumnInfo(name = "processing")
	private boolean processing;

	public String getWidgetProviderClassName() {
		return widgetProviderClassName;
	}

	public void setWidgetProviderClassName(String widgetProviderClassName) {
		this.widgetProviderClassName = widgetProviderClassName;
	}

	public boolean isMultipleWeatherDataSource() {
		return multipleWeatherDataSource;
	}

	public void setMultipleWeatherDataSource(boolean multipleWeatherDataSource) {
		this.multipleWeatherDataSource = multipleWeatherDataSource;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public String getResponseText() {
		return responseText;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
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

	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}


	public boolean isTopPriorityKma() {
		return topPriorityKma;
	}

	public void setTopPriorityKma(boolean topPriorityKma) {
		this.topPriorityKma = topPriorityKma;
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

	public void addWeatherProviderType(WeatherProviderType newType) {
		if (weatherProviderTypeSet == null) {
			weatherProviderTypeSet = new HashSet<>();
		}
		weatherProviderTypeSet.add(newType);
	}

	public void removeWeatherSourceType(WeatherProviderType removeType) {
		weatherProviderTypeSet.remove(removeType);
	}
	
	public Set<WeatherProviderType> getWeatherProviderTypeSet() {
		if (countryCode != null) {
			if (topPriorityKma && countryCode.equals("KR")) {
				if (!multipleWeatherDataSource) {
					weatherProviderTypeSet.remove(WeatherProviderType.OWM_ONECALL);
					weatherProviderTypeSet.remove(WeatherProviderType.MET_NORWAY);
				}
				weatherProviderTypeSet.add(WeatherProviderType.KMA_WEB);
			}

			if (!countryCode.equals("KR") && multipleWeatherDataSource) {
				weatherProviderTypeSet.clear();
				weatherProviderTypeSet.add(WeatherProviderType.OWM_ONECALL);
				weatherProviderTypeSet.add(WeatherProviderType.MET_NORWAY);
			}
		}
		return weatherProviderTypeSet;
	}

	public void setWeatherProviderTypeSet(Set<WeatherProviderType> weatherProviderTypeSet) {
		this.weatherProviderTypeSet = weatherProviderTypeSet;
	}

	public void setLastErrorType(RemoteViewsUtil.ErrorType lastErrorType) {
		this.lastErrorType = lastErrorType;
	}

	public RemoteViewsUtil.ErrorType getLastErrorType() {
		return lastErrorType;
	}

	public void setProcessing(boolean processing) {
		this.processing = processing;
	}

	public boolean isProcessing() {
		return processing;
	}
}