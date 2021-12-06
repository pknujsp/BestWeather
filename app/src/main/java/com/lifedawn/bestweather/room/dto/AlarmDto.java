package com.lifedawn.bestweather.room.dto;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Set;

@Entity(tableName = "alarm_table")
public class AlarmDto implements Serializable {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private Integer id;

	@ColumnInfo
	private int enabled;

	@ColumnInfo
	private String alarmTime;
	@ColumnInfo
	private String alarmDays;
	@ColumnInfo
	private String alarmSoundUri;
	@ColumnInfo
	private int alarmSoundVolume;
	@ColumnInfo
	private int enableSound;
	@ColumnInfo
	private int alarmVibration;
	@ColumnInfo
	private int repeat;
	@ColumnInfo
	private int repeatInterval;
	@ColumnInfo
	private int repeatCount;

	@ColumnInfo
	private int addedLocation;
	@ColumnInfo
	private String locationAddressName;
	@ColumnInfo
	private String locationLatitude;
	@ColumnInfo
	private String locationLongitude;
	@ColumnInfo
	private String locationCountryCode;
	@ColumnInfo
	private String locationCountryName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setAlarmDays(String alarmDays) {
		this.alarmDays = alarmDays;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	public String getAlarmDays() {
		return alarmDays;
	}

	public void setAlarmDays(Set<Integer> daySet) {
		String day = "";
		for (Integer d : daySet) {
			day += d.toString();
		}
		this.alarmDays = day;
	}

	public String getAlarmSoundUri() {
		return alarmSoundUri;
	}

	public void setAlarmSoundUri(String alarmSoundUri) {
		this.alarmSoundUri = alarmSoundUri;
	}

	public int getAlarmSoundVolume() {
		return alarmSoundVolume;
	}

	public void setAlarmSoundVolume(int alarmSoundVolume) {
		this.alarmSoundVolume = alarmSoundVolume;
	}

	public int getAlarmVibration() {
		return alarmVibration;
	}

	public void setAlarmVibration(int alarmVibration) {
		this.alarmVibration = alarmVibration;
	}

	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}

	public int getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(int repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}


	public int getEnableSound() {
		return enableSound;
	}

	public void setEnableSound(int enableSound) {
		this.enableSound = enableSound;
	}

	public String getLocationAddressName() {
		return locationAddressName;
	}

	public void setLocationAddressName(String locationAddressName) {
		this.locationAddressName = locationAddressName;
	}

	public String getLocationLatitude() {
		return locationLatitude;
	}

	public void setLocationLatitude(String locationLatitude) {
		this.locationLatitude = locationLatitude;
	}

	public String getLocationLongitude() {
		return locationLongitude;
	}

	public void setLocationLongitude(String locationLongitude) {
		this.locationLongitude = locationLongitude;
	}

	public String getLocationCountryCode() {
		return locationCountryCode;
	}

	public void setLocationCountryCode(String locationCountryCode) {
		this.locationCountryCode = locationCountryCode;
	}

	public String getLocationCountryName() {
		return locationCountryName;
	}

	public void setLocationCountryName(String locationCountryName) {
		this.locationCountryName = locationCountryName;
	}

	public void setAddedLocation(int addedLocation) {
		this.addedLocation = addedLocation;
	}

	public int getAddedLocation() {
		return addedLocation;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
}
