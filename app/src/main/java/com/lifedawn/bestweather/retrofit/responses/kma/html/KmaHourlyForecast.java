package com.lifedawn.bestweather.retrofit.responses.kma.html;

import java.time.ZonedDateTime;

public class KmaHourlyForecast {
    private ZonedDateTime hour;
    private boolean hasShower;
    private String weatherDescription;
    private String temp;
    private String feelsLikeTemp;
    private String rainVolume;
    private String snowVolume;
    private String pop;
    private String windDirection;
    private String windSpeed;
    private String humidity;
    private boolean hasRain;
    private boolean hasSnow;

    public String getSnowVolume() {
        return snowVolume;
    }

    public KmaHourlyForecast setSnowVolume(String snowVolume) {
        this.snowVolume = snowVolume;
        return this;
    }

    public boolean isHasRain() {
        return hasRain;
    }

    public KmaHourlyForecast setHasRain(boolean hasRain) {
        this.hasRain = hasRain;
        return this;
    }

    public boolean isHasSnow() {
        return hasSnow;
    }

    public KmaHourlyForecast setHasSnow(boolean hasSnow) {
        this.hasSnow = hasSnow;
        return this;
    }

    public ZonedDateTime getHour() {
        return hour;
    }

    public KmaHourlyForecast setHour(ZonedDateTime hour) {
        this.hour = hour;
        return this;
    }

    public boolean isHasShower() {
        return hasShower;
    }

    public KmaHourlyForecast setHasShower(boolean hasShower) {
        this.hasShower = hasShower;
        return this;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public KmaHourlyForecast setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
        return this;
    }

    public String getTemp() {
        return temp;
    }

    public KmaHourlyForecast setTemp(String temp) {
        this.temp = temp;
        return this;
    }

    public String getFeelsLikeTemp() {
        return feelsLikeTemp;
    }

    public KmaHourlyForecast setFeelsLikeTemp(String feelsLikeTemp) {
        this.feelsLikeTemp = feelsLikeTemp;
        return this;
    }

    public String getRainVolume() {
        return rainVolume;
    }

    public KmaHourlyForecast setRainVolume(String rainVolume) {
        this.rainVolume = rainVolume;
        return this;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public KmaHourlyForecast setWindDirection(String windDirection) {
        this.windDirection = windDirection;
        return this;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public KmaHourlyForecast setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public String getHumidity() {
        return humidity;
    }

    public KmaHourlyForecast setHumidity(String humidity) {
        this.humidity = humidity;
        return this;
    }

    public String getPop() {
        return pop;
    }

    public KmaHourlyForecast setPop(String pop) {
        this.pop = pop;
        return this;
    }
}
