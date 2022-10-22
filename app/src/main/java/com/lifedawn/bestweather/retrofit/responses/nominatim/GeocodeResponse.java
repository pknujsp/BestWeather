package com.lifedawn.bestweather.retrofit.responses.nominatim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeocodeResponse {

	@Expose
	@SerializedName("features")
	private List<Features> features;

	@Expose
	@SerializedName("type")
	private String type;

	public List<Features> getFeatures() {
		return features;
	}

	public void setFeatures(List<Features> features) {
		this.features = features;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static class Features {
		@Expose
		@SerializedName("geometry")
		private Geometry geometry;
		@Expose

		@SerializedName("bbox")
		private List<Double> bbox;
		@Expose

		@SerializedName("properties")
		private Properties properties;
		@Expose
		@SerializedName("type")
		private String type;

		public Geometry getGeometry() {
			return geometry;
		}

		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}

		public List<Double> getBbox() {
			return bbox;
		}

		public void setBbox(List<Double> bbox) {
			this.bbox = bbox;
		}

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public static class Geometry {
		@Expose
		@SerializedName("coordinates")
		private List<Double> coordinates;
		@Expose
		@SerializedName("type")
		private String type;

		public List<Double> getCoordinates() {
			return coordinates;
		}

		public void setCoordinates(List<Double> coordinates) {
			this.coordinates = coordinates;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public static class Properties {
		@Expose
		@SerializedName("address")
		private Address address;
		@Expose
		@SerializedName("icon")
		private String icon;

		@Expose
		@SerializedName("type")
		private String type;
		@Expose
		@SerializedName("category")
		private String category;
		@Expose
		@SerializedName("place_rank")
		private int placeRank;
		@Expose
		@SerializedName("display_name")
		private String displayName;
		@Expose
		@SerializedName("osm_id")
		private String osmId;
		@Expose
		@SerializedName("osm_type")
		private String osmType;
		@Expose
		@SerializedName("place_id")
		private String placeId;

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}


		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public int getPlaceRank() {
			return placeRank;
		}

		public void setPlaceRank(int placeRank) {
			this.placeRank = placeRank;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getOsmId() {
			return osmId;
		}

		public void setOsmId(String osmId) {
			this.osmId = osmId;
		}

		public String getOsmType() {
			return osmType;
		}

		public void setOsmType(String osmType) {
			this.osmType = osmType;
		}

		public String getPlaceId() {
			return placeId;
		}

		public void setPlaceId(String placeId) {
			this.placeId = placeId;
		}
	}

	public static class Address {
		@Expose
		@SerializedName("country_code")
		private String countryCode;
		@Expose
		@SerializedName("country")
		private String country;
		@Expose
		@SerializedName("state")
		private String state;
		@Expose
		@SerializedName("town")
		private String town;
		@Expose
		@SerializedName("railway")
		private String railway;

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}


		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public String getTown() {
			return town;
		}

		public void setTown(String town) {
			this.town = town;
		}

		public String getRailway() {
			return railway;
		}

		public void setRailway(String railway) {
			this.railway = railway;
		}
	}
}
