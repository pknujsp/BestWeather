package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;


@Xml(name = "item", inheritance = true)
public class MidLandFcstItem {
	//중기육상예보
	@Expose
	@SerializedName("regId")
	@PropertyElement(name = "regId")

	private String regId;

	@Expose
	@SerializedName("rnSt10")
	@PropertyElement(name = "rnSt10")

	private String rnSt10;

	@Expose
	@SerializedName("rnSt3Am")
	@PropertyElement(name = "rnSt3Am")

	private String rnSt3Am;

	@Expose
	@SerializedName("rnSt3Pm")
	@PropertyElement(name = "rnSt3Pm")

	private String rnSt3Pm;

	@Expose
	@SerializedName("rnSt4Am")
	@PropertyElement(name = "rnSt4Am")

	private String rnSt4Am;

	@Expose
	@SerializedName("rnSt4Pm")
	@PropertyElement(name = "rnSt4Pm")

	private String rnSt4Pm;

	@Expose
	@SerializedName("rnSt5Am")
	@PropertyElement(name = "rnSt5Am")

	private String rnSt5Am;

	@Expose
	@SerializedName("rnSt5Pm")
	@PropertyElement(name = "rnSt5Pm")

	private String rnSt5Pm;

	@Expose
	@SerializedName("rnSt6Am")
	@PropertyElement(name = "rnSt6Am")

	private String rnSt6Am;

	@Expose
	@SerializedName("rnSt6Pm")
	@PropertyElement(name = "rnSt6Pm")

	private String rnSt6Pm;

	@Expose
	@SerializedName("rnSt7Am")
	@PropertyElement(name = "rnSt7Am")

	private String rnSt7Am;

	@Expose
	@SerializedName("rnSt7Pm")
	@PropertyElement(name = "rnSt7Pm")

	private String rnSt7Pm;

	@Expose
	@SerializedName("rnSt8")
	@PropertyElement(name = "rnSt8")

	private String rnSt8;

	@Expose
	@SerializedName("rnSt9")
	@PropertyElement(name = "rnSt9")

	private String rnSt9;

	@Expose
	@SerializedName("wf10")
	@PropertyElement(name = "wf10")

	private String wf10;

	@Expose
	@SerializedName("wf3Am")
	@PropertyElement(name = "wf3Am")

	private String wf3Am;

	@Expose
	@SerializedName("wf3Pm")
	@PropertyElement(name = "wf3Pm")

	private String wf3Pm;

	@Expose
	@SerializedName("wf4Am")
	@PropertyElement(name = "wf4Am")

	private String wf4Am;

	@Expose
	@SerializedName("wf4Pm")
	@PropertyElement(name = "wf4Pm")

	private String wf4Pm;

	@Expose
	@SerializedName("wf5Am")
	@PropertyElement(name = "wf5Am")

	private String wf5Am;

	@Expose
	@SerializedName("wf5Pm")
	@PropertyElement(name = "wf5Pm")

	private String wf5Pm;

	@Expose
	@SerializedName("wf6Am")
	@PropertyElement(name = "wf6Am")

	private String wf6Am;

	@Expose
	@SerializedName("wf6Pm")
	@PropertyElement(name = "wf6Pm")

	private String wf6Pm;

	@Expose
	@SerializedName("wf7Am")
	@PropertyElement(name = "wf7Am")

	private String wf7Am;

	@Expose
	@SerializedName("wf7Pm")
	@PropertyElement(name = "wf7Pm")

	private String wf7Pm;

	@Expose
	@SerializedName("wf8")
	@PropertyElement(name = "wf8")

	private String wf8;

	@Expose
	@SerializedName("wf9")
	@PropertyElement(name = "wf9")

