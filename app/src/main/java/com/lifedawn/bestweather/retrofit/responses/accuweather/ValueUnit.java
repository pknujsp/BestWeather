package com.lifedawn.bestweather.retrofit.responses.accuweather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ValueUnit {
	@Expose
	@SerializedName("Value")
	private String value;

	@Expose
	@SerializedName("Unit")
	private String unit;

	@Expose
	@SerializedName("UnitType")
	private String unitType;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}
}
