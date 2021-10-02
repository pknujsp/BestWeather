package com.lifedawn.bestweather.retrofit.parameters.kma;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

public class MidTaParameter {
	private String serviceKey = RetrofitClient.MID_FCST_INFO_SERVICE_SERVICE_KEY;
	private final String numOfRows = "300";
	private String pageNo;
	private String dataType = RetrofitClient.DATATYPE;
	private String regId;
	private String tmFc;
	private Map<String, String> map = new HashMap<>();
	
	
	public MidTaParameter() {
	}
	
	public MidTaParameter(String pageNo, String regId, String tmFc) {
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
	
	public MidTaParameter setPageNo(String pageNo) {
		this.pageNo = pageNo;
		return this;
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
	
	public MidTaParameter deepCopy() {
		return new MidTaParameter(pageNo, regId, tmFc);
	}
}
