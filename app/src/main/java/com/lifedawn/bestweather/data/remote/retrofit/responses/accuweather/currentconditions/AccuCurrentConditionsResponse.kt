package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.currentconditions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.ValuesUnit;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.Wind;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.WindGust;

import java.util.ArrayList;
import java.util.List;

public class AccuCurrentConditionsResponse {
	
	private List<Item> items;
	
	public List<Item> getItems() {
		return items;
	}
	
	public void setItems(JsonElement responseJsonElement) {
		JsonArray jsonArray = responseJsonElement.getAsJsonArray();
		Gson gson = new Gson();
		
		items = new ArrayList<>();
		for (JsonElement jsonElement : jsonArray) {
			Item item = gson.fromJson(jsonElement.toString(), Item.class);
			items.add(item);
		}
	}
	
	public static class Item {
		@Expose
		@SerializedName("LocalObservationDateTime")
		private String localObservationDateTime;
		
		@Expose
		@SerializedName("EpochTime")
		private String epochTime;
		
		@Expose
		@SerializedName("WeatherText")
		private String weatherText;
		
		@Expose
		@SerializedName("WeatherIcon")
		private String weatherIcon;
		
		@Expose
		@SerializedName("HasPrecipitation")
		private String hasPrecipitation;
		
		@Expose
		@SerializedName("PrecipitationType")
		private String precipitationType;
		
		@Expose
		@SerializedName("IsDayTime")
		private String isDayTime;
		
		@Expose
		@SerializedName("RelativeHumidity")
		private String relativeHumidity;
		
		@Expose
		@SerializedName("IndoorRelativeHumidity")
		private String indoorRelativeHumidity;
		
		@Expose
		@SerializedName("UVIndex")
		private String uVIndex;
		
		@Expose
		@SerializedName("UVIndexText")
		private String uVIndexText;
		
		@Expose
		@SerializedName("ObstructionsToVisibility")
		private String obstructionsToVisibility;
		
		@Expose
		@SerializedName("CloudCover")
		private String cloudCover;
		
		@Expose
		@SerializedName("Temperature")
		private ValuesUnit temperature;
		
		@Expose
		@SerializedName("RealFeelTemperature")
		private ValuesUnit realFeelTemperature;
		
		
		@Expose
		@SerializedName("RealFeelTemperatureShade")
		private ValuesUnit realFeelTemperatureShade;
		
		
		@Expose
		@SerializedName("DewPoint")
		private ValuesUnit dewPoint;
		
		
		@Expose
		@SerializedName("Wind")
		private Wind wind;
		
		
		@Expose
		@SerializedName("WindGust")
		private WindGust windGust;
		
		
		@Expose
		@SerializedName("Visibility")
		private ValuesUnit visibility;
		
		
		@Expose
		@SerializedName("Ceiling")
		private ValuesUnit ceiling;
		
		
		@Expose
		@SerializedName("Pressure")
		private ValuesUnit pressure;
		
		
		@Expose
		@SerializedName("PressureTendency")
		private PressureTendency pressureTendency;
		
		
		@Expose
		@SerializedName("Past24HourTemperatureDeparture")
		private ValuesUnit past24HourTemperatureDeparture;
		
		
		@Expose
		@SerializedName("ApparentTemperature")
		private ValuesUnit apparentTemperature;
		
		
		@Expose
		@SerializedName("WindChillTemperature")
		private ValuesUnit windChillTemperature;
		
		
		@Expose
		@SerializedName("WetBulbTemperature")
		private ValuesUnit wetBulbTemperature;
		
		
		@Expose
		@SerializedName("Precip1hr")
		private ValuesUnit precip1hr;
		
		
		@Expose
		@SerializedName("PrecipitationSummary")
		private PrecipitationSummary precipitationSummary;
		
		
		@Expose
		@SerializedName("TemperatureSummary")
		private TemperatureSummary temperatureSummary;
		
		public String getLocalObservationDateTime() {
			return localObservationDateTime;
		}
		
		public void setLocalObservationDateTime(String localObservationDateTime) {
			this.localObservationDateTime = localObservationDateTime;
		}
		
		public String getEpochTime() {
			return epochTime;
		}
		
		public void setEpochTime(String epochTime) {
			this.epochTime = epochTime;
		}
		
		public String getWeatherText() {
			return weatherText;
		}
		
		public void setWeatherText(String weatherText) {
			this.weatherText = weatherText;
		}
		
		public String getWeatherIcon() {
			return weatherIcon;
		}
		
		public void setWeatherIcon(String weatherIcon) {
			this.weatherIcon = weatherIcon;
		}
		
		public String getHasPrecipitation() {
			return hasPrecipitation;
		}
		
