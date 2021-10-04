package com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Direction;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Maximum;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Minimum;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValueUnit;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Wind;
import com.lifedawn.bestweather.retrofit.responses.accuweather.WindGust;

import java.util.List;

public class FiveDaysOfDailyForecastsResponse {
	@Expose
	@SerializedName("Headline")
	private Headline headline;

	@Expose
	@SerializedName("DailyForecasts")
	private List<DailyForecasts> dailyForecasts;

	public Headline getHeadline() {
		return headline;
	}

	public void setHeadline(Headline headline) {
		this.headline = headline;
	}

	public List<DailyForecasts> getDailyForecasts() {
		return dailyForecasts;
	}

	public void setDailyForecasts(List<DailyForecasts> dailyForecasts) {
		this.dailyForecasts = dailyForecasts;
	}

	public static class Headline {
		@Expose
		@SerializedName("EffectiveDate")
		private String effectiveDate;

		@Expose
		@SerializedName("EffectiveEpochDate")
		private String effectiveEpochDate;

		@Expose
		@SerializedName("Severity")
		private String severity;

		@Expose
		@SerializedName("Text")
		private String text;

		@Expose
		@SerializedName("Category")
		private String category;

		@Expose
		@SerializedName("EndDate")
		private String endDate;

		@Expose
		@SerializedName("EndEpochDate")
		private String endEpochDate;

		public String getEffectiveDate() {
			return effectiveDate;
		}

		public void setEffectiveDate(String effectiveDate) {
			this.effectiveDate = effectiveDate;
		}

		public String getEffectiveEpochDate() {
			return effectiveEpochDate;
		}

		public void setEffectiveEpochDate(String effectiveEpochDate) {
			this.effectiveEpochDate = effectiveEpochDate;
		}

		public String getSeverity() {
			return severity;
		}

		public void setSeverity(String severity) {
			this.severity = severity;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public String getEndEpochDate() {
			return endEpochDate;
		}

		public void setEndEpochDate(String endEpochDate) {
			this.endEpochDate = endEpochDate;
		}
	}

	public static class DailyForecasts {
		@Expose
		@SerializedName("Date")
		private String date;

		@Expose
		@SerializedName("EpochDate")
		private String epochDate;

		@Expose
		@SerializedName("EndEpochDate")
		private String endEpochDate;

		@Expose
		@SerializedName("Temperature")
		private Temperature temperature;

		@Expose
		@SerializedName("RealFeelTemperature")
		private RealFeelTemperature realFeelTemperature;

		@Expose
		@SerializedName("RealFeelTemperatureShade")
		private RealFeelTemperatureShade realFeelTemperatureShade;

		@Expose
		@SerializedName("Day")
		private DayOrNightValues day;

		@Expose
		@SerializedName("Night")
		private DayOrNightValues night;

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getEpochDate() {
			return epochDate;
		}

		public void setEpochDate(String epochDate) {
			this.epochDate = epochDate;
		}

		public String getEndEpochDate() {
			return endEpochDate;
		}

		public void setEndEpochDate(String endEpochDate) {
			this.endEpochDate = endEpochDate;
		}

		public Temperature getTemperature() {
			return temperature;
		}

		public void setTemperature(Temperature temperature) {
			this.temperature = temperature;
		}

		public RealFeelTemperature getRealFeelTemperature() {
			return realFeelTemperature;
		}

		public void setRealFeelTemperature(RealFeelTemperature realFeelTemperature) {
			this.realFeelTemperature = realFeelTemperature;
		}

		public RealFeelTemperatureShade getRealFeelTemperatureShade() {
			return realFeelTemperatureShade;
		}

		public void setRealFeelTemperatureShade(RealFeelTemperatureShade realFeelTemperatureShade) {
			this.realFeelTemperatureShade = realFeelTemperatureShade;
		}

		public DayOrNightValues getDay() {
			return day;
		}

		public void setDay(DayOrNightValues day) {
			this.day = day;
		}

		public DayOrNightValues getNight() {
			return night;
		}

		public void setNight(DayOrNightValues night) {
			this.night = night;
		}

		public static class Temperature {
			@Expose
			@SerializedName("Minimum")
			private Minimum minimum;

			@Expose
			@SerializedName("Maximum")
			private Maximum maximum;

			public Minimum getMinimum() {
				return minimum;
			}

			public void setMinimum(Minimum minimum) {
				this.minimum = minimum;
			}

			public Maximum getMaximum() {
				return maximum;
			}

			public void setMaximum(Maximum maximum) {
				this.maximum = maximum;
			}
		}

		public static class RealFeelTemperature {
			@Expose
			@SerializedName("Minimum")
			private Minimum minimum;

			@Expose
			@SerializedName("Maximum")
			private Maximum maximum;

			public Minimum getMinimum() {
				return minimum;
			}

			public void setMinimum(Minimum minimum) {
				this.minimum = minimum;
			}

			public Maximum getMaximum() {
				return maximum;
			}

			public void setMaximum(Maximum maximum) {
				this.maximum = maximum;
			}
		}

		public static class RealFeelTemperatureShade {
			@Expose
			@SerializedName("Minimum")
			private Minimum minimum;

