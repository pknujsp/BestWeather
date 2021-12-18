package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.HashMap;
import java.util.Map;

public class MidTaParameter extends RequestParameter {
	private String serviceKey = RetrofitClient.MID_FCST_INFO_SERVICE_SERVICE_KEY;
	private final String numOfRows = "300";
	private final String pageNo = "1";
	private String dataType = RetrofitClient.XML_DATATYPE;
	private String regId;
	private String tmFc;
	private double latitude;
	private double longitude;
	private Map<String, String> map = new HashMap<>();


	public MidTaParameter() {
	}


	public Map<String, String> getMap() {
		map.clear();

		map.put("serviceKey", serviceKey);
		map.put("numOfRows", numOfRows);
		map.put("pageNo", pageNo);
		map.put("dataType", dataType);
		map.put("regId", regId);
		map.put("tmFc", tmFc);

		return map;
	}


	public String getRegId() {
		return regId;
	}

	public MidTaParameter setRegId(String regId) {
		this.regId = regId;
		return this;
	}

	public String getTmFc() {
		return tmFc;
	}

	public MidTaParameter setTmFc(String tmFc) {
		this.tmFc = tmFc;
		return this;
	}

	public double getLatitude() {
		return latitude;
	}

	public MidTaParameter setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}

	public double getLongitude() {
		return longitude;
	}

	public MidTaParameter setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}
}
