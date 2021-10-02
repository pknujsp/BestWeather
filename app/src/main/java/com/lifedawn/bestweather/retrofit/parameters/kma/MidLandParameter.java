package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class MidLandParameter  {
	private String serviceKey = RetrofitClient.MID_FCST_INFO_SERVICE_SERVICE_KEY;
	private final String numOfRows = "300";
	private String pageNo;
	private String dataType = RetrofitClient.DATATYPE;
	private String regId;
	private String tmFc;
	private Map<String, String> map = new HashMap<>();
	
	
	public MidLandParameter() {
	}
	
	public MidLandParameter(String pageNo, String regId, String tmFc) {
		this.pageNo = pageNo;
		this.regId = regId;
		this.tmFc = tmFc;
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
	
	
	public String getNumOfRows() {
		return numOfRows;
	}
	
	public String getPageNo() {
		return pageNo;
	}
	
	public MidLandParameter setPageNo(String pageNo) {
		this.pageNo = pageNo;
		return this;
	}
	
	
	public String getRegId() {
		return regId;
	}
	
	public MidLandParameter setRegId(String regId) {
		this.regId = regId;
		return this;
	}
	
	public String getTmFc() {
		return tmFc;
	}
	
	public MidLandParameter setTmFc(String tmFc) {
		this.tmFc = tmFc;
		return this;
	}
	
	public MidLandParameter deepCopy() {
		return new MidLandParameter(pageNo, regId, tmFc);
	}
}
