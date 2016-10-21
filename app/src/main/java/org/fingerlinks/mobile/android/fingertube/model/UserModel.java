package org.fingerlinks.mobile.android.fingertube.model;

import java.io.Serializable;

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
}
