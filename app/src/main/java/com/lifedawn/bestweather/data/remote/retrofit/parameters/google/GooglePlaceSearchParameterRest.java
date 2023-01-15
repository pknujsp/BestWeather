package com.lifedawn.bestweather.data.remote.retrofit.parameters.google;

import android.util.ArrayMap;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter;

import java.util.Map;

public class GooglePlaceSearchParameterRest extends RestRequestParameter {
	private String query;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		boolean containKr = query.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

		map.put("query", query);
		map.put("key", "");
		map.put("language", containKr ? "ko" : "en");

		return map;
	}

	public GooglePlaceSearchParameterRest(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public GooglePlaceSearchParameterRest setQuery(String query) {
		this.query = query;
		return this;
	}
}
