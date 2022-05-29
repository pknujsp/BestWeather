package com.lifedawn.bestweather.retrofit.responses.google.placesearch;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GooglePlaceSearchResponse {
	@SerializedName("status")
	@Expose
	private String status;

	@SerializedName("results")
	@Expose
	private ArrayList<Item> results;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ArrayList<Item> getResults() {
		return results;
	}

	public void setResults(ArrayList<Item> results) {
		this.results = results;
	}

	public static class Item {
		@SerializedName("name")
		@Expose
		private String name;

		@SerializedName("formatted_address")
		@Expose
		private String formatted_address;

		@SerializedName("geometry")
		@Expose
		private Geometry geometry;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFormatted_address() {
			return formatted_address;
		}

		public void setFormatted_address(String formatted_address) {
			this.formatted_address = formatted_address;
		}

		public Geometry getGeometry() {
			return geometry;
		}

		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}

		public static class Geometry {
			@SerializedName("location")
			@Expose
			private LatLng location;

			public LatLng getLocation() {
				return location;
			}

			public void setLocation(LatLng location) {
				this.location = location;
			}

			public static class LatLng {
				@SerializedName("lat")
				@Expose
				private String lat;

				@SerializedName("lng")
				@Expose
				private String lng;

				public String getLat() {
					return lat;
				}

				public void setLat(String lat) {
					this.lat = lat;
				}

				public String getLng() {
					return lng;
				}

				public void setLng(String lng) {
					this.lng = lng;
				}
			}
		}
	}
}
