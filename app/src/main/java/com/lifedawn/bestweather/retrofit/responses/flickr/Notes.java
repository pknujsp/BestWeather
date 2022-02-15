
package com.lifedawn.bestweather.retrofit.responses.flickr;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notes implements Serializable
{

    @SerializedName("note")
    @Expose
    private List<Object> note = null;
    private final static long serialVersionUID = 7574167542211636876L;

    public List<Object> getNote() {
        return note;
    }

    public void setNote(List<Object> note) {
        this.note = note;
    }

}
