package com.lifedawn.bestweather.weathers.detailfragment.dto;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class GridItemDto {
	public final String label;
	public final String value;
	public final Drawable img;
	
	public GridItemDto(String label, String value, @Nullable Drawable img) {
		this.label = label;
		this.value = value;
		this.img = img;
	}
}