			@Expose
			@SerializedName("Maximum")
			private Maximum maximum;

			public Minimum getMinimum() {
				return minimum;
			}

			public void setMinimum(Minimum minimum) {
				this.minimum = minimum;
			}

			public Maximum getMaximum() {
				return maximum;
			}

			public void setMaximum(Maximum maximum) {
				this.maximum = maximum;
			}
		}

		public static class DayOrNightValues {
			@Expose
			@SerializedName("Icon")
			private String icon;

			@Expose
			@SerializedName("IconPhrase")
			private String iconPhrase;

			@Expose
			@SerializedName("HasPrecipitation")
			private String hasPrecipitation;

			@Expose
			@SerializedName("PrecipitationProbability")
			private String precipitationProbability;

			@Expose
			@SerializedName("ThunderstormProbability")
			private String thunderstormProbability;

			@Expose
			@SerializedName("RainProbability")
			private String rainProbability;

			@Expose
			@SerializedName("SnowProbability")
			private String snowProbability;

			@Expose
			@SerializedName("IceProbability")
			private String iceProbability;

			@Expose
			@SerializedName("HoursOfPrecipitation")
			private String hoursOfPrecipitation;

			@Expose
			@SerializedName("HoursOfRain")
			private String hoursOfRain;

			@Expose
			@SerializedName("HoursOfSnow")
			private String hoursOfSnow;

			@Expose
			@SerializedName("HoursOfIce")
			private String hoursOfIce;

			@Expose
			@SerializedName("CloudCover")
			private String cloudCover;

			@Expose
			@SerializedName("Wind")
			private Wind wind;

			@Expose
			@SerializedName("WindGust")
			private WindGust windGust;

			@Expose
			@SerializedName("TotalLiquid")
			private ValueUnit totalLiquid;

			@Expose
			@SerializedName("Rain")
			private ValueUnit rain;

			@Expose
			@SerializedName("Snow")
			private ValueUnit snow;

			@Expose
			@SerializedName("Ice")
			private ValueUnit ice;

			public String getIcon() {
				return icon;
			}

			public void setIcon(String icon) {
				this.icon = icon;
			}

			public String getIconPhrase() {
				return iconPhrase;
			}

			public void setIconPhrase(String iconPhrase) {
				this.iconPhrase = iconPhrase;
			}

			public String getHasPrecipitation() {
				return hasPrecipitation;
			}

			public void setHasPrecipitation(String hasPrecipitation) {
				this.hasPrecipitation = hasPrecipitation;
			}

			public String getPrecipitationProbability() {
				return precipitationProbability;
			}

			public void setPrecipitationProbability(String precipitationProbability) {
				this.precipitationProbability = precipitationProbability;
			}

			public String getThunderstormProbability() {
				return thunderstormProbability;
			}

			public void setThunderstormProbability(String thunderstormProbability) {
				this.thunderstormProbability = thunderstormProbability;
			}

			public String getRainProbability() {
				return rainProbability;
			}

			public void setRainProbability(String rainProbability) {
				this.rainProbability = rainProbability;
			}

			public String getSnowProbability() {
				return snowProbability;
			}

			public void setSnowProbability(String snowProbability) {
				this.snowProbability = snowProbability;
			}

			public String getIceProbability() {
				return iceProbability;
			}

			public void setIceProbability(String iceProbability) {
				this.iceProbability = iceProbability;
			}

			public String getHoursOfPrecipitation() {
				return hoursOfPrecipitation;
			}

			public void setHoursOfPrecipitation(String hoursOfPrecipitation) {
				this.hoursOfPrecipitation = hoursOfPrecipitation;
			}

			public String getHoursOfRain() {
				return hoursOfRain;
			}

			public void setHoursOfRain(String hoursOfRain) {
				this.hoursOfRain = hoursOfRain;
			}

			public String getHoursOfSnow() {
				return hoursOfSnow;
			}

			public void setHoursOfSnow(String hoursOfSnow) {
				this.hoursOfSnow = hoursOfSnow;
			}

			public String getHoursOfIce() {
				return hoursOfIce;
			}

			public void setHoursOfIce(String hoursOfIce) {
				this.hoursOfIce = hoursOfIce;
			}

			public String getCloudCover() {
				return cloudCover;
			}

			public void setCloudCover(String cloudCover) {
				this.cloudCover = cloudCover;
			}

			public Wind getWind() {
				return wind;
			}

			public void setWind(Wind wind) {
				this.wind = wind;
			}

			public WindGust getWindGust() {
				return windGust;
			}

			public void setWindGust(WindGust windGust) {
				this.windGust = windGust;
			}

			public ValueUnit getTotalLiquid() {
				return totalLiquid;
			}

			public void setTotalLiquid(ValueUnit totalLiquid) {
				this.totalLiquid = totalLiquid;
			}

			public ValueUnit getRain() {
				return rain;
			}

			public void setRain(ValueUnit rain) {
				this.rain = rain;
			}

			public ValueUnit getSnow() {
				return snow;
			}

			public void setSnow(ValueUnit snow) {
				this.snow = snow;
			}

			public ValueUnit getIce() {
				return ice;
			}

			public void setIce(ValueUnit ice) {
				this.ice = ice;
			}


		}
	}
}
