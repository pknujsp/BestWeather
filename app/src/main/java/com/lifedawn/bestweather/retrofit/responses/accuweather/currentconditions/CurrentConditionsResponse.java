package com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Direction;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Maximum;
import com.lifedawn.bestweather.retrofit.responses.accuweather.Minimum;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValueUnit;

public class CurrentConditionsResponse {

	public static class Item {
		@Expose
		@SerializedName("LocalObservationDateTime")
		private String localObservationDateTime;

		@Expose
		@SerializedName("EpochTime")
		private String epochTime;

		@Expose
		@SerializedName("WeatherText")
		private String weatherText;

		@Expose
		@SerializedName("WeatherIcon")
		private String weatherIcon;

		@Expose
		@SerializedName("HasPrecipitation")
		private String hasPrecipitation;

		@Expose
		@SerializedName("PrecipitationType")
		private String precipitationType;

		@Expose
		@SerializedName("IsDayTime")
		private String isDayTime;

		@Expose
		@SerializedName("RelativeHumidity")
		private String relativeHumidity;

		@Expose
		@SerializedName("IndoorRelativeHumidity")
		private String indoorRelativeHumidity;

		@Expose
		@SerializedName("UVIndex")
		private String uVIndex;

		@Expose
		@SerializedName("UVIndexText")
		private String uVIndexText;

		@Expose
		@SerializedName("ObstructionsToVisibility")
		private String obstructionsToVisibility;

		@Expose
		@SerializedName("CloudCover")
		private String cloudCover;

		@Expose
		@SerializedName("Temperature")
		private ValueUnit temperature;

		@Expose
		@SerializedName("RealFeelTemperature")
		private ValueUnit realFeelTemperature;


		@Expose
		@SerializedName("RealFeelTemperatureShade")
		private ValueUnit realFeelTemperatureShade;


		@Expose
		@SerializedName("DewPoint")
		private ValueUnit dewPoint;


		@Expose
		@SerializedName("Wind")
		private Wind wind;


		@Expose
		@SerializedName("WindGust")
		private WindGust windGust;


		@Expose
		@SerializedName("Visibility")
		private Visibility visibility;


		@Expose
		@SerializedName("Ceiling")
		private Ceiling ceiling;


		@Expose
		@SerializedName("Pressure")
		private Pressure pressure;


		@Expose
		@SerializedName("PressureTendency")
		private PressureTendency pressureTendency;


		@Expose
		@SerializedName("Past24HourTemperatureDeparture")
		private Past24HourTemperatureDeparture past24HourTemperatureDeparture;


		@Expose
		@SerializedName("ApparentTemperature")
		private ApparentTemperature apparentTemperature;


		@Expose
		@SerializedName("WindChillTemperature")
		private WindChillTemperature windChillTemperature;


		@Expose
		@SerializedName("WetBulbTemperature")
		private WetBulbTemperature wetBulbTemperature;


		@Expose
		@SerializedName("Precip1hr")
		private Precip1hr precip1hr;


		@Expose
		@SerializedName("PrecipitationSummary")
		private PrecipitationSummary precipitationSummary;


		@Expose
		@SerializedName("TemperatureSummary")
		private TemperatureSummary temperatureSummary;


		public static class Wind {
			@Expose
			@SerializedName("Direction")
			private Direction direction;

			@Expose
			@SerializedName("Speed")
			private ValueUnit valueUnit;

			public Direction getDirection() {
				return direction;
			}

			public void setDirection(Direction direction) {
				this.direction = direction;
			}

			public ValueUnit getValueUnit() {
				return valueUnit;
			}

			public void setValueUnit(ValueUnit valueUnit) {
				this.valueUnit = valueUnit;
			}
		}

		public static class WindGust {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class Visibility {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class Ceiling {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class Pressure {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class PressureTendency {
			@Expose
			@SerializedName("LocalizedText")
			private String localizedText;

			@Expose
			@SerializedName("Code")
			private String code;

			public String getLocalizedText() {
				return localizedText;
			}

			public void setLocalizedText(String localizedText) {
				this.localizedText = localizedText;
			}

			public String getCode() {
				return code;
			}

			public void setCode(String code) {
				this.code = code;
			}
		}

		public static class Past24HourTemperatureDeparture {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class ApparentTemperature {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class WindChillTemperature {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class WetBulbTemperature {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class Precip1hr {
			@Expose
			@SerializedName("Metric")
			private ValueUnit metric;

