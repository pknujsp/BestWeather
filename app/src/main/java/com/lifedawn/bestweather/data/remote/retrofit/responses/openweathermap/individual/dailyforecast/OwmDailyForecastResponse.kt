package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.dailyforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Coord;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.FeelsLike;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Temp;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Weather;

import java.util.List;

public class OwmDailyForecastResponse {
	@Expose
	@SerializedName("city")
	private City city;

	@Expose
	@SerializedName("cod")
	private String cod;

	@Expose
	@SerializedName("message")
	private String message;

	@Expose
	@SerializedName("cnt")
	private String cnt;

	@Expose
	@SerializedName("list")
	private List<Item> list;

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public String getCod() {
		return cod;
	}

	public void setCod(String cod) {
		this.cod = cod;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCnt() {
		return cnt;
	}

	public void setCnt(String cnt) {
		this.cnt = cnt;
	}

	public List<Item> getList() {
		return list;
	}

	public void setList(List<Item> list) {
		this.list = list;
	}

	public static class City {
		@Expose
		@SerializedName("id")
		private String id;

		@Expose
		@SerializedName("name")
		private String name;

		@Expose
		@SerializedName("country")
		private String country;

		@Expose
		@SerializedName("population")
		private String population;

		@Expose
		@SerializedName("timezone")
		private String timezone;

		@Expose
		@SerializedName("coord")
		private Coord coord;

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

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getPopulation() {
			return population;
		}

		public void setPopulation(String population) {
			this.population = population;
		}

		public String getTimezone() {
			return timezone;
		}

		public void setTimezone(String timezone) {
			this.timezone = timezone;
		}

		public Coord getCoord() {
			return coord;
		}

		public void setCoord(Coord coord) {
			this.coord = coord;
		}
	}

	public static class Item {
		@Expose
		@SerializedName("dt")
		private String dt;

		@Expose
		@SerializedName("pressure")
		private String pressure;

		@Expose
		@SerializedName("humidity")
		private String humidity;

		@Expose
		@SerializedName("speed")
		private String speed;

		@Expose
		@SerializedName("deg")
		private String deg;

		@Expose
		@SerializedName("gust")
		private String gust;

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
		@SerializedName("temp")
		private Temp temp;

		@Expose
		@SerializedName("feels_like")
		private FeelsLike feelsLike;

		@Expose
		@SerializedName("weather")
		private List<Weather> weather;

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

		public String getDt() {
			return dt;
		}

		public void setDt(String dt) {
			this.dt = dt;
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

		public String getGust() {
			return gust;
		}

		public void setGust(String gust) {
			this.gust = gust;
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

		public List<Weather> getWeather() {
			return weather;
		}

		public void setWeather(List<Weather> weather) {
			this.weather = weather;
		}


	}
}
