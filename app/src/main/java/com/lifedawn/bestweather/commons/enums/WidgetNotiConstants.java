package com.lifedawn.bestweather.commons.enums;

public class WidgetNotiConstants {
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

	public enum DailyNotiAttributes {
		ALARM_CLOCK
	}

}
