package com.lifedawn.bestweather.commons.classes

import java.time.ZonedDateTime

data class ForecastObj<T>(val dateTime: ZonedDateTime, val e: T)