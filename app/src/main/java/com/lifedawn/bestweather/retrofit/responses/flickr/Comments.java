
package com.lifedawn.bestweather.retrofit.responses.flickr;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Comments implements Serializable
{

    @SerializedName("_content")
    @Expose
    private Integer content;
    private final static long serialVersionUID = -751650815179992814L;

    public Integer getContent() {
        return content;
    }

    public void setContent(Integer content) {
        this.content = content;
    }

}
