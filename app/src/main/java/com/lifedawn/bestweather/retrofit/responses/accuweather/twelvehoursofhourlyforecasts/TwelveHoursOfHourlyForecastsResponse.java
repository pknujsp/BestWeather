package com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValueUnit;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Wind;
import com.lifedawn.bestweather.retrofit.responses.accuweather.WindGust;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class TwelveHoursOfHourlyForecastsResponse {
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
		@SerializedName("DateTime")
		private String dateTime;

		@Expose
		@SerializedName("EpochDateTime")
		private String epochDateTime;

		@Expose
		@SerializedName("WeatherIcon")
		private String weatherIcon;

		@Expose
		@SerializedName("IconPhrase")
		private String iconPhrase;

		@Expose
		@SerializedName("HasPrecipitation")
		private String hasPrecipitation;

		@Expose
		@SerializedName("IsDaylight")
		private String isDaylight;

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
		@SerializedName("PrecipitationProbability")
		private String precipitationProbability;

		@Expose
		@SerializedName("ThunderstormProbability")
		private String thunderstormProbability;

		@Expose
		@SerializedName("RainProbability")
		private String rainProbability;

		@Expose
		@SerializedName("SnowProbability")
		private String snowProbability;

		@Expose
		@SerializedName("IceProbability")
		private String iceProbability;


		@Expose
		@SerializedName("CloudCover")
		private String cloudCover;

		@Expose
		@SerializedName("Wind")
		private Wind wind;

		@Expose
		@SerializedName("WindGust")
		private WindGust windGust;

		@Expose
		@SerializedName("TotalLiquid")
		private ValueUnit totalLiquid;

		@Expose
		@SerializedName("Rain")
		private ValueUnit rain;

		@Expose
		@SerializedName("Snow")
		private ValueUnit snow;

		@Expose
		@SerializedName("Ice")
		private ValueUnit ice;

		@Expose
		@SerializedName("Temperature")
		private ValueUnit temperature;

		@Expose
		@SerializedName("RealFeelTemperature")
		private ValueUnit realFeelTemperature;

		@Expose
		@SerializedName("RealFeelTemperatureShade")
		private ValueUnit realFeelTemperatureShade;

		@Expose
		@SerializedName("WetBulbTemperature")
		private ValueUnit wetBulbTemperature;

		@Expose
		@SerializedName("DewPoint")
		private ValueUnit dewPoint;

		@Expose
		@SerializedName("Visibility")
		private ValueUnit visibility;

		@Expose
		@SerializedName("Ceiling")
		private ValueUnit ceiling;

		public String getDateTime() {
			return dateTime;
		}

		public void setDateTime(String dateTime) {
			this.dateTime = dateTime;
		}

		public String getEpochDateTime() {
			return epochDateTime;
		}

		public void setEpochDateTime(String epochDateTime) {
			this.epochDateTime = epochDateTime;
		}

		public String getWeatherIcon() {
			return weatherIcon;
		}

		public void setWeatherIcon(String weatherIcon) {
			this.weatherIcon = weatherIcon;
		}

		public String getIconPhrase() {
			return iconPhrase;
		}

		public void setIconPhrase(String iconPhrase) {
			this.iconPhrase = iconPhrase;
		}

		public String getHasPrecipitation() {
			return hasPrecipitation;
		}

		public void setHasPrecipitation(String hasPrecipitation) {
			this.hasPrecipitation = hasPrecipitation;
		}

		public String getIsDaylight() {
			return isDaylight;
		}

		public void setIsDaylight(String isDaylight) {
			this.isDaylight = isDaylight;
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

		public String getPrecipitationProbability() {
			return precipitationProbability;
		}

		public void setPrecipitationProbability(String precipitationProbability) {
			this.precipitationProbability = precipitationProbability;
		}

		public String getThunderstormProbability() {
			return thunderstormProbability;
		}

		public void setThunderstormProbability(String thunderstormProbability) {
			this.thunderstormProbability = thunderstormProbability;
		}

		public String getRainProbability() {
			return rainProbability;
		}

		public void setRainProbability(String rainProbability) {
			this.rainProbability = rainProbability;
		}

		public String getSnowProbability() {
			return snowProbability;
		}

		public void setSnowProbability(String snowProbability) {
			this.snowProbability = snowProbability;
		}

		public String getIceProbability() {
			return iceProbability;
		}

		public void setIceProbability(String iceProbability) {
			this.iceProbability = iceProbability;
		}

		public String getCloudCover() {
			return cloudCover;
		}

		public void setCloudCover(String cloudCover) {
			this.cloudCover = cloudCover;
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

		public ValueUnit getTotalLiquid() {
			return totalLiquid;
		}

		public void setTotalLiquid(ValueUnit totalLiquid) {
			this.totalLiquid = totalLiquid;
		}

		public ValueUnit getRain() {
			return rain;
		}

		public void setRain(ValueUnit rain) {
			this.rain = rain;
		}

		public ValueUnit getSnow() {
			return snow;
		}

		public void setSnow(ValueUnit snow) {
			this.snow = snow;
		}

		public ValueUnit getIce() {
			return ice;
		}

		public void setIce(ValueUnit ice) {
			this.ice = ice;
		}

		public ValueUnit getTemperature() {
			return temperature;
		}

		public void setTemperature(ValueUnit temperature) {
			this.temperature = temperature;
		}

		public ValueUnit getRealFeelTemperature() {
			return realFeelTemperature;
		}

		public void setRealFeelTemperature(ValueUnit realFeelTemperature) {
			this.realFeelTemperature = realFeelTemperature;
		}

		public ValueUnit getRealFeelTemperatureShade() {
			return realFeelTemperatureShade;
		}

		public void setRealFeelTemperatureShade(ValueUnit realFeelTemperatureShade) {
			this.realFeelTemperatureShade = realFeelTemperatureShade;
		}

		public ValueUnit getWetBulbTemperature() {
			return wetBulbTemperature;
		}

		public void setWetBulbTemperature(ValueUnit wetBulbTemperature) {
			this.wetBulbTemperature = wetBulbTemperature;
		}

		public ValueUnit getDewPoint() {
			return dewPoint;
		}

		public void setDewPoint(ValueUnit dewPoint) {
			this.dewPoint = dewPoint;
		}

		public ValueUnit getVisibility() {
			return visibility;
		}

		public void setVisibility(ValueUnit visibility) {
			this.visibility = visibility;
		}

		public ValueUnit getCeiling() {
			return ceiling;
		}

		public void setCeiling(ValueUnit ceiling) {
			this.ceiling = ceiling;
		}
	}
}
