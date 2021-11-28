package com.lifedawn.bestweather.forremoteviews.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherJsonObj {
	//	ForecastJson, Header, Current, Hourly, Daily;
	@Expose
	@SerializedName("successful")
	private boolean successful;

	@Expose
	@SerializedName("Header")
	private HeaderObj headerObj;

	@Expose
	@SerializedName("Current")
	private CurrentConditionsObj currentConditionsObj;

	@Expose
	@SerializedName("Hourly")
	private HourlyForecasts hourlyForecasts;

	@Expose
	@SerializedName("Daily")
	private DailyForecasts dailyForecasts;


	public static class HourlyForecasts {
		@Expose
		@SerializedName("zoneId")
		private String zoneId;

		@Expose
		@SerializedName("forecasts")
		private List<HourlyForecastObj> hourlyForecastObjs;

		public List<HourlyForecastObj> getHourlyForecastObjs() {
			return hourlyForecastObjs;
		}

		public void setHourlyForecastObjs(List<HourlyForecastObj> hourlyForecastObjs) {
			this.hourlyForecastObjs = hourlyForecastObjs;
		}

		public String getZoneId() {
			return zoneId;
		}

		public void setZoneId(String zoneId) {
			this.zoneId = zoneId;
		}
	}

	public static class DailyForecasts {
		@Expose
		@SerializedName("zoneId")
		private String zoneId;

		@Expose
		@SerializedName("forecasts")
		private List<DailyForecastObj> dailyForecastObjs;

		public List<DailyForecastObj> getDailyForecastObjs() {
			return dailyForecastObjs;
		}

		public void setDailyForecastObjs(List<DailyForecastObj> dailyForecastObjs) {
			this.dailyForecastObjs = dailyForecastObjs;
		}

		public String getZoneId() {
			return zoneId;
		}

		public void setZoneId(String zoneId) {
			this.zoneId = zoneId;
		}
	}

	public HeaderObj getHeaderObj() {
		return headerObj;
	}

	public void setHeaderObj(HeaderObj headerObj) {
		this.headerObj = headerObj;
	}

	public CurrentConditionsObj getCurrentConditionsObj() {
		return currentConditionsObj;
	}

	public void setCurrentConditionsObj(CurrentConditionsObj currentConditionsObj) {
		this.currentConditionsObj = currentConditionsObj;
	}

	public HourlyForecasts getHourlyForecasts() {
		return hourlyForecasts;
	}

	public void setHourlyForecasts(HourlyForecasts hourlyForecasts) {
		this.hourlyForecasts = hourlyForecasts;
	}

	public DailyForecasts getDailyForecasts() {
		return dailyForecasts;
	}

	public void setDailyForecasts(DailyForecasts dailyForecasts) {
		this.dailyForecasts = dailyForecasts;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
}
