package com.lifedawn.bestweather.retrofit.parameters.google;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import java.util.Map;

public class GooglePlaceSearchParameter extends RequestParameter {
	private String query;

	public Map<String, String> getMap() {
		Map<String, String> map = new ArrayMap<>();

		boolean containKr = query.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

		map.put("query", query);
		map.put("key", "");
		map.put("language", containKr ? "ko" : "en");

		return map;
	}

	public GooglePlaceSearchParameter(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public GooglePlaceSearchParameter setQuery(String query) {
		this.query = query;
		return this;
	}
}
