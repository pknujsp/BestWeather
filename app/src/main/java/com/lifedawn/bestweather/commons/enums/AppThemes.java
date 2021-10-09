package com.lifedawn.bestweather.commons.enums;

public enum AppThemes {
	BLACK, WHITE;


	public static AppThemes enumOf(String value) throws IllegalArgumentException {
		for (AppThemes appTheme : values()) {
			if (value.equals(appTheme.name())) {
				return appTheme;
			}
		}
		throw new IllegalArgumentException();
	}
}
