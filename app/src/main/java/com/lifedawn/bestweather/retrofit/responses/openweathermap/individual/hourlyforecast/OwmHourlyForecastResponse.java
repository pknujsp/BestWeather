package com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.hourlyforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.Coord;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.Weather;

import java.util.List;

public class OwmHourlyForecastResponse {
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

	@Expose
	@SerializedName("city")
	private City city;

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

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public static class Item {

		@Expose
		@SerializedName("visibility")
		private String visibility;

		@Expose
		@SerializedName("dt")
		private String dt;

		@Expose
		@SerializedName("pop")
		private String pop;

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
		@SerializedName("rain")
		private Rain rain;

		@Expose
		@SerializedName("snow")
		private Snow snow;

		@Expose
		@SerializedName("sys")
		private Sys sys;

		@Expose
		@SerializedName("dt_txt")
		private String dtTxt;

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

		public String getPop() {
			return pop;
		}

		public void setPop(String pop) {
			this.pop = pop;
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

		public void setSys(Sys sys) {
			this.sys = sys;
		}

		public String getDtTxt() {
			return dtTxt;
		}

		public void setDtTxt(String dtTxt) {
			this.dtTxt = dtTxt;
		}

		public static class Rain {
			@Expose
			@SerializedName("1h")
			private String rainVolumeOneHour;

			public String getRainVolumeOneHour() {
				return rainVolumeOneHour;
			}

			public void setRainVolumeOneHour(String rainVolumeOneHour) {
				this.rainVolumeOneHour = rainVolumeOneHour;
			}
		}

		public static class Snow {
			@Expose
			@SerializedName("1h")
			private String snowVolumeOneHour;

			public String getSnowVolumeOneHour() {
				return snowVolumeOneHour;
			}

			public void setSnowVolumeOneHour(String snowVolumeOneHour) {
				this.snowVolumeOneHour = snowVolumeOneHour;
			}
		}


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
			@SerializedName("sea_level")
			private String seaLevel;

			@Expose
			@SerializedName("grnd_level")
			private String grndLevel;

			@Expose
			@SerializedName("humidity")
			private String humidity;

			@Expose
			@SerializedName("temp_kf")
			private String tempKf;

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

			public String getSeaLevel() {
				return seaLevel;
			}

			public void setSeaLevel(String seaLevel) {
				this.seaLevel = seaLevel;
			}

			public String getGrndLevel() {
				return grndLevel;
			}

			public void setGrndLevel(String grndLevel) {
				this.grndLevel = grndLevel;
			}

			public String getHumidity() {
				return humidity;
			}

			public void setHumidity(String humidity) {
				this.humidity = humidity;
			}

			public String getTempKf() {
				return tempKf;
			}

			public void setTempKf(String tempKf) {
				this.tempKf = tempKf;
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

			public void setGust(String gust) {
				this.gust = gust;
			}

			public String getGust() {
				return gust;
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
			@SerializedName("pod")
			private String pod;

			public void setPod(String pod) {
				this.pod = pod;
			}

			public String getPod() {
				return pod;
			}
		}
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
}
