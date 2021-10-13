package com.lifedawn.bestweather.retrofit.responses.aqicn;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeolocalizedFeedResponse {
	@Expose
	@SerializedName("status")
	private String status;
	
	@Expose
	@SerializedName("data")
	private Data data;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Data getData() {
		return data;
	}
	
	public void setData(Data data) {
		this.data = data;
	}
	
	public static class Data {
		@Expose
		@SerializedName("aqi")
		private String aqi;
		
		@Expose
		@SerializedName("idx")
		private String idx;
		
		@Expose
		@SerializedName("dominentpol")
		private String dominentPol;
		
		@Expose
		@SerializedName("city")
		private City city;
		
		@Expose
		@SerializedName("iaqi")
		private IAqi iaqi;
		
		@Expose
		@SerializedName("time")
		private Time time;
		
		@Expose
		@SerializedName("forecast")
		private Forecast forecast;
		
		public City getCity() {
			return city;
		}
		
		public Data setCity(City city) {
			this.city = city;
			return this;
		}
		
		public IAqi getIaqi() {
			return iaqi;
		}
		
		public Data setIaqi(IAqi iaqi) {
			this.iaqi = iaqi;
			return this;
		}
		
		public Time getTime() {
			return time;
		}
		
		public Data setTime(Time time) {
			this.time = time;
			return this;
		}
		
		public Forecast getForecast() {
			return forecast;
		}
		
		public Data setForecast(Forecast forecast) {
			this.forecast = forecast;
			return this;
		}
		
		public String getAqi() {
			return aqi;
		}
		
		public Data setAqi(String aqi) {
			this.aqi = aqi;
			return this;
		}
		
		public String getIdx() {
			return idx;
		}
		
		public Data setIdx(String idx) {
			this.idx = idx;
			return this;
		}
		
		public String getDominentPol() {
			return dominentPol;
		}
		
		public Data setDominentPol(String dominentPol) {
			this.dominentPol = dominentPol;
			return this;
		}
		
		public static class City {
			@Expose
			@SerializedName("geo")
			private List<String> geo;
			
			@Expose
			@SerializedName("name")
			private String name;
			
			@Expose
			@SerializedName("url")
			private String url;
			
			public List<String> getGeo() {
				return geo;
			}
			
			public void setGeo(List<String> geo) {
				this.geo = geo;
			}
			
			public String getName() {
				return name;
			}
			
			public void setName(String name) {
				this.name = name;
			}
			
			public String getUrl() {
				return url;
			}
			
			public void setUrl(String url) {
				this.url = url;
			}
		}
		
		public static class IAqi {
			@Expose
			@SerializedName("co")
			private ValueMap co;
			
			@Expose
			@SerializedName("dew")
			private ValueMap dew;
			
			@Expose
			@SerializedName("no2")
			private ValueMap no2;
			
			@Expose
			@SerializedName("o3")
			private ValueMap o3;
			
			@Expose
			@SerializedName("pm10")
			private ValueMap pm10;
			
			@Expose
			@SerializedName("pm25")
			private ValueMap pm25;
			
			@Expose
			@SerializedName("so2")
			private ValueMap so2;
			
			public ValueMap getCo() {
				return co;
			}
			
			public void setCo(ValueMap co) {
				this.co = co;
			}
			
			public ValueMap getDew() {
				return dew;
			}
			
			public void setDew(ValueMap dew) {
				this.dew = dew;
			}
			
			public ValueMap getNo2() {
				return no2;
			}
			
			public void setNo2(ValueMap no2) {
				this.no2 = no2;
			}
			
			public ValueMap getO3() {
				return o3;
			}
			
			public void setO3(ValueMap o3) {
				this.o3 = o3;
			}
			
			public ValueMap getPm10() {
				return pm10;
			}
			
			public void setPm10(ValueMap pm10) {
				this.pm10 = pm10;
			}
			
			public ValueMap getPm25() {
				return pm25;
			}
			
			public void setPm25(ValueMap pm25) {
				this.pm25 = pm25;
			}
			
			public ValueMap getSo2() {
				return so2;
			}
			
			public void setSo2(ValueMap so2) {
				this.so2 = so2;
			}
			
			public static class ValueMap {
				@Expose
				@SerializedName("v")
				private String value;
				
				public String getValue() {
					return value;
				}
				
				public void setValue(String value) {
					this.value = value;
				}
			}
		}
		
		public static class Time {
			@Expose
			@SerializedName("s")
			private String s;
			
			@Expose
			@SerializedName("tz")
			private String tz;
			
			@Expose
			@SerializedName("v")
			private String v;
			
			@Expose
			@SerializedName("iso")
			private String iso;
			
			public String getS() {
				return s;
			}
			
			public void setS(String s) {
				this.s = s;
			}
			
			public String getTz() {
				return tz;
			}
			
			public void setTz(String tz) {
				this.tz = tz;
			}
			
			public String getV() {
				return v;
			}
			
			public void setV(String v) {
				this.v = v;
			}
			
			public String getIso() {
				return iso;
			}
			
			public void setIso(String iso) {
				this.iso = iso;
			}
		}
		
		public static class Forecast {
			@Expose
			@SerializedName("daily")
			private Daily daily;
			
			
			public Daily getDaily() {
				return daily;
			}
			
			public Forecast setDaily(Daily daily) {
				this.daily = daily;
				return this;
			}
			
			public static class Daily {
				@Expose
				@SerializedName("o3")
				private List<Daily.ValueMap> o3;
				
				@Expose
				@SerializedName("pm10")
				private List<Daily.ValueMap> pm10;
				
				@Expose
				@SerializedName("pm25")
				private List<Daily.ValueMap> pm25;
				
				@Expose
				@SerializedName("uvi")
				private List<Daily.ValueMap> uvi;
				
				public List<Daily.ValueMap> getO3() {
					return o3;
				}
				
				public Daily setO3(List<ValueMap> o3) {
					this.o3 = o3;
					return this;
				}
				
				public List<ValueMap> getPm10() {
					return pm10;
				}
				
				public Daily setPm10(List<ValueMap> pm10) {
					this.pm10 = pm10;
					return this;
				}
				
				public List<ValueMap> getPm25() {
					return pm25;
				}
				
				public Daily setPm25(List<ValueMap> pm25) {
					this.pm25 = pm25;
					return this;
				}
				
				public List<ValueMap> getUvi() {
					return uvi;
				}
				
				public Daily setUvi(List<ValueMap> uvi) {
					this.uvi = uvi;
					return this;
				}
				
				public static class ValueMap {
					@Expose
					@SerializedName("avg")
					private String avg;
					
					@Expose
					@SerializedName("day")
					private String day;
					
					@Expose
					@SerializedName("max")
					private String max;
					
					@Expose
					@SerializedName("min")
					private String min;
					
					public String getAvg() {
						return avg;
					}
					
					public void setAvg(String avg) {
						this.avg = avg;
					}
					
					public String getDay() {
						return day;
					}
					
					public void setDay(String day) {
						this.day = day;
					}
					
					public String getMax() {
						return max;
					}
					
					public void setMax(String max) {
						this.max = max;
					}
					
					public String getMin() {
						return min;
					}
					
					public void setMin(String min) {
						this.min = min;
					}
				}
			}
			
			
		}
	}
}