		public void setHasPrecipitation(String hasPrecipitation) {
			this.hasPrecipitation = hasPrecipitation;
		}
		
		public String getPrecipitationType() {
			return precipitationType;
		}
		
		public void setPrecipitationType(String precipitationType) {
			this.precipitationType = precipitationType;
		}
		
		public String getIsDayTime() {
			return isDayTime;
		}
		
		public void setIsDayTime(String isDayTime) {
			this.isDayTime = isDayTime;
		}
		
		public String getRelativeHumidity() {
			return relativeHumidity;
		}
		
		public void setRelativeHumidity(String relativeHumidity) {
			this.relativeHumidity = relativeHumidity;
		}
		
		public String getIndoorRelativeHumidity() {
			return indoorRelativeHumidity;
		}
		
		public void setIndoorRelativeHumidity(String indoorRelativeHumidity) {
			this.indoorRelativeHumidity = indoorRelativeHumidity;
		}
		
		public String getuVIndex() {
			return uVIndex;
		}
		
		public void setuVIndex(String uVIndex) {
			this.uVIndex = uVIndex;
		}
		
		public String getuVIndexText() {
			return uVIndexText;
		}
		
		public void setuVIndexText(String uVIndexText) {
			this.uVIndexText = uVIndexText;
		}
		
		public String getObstructionsToVisibility() {
			return obstructionsToVisibility;
		}
		
		public void setObstructionsToVisibility(String obstructionsToVisibility) {
			this.obstructionsToVisibility = obstructionsToVisibility;
		}
		
		public String getCloudCover() {
			return cloudCover;
		}
		
		public void setCloudCover(String cloudCover) {
			this.cloudCover = cloudCover;
		}
		
		public ValuesUnit getTemperature() {
			return temperature;
		}
		
		public void setTemperature(ValuesUnit temperature) {
			this.temperature = temperature;
		}
		
		public ValuesUnit getRealFeelTemperature() {
			return realFeelTemperature;
		}
		
		public void setRealFeelTemperature(ValuesUnit realFeelTemperature) {
			this.realFeelTemperature = realFeelTemperature;
		}
		
		public ValuesUnit getRealFeelTemperatureShade() {
			return realFeelTemperatureShade;
		}
		
		public void setRealFeelTemperatureShade(ValuesUnit realFeelTemperatureShade) {
			this.realFeelTemperatureShade = realFeelTemperatureShade;
		}
		
		public ValuesUnit getDewPoint() {
			return dewPoint;
		}
		
		public void setDewPoint(ValuesUnit dewPoint) {
			this.dewPoint = dewPoint;
		}
		
		public Wind getWind() {
			return wind;
		}
		
		public void setWind(Wind wind) {
			this.wind = wind;
		}
		
		public WindGust getWindGust() {
			return windGust;
		}
		
		public void setWindGust(WindGust windGust) {
			this.windGust = windGust;
		}
		
		public ValuesUnit getVisibility() {
			return visibility;
		}
		
		public void setVisibility(ValuesUnit visibility) {
			this.visibility = visibility;
		}
		
		public ValuesUnit getCeiling() {
			return ceiling;
		}
		
		public void setCeiling(ValuesUnit ceiling) {
			this.ceiling = ceiling;
		}
		
		public ValuesUnit getPressure() {
			return pressure;
		}
		
		public void setPressure(ValuesUnit pressure) {
			this.pressure = pressure;
		}
		
		public PressureTendency getPressureTendency() {
			return pressureTendency;
		}
		
		public void setPressureTendency(PressureTendency pressureTendency) {
			this.pressureTendency = pressureTendency;
		}
		
		public ValuesUnit getPast24HourTemperatureDeparture() {
			return past24HourTemperatureDeparture;
		}
		
		public void setPast24HourTemperatureDeparture(ValuesUnit past24HourTemperatureDeparture) {
			this.past24HourTemperatureDeparture = past24HourTemperatureDeparture;
		}
		
		public ValuesUnit getApparentTemperature() {
			return apparentTemperature;
		}
		
		public void setApparentTemperature(ValuesUnit apparentTemperature) {
			this.apparentTemperature = apparentTemperature;
		}
		
		public ValuesUnit getWindChillTemperature() {
			return windChillTemperature;
		}
		
		public void setWindChillTemperature(ValuesUnit windChillTemperature) {
			this.windChillTemperature = windChillTemperature;
		}
		
		public ValuesUnit getWetBulbTemperature() {
			return wetBulbTemperature;
		}
		
		public void setWetBulbTemperature(ValuesUnit wetBulbTemperature) {
			this.wetBulbTemperature = wetBulbTemperature;
		}
		
		public ValuesUnit getPrecip1hr() {
			return precip1hr;
		}
		
