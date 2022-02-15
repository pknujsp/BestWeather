
package com.lifedawn.bestweather.retrofit.responses.flickr;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dates implements Serializable
{

    @SerializedName("posted")
    @Expose
    private String posted;
    @SerializedName("taken")
    @Expose
    private String taken;
    @SerializedName("takengranularity")
    @Expose
    private Integer takengranularity;
    @SerializedName("takenunknown")
    @Expose
    private Integer takenunknown;
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

    public Integer getTakengranularity() {
        return takengranularity;
    }

    public void setTakengranularity(Integer takengranularity) {
        this.takengranularity = takengranularity;
    }

    public Integer getTakenunknown() {
        return takenunknown;
    }

    public void setTakenunknown(Integer takenunknown) {
        this.takenunknown = takenunknown;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

}
