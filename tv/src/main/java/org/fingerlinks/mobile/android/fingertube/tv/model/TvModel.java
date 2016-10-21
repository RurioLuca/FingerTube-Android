package org.fingerlinks.mobile.android.fingertube.tv.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by raphaelbussa on 21/10/16.
 */

public class TvModel implements Serializable {

    public String user_id;
    public String model;
    public String video_id;

    public TvModel(String user_id, String model, String video_id) {
        this.user_id = user_id;
        this.model = model;
        this.video_id = video_id;
    }

    public TvModel() {
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user_id", user_id);
        result.put("model", model);
        result.put("video_id", video_id);
        return result;
    }


}
