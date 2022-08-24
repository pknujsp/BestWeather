package com.lifedawn.bestweather.retrofit.parameters.google;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.Map;

public class GoogleFindPlaceParameter extends RequestParameter {
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

	public GoogleFindPlaceParameter(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}

	public GoogleFindPlaceParameter setInput(String input) {
		this.input = input;
		return this;
	}

}
