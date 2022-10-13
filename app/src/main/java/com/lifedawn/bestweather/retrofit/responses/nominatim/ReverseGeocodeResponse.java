package com.lifedawn.bestweather.retrofit.responses.nominatim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReverseGeocodeResponse {

	@Expose
	@SerializedName("features")
	private List<Features> features;
	@Expose
	@SerializedName("licence")
	private String licence;
	@Expose
	@SerializedName("type")
	private String type;

	public List<Features> getFeatures() {
		return features;
	}

	public void setFeatures(List<Features> features) {
		this.features = features;
	}

	public String getLicence() {
		return licence;
	}

	public void setLicence(String licence) {
		this.licence = licence;
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
		@SerializedName("display_name")
		private String displayName;
		@Expose
		@SerializedName("name")
		private String name;
		@Expose
		@SerializedName("addresstype")
		private String addresstype;
		@Expose
		@SerializedName("importance")
		private double importance;
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
		@SerializedName("osm_id")
		private String osmId;
		@Expose
		@SerializedName("osm_type")
		private String osmType;
		@Expose
		@SerializedName("place_id")
		private int placeId;

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddresstype() {
			return addresstype;
		}

		public void setAddresstype(String addresstype) {
			this.addresstype = addresstype;
		}

		public double getImportance() {
			return importance;
		}

		public void setImportance(double importance) {
			this.importance = importance;
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

		public int getPlaceId() {
			return placeId;
		}

		public void setPlaceId(int placeId) {
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
		@SerializedName("postcode")
		private String postcode;

		@Expose
		@SerializedName("city")
		private String city;
		@Expose
		@SerializedName("state")
		private String state;
		@Expose
		@SerializedName("borough")
		private String borough;
		@Expose
		@SerializedName("village")
		private String village;
		@Expose
		@SerializedName("neighbourhood")
		private String neighbourhood;
		@Expose
		@SerializedName("road")
		private String road;

		@Expose
		@SerializedName("house_number")
		private String houseNumber;

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

		public String getPostcode() {
			return postcode;
		}

		public void setPostcode(String postcode) {
			this.postcode = postcode;
		}


		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getBorough() {
			return borough;
		}

		public void setBorough(String borough) {
			this.borough = borough;
		}

		public String getVillage() {
			return village;
		}

		public void setVillage(String village) {
			this.village = village;
		}

		public String getNeighbourhood() {
			return neighbourhood;
		}

		public void setNeighbourhood(String neighbourhood) {
			this.neighbourhood = neighbourhood;
		}

		public String getRoad() {
			return road;
		}

		public void setRoad(String road) {
			this.road = road;
		}

		public void setHouseNumber(String houseNumber) {
			this.houseNumber = houseNumber;
		}

		public String getHouseNumber() {
			return houseNumber;
		}


		public void setState(String state) {
			this.state = state;
		}

		public String getState() {
			return state;
		}
	}
}
