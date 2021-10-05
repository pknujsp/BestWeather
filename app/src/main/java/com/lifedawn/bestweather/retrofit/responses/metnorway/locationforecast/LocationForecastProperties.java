package com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries.LocationForecastTimeSeriesItem;

import java.util.List;

public class LocationForecastProperties {
	@Expose
	@SerializedName("meta")
	private LocationForecastMeta meta;

	@Expose
	@SerializedName("timeseries")
	private List<LocationForecastTimeSeriesItem> timeSeries;

	public LocationForecastMeta getMeta() {
		return meta;
	}

	public void setMeta(LocationForecastMeta meta) {
		this.meta = meta;
	}

	public List<LocationForecastTimeSeriesItem> getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(List<LocationForecastTimeSeriesItem> timeSeries) {
		this.timeSeries = timeSeries;
	}
}
