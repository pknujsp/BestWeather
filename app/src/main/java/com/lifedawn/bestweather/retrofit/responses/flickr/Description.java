
package com.lifedawn.bestweather.retrofit.responses.flickr;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Description implements Serializable
{

    @SerializedName("_content")
    @Expose
    private String content;
    private final static long serialVersionUID = -2580127429010408574L;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
