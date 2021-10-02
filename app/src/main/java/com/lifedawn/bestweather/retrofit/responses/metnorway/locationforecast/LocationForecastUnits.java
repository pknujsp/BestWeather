package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationForecastUnits {
	@Expose
	@SerializedName("air_pressure_at_sea_level")
	private String airPressureAtSeaLevel;
	
	@Expose
	@SerializedName("air_temperature")
	private String airTemperature;
	
	@Expose
	@SerializedName("air_temperature_max")
	private String airTemperatureMax;
	
	@Expose
	@SerializedName("air_temperature_min")
	private String airTemperatureMin;
	
	@Expose
	@SerializedName("cloud_area_fraction")
	private String cloudAreaFraction;
	
	@Expose
	@SerializedName("cloud_area_fraction_high")
	private String cloudAreaFractionHigh;
	
	@Expose
	@SerializedName("cloud_area_fraction_low")
	private String cloudAreaFractionLow;
	
	@Expose
	@SerializedName("cloud_area_fraction_medium")
	private String cloudAreaFractionMedium;
	
	@Expose
	@SerializedName("dew_point_temperature")
	private String dewPointTemperature;
	
	@Expose
	@SerializedName("fog_area_fraction")
	private String fogAreaFraction;
	
	@Expose
	@SerializedName("precipitation_amount")
	private String precipitationAmount;
	
	@Expose
	@SerializedName("precipitation_amount_max")
	private String precipitationAmountMax;
	
	@Expose
	@SerializedName("precipitation_amount_min")
	private String precipitationAmountMin;
	
	@Expose
	@SerializedName("probability_of_precipitation")
	private String probabilityOfPrecipitation;
	
	@Expose
	@SerializedName("probability_of_thunder")
	private String probabilityOfThunder;
	
	@Expose
	@SerializedName("relative_humidity")
	private String relativeHumidity;
	
	@Expose
	@SerializedName("ultraviolet_index_clear_sky")
	private String ultravioletIndexClearSky;
	
	@Expose
	@SerializedName("wind_from_direction")
	private String windFromDirection;
	
	@Expose
	@SerializedName("wind_speed")
	private String windSpeed;
	
	@Expose
	@SerializedName("wind_speed_of_gust")
	private String windSpeedOfGust;
	
	public String getAirPressureAtSeaLevel() {
		return airPressureAtSeaLevel;
	}
	
	public void setAirPressureAtSeaLevel(String airPressureAtSeaLevel) {
		this.airPressureAtSeaLevel = airPressureAtSeaLevel;
	}
	
	public String getAirTemperature() {
		return airTemperature;
	}
	
	public void setAirTemperature(String airTemperature) {
		this.airTemperature = airTemperature;
	}
	
	public String getAirTemperatureMax() {
		return airTemperatureMax;
	}
	
	public void setAirTemperatureMax(String airTemperatureMax) {
		this.airTemperatureMax = airTemperatureMax;
	}
	
	public String getAirTemperatureMin() {
		return airTemperatureMin;
	}
	
	public void setAirTemperatureMin(String airTemperatureMin) {
		this.airTemperatureMin = airTemperatureMin;
	}
	
	public String getCloudAreaFraction() {
		return cloudAreaFraction;
	}
	
	public void setCloudAreaFraction(String cloudAreaFraction) {
		this.cloudAreaFraction = cloudAreaFraction;
	}
	
	public String getCloudAreaFractionHigh() {
		return cloudAreaFractionHigh;
	}
	
	public void setCloudAreaFractionHigh(String cloudAreaFractionHigh) {
		this.cloudAreaFractionHigh = cloudAreaFractionHigh;
	}
	
	public String getCloudAreaFractionLow() {
		return cloudAreaFractionLow;
	}
	
	public void setCloudAreaFractionLow(String cloudAreaFractionLow) {
		this.cloudAreaFractionLow = cloudAreaFractionLow;
	}
	
	public String getCloudAreaFractionMedium() {
		return cloudAreaFractionMedium;
	}
	
	public void setCloudAreaFractionMedium(String cloudAreaFractionMedium) {
		this.cloudAreaFractionMedium = cloudAreaFractionMedium;
	}
	
	public String getDewPointTemperature() {
		return dewPointTemperature;
	}
	
	public void setDewPointTemperature(String dewPointTemperature) {
		this.dewPointTemperature = dewPointTemperature;
	}
	
	public String getFogAreaFraction() {
		return fogAreaFraction;
	}
	
	public void setFogAreaFraction(String fogAreaFraction) {
		this.fogAreaFraction = fogAreaFraction;
	}
	
	public String getPrecipitationAmount() {
		return precipitationAmount;
	}
	
	public void setPrecipitationAmount(String precipitationAmount) {
		this.precipitationAmount = precipitationAmount;
	}
	
	public String getPrecipitationAmountMax() {
		return precipitationAmountMax;
	}
	
	public void setPrecipitationAmountMax(String precipitationAmountMax) {
		this.precipitationAmountMax = precipitationAmountMax;
	}
	
	public String getPrecipitationAmountMin() {
		return precipitationAmountMin;
	}
	
	public void setPrecipitationAmountMin(String precipitationAmountMin) {
		this.precipitationAmountMin = precipitationAmountMin;
	}
	
	public String getProbabilityOfPrecipitation() {
		return probabilityOfPrecipitation;
	}
	
	public void setProbabilityOfPrecipitation(String probabilityOfPrecipitation) {
		this.probabilityOfPrecipitation = probabilityOfPrecipitation;
	}
	
	public String getProbabilityOfThunder() {
		return probabilityOfThunder;
	}
	
	public void setProbabilityOfThunder(String probabilityOfThunder) {
		this.probabilityOfThunder = probabilityOfThunder;
	}
	
	public String getRelativeHumidity() {
		return relativeHumidity;
	}
	
	public void setRelativeHumidity(String relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}
	
	public String getUltravioletIndexClearSky() {
		return ultravioletIndexClearSky;
	}
	
	public void setUltravioletIndexClearSky(String ultravioletIndexClearSky) {
		this.ultravioletIndexClearSky = ultravioletIndexClearSky;
	}
	
	public String getWindFromDirection() {
		return windFromDirection;
	}
	
	public void setWindFromDirection(String windFromDirection) {
		this.windFromDirection = windFromDirection;
	}
	
	public String getWindSpeed() {
		return windSpeed;
	}
	
	public void setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
	}
	
	public String getWindSpeedOfGust() {
		return windSpeedOfGust;
	}
	
	public void setWindSpeedOfGust(String windSpeedOfGust) {
		this.windSpeedOfGust = windSpeedOfGust;
	}
}