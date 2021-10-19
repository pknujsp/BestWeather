package com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.FeelsLike;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.Temp;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.Weather;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.dailyforecast.DailyForecast;

import java.util.List;

public class OneCallResponse {
	@Expose
	@SerializedName("lat")
	private String latitude;

	@Expose
	@SerializedName("lon")
	private String longitude;

	@Expose
	@SerializedName("timezone")
	private String timezone;

	@Expose
	@SerializedName("timezone_offset")
	private String timezoneOffset;

	@Expose
	@SerializedName("current")
	private Current current;

	@Expose
	@SerializedName("minutely")
	private List<Minutely> minutely;

	@Expose
	@SerializedName("hourly")
	private List<Hourly> hourly;

	@Expose
	@SerializedName("daily")
	private List<Daily> daily;

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(String timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	public Current getCurrent() {
		return current;
	}

	public void setCurrent(Current current) {
		this.current = current;
	}

	public List<Minutely> getMinutely() {
		return minutely;
	}

	public void setMinutely(List<Minutely> minutely) {
		this.minutely = minutely;
	}

	public List<Hourly> getHourly() {
		return hourly;
	}

	public void setHourly(List<Hourly> hourly) {
		this.hourly = hourly;
	}

	public List<Daily> getDaily() {
		return daily;
	}

	public void setDaily(List<Daily> daily) {
		this.daily = daily;
	}

	public static class Current {
		@Expose
		@SerializedName("dt")
		private String dt;

		@Expose
		@SerializedName("temp")
		private String temp;

		@Expose
		@SerializedName("feels_like")
		private String feelsLike;

		@Expose
		@SerializedName("pressure")
		private String pressure;

		@Expose
		@SerializedName("humidity")
		private String humidity;

		@Expose
		@SerializedName("dew_point")
		private String dewPoint;

		@Expose
		@SerializedName("uvi")
		private String uvi;

		@Expose
		@SerializedName("clouds")
		private String clouds;

		@Expose
		@SerializedName("visibility")
		private String visibility;

		@Expose
		@SerializedName("wind_speed")
		private String wind_speed;

		@Expose
		@SerializedName("wind_gust")
		private String windGust;

		@Expose
		@SerializedName("wind_deg")
		private String wind_deg;

		@Expose
		@SerializedName("rain")
		private Rain rain;

		@Expose
		@SerializedName("snow")
		private Snow snow;

		@Expose
		@SerializedName("weather")
		private List<Weather> weather;

		public String getDt() {
			return dt;
		}

		public void setDt(String dt) {
			this.dt = dt;
		}

		public String getTemp() {
			return temp;
		}

		public void setTemp(String temp) {
			this.temp = temp;
		}

		public String getFeelsLike() {
			return feelsLike;
		}

		public void setFeelsLike(String feelsLike) {
			this.feelsLike = feelsLike;
		}

		public String getPressure() {
			return pressure;
		}

		public void setPressure(String pressure) {
			this.pressure = pressure;
		}

		public String getHumidity() {
			return humidity;
		}

		public void setHumidity(String humidity) {
			this.humidity = humidity;
		}

		public String getDewPoint() {
			return dewPoint;
		}

		public void setDewPoint(String dewPoint) {
			this.dewPoint = dewPoint;
		}

		public String getUvi() {
			return uvi;
		}

		public void setUvi(String uvi) {
			this.uvi = uvi;
		}

		public String getClouds() {
			return clouds;
		}

		public void setClouds(String clouds) {
			this.clouds = clouds;
		}

		public String getVisibility() {
			return visibility;
		}

		public void setVisibility(String visibility) {
			this.visibility = visibility;
		}

		public String getWind_speed() {
			return wind_speed;
		}

		public void setWind_speed(String wind_speed) {
			this.wind_speed = wind_speed;
		}

		public String getWind_deg() {
			return wind_deg;
		}

		public void setWind_deg(String wind_deg) {
			this.wind_deg = wind_deg;
		}

		public List<Weather> getWeather() {
			return weather;
		}

		public void setWeather(List<Weather> weather) {
			this.weather = weather;
		}

		public Rain getRain() {
			return rain;
		}

		public void setRain(Rain rain) {
			this.rain = rain;
		}

		public Snow getSnow() {
			return snow;
		}

		public void setSnow(Snow snow) {
			this.snow = snow;
		}

		public String getWindGust() {
			return windGust;
		}

		public void setWindGust(String windGust) {
			this.windGust = windGust;
		}
	}

	public static class Minutely {
		@Expose
		@SerializedName("dt")
		private String dt;

		@Expose
		@SerializedName("precipitation")
		private String precipitation;

		public String getDt() {
			return dt;
		}

		public void setDt(String dt) {
			this.dt = dt;
		}

		public String getPrecipitation() {
			return precipitation;
		}

		public void setPrecipitation(String precipitation) {
			this.precipitation = precipitation;
		}
	}

	public static class Hourly extends Current {
	

		@Expose
		@SerializedName("pop")
		private String pop;
		

		public String getPop() {
			return pop;
		}

		public void setPop(String pop) {
			this.pop = pop;
		}
	}

	public static class Daily {
		@Expose
		@SerializedName("dt")
		private String dt;

		@Expose
		@SerializedName("temp")
		private Temp temp;

		@Expose
		@SerializedName("feels_like")
		private FeelsLike feelsLike;

		@Expose
		@SerializedName("pressure")
		private String pressure;

		@Expose
		@SerializedName("humidity")
		private String humidity;

		@Expose
		@SerializedName("dew_point")
		private String dew_point;

		@Expose
		@SerializedName("wind_speed")
		private String windSpeed;

		@Expose
		@SerializedName("wind_deg")
		private String windDeg;

		@Expose
		@SerializedName("wind_gust")
		private String windGust;

		@Expose
		@SerializedName("weather")
		private List<Weather> weather;

		@Expose
		@SerializedName("clouds")
		private String clouds;

		@Expose
		@SerializedName("pop")
		private String pop;

		@Expose
		@SerializedName("rain")
		private String rain;


		@Expose
		@SerializedName("snow")
		private String snow;


		@Expose
		@SerializedName("uvi")
		private String uvi;

		public String getDt() {
			return dt;
		}

		public void setDt(String dt) {
			this.dt = dt;
		}

		public Temp getTemp() {
			return temp;
		}

		public void setTemp(Temp temp) {
			this.temp = temp;
		}

		public FeelsLike getFeelsLike() {
			return feelsLike;
		}

		public void setFeelsLike(FeelsLike feelsLike) {
			this.feelsLike = feelsLike;
		}

		public String getPressure() {
			return pressure;
		}

		public void setPressure(String pressure) {
			this.pressure = pressure;
		}

		public String getHumidity() {
			return humidity;
		}

		public void setHumidity(String humidity) {
			this.humidity = humidity;
		}

		public String getDew_point() {
			return dew_point;
		}

		public void setDew_point(String dew_point) {
			this.dew_point = dew_point;
		}

		public String getWindSpeed() {
			return windSpeed;
		}

		public void setWindSpeed(String windSpeed) {
			this.windSpeed = windSpeed;
		}

		public String getWindDeg() {
			return windDeg;
		}

		public void setWindDeg(String windDeg) {
			this.windDeg = windDeg;
		}

		public String getWindGust() {
			return windGust;
		}

		public void setWindGust(String windGust) {
			this.windGust = windGust;
		}

		public List<Weather> getWeather() {
			return weather;
		}

		public void setWeather(List<Weather> weather) {
			this.weather = weather;
		}

		public String getClouds() {
			return clouds;
		}

		public void setClouds(String clouds) {
			this.clouds = clouds;
		}

		public String getPop() {
			return pop;
		}

		public void setPop(String pop) {
			this.pop = pop;
		}

		public String getRain() {
			return rain;
		}

		public void setRain(String rain) {
			this.rain = rain;
		}

		public String getSnow() {
			return snow;
		}

		public void setSnow(String snow) {
			this.snow = snow;
		}

		public String getUvi() {
			return uvi;
		}

		public void setUvi(String uvi) {
			this.uvi = uvi;
		}
	}

	public static class Rain {
		@Expose
		@SerializedName("1h")
		private String precipitation1Hour;

		public String getPrecipitation1Hour() {
			return precipitation1Hour;
		}

		public void setPrecipitation1Hour(String precipitation1Hour) {
			this.precipitation1Hour = precipitation1Hour;
		}
	}

	public static class Snow {
		@Expose
		@SerializedName("1h")
		private String precipitation1Hour;

		public String getPrecipitation1Hour() {
			return precipitation1Hour;
		}

		public void setPrecipitation1Hour(String precipitation1Hour) {
			this.precipitation1Hour = precipitation1Hour;
		}
	}

}