		public void setPrecip1hr(ValuesUnit precip1hr) {
			this.precip1hr = precip1hr;
		}
		
		public PrecipitationSummary getPrecipitationSummary() {
			return precipitationSummary;
		}
		
		public void setPrecipitationSummary(PrecipitationSummary precipitationSummary) {
			this.precipitationSummary = precipitationSummary;
		}
		
		public TemperatureSummary getTemperatureSummary() {
			return temperatureSummary;
		}
		
		public void setTemperatureSummary(TemperatureSummary temperatureSummary) {
			this.temperatureSummary = temperatureSummary;
		}
		
		public static class PressureTendency {
			@Expose
			@SerializedName("LocalizedText")
			private String localizedText;
			
			@Expose
			@SerializedName("Code")
			private String code;
			
			public String getLocalizedText() {
				return localizedText;
			}
			
			public void setLocalizedText(String localizedText) {
				this.localizedText = localizedText;
			}
			
			public String getCode() {
				return code;
			}
			
			public void setCode(String code) {
				this.code = code;
			}
		}
		
		
		public static class PrecipitationSummary {
			
			@Expose
			@SerializedName("Precipitation")
			private ValuesUnit precipitation;
			
			@Expose
			@SerializedName("PastHour")
			private ValuesUnit pastHour;
			
			@Expose
			@SerializedName("Past3Hours")
			private ValuesUnit past3Hours;
			
			@Expose
			@SerializedName("Past6Hours")
			private ValuesUnit past6Hours;
			
			@Expose
			@SerializedName("Past9Hours")
			private ValuesUnit past9Hours;
			
			@Expose
			@SerializedName("Past12Hours")
			private ValuesUnit past12Hours;
			
			@Expose
			@SerializedName("Past18Hours")
			private ValuesUnit past18Hours;
			
			@Expose
			@SerializedName("Past24Hours")
			private ValuesUnit past24Hours;
			
			public ValuesUnit getPrecipitation() {
				return precipitation;
			}
			
			public PrecipitationSummary setPrecipitation(ValuesUnit precipitation) {
				this.precipitation = precipitation;
				return this;
			}
			
			public ValuesUnit getPastHour() {
				return pastHour;
			}
			
			public PrecipitationSummary setPastHour(ValuesUnit pastHour) {
				this.pastHour = pastHour;
				return this;
			}
			
			public ValuesUnit getPast3Hours() {
				return past3Hours;
			}
			
			public PrecipitationSummary setPast3Hours(ValuesUnit past3Hours) {
				this.past3Hours = past3Hours;
				return this;
			}
			
			public ValuesUnit getPast6Hours() {
				return past6Hours;
			}
			
			public PrecipitationSummary setPast6Hours(ValuesUnit past6Hours) {
				this.past6Hours = past6Hours;
				return this;
			}
			
			public ValuesUnit getPast9Hours() {
				return past9Hours;
			}
			
			public PrecipitationSummary setPast9Hours(ValuesUnit past9Hours) {
				this.past9Hours = past9Hours;
				return this;
			}
			
			public ValuesUnit getPast12Hours() {
				return past12Hours;
			}
			
			public PrecipitationSummary setPast12Hours(ValuesUnit past12Hours) {
				this.past12Hours = past12Hours;
				return this;
			}
			
			public ValuesUnit getPast18Hours() {
				return past18Hours;
			}
			
			public PrecipitationSummary setPast18Hours(ValuesUnit past18Hours) {
				this.past18Hours = past18Hours;
				return this;
			}
			
			public ValuesUnit getPast24Hours() {
				return past24Hours;
			}
			
			public PrecipitationSummary setPast24Hours(ValuesUnit past24Hours) {
				this.past24Hours = past24Hours;
				return this;
			}
		}
		
		public static class TemperatureSummary {
			
			@Expose
			@SerializedName("Past6HourRange")
			private PastHourRange pastHourRange;
			
			@Expose
			@SerializedName("Past12HourRange")
			private PastHourRange past12HourRange;
			
			@Expose
			@SerializedName("Past24HourRange")
			private PastHourRange past24HourRange;
			
			
			public static class PastHourRange {
				@Expose
				@SerializedName("Minimum")
				private ValuesUnit minimum;
				
				@Expose
				@SerializedName("Maximum")
				private ValuesUnit maximum;
				
				public ValuesUnit getMinimum() {
					return minimum;
				}
				
				public PastHourRange setMinimum(ValuesUnit minimum) {
					this.minimum = minimum;
					return this;
				}
				
				public ValuesUnit getMaximum() {
					return maximum;
				}
				
				public PastHourRange setMaximum(ValuesUnit maximum) {
					this.maximum = maximum;
					return this;
				}
			}
			
		}
		
		
	}
	
	
}
