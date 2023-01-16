package com.lifedawn.bestweather.commons.classes

import java.time.ZonedDateTime

class ForecastObj<T>(val dateTime: ZonedDateTime, val e: T)