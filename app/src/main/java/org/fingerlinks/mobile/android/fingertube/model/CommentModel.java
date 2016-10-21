package org.fingerlinks.mobile.android.fingertube.model;

import java.io.Serializable;

/**
 * Created by raphaelbussa on 21/10/16.
 */

public class CommentModel implements Serializable {

    public String user_id;
    public String user_display_name;
    public String user_email;
    public long timestamp;
    public String comment;

    public CommentModel() {
    }

    public CommentModel(String user_id, String user_display_name, String user_email, long timestamp, String comment) {
        this.user_id = user_id;
        this.user_display_name = user_display_name;
        this.user_email = user_email;
        this.timestamp = timestamp;
        this.comment = comment;
    }
}
