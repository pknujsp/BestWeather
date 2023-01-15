package com.lifedawn.bestweather.data.remote.retrofit.parameters.google;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter;

import java.util.Map;

public class GoogleFindPlaceParameterRest extends RestRequestParameter {
	private String input;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		//boolean containKr = query.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
		map.put("input", input);
		map.put("key", "");
		//map.put("language", containKr ? "ko" : "en");
		map.put("inputtype", "textquery");

		return map;
	}

	public GoogleFindPlaceParameterRest(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}

	public GoogleFindPlaceParameterRest setInput(String input) {
		this.input = input;
		return this;
	}

}
