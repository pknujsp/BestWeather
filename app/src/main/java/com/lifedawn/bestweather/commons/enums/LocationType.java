package com.lifedawn.bestweather.commons.enums;

import java.io.Serializable;

public enum LocationType implements Serializable {
	CurrentLocation, SelectedAddress;

	public static LocationType enumOf(String value) throws IllegalArgumentException {
		for (LocationType locationType : values()) {
			if (value.equals(locationType.name())) {
				return locationType;
			}
		}
		throw new IllegalArgumentException();
	}
}
