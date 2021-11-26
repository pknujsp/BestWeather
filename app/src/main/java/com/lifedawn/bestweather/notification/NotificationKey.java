package com.lifedawn.bestweather.notification;

public class NotificationKey {
	public enum NotiAttributes {
		LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA, UPDATE_INTERVAL, SELECTED_ADDRESS_DTO_ID
	}
	
	public static class NotiTextViews {
		public enum Header {
			ADDRESS_TEXT_IN_HEADER, REFRESH_TEXT_IN_HEADER
		}
		
		public enum Current {
			TEMP_TEXT_IN_CURRENT, REAL_FEEL_TEMP_TEXT_IN_CURRENT, AIR_QUALITY_TEXT_IN_CURRENT, PRECIPITATION_TEXT_IN_CURRENT
		}
		
		public enum Hourly {
			CLOCK_TEXT_IN_HOURLY, TEMP_TEXT_IN_HOURLY
		}
		
		public enum Daily {
			DATE_TEXT_IN_DAILY, TEMP_TEXT_IN_DAILY
		}
	}
	
	public static class NotiJsonKey {
		
		public enum Root {
			successful
		}
		
		public enum Type {
			ForecastJson, Header, Current, Hourly, Daily, zoneId
		}
		
		public enum Header {
			address, refreshDateTime
		}
		
		public enum Current {
			weatherIcon, temp, realFeelTemp, airQuality, precipitation
		}
		
		public enum Hourly {
			forecasts, clock, temp, weatherIcon
		}
		
		public enum Daily {
			forecasts, date, minTemp, maxTemp, leftWeatherIcon, rightWeatherIcon, isSingle
		}
	}
	
}
