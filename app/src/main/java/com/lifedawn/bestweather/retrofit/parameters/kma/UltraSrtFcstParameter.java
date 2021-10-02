package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class UltraSrtFcstParameter {
	private String serviceKey = RetrofitClient.VILAGE_FCST_INFO_SERVICE_SERVICE_KEY;
	private final String numOfRows = "1000";
	private String pageNo;
	private String dataType = RetrofitClient.DATATYPE;
	private String baseDate;
	private String baseTime;
	private String nx;
	private String ny;
	private Map<String, String> map = new HashMap<>();
	
	public UltraSrtFcstParameter() {
	
	}
	
	public UltraSrtFcstParameter(String pageNo, String baseDate, String baseTime, String nx, String ny) {
		this.pageNo = pageNo;
		this.baseDate = baseDate;
		this.baseTime = baseTime;
		this.nx = nx;
		this.ny = ny;
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
	
	
	public String getNumOfRows() {
		return numOfRows;
	}
	
	
	public String getPageNo() {
		return pageNo;
	}
	
	public UltraSrtFcstParameter setPageNo(String pageNo) {
		this.pageNo = pageNo;
		return this;
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
	
	public UltraSrtFcstParameter deepCopy() {
		return new UltraSrtFcstParameter(pageNo, baseDate, baseTime, nx, ny);
	}
}
