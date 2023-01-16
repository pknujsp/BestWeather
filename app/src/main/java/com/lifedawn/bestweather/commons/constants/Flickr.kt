package com.lifedawn.bestweather.commons.constants

class Flickr {
    enum class Time(val text: String) {
        sunrise("sunrise"), sunset("sunrise"), day("day"), night("night");

    }

    enum class Weather(val text: String) {
        clear("clear"), partlyCloudy("partly_cloudy"), mostlyCloudy("mostly_cloudy"), overcast("overcast"), rain("rain"), snow("snow");

    }
}