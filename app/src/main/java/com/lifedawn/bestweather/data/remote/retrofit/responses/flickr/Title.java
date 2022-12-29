
package com.lifedawn.bestweather.data.remote.retrofit.responses.flickr;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Title implements Serializable
{

    @SerializedName("_content")
    @Expose
    private String content;
    private final static long serialVersionUID = -409632446508013393L;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
