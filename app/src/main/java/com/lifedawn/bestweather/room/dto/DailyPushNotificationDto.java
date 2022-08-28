package com.lifedawn.bestweather.room.dto;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.notification.daily.DailyPushNotificationType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(tableName = "daily_push_notifications_table")
public class DailyPushNotificationDto implements Serializable {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private int id;

	@ColumnInfo(name = "locationType")
	private LocationType locationType;

	@ColumnInfo(name = "notificationType")
	private DailyPushNotificationType notificationType;

	@ColumnInfo(name = "weatherSourceTypeSet")
	private Set<WeatherProviderType> weatherProviderTypeSet;

	@ColumnInfo(name = "topPriorityKma")
	private boolean topPriorityKma;

	@ColumnInfo(name = "addressName")
	private String addressName;

	@ColumnInfo(name = "admin")
	private String admin;

	@ColumnInfo(name = "latitude")
	private Double latitude;

	@ColumnInfo(name = "longitude")
	private Double longitude;

	@ColumnInfo(name = "countryCode")
	private String countryCode;

	@ColumnInfo(name = "alarmClock")
	private String alarmClock;

	@ColumnInfo(name = "enabled")
	private boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}

	public void setWeatherSourceTypeSet(Set<WeatherProviderType> weatherProviderTypeSet) {
		this.weatherProviderTypeSet = weatherProviderTypeSet;
	}


	public boolean isTopPriorityKma() {
		return topPriorityKma;
	}

	public void setTopPriorityKma(boolean topPriorityKma) {
		this.topPriorityKma = topPriorityKma;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getAlarmClock() {
		return alarmClock;
	}

	public void setAlarmClock(String alarmClock) {
		this.alarmClock = alarmClock;
	}

	public DailyPushNotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(DailyPushNotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public void addWeatherSourceType(WeatherProviderType newType) {
		if (weatherProviderTypeSet == null) {
			weatherProviderTypeSet = new HashSet<>();
		}
		weatherProviderTypeSet.add(newType);
	}

	public void removeWeatherSourceType(WeatherProviderType removeType) {
		weatherProviderTypeSet.remove(removeType);
	}

	public Set<WeatherProviderType> getWeatherProviderTypeSet() {
		return weatherProviderTypeSet;
	}

	public void setWeatherProviderTypeSet(Set<WeatherProviderType> weatherProviderTypeSet) {
		this.weatherProviderTypeSet = weatherProviderTypeSet;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getAdmin() {
		return admin;
	}
}
