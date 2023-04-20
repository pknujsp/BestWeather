package com.lifedawn.bestweather.commons.constants

class WidgetNotiConstants {
    class Commons {
        enum class Attributes {
            LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA, UPDATE_INTERVAL, SELECTED_ADDRESS_DTO_ID
        }

        enum class DataKeys {
            ADDRESS_NAME, LATITUDE, LONGITUDE, COUNTRY_CODE, ZONE_ID
        }
    }

    enum class WidgetAttributes {
        REMOTE_VIEWS
    }

    enum class DailyNotiAttributes {
        ALARM_CLOCK
    }

    enum class OngoingNotiAttributes {
        DATA_TYPE_OF_ICON
    }

    enum class DataTypeOfIcon {
        TEMPERATURE, WEATHER_ICON
    }
}