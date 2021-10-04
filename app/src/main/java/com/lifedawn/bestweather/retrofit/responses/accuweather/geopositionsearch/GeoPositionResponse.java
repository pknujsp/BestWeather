package com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.SkipCallbackExecutor;

public class GeoPositionResponse {
	@Expose
	@SerializedName("Version")
	private String version;

	@Expose
	@SerializedName("Key")
	private String key;

	@Expose
	@SerializedName("Type")
	private String type;

	@Expose
	@SerializedName("Rank")
	private String rank;

	@Expose
	@SerializedName("LocalizedName")
	private String localizedName;

	@Expose
	@SerializedName("EnglishName")
	private String englishName;

	@Expose
	@SerializedName("PrimaryPostalCode")
	private String primaryPostalCode;

	@Expose
	@SerializedName("Region")
	private Region region;

	@Expose
	@SerializedName("Country")
	private Country country;

	@Expose
	@SerializedName("AdministrativeArea")
	private AdministrativeArea administrativeArea;

	@Expose
	@SerializedName("TimeZone")
	private TimeZone timeZone;

	@Expose
	@SerializedName("GeoPosition")
	private GeoPosition geoPosition;

	@Expose
	@SerializedName("ParentCity")
	private ParentCity parentCity;

	public static class Region {
		@Expose
		@SerializedName("ID")
		private String id;

		@Expose
		@SerializedName("LocalizedName")
		private String localizedName;

		@Expose
		@SerializedName("EnglishName")
		private String englishName;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getLocalizedName() {
			return localizedName;
		}

		public void setLocalizedName(String localizedName) {
			this.localizedName = localizedName;
		}

		public String getEnglishName() {
			return englishName;
		}

		public void setEnglishName(String englishName) {
			this.englishName = englishName;
		}
	}

	public static class Country {
		@Expose
		@SerializedName("ID")
		private String id;

		@Expose
		@SerializedName("LocalizedName")
		private String localizedName;

		@Expose
		@SerializedName("EnglishName")
		private String englishName;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getLocalizedName() {
			return localizedName;
		}

		public void setLocalizedName(String localizedName) {
			this.localizedName = localizedName;
		}

		public String getEnglishName() {
			return englishName;
		}

		public void setEnglishName(String englishName) {
			this.englishName = englishName;
		}
	}

	public static class AdministrativeArea {
		@Expose
		@SerializedName("ID")
		private String id;

		@Expose
		@SerializedName("LocalizedName")
		private String localizedName;

		@Expose
		@SerializedName("EnglishName")
		private String englishName;

		@Expose
		@SerializedName("Level")
		private String level;

		@Expose
		@SerializedName("LocalizedType")
		private String localizedType;

		@Expose
		@SerializedName("EnglishType")
		private String englishType;

		@Expose
		@SerializedName("CountryID")
		private String countryID;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getLocalizedName() {
			return localizedName;
		}

		public void setLocalizedName(String localizedName) {
			this.localizedName = localizedName;
		}

		public String getEnglishName() {
			return englishName;
		}

		public void setEnglishName(String englishName) {
			this.englishName = englishName;
		}

		public String getLevel() {
			return level;
		}

		public void setLevel(String level) {
			this.level = level;
		}

		public String getLocalizedType() {
			return localizedType;
		}

		public void setLocalizedType(String localizedType) {
			this.localizedType = localizedType;
		}

		public String getEnglishType() {
			return englishType;
		}

		public void setEnglishType(String englishType) {
			this.englishType = englishType;
		}

		public String getCountryID() {
			return countryID;
		}

		public void setCountryID(String countryID) {
			this.countryID = countryID;
		}
	}

	public static class TimeZone {
		@Expose
		@SerializedName("Code")
		private String code;

		@Expose
		@SerializedName("Name")
		private String name;

		@Expose
		@SerializedName("GmtOffset")
		private String gmtOffset;

		@Expose
		@SerializedName("IsDaylightSaving")
		private String isDaylightSaving;

		@Expose
		@SerializedName("NextOffsetChange")
		private String nextOffsetChange;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getGmtOffset() {
			return gmtOffset;
		}

		public void setGmtOffset(String gmtOffset) {
			this.gmtOffset = gmtOffset;
		}

		public String getIsDaylightSaving() {
			return isDaylightSaving;
		}

		public void setIsDaylightSaving(String isDaylightSaving) {
			this.isDaylightSaving = isDaylightSaving;
		}

		public String getNextOffsetChange() {
			return nextOffsetChange;
		}

		public void setNextOffsetChange(String nextOffsetChange) {
			this.nextOffsetChange = nextOffsetChange;
		}
	}

	public static class GeoPosition {
		@Expose
		@SerializedName("Latitude")
		private String latitude;

		@Expose
		@SerializedName("Longitude")
		private String longitude;

