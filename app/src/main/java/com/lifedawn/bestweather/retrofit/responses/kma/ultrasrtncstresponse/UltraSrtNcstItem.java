package com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UltraSrtNcstItem {
	// 초단기실황조회 아이템
	@Expose
	@SerializedName("baseDate")
	private String baseDate;

	@Expose
	@SerializedName("baseTime")
	private String baseTime;

	@Expose
	@SerializedName("category")
	private String category;

	@Expose
	@SerializedName("nx")
	private String nx;

	@Expose
	@SerializedName("ny")
	private String ny;

	@Expose
	@SerializedName("obsrValue")
	private String obsrValue;

	public String getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}

	public String getBaseTime() {
		return baseTime;
	}

	public void setBaseTime(String baseTime) {
		this.baseTime = baseTime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getNx() {
		return nx;
	}

	public void setNx(String nx) {
		this.nx = nx;
	}

	public String getNy() {
		return ny;
	}

	public void setNy(String ny) {
		this.ny = ny;
	}

	public String getObsrValue() {
		return obsrValue;
	}

	public void setObsrValue(String obsrValue) {
		this.obsrValue = obsrValue;
	}
}
