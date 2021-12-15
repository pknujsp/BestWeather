package com.lifedawn.bestweather.weathers.models;

import java.time.ZonedDateTime;
import java.util.List;

public class AirQualityDto {
	private Integer aqi;
	private Integer idx;
	private Double latitude;
	private Double longitude;
	private String cityName;
	private String aqiCnUrl;
	private ZonedDateTime time;
	private Current current;
	private List<DailyForecast> dailyForecastList;

	public static class Current {
		private Integer co;
		private boolean hasCo;

		private Integer dew;
		private boolean hasDew;

		private Integer no2;
		private boolean hasNo2;

		private Integer o3;
		private boolean hasO3;

		private Integer pm10;
		private boolean hasPm10;

		private Integer pm25;
		private boolean hasPm25;

		private Integer so2;
		private boolean hasSo2;

		public Integer getCo() {
			return co;
		}

		public Current setCo(Integer co) {
			this.co = co;
			hasCo = true;
			return this;
		}

		public boolean isHasCo() {
			return hasCo;
		}

		public Current setHasCo(boolean hasCo) {
			this.hasCo = hasCo;
			return this;
		}

		public Integer getDew() {
			return dew;
		}

		public Current setDew(Integer dew) {
			this.dew = dew;
			hasDew = true;
			return this;
		}

		public boolean isHasDew() {
			return hasDew;
		}

		public Current setHasDew(boolean hasDew) {
			this.hasDew = hasDew;
			return this;
		}

		public Integer getNo2() {
			return no2;
		}

		public Current setNo2(Integer no2) {
			this.no2 = no2;
			hasNo2 = true;
			return this;
		}

		public boolean isHasNo2() {
			return hasNo2;
		}

		public Current setHasNo2(boolean hasNo2) {
			this.hasNo2 = hasNo2;
			return this;
		}

		public Integer getO3() {
			return o3;
		}

		public Current setO3(Integer o3) {
			this.o3 = o3;
			hasO3 = true;
			return this;
		}

		public boolean isHasO3() {
			return hasO3;
		}

		public Current setHasO3(boolean hasO3) {
			this.hasO3 = hasO3;
			return this;
		}

		public Integer getPm10() {
			return pm10;
		}

		public Current setPm10(Integer pm10) {
			this.pm10 = pm10;
			hasPm10 = true;
			return this;
		}

		public boolean isHasPm10() {
			return hasPm10;
		}

		public Current setHasPm10(boolean hasPm10) {
			this.hasPm10 = hasPm10;
			return this;
		}

		public Integer getPm25() {
			return pm25;
		}

		public Current setPm25(Integer pm25) {
			this.pm25 = pm25;
			hasPm25 = true;
			return this;
		}

		public boolean isHasPm25() {
			return hasPm25;
		}

		public Current setHasPm25(boolean hasPm25) {
			this.hasPm25 = hasPm25;
			return this;
		}

		public Integer getSo2() {
			return so2;
		}

		public Current setSo2(Integer so2) {
			this.so2 = so2;
			hasSo2 = true;
			return this;
		}

		public boolean isHasSo2() {
			return hasSo2;
		}

		public Current setHasSo2(boolean hasSo2) {
			this.hasSo2 = hasSo2;
			return this;
		}
	}

	public static class DailyForecast {
		private ZonedDateTime date;
		private boolean hasO3;
		private boolean hasPm10;
		private boolean hasPm25;
		private boolean hasUvi;

		private Val o3;
		private Val pm10;
		private Val pm25;
		private Val uvi;

		public static class Val {
			private Integer max;
			private Integer min;
			private Integer avg;

			public Integer getMax() {
				return max;
			}

			public Val setMax(Integer max) {
				this.max = max;
				return this;
			}

			public Integer getMin() {
				return min;
			}

			public Val setMin(Integer min) {
				this.min = min;
				return this;
			}

			public Integer getAvg() {
				return avg;
			}

			public Val setAvg(Integer avg) {
				this.avg = avg;
				return this;
			}
		}

		public ZonedDateTime getDate() {
			return date;
		}

		public DailyForecast setDate(ZonedDateTime date) {
			this.date = date;
			return this;
		}

		public boolean isHasO3() {
			return hasO3;
		}

		public DailyForecast setHasO3(boolean hasO3) {
			this.hasO3 = hasO3;
			return this;
		}

		public boolean isHasPm10() {
			return hasPm10;
		}

		public DailyForecast setHasPm10(boolean hasPm10) {
			this.hasPm10 = hasPm10;
			return this;
		}

		public boolean isHasPm25() {
			return hasPm25;
		}

		public DailyForecast setHasPm25(boolean hasPm25) {
			this.hasPm25 = hasPm25;
			return this;
		}

		public boolean isHasUvi() {
			return hasUvi;
		}

		public DailyForecast setHasUvi(boolean hasUvi) {
			this.hasUvi = hasUvi;
			return this;
		}

		public Val getO3() {
			return o3;
		}

		public DailyForecast setO3(Val o3) {
			hasO3 = true;
			this.o3 = o3;
			return this;
		}

		public Val getPm10() {
			return pm10;
		}

		public DailyForecast setPm10(Val pm10) {
			this.pm10 = pm10;
			hasPm10 = true;
			return this;
		}

		public Val getPm25() {
			return pm25;
		}

		public DailyForecast setPm25(Val pm25) {
			this.pm25 = pm25;
			hasPm25 = true;
			return this;
		}

		public Val getUvi() {
			return uvi;
		}

		public DailyForecast setUvi(Val uvi) {
			this.uvi = uvi;
			hasUvi = true;
			return this;
		}
	}

	public Integer getAqi() {
		return aqi;
	}

	public AirQualityDto setAqi(Integer aqi) {
		this.aqi = aqi;
		return this;
	}

	public Integer getIdx() {
		return idx;
	}

	public AirQualityDto setIdx(Integer idx) {
		this.idx = idx;
		return this;
	}

	public Double getLatitude() {
		return latitude;
	}

	public AirQualityDto setLatitude(Double latitude) {
		this.latitude = latitude;
		return this;
	}

	public Double getLongitude() {
		return longitude;
	}

	public AirQualityDto setLongitude(Double longitude) {
		this.longitude = longitude;
		return this;
	}

	public String getCityName() {
		return cityName;
	}

	public AirQualityDto setCityName(String cityName) {
		this.cityName = cityName;
		return this;
	}

	public ZonedDateTime getTime() {
		return time;
	}

	public AirQualityDto setTime(ZonedDateTime time) {
		this.time = time;
		return this;
	}

	public Current getCurrent() {
		return current;
	}

	public AirQualityDto setCurrent(Current current) {
		this.current = current;
		return this;
	}

	public List<DailyForecast> getDailyForecastList() {
		return dailyForecastList;
	}

	public AirQualityDto setDailyForecastList(List<DailyForecast> dailyForecastList) {
		this.dailyForecastList = dailyForecastList;
		return this;
	}

	public String getAqiCnUrl() {
		return aqiCnUrl;
	}

	public AirQualityDto setAqiCnUrl(String aqiCnUrl) {
		this.aqiCnUrl = aqiCnUrl;
		return this;
	}
}
