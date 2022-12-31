package com.lifedawn.bestweather.ui.weathers.detailfragment.dto;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class GridItemDto {
	public final String label;
	public final String value;
	public final Drawable img;
	public Integer imgRotate;
	
	public GridItemDto(String label, String value, @Nullable Drawable img) {
		this.label = label;
		this.value = value;
		this.img = img;
	}

	public GridItemDto(String label, String value, Drawable img, Integer imgRotate) {
		this.label = label;
		this.value = value;
		this.img = img;
		this.imgRotate = imgRotate;
	}

	public GridItemDto setImgRotate(int imgRotate) {
		this.imgRotate = imgRotate;
		return this;
	}
}
