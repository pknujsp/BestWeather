package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.currentweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Coord;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Weather;

import java.util.List;

public class OwmCurrentConditionsResponse {

	@Expose
	@SerializedName("base")
	private String base;

	@Expose
	@SerializedName("visibility")
	private String visibility;

	@Expose
	@SerializedName("dt")
	private String dt;

	@Expose
	@SerializedName("timezone")
	private String timezone;

	@Expose
	@SerializedName("id")
	private String id;

	@Expose
	@SerializedName("name")
	private String name;

	@Expose
	@SerializedName("cod")
	private String cod;

	@Expose
	@SerializedName("coord")
	private Coord coord;

	@Expose
	@SerializedName("weather")
	private List<Weather> weather;

	@Expose
	@SerializedName("main")
	private Main main;

	@Expose
	@SerializedName("wind")
	private Wind wind;

	@Expose
	@SerializedName("clouds")
	private Clouds clouds;

	@Expose
	@SerializedName("sys")
	private Sys sys;

	@Expose
	@SerializedName("rain")
	private Rain rain;


	@Expose
	@SerializedName("snow")
	private Snow snow;


	public static class Main {
		@Expose
		@SerializedName("temp")
		private String temp;

		@Expose
		@SerializedName("feels_like")
		private String feels_like;

		@Expose
		@SerializedName("temp_min")
		private String temp_min;

		@Expose
		@SerializedName("temp_max")
		private String temp_max;

		@Expose
		@SerializedName("pressure")
		private String pressure;

		@Expose
		@SerializedName("humidity")
		private String humidity;

		public String getTemp() {
			return temp;
		}

		public void setTemp(String temp) {
			this.temp = temp;
		}

		public String getFeels_like() {
			return feels_like;
		}

		public void setFeels_like(String feels_like) {
			this.feels_like = feels_like;
		}

		public String getTemp_min() {
			return temp_min;
		}

		public void setTemp_min(String temp_min) {
			this.temp_min = temp_min;
		}

		public String getTemp_max() {
			return temp_max;
		}

		public void setTemp_max(String temp_max) {
			this.temp_max = temp_max;
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
	}

	public static class Wind {
		@Expose
		@SerializedName("speed")
		private String speed;

		@Expose
		@SerializedName("deg")
		private String deg;

		@Expose
		@SerializedName("gust")
		private String gust;

		public String getGust() {
			return gust;
		}

		public void setGust(String gust) {
			this.gust = gust;
		}

		public String getSpeed() {
			return speed;
		}

		public void setSpeed(String speed) {
			this.speed = speed;
		}

		public String getDeg() {
			return deg;
		}

		public void setDeg(String deg) {
			this.deg = deg;
		}
	}

	public static class Clouds {
		@Expose
		@SerializedName("all")
		private String all;

		public void setAll(String all) {
			this.all = all;
		}

		public String getAll() {
			return all;
		}
	}

	public static class Sys {
		@Expose
		@SerializedName("type")
		private String type;

		@Expose
		@SerializedName("id")
		private String id;

		@Expose
		@SerializedName("message")
		private String message;

		@Expose
		@SerializedName("country")
		private String country;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}
	}

	public static class Rain {
		@Expose
		@SerializedName("1h")
		private String rainVolume1Hour;

		@Expose
		@SerializedName("3h")
		private String rainVolume3Hour;

		public String getRainVolume1Hour() {
			return rainVolume1Hour;
		}

		public void setRainVolume1Hour(String rainVolume1Hour) {
			this.rainVolume1Hour = rainVolume1Hour;
		}

		public String getRainVolume3Hour() {
			return rainVolume3Hour;
		}

		public void setRainVolume3Hour(String rainVolume3Hour) {
			this.rainVolume3Hour = rainVolume3Hour;
		}
	}

	public static class Snow {
		@Expose
		@SerializedName("1h")
		private String snowVolume1Hour;

		@Expose
		@SerializedName("3h")
		private String snowVolume3Hour;

		public String getSnowVolume1Hour() {
			return snowVolume1Hour;
		}

		public void setSnowVolume1Hour(String snowVolume1Hour) {
			this.snowVolume1Hour = snowVolume1Hour;
		}

		public String getSnowVolume3Hour() {
			return snowVolume3Hour;
		}

		public void setSnowVolume3Hour(String snowVolume3Hour) {
			this.snowVolume3Hour = snowVolume3Hour;
		}
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getDt() {
		return dt;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCod() {
		return cod;
	}

	public void setCod(String cod) {
		this.cod = cod;
	}

	public Coord getCoord() {
		return coord;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public List<Weather> getWeather() {
		return weather;
	}

	public void setWeather(List<Weather> weather) {
		this.weather = weather;
	}

	public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public Wind getWind() {
		return wind;
	}

	public void setWind(Wind wind) {
		this.wind = wind;
	}

	public Clouds getClouds() {
		return clouds;
	}

	public void setClouds(Clouds clouds) {
		this.clouds = clouds;
	}

	public Sys getSys() {
		return sys;
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

	public void setSys(Sys sys) {
		this.sys = sys;
	}
}
