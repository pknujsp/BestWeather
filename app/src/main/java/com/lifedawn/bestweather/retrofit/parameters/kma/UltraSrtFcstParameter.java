package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.HashMap;
import java.util.Map;

public class UltraSrtFcstParameter extends RequestParameter {
	private String serviceKey = RetrofitClient.VILAGE_FCST_INFO_SERVICE_SERVICE_KEY;
	private final String numOfRows = "1000";
	private final String pageNo = "1";
	private String dataType = RetrofitClient.DATATYPE;
	private String baseDate;
	private String baseTime;
	private String nx;
	private String ny;
	private double latitude;
	private double longitude;
	private Map<String, String> map = new HashMap<>();

	public UltraSrtFcstParameter() {

	}

	public Map<String, String> getMap() {
		map.clear();

		map.put("serviceKey", serviceKey);
		map.put("numOfRows", numOfRows);
		map.put("pageNo", pageNo);
		map.put("dataType", dataType);
		map.put("base_date", baseDate);
		map.put("base_time", baseTime);
		map.put("nx", nx);
		map.put("ny", ny);

		return map;
	}


	public String getBaseDate() {
		return baseDate;
	}

	public UltraSrtFcstParameter setBaseDate(String baseDate) {
		this.baseDate = baseDate;
		return this;
	}

	public String getBaseTime() {
		return baseTime;
	}

	public UltraSrtFcstParameter setBaseTime(String baseTime) {
		this.baseTime = baseTime;
		return this;
	}

	public String getNx() {
		return nx;
	}

	public UltraSrtFcstParameter setNx(String nx) {
		this.nx = nx;
		return this;
	}

	public String getNy() {
		return ny;
	}

	public UltraSrtFcstParameter setNy(String ny) {
		this.ny = ny;
		return this;
	}

	public double getLatitude() {
		return latitude;
	}

	public UltraSrtFcstParameter setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}

	public double getLongitude() {
		return longitude;
	}

	public UltraSrtFcstParameter setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}
}
