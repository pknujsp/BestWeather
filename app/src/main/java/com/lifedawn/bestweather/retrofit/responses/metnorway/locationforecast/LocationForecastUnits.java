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
}
