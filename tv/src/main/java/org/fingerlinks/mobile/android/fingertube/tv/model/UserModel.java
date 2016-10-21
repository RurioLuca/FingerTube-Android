package org.fingerlinks.mobile.android.fingertube.tv.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by raphaelbussa on 20/10/16.
 */

public class UserModel implements Serializable {

    public String tv_id;
    public String email;
    public String display_name;


    public UserModel() {

    }

    public UserModel(String tv_id, String email, String display_name) {
        this.tv_id = tv_id;
        this.email = email;
        this.display_name = display_name;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("tv_id", tv_id);
        result.put("email", email);
        result.put("display_name", display_name);
        return result;
    }

}
