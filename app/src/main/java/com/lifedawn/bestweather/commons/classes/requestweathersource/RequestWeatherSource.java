package com.lifedawn.bestweather.commons.classes.requestweathersource;

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;

import java.util.HashSet;
import java.util.Set;

public class RequestWeatherSource {
	private Set<RetrofitClient.ServiceType> requestServiceTypes = new HashSet<>();

	public Set<RetrofitClient.ServiceType> getRequestServiceTypes() {
		return requestServiceTypes;
	}

	public RequestWeatherSource addRequestServiceType(RetrofitClient.ServiceType serviceType) {
		requestServiceTypes.add(serviceType);
		return this;
	}
}
