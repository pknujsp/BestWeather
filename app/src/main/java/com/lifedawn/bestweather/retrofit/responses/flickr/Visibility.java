
package com.lifedawn.bestweather.retrofit.responses.flickr;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Visibility implements Serializable {

	@SerializedName("ispublic")
	@Expose
	private Integer ispublic;
	@SerializedName("isfriend")
	@Expose
	private Integer isfriend;
	@SerializedName("isfamily")
	@Expose
	private Integer isfamily;
	private final static long serialVersionUID = -8291273102820984400L;

	public Integer getIspublic() {
		return ispublic;
	}

	public void setIspublic(Integer ispublic) {
		this.ispublic = ispublic;
	}

	public Integer getIsfriend() {
		return isfriend;
	}

	public void setIsfriend(Integer isfriend) {
		this.isfriend = isfriend;
	}

	public Integer getIsfamily() {
		return isfamily;
	}

	public void setIsfamily(Integer isfamily) {
		this.isfamily = isfamily;
	}

}
