
package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dates implements Serializable {

	@SerializedName("posted")
	@Expose
	private String posted;
	@SerializedName("taken")
	@Expose
	private String taken;
	@SerializedName("takengranularity")
	@Expose
	private String takengranularity;
	@SerializedName("takenunknown")
	@Expose
	private String takenunknown;
	@SerializedName("lastupdate")
	@Expose
	private String lastupdate;
	private final static long serialVersionUID = 838551288520418340L;

	public String getPosted() {
		return posted;
	}

	public void setPosted(String posted) {
		this.posted = posted;
	}

	public String getTaken() {
		return taken;
	}

	public void setTaken(String taken) {
		this.taken = taken;
	}

	public String getTakengranularity() {
		return takengranularity;
	}

	public void setTakengranularity(String takengranularity) {
		this.takengranularity = takengranularity;
	}

	public String getTakenunknown() {
		return takenunknown;
	}

	public void setTakenunknown(String takenunknown) {
		this.takenunknown = takenunknown;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(String lastupdate) {
		this.lastupdate = lastupdate;
	}

}
