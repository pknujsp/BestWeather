package com.lifedawn.bestweather.commons.enums;

public class WidgetNotiConstants {
	public static class JsonKey {

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
			forecasts, date, minTemp, maxTemp, leftWeatherIcon, rightWeatherIcon, isSingle, leftPop, rightPop
		}
	}

	public static class Commons {
		public enum Attributes {
			LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA,
			UPDATE_INTERVAL, SELECTED_ADDRESS_DTO_ID
		}

		public enum DataKeys {
			ADDRESS_NAME, LATITUDE, LONGITUDE, COUNTRY_CODE, TIMEZONE_ID
		}
	}

	public enum WidgetAttributes {
		WIDGET_ATTRIBUTES_ID, APP_WIDGET_ID, BACKGROUND_ALPHA, DISPLAY_CLOCK, DISPLAY_LOCAL_CLOCK, WIDGET_CLASS, REMOTE_VIEWS
	}

	public static class WidgetTextViews {
		public enum Header {
			ADDRESS_TEXT_IN_HEADER, REFRESH_TEXT_IN_HEADER
		}

		public enum Clock {
			DATE_TEXT_IN_CLOCK, TIME_TEXT_IN_CLOCK
		}

		public enum Current {
			TEMP_TEXT_IN_CURRENT, REAL_FEEL_TEMP_TEXT_IN_CURRENT, AIR_QUALITY_TEXT_IN_CURRENT, PRECIPITATION_TEXT_IN_CURRENT
		}
	}

	public enum NotiAttributes {

	}

}
