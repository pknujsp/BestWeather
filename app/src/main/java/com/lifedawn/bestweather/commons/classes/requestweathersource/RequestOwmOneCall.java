package com.lifedawn.bestweather.commons.classes.requestweathersource;

import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter;

import java.util.Set;

public class RequestOwmOneCall extends RequestWeatherSource {
	private Set<OwmOneCallParameter.OneCallApis> excludeApis;

	public void setExcludeApis(Set<OwmOneCallParameter.OneCallApis> excludeApis) {
		this.excludeApis = excludeApis;
	}

	public Set<OwmOneCallParameter.OneCallApis> getExcludeApis() {
		return excludeApis;
	}
}
