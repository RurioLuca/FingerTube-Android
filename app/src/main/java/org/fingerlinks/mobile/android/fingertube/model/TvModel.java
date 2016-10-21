package org.fingerlinks.mobile.android.fingertube.model;

import java.io.Serializable;

/**
 * Created by raphaelbussa on 21/10/16.
 */

public class TvModel implements Serializable {

    public String user_id;

    public TvModel(String user_id) {
        this.user_id = user_id;
    }

    public TvModel() {
    }
}
