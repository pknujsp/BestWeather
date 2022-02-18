package com.lifedawn.bestweather.weathers.models;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class DailyForecastDto implements Serializable {
	private ZonedDateTime date;
	private boolean isSingle;

	private Values amValues;
	private Values pmValues;
	private Values singleValues;

	private String minTemp;
	private String maxTemp;
	private String minFeelsLikeTemp;
	private String maxFeelsLikeTemp;

	public final static class Values implements Serializable {
		private int weatherIcon;
		private String weatherDescription;

		private String pop;
		private String pos;
		private String por;

		private String precipitationVolume;
		private String rainVolume;
		private String snowVolume;

		private String windDirection;
		private int windDirectionVal;
		private String windSpeed;
		private String windStrength;
		private String windGust;
		private String pressure;
		private String humidity;
		private String dewPointTemp;
		private String cloudiness;
		private String visibility;
		private String uvIndex;
		private String precipitationType;
		private int precipitationTypeIcon;

		private boolean hasRainVolume;
		private boolean hasSnowVolume;
		private boolean hasPrecipitationVolume;

		public boolean isHasRainVolume() {
			return hasRainVolume;
		}

		public Values setHasRainVolume(boolean hasRainVolume) {
			this.hasRainVolume = hasRainVolume;
			return this;
		}

		public boolean isHasSnowVolume() {
			return hasSnowVolume;
		}

		public Values setHasSnowVolume(boolean hasSnowVolume) {
			this.hasSnowVolume = hasSnowVolume;
			return this;
		}

		public boolean isHasPrecipitationVolume() {
			return hasPrecipitationVolume;
		}

		public Values setHasPrecipitationVolume(boolean hasPrecipitationVolume) {
			this.hasPrecipitationVolume = hasPrecipitationVolume;
			return this;
		}

		public int getWeatherIcon() {
			return weatherIcon;
		}

		public Values setWeatherIcon(int weatherIcon) {
			this.weatherIcon = weatherIcon;
			return this;
		}

		public String getWeatherDescription() {
			return weatherDescription;
		}

		public Values setWeatherDescription(String weatherDescription) {
			this.weatherDescription = weatherDescription;
			return this;
		}

		public String getPop() {
			return pop;
		}

		public Values setPop(String pop) {
			this.pop = pop;
			return this;
		}

		public String getPos() {
			return pos;
		}

		public Values setPos(String pos) {
			this.pos = pos;
			return this;
		}

		public String getPor() {
			return por;
		}

		public Values setPor(String por) {
			this.por = por;
			return this;
		}

		public String getPrecipitationVolume() {
			return precipitationVolume;
		}

		public Values setPrecipitationVolume(String precipitationVolume) {
			this.precipitationVolume = precipitationVolume;
			return this;
		}

		public String getRainVolume() {
			return rainVolume;
		}

		public Values setRainVolume(String rainVolume) {
			this.rainVolume = rainVolume;
			return this;
		}

		public String getSnowVolume() {
			return snowVolume;
		}

		public Values setSnowVolume(String snowVolume) {
			this.snowVolume = snowVolume;
			return this;
		}

		public String getWindDirection() {
			return windDirection;
		}

		public Values setWindDirection(String windDirection) {
			this.windDirection = windDirection;
			return this;
		}

		public int getWindDirectionVal() {
			return windDirectionVal;
		}

		public Values setWindDirectionVal(int windDirectionVal) {
			this.windDirectionVal = windDirectionVal;
			return this;
		}

		public String getWindSpeed() {
			return windSpeed;
		}

		public Values setWindSpeed(String windSpeed) {
			this.windSpeed = windSpeed;
			return this;
		}

		public String getWindStrength() {
			return windStrength;
		}

		public Values setWindStrength(String windStrength) {
			this.windStrength = windStrength;
			return this;
		}

		public String getWindGust() {
			return windGust;
		}

		public Values setWindGust(String windGust) {
			this.windGust = windGust;
			return this;
		}

		public String getPressure() {
			return pressure;
		}

		public Values setPressure(String pressure) {
			this.pressure = pressure;
			return this;
		}

		public String getHumidity() {
			return humidity;
		}

		public Values setHumidity(String humidity) {
			this.humidity = humidity;
			return this;
		}

		public String getDewPointTemp() {
			return dewPointTemp;
		}

		public Values setDewPointTemp(String dewPointTemp) {
			this.dewPointTemp = dewPointTemp;
			return this;
		}

		public String getCloudiness() {
			return cloudiness;
		}

		public Values setCloudiness(String cloudiness) {
			this.cloudiness = cloudiness;
			return this;
		}

		public String getVisibility() {
			return visibility;
		}

		public Values setVisibility(String visibility) {
			this.visibility = visibility;
			return this;
		}

		public String getUvIndex() {
			return uvIndex;
		}

		public Values setUvIndex(String uvIndex) {
			this.uvIndex = uvIndex;
			return this;
		}

		public String getPrecipitationType() {
			return precipitationType;
		}

		public Values setPrecipitationType(String precipitationType) {
			this.precipitationType = precipitationType;
			return this;
		}

		public int getPrecipitationTypeIcon() {
			return precipitationTypeIcon;
		}

		public Values setPrecipitationTypeIcon(int precipitationTypeIcon) {
			this.precipitationTypeIcon = precipitationTypeIcon;
			return this;
		}
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public DailyForecastDto setDate(ZonedDateTime date) {
		this.date = date;
		return this;
	}

	public boolean isSingle() {
		return isSingle;
	}

	public DailyForecastDto setSingle(boolean single) {
		isSingle = single;
		return this;
	}

	public Values getAmValues() {
		return amValues;
	}

	public DailyForecastDto setAmValues(Values amValues) {
		this.amValues = amValues;
		return this;
	}

	public Values getPmValues() {
		return pmValues;
	}

	public DailyForecastDto setPmValues(Values pmValues) {
		this.pmValues = pmValues;
		return this;
	}

	public String getMinTemp() {
		return minTemp;
	}

	public DailyForecastDto setMinTemp(String minTemp) {
		this.minTemp = minTemp;
		return this;
	}

	public String getMaxTemp() {
		return maxTemp;
	}

	public DailyForecastDto setMaxTemp(String maxTemp) {
		this.maxTemp = maxTemp;
		return this;
	}

	public String getMinFeelsLikeTemp() {
		return minFeelsLikeTemp;
	}

	public DailyForecastDto setMinFeelsLikeTemp(String minFeelsLikeTemp) {
		this.minFeelsLikeTemp = minFeelsLikeTemp;
		return this;
	}

	public String getMaxFeelsLikeTemp() {
		return maxFeelsLikeTemp;
	}

	public DailyForecastDto setMaxFeelsLikeTemp(String maxFeelsLikeTemp) {
		this.maxFeelsLikeTemp = maxFeelsLikeTemp;
		return this;
	}

	public Values getSingleValues() {
		return singleValues;
	}

	public DailyForecastDto setSingleValues(Values singleValues) {
		this.singleValues = singleValues;
		return this;
	}
}
