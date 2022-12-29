package com.lifedawn.bestweather.commons.classes.requestweathersource;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OneCallParameter;

import java.util.Set;

public class RequestOwmOneCall extends RequestWeatherSource {
	private Set<OneCallParameter.OneCallApis> excludeApis;

	public void setExcludeApis(Set<OneCallParameter.OneCallApis> excludeApis) {
		this.excludeApis = excludeApis;
	}

	public Set<OneCallParameter.OneCallApis> getExcludeApis() {
		return excludeApis;
	}
}
