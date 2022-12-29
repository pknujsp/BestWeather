package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html;

public class KmaCurrentConditions
{
    private String baseDateTime;
    private String temp;
    private String yesterdayTemp;
    private String feelsLikeTemp;
    private String humidity;
    private String windDirection;
    private String windSpeed;
    private String precipitationVolume;
    private String pty;

    public String getPty() {
        return pty;
    }

    public KmaCurrentConditions setPty(String pty) {
        this.pty = pty;
        return this;
    }

    public String getYesterdayTemp() {
        return yesterdayTemp;
    }

    public KmaCurrentConditions setYesterdayTemp(String yesterdayTemp) {
        this.yesterdayTemp = yesterdayTemp;
        return this;
    }

    public String getBaseDateTime() {
        return baseDateTime;
    }

    public KmaCurrentConditions setBaseDateTime(String baseDateTime) {
        this.baseDateTime = baseDateTime;
        return this;
    }

    public String getTemp()
    {
        return temp;
    }

    public KmaCurrentConditions setTemp(String temp)
    {
        this.temp = temp;
        return this;
    }

    public String getFeelsLikeTemp()
    {
        return feelsLikeTemp;
    }

    public KmaCurrentConditions setFeelsLikeTemp(String feelsLikeTemp)
    {
        this.feelsLikeTemp = feelsLikeTemp;
        return this;
    }

    public String getHumidity()
    {
        return humidity;
    }

    public KmaCurrentConditions setHumidity(String humidity)
    {
        this.humidity = humidity;
        return this;
    }

    public String getWindDirection()
    {
        return windDirection;
    }

    public KmaCurrentConditions setWindDirection(String windDirection)
    {
        this.windDirection = windDirection;
        return this;
    }

    public String getWindSpeed()
    {
        return windSpeed;
    }

    public KmaCurrentConditions setWindSpeed(String windSpeed)
    {
        this.windSpeed = windSpeed;
        return this;
    }

    public String getPrecipitationVolume()
    {
        return precipitationVolume;
    }

    public KmaCurrentConditions setPrecipitationVolume(String precipitationVolume)
    {
        this.precipitationVolume = precipitationVolume;
        return this;
    }
}
