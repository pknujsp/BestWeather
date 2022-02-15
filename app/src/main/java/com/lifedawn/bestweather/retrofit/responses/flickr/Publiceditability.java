
package com.lifedawn.bestweather.retrofit.responses.flickr;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Publiceditability implements Serializable
{

    @SerializedName("cancomment")
    @Expose
    private Integer cancomment;
    @SerializedName("canaddmeta")
    @Expose
    private Integer canaddmeta;
    private final static long serialVersionUID = 5373334880571009989L;

    public Integer getCancomment() {
        return cancomment;
    }

    public void setCancomment(Integer cancomment) {
        this.cancomment = cancomment;
    }

    public Integer getCanaddmeta() {
        return canaddmeta;
    }

    public void setCanaddmeta(Integer canaddmeta) {
        this.canaddmeta = canaddmeta;
    }

}
