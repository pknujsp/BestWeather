package com.lifedawn.bestweather.commons.constants;

public class WidgetNotiConstants {
	public static class Commons {
		public enum Attributes {
			LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA,
			UPDATE_INTERVAL, SELECTED_ADDRESS_DTO_ID
		}

		public enum DataKeys {
			ADDRESS_NAME, LATITUDE, LONGITUDE, COUNTRY_CODE, ZONE_ID
		}
	}

	public enum WidgetAttributes {
		REMOTE_VIEWS
	}

	public enum DailyNotiAttributes {
		ALARM_CLOCK
	}

	public enum OngoingNotiAttributes {
		DATA_TYPE_OF_ICON
	}

	public enum DataTypeOfIcon {
		TEMPERATURE, WEATHER_ICON
	}

}
