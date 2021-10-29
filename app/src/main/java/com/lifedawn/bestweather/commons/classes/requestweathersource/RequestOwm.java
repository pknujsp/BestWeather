package com.lifedawn.bestweather.commons.classes.requestweathersource;

import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;

import java.util.HashSet;
import java.util.Set;

public class RequestOwm extends RequestWeatherSource {
	private Set<OneCallParameter.OneCallApis> excludeApis;

	public void setExcludeApis(Set<OneCallParameter.OneCallApis> excludeApis) {
		this.excludeApis = excludeApis;
	}

	public Set<OneCallParameter.OneCallApis> getExcludeApis() {
		return excludeApis;
	}
}