			@Expose
			@SerializedName("Imperial")
			private ValueUnit imperial;

			public ValueUnit getMetric() {
				return metric;
			}

			public void setMetric(ValueUnit metric) {
				this.metric = metric;
			}

			public ValueUnit getImperial() {
				return imperial;
			}

			public void setImperial(ValueUnit imperial) {
				this.imperial = imperial;
			}
		}

		public static class PrecipitationSummary {

			@Expose
			@SerializedName("Precipitation")
			private Precipitation precipitation;

			@Expose
			@SerializedName("PastHour")
			private PastHour pastHour;

			@Expose
			@SerializedName("Past3Hours")
			private Past3Hours past3Hours;

			@Expose
			@SerializedName("Past6Hours")
			private Past6Hours past6Hours;

			@Expose
			@SerializedName("Past9Hours")
			private Past9Hours past9Hours;

			@Expose
			@SerializedName("Past12Hours")
			private Past12Hours past12Hours;

			@Expose
			@SerializedName("Past18Hours")
			private Past18Hours past18Hours;

			@Expose
			@SerializedName("Past24Hours")
			private Past24Hours past24Hours;

			public Precipitation getPrecipitation() {
				return precipitation;
			}

			public void setPrecipitation(Precipitation precipitation) {
				this.precipitation = precipitation;
			}

			public PastHour getPastHour() {
				return pastHour;
			}

			public void setPastHour(PastHour pastHour) {
				this.pastHour = pastHour;
			}

			public Past3Hours getPast3Hours() {
				return past3Hours;
			}

			public void setPast3Hours(Past3Hours past3Hours) {
				this.past3Hours = past3Hours;
			}

			public Past6Hours getPast6Hours() {
				return past6Hours;
			}

			public void setPast6Hours(Past6Hours past6Hours) {
				this.past6Hours = past6Hours;
			}

			public Past9Hours getPast9Hours() {
				return past9Hours;
			}

			public void setPast9Hours(Past9Hours past9Hours) {
				this.past9Hours = past9Hours;
			}

			public Past12Hours getPast12Hours() {
				return past12Hours;
			}

			public void setPast12Hours(Past12Hours past12Hours) {
				this.past12Hours = past12Hours;
			}

			public Past18Hours getPast18Hours() {
				return past18Hours;
			}

			public void setPast18Hours(Past18Hours past18Hours) {
				this.past18Hours = past18Hours;
			}

			public Past24Hours getPast24Hours() {
				return past24Hours;
			}

			public void setPast24Hours(Past24Hours past24Hours) {
				this.past24Hours = past24Hours;
			}

			public static class Precipitation {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class PastHour {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class Past3Hours {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class Past6Hours {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class Past9Hours {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class Past12Hours {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class Past18Hours {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}

			public static class Past24Hours {
				@Expose
				@SerializedName("Metric")
				private ValueUnit metric;

				@Expose
				@SerializedName("Imperial")
				private ValueUnit imperial;

				public ValueUnit getMetric() {
					return metric;
				}

				public void setMetric(ValueUnit metric) {
					this.metric = metric;
				}

				public ValueUnit getImperial() {
					return imperial;
				}

				public void setImperial(ValueUnit imperial) {
					this.imperial = imperial;
				}
			}
		}

		public static class TemperatureSummary {

			@Expose
			@SerializedName("Past6HourRange")
			private Past6HourRange past6HourRange;

			@Expose
			@SerializedName("Past12HourRange")
			private Past12HourRange past12HourRange;

			@Expose
			@SerializedName("Past24HourRange")
			private Past24HourRange past24HourRange;

			public Past6HourRange getPast6HourRange() {
				return past6HourRange;
			}

			public void setPast6HourRange(Past6HourRange past6HourRange) {
				this.past6HourRange = past6HourRange;
			}

			public Past12HourRange getPast12HourRange() {
				return past12HourRange;
			}

			public void setPast12HourRange(Past12HourRange past12HourRange) {
				this.past12HourRange = past12HourRange;
			}

			public Past24HourRange getPast24HourRange() {
				return past24HourRange;
			}

			public void setPast24HourRange(Past24HourRange past24HourRange) {
				this.past24HourRange = past24HourRange;
			}

			public static class Past6HourRange {
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

			public static class Past12HourRange {
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


			public static class Past24HourRange {
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

		}
	}


}
