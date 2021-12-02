package com.lifedawn.bestweather.room.dto;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_area_code_table")
public class KmaAreaCodeDto {
	@ColumnInfo(name = "administrative_area_code")
	@PrimaryKey
	@NonNull
	private String administrativeAreaCode;

	@ColumnInfo(name = "phase1")
	private String phase1;

	@ColumnInfo(name = "phase2")
	private String phase2;

	@ColumnInfo(name = "phase3")
	private String phase3;

	@ColumnInfo(name = "x")
	private String x;

	@ColumnInfo(name = "y")
	private String y;

	@ColumnInfo(name = "longitude_hours")
	private String longitudeHours;

	@ColumnInfo(name = "longitude_minutes")
	private String longitudeMinutes;

	@ColumnInfo(name = "longitude_seconds")
	private String longitudeSeconds;

	@ColumnInfo(name = "latitude_hours")
	private String latitudeHours;

	@ColumnInfo(name = "latitude_minutes")
	private String latitudeMinutes;

	@ColumnInfo(name = "latitude_seconds")
	private String latitudeSeconds;

	@ColumnInfo(name = "longitude_seconds_divide_100")
	private String longitudeSecondsDivide100;

	@ColumnInfo(name = "latitude_seconds_divide_100")
	private String latitudeSecondsDivide100;

	@ColumnInfo(name = "mid_land_fcst_code")
	private String midLandFcstCode;

	@ColumnInfo(name = "mid_ta_code")
	private String midTaCode;

	@NonNull
	public String getAdministrativeAreaCode() {
		return administrativeAreaCode;
	}

	public void setAdministrativeAreaCode(@NonNull String administrativeAreaCode) {
		this.administrativeAreaCode = administrativeAreaCode;
	}

	public String getPhase1() {
		return phase1;
	}

	public void setPhase1(String phase1) {
		this.phase1 = phase1;
	}

	public String getPhase2() {
		return phase2;
	}

	public void setPhase2(String phase2) {
		this.phase2 = phase2;
	}

	public String getPhase3() {
		return phase3;
	}

	public void setPhase3(String phase3) {
		this.phase3 = phase3;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x.contains(".0") ? x.replace(".0", "") : x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y.contains(".0") ? y.replace(".0", "") : y;

	}

	public String getLongitudeHours() {
		return longitudeHours;
	}

	public void setLongitudeHours(String longitudeHours) {
		this.longitudeHours = longitudeHours;
	}

	public String getLongitudeMinutes() {
		return longitudeMinutes;
	}

	public void setLongitudeMinutes(String longitudeMinutes) {
		this.longitudeMinutes = longitudeMinutes;
	}

	public String getLongitudeSeconds() {
		return longitudeSeconds;
	}

	public void setLongitudeSeconds(String longitudeSeconds) {
		this.longitudeSeconds = longitudeSeconds;
	}

	public String getLatitudeHours() {
		return latitudeHours;
	}

	public void setLatitudeHours(String latitudeHours) {
		this.latitudeHours = latitudeHours;
	}

	public String getLatitudeMinutes() {
		return latitudeMinutes;
	}

	public void setLatitudeMinutes(String latitudeMinutes) {
		this.latitudeMinutes = latitudeMinutes;
	}

	public String getLatitudeSeconds() {
		return latitudeSeconds;
	}

	public void setLatitudeSeconds(String latitudeSeconds) {
		this.latitudeSeconds = latitudeSeconds;
	}

	public String getLongitudeSecondsDivide100() {
		return longitudeSecondsDivide100;
	}

	public void setLongitudeSecondsDivide100(String longitudeSecondsDivide100) {
		this.longitudeSecondsDivide100 = longitudeSecondsDivide100;
	}

	public String getLatitudeSecondsDivide100() {
		return latitudeSecondsDivide100;
	}

	public void setLatitudeSecondsDivide100(String latitudeSecondsDivide100) {
		this.latitudeSecondsDivide100 = latitudeSecondsDivide100;
	}

	public String getMidLandFcstCode() {
		return midLandFcstCode;
	}

	public void setMidLandFcstCode(String midLandFcstCode) {
		this.midLandFcstCode = midLandFcstCode;
	}

	public String getMidTaCode() {
		return midTaCode;
	}

	public void setMidTaCode(String midTaCode) {
		this.midTaCode = midTaCode;
	}
}
