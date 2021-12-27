package com.lifedawn.bestweather.retrofit.responses.kma.html;

import java.time.ZonedDateTime;

public class KmaDailyForecast
{
    private ZonedDateTime date;
    private boolean single;
    private Values amValues;
    private Values pmValues;
    private Values singleValues;
    private String minTemp;
    private String maxTemp;

    public static class Values
    {
        private String weatherDescription;
        private String pop;

        public String getWeatherDescription()
        {
            return weatherDescription;
        }

        public Values setWeatherDescription(String weatherDescription)
        {
            this.weatherDescription = weatherDescription;
            return this;
        }

        public String getPop()
        {
            return pop;
        }

        public Values setPop(String pop)
        {
            this.pop = pop;
            return this;
        }
    }

    public ZonedDateTime getDate()
    {
        return date;
    }

    public KmaDailyForecast setDate(ZonedDateTime date)
    {
        this.date = date;
        return this;
    }

    public boolean isSingle()
    {
        return single;
    }

    public KmaDailyForecast setSingle(boolean single)
    {
        this.single = single;
        return this;
    }

    public Values getAmValues()
    {
        return amValues;
    }

    public KmaDailyForecast setAmValues(Values amValues)
    {
        this.amValues = amValues;
        return this;
    }

    public Values getPmValues()
    {
        return pmValues;
    }

    public KmaDailyForecast setPmValues(Values pmValues)
    {
        this.pmValues = pmValues;
        return this;
    }

    public Values getSingleValues()
    {
        return singleValues;
    }

    public KmaDailyForecast setSingleValues(Values singleValues)
    {
        this.singleValues = singleValues;
        return this;
    }

    public String getMinTemp()
    {
        return minTemp;
    }

    public KmaDailyForecast setMinTemp(String minTemp)
    {
        this.minTemp = minTemp;
        return this;
    }

    public String getMaxTemp()
    {
        return maxTemp;
    }

    public KmaDailyForecast setMaxTemp(String maxTemp)
    {
        this.maxTemp = maxTemp;
        return this;
    }
}
