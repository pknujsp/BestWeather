package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class UltraSrtNcstParameter {
	private String serviceKey = RetrofitClient.VILAGE_FCST_INFO_SERVICE_SERVICE_KEY;
	private final String numOfRows = "300";
	private final String pageNo = "1";
	private String dataType = RetrofitClient.DATATYPE;
	private String baseDate;
	private String baseTime;
	private String nx;
	private String ny;
	private Map<String, String> map = new HashMap<>();

	public UltraSrtNcstParameter() {

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

	public UltraSrtNcstParameter setBaseDate(String baseDate) {
		this.baseDate = baseDate;
		return this;
	}

	public String getBaseTime() {
		return baseTime;
	}

	public UltraSrtNcstParameter setBaseTime(String baseTime) {
		this.baseTime = baseTime;
		return this;
	}

	public String getNx() {
		return nx;
	}

	public UltraSrtNcstParameter setNx(String nx) {
		this.nx = nx;
		return this;
	}

	public String getNy() {
		return ny;
	}

	public UltraSrtNcstParameter setNy(String ny) {
		this.ny = ny;
		return this;
	}
}
