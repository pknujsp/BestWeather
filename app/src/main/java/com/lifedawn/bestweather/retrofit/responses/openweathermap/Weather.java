package com.lifedawn.bestweather.retrofit.responses.openweathermap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Weather {
	@Expose
	@SerializedName("id")
	private String id;

	@Expose
	@SerializedName("main")
	private String main;

	@Expose
	@SerializedName("description")
	private String description;

	@Expose
	@SerializedName("icon")
	private String icon;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