		@Expose
		@SerializedName("Elevation")
		private Elevation elevation;

		public static class Elevation {
			@Expose
			@SerializedName("Metric")
			private Metric metric;

			@Expose
			@SerializedName("Imperial")
			private Imperial imperial;

			public static class Metric {
				@Expose
				@SerializedName("Value")
				private String value;

				@Expose
				@SerializedName("Unit")
				private String unit;

				@Expose
				@SerializedName("UnitType")
				private String unitType;

				public String getValue() {
					return value;
				}

				public void setValue(String value) {
					this.value = value;
				}

				public String getUnit() {
					return unit;
				}

				public void setUnit(String unit) {
					this.unit = unit;
				}

				public String getUnitType() {
					return unitType;
				}

				public void setUnitType(String unitType) {
					this.unitType = unitType;
				}
			}

			public static class Imperial {
				@Expose
				@SerializedName("Value")
				private String value;

				@Expose
				@SerializedName("Unit")
				private String unit;

				@Expose
				@SerializedName("UnitType")
				private String unitType;

				public String getValue() {
					return value;
				}

				public void setValue(String value) {
					this.value = value;
				}

				public String getUnit() {
					return unit;
				}

				public void setUnit(String unit) {
					this.unit = unit;
				}

				public String getUnitType() {
					return unitType;
				}

				public void setUnitType(String unitType) {
					this.unitType = unitType;
				}
			}

			public Metric getMetric() {
				return metric;
			}

			public void setMetric(Metric metric) {
				this.metric = metric;
			}

			public Imperial getImperial() {
				return imperial;
			}

			public void setImperial(Imperial imperial) {
				this.imperial = imperial;
			}
		}

		public String getLatitude() {
			return latitude;
		}

		public void setLatitude(String latitude) {
			this.latitude = latitude;
		}

		public String getLongitude() {
			return longitude;
		}

		public void setLongitude(String longitude) {
			this.longitude = longitude;
		}

		public Elevation getElevation() {
			return elevation;
		}

		public void setElevation(Elevation elevation) {
			this.elevation = elevation;
		}
	}

	@Expose
	@SerializedName("IsAlias")
	private String isAlias;


	public static class ParentCity {
		@Expose
		@SerializedName("Key")
		private String key;

		@Expose
		@SerializedName("LocalizedName")
		private String localizedName;

		@Expose
		@SerializedName("EnglishName")
		private String englishName;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getLocalizedName() {
			return localizedName;
		}

		public void setLocalizedName(String localizedName) {
			this.localizedName = localizedName;
		}

		public String getEnglishName() {
			return englishName;
		}

		public void setEnglishName(String englishName) {
			this.englishName = englishName;
		}
	}

	@Expose
	@SerializedName("SupplementalAdminAreas")
	private List<SupplementalAdminAreas> supplementalAdminAreas;

	public static class SupplementalAdminAreas {
		@Expose
		@SerializedName("Level")
		private String level;

		@Expose
		@SerializedName("LocalizedName")
		private String localizedName;

		@Expose
		@SerializedName("EnglishName")
		private String englishName;

		public String getLevel() {
			return level;
		}

		public void setLevel(String level) {
			this.level = level;
		}

		public String getLocalizedName() {
			return localizedName;
		}

		public void setLocalizedName(String localizedName) {
			this.localizedName = localizedName;
		}

		public String getEnglishName() {
			return englishName;
		}

		public void setEnglishName(String englishName) {
			this.englishName = englishName;
		}
	}

	@Expose
	@SerializedName("DataSets")
	private List<String> dataSets;


	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getLocalizedName() {
		return localizedName;
	}

	public void setLocalizedName(String localizedName) {
		this.localizedName = localizedName;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public String getPrimaryPostalCode() {
		return primaryPostalCode;
	}

	public void setPrimaryPostalCode(String primaryPostalCode) {
		this.primaryPostalCode = primaryPostalCode;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public AdministrativeArea getAdministrativeArea() {
		return administrativeArea;
	}

	public void setAdministrativeArea(AdministrativeArea administrativeArea) {
		this.administrativeArea = administrativeArea;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public GeoPosition getGeoPosition() {
		return geoPosition;
	}

	public void setGeoPosition(GeoPosition geoPosition) {
		this.geoPosition = geoPosition;
	}

	public ParentCity getParentCity() {
		return parentCity;
	}

	public void setParentCity(ParentCity parentCity) {
		this.parentCity = parentCity;
	}

	public String getIsAlias() {
		return isAlias;
	}

	public void setIsAlias(String isAlias) {
		this.isAlias = isAlias;
	}

	public List<String> getDataSets() {
		return dataSets;
	}

	public void setDataSets(List<String> dataSets) {
		this.dataSets = dataSets;
	}
}
