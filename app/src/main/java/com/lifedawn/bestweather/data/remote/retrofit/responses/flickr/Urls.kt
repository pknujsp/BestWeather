
package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Urls implements Serializable
{

    @SerializedName("url")
    @Expose
    private List<Url> url = null;
    private final static long serialVersionUID = -7370851300232911951L;

    public List<Url> getUrl() {
        return url;
    }

    public void setUrl(List<Url> url) {
        this.url = url;
    }

}