	private String wf9;

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}

	public String getRnSt10() {
		return rnSt10;
	}

	public void setRnSt10(String rnSt10) {
		this.rnSt10 = rnSt10;
	}

	public String getRnSt3Am() {
		return rnSt3Am;
	}

	public void setRnSt3Am(String rnSt3Am) {
		this.rnSt3Am = rnSt3Am;
	}

	public String getRnSt3Pm() {
		return rnSt3Pm;
	}

	public void setRnSt3Pm(String rnSt3Pm) {
		this.rnSt3Pm = rnSt3Pm;
	}

	public String getRnSt4Am() {
		return rnSt4Am;
	}

	public void setRnSt4Am(String rnSt4Am) {
		this.rnSt4Am = rnSt4Am;
	}

	public String getRnSt4Pm() {
		return rnSt4Pm;
	}

	public void setRnSt4Pm(String rnSt4Pm) {
		this.rnSt4Pm = rnSt4Pm;
	}

	public String getRnSt5Am() {
		return rnSt5Am;
	}

	public void setRnSt5Am(String rnSt5Am) {
		this.rnSt5Am = rnSt5Am;
	}

	public String getRnSt5Pm() {
		return rnSt5Pm;
	}

	public void setRnSt5Pm(String rnSt5Pm) {
		this.rnSt5Pm = rnSt5Pm;
	}

	public String getRnSt6Am() {
		return rnSt6Am;
	}

	public void setRnSt6Am(String rnSt6Am) {
		this.rnSt6Am = rnSt6Am;
	}

	public String getRnSt6Pm() {
		return rnSt6Pm;
	}

	public void setRnSt6Pm(String rnSt6Pm) {
		this.rnSt6Pm = rnSt6Pm;
	}

	public String getRnSt7Am() {
		return rnSt7Am;
	}

	public void setRnSt7Am(String rnSt7Am) {
		this.rnSt7Am = rnSt7Am;
	}

	public String getRnSt7Pm() {
		return rnSt7Pm;
	}

	public void setRnSt7Pm(String rnSt7Pm) {
		this.rnSt7Pm = rnSt7Pm;
	}

	public String getRnSt8() {
		return rnSt8;
	}

	public void setRnSt8(String rnSt8) {
		this.rnSt8 = rnSt8;
	}

	public String getRnSt9() {
		return rnSt9;
	}

	public void setRnSt9(String rnSt9) {
		this.rnSt9 = rnSt9;
	}

	public String getWf10() {
		return wf10;
	}

	public void setWf10(String wf10) {
		this.wf10 = wf10;
	}

	public String getWf3Am() {
		return wf3Am;
	}

	public void setWf3Am(String wf3Am) {
		this.wf3Am = wf3Am;
	}

	public String getWf3Pm() {
		return wf3Pm;
	}

	public void setWf3Pm(String wf3Pm) {
		this.wf3Pm = wf3Pm;
	}

	public String getWf4Am() {
		return wf4Am;
	}

	public void setWf4Am(String wf4Am) {
		this.wf4Am = wf4Am;
	}

	public String getWf4Pm() {
		return wf4Pm;
	}

	public void setWf4Pm(String wf4Pm) {
		this.wf4Pm = wf4Pm;
	}

	public String getWf5Am() {
		return wf5Am;
	}

	public void setWf5Am(String wf5Am) {
		this.wf5Am = wf5Am;
	}

	public String getWf5Pm() {
		return wf5Pm;
	}

	public void setWf5Pm(String wf5Pm) {
		this.wf5Pm = wf5Pm;
	}

	public String getWf6Am() {
		return wf6Am;
	}

	public void setWf6Am(String wf6Am) {
		this.wf6Am = wf6Am;
	}

	public String getWf6Pm() {
		return wf6Pm;
	}

	public void setWf6Pm(String wf6Pm) {
		this.wf6Pm = wf6Pm;
	}

	public String getWf7Am() {
		return wf7Am;
	}

	public void setWf7Am(String wf7Am) {
		this.wf7Am = wf7Am;
	}

	public String getWf7Pm() {
		return wf7Pm;
	}

	public void setWf7Pm(String wf7Pm) {
		this.wf7Pm = wf7Pm;
	}

	public String getWf8() {
		return wf8;
	}

	public void setWf8(String wf8) {
		this.wf8 = wf8;
	}

	public String getWf9() {
		return wf9;
	}

	public void setWf9(String wf9) {
		this.wf9 = wf9;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
