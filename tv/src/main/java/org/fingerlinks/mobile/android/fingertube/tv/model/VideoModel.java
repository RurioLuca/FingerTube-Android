package org.fingerlinks.mobile.android.fingertube.tv.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class VideoModel implements Serializable {

    public String title;
    public String url;
    public String image;

    @Exclude
    public String key;

    public VideoModel() {
    }

    public VideoModel(String title, String url, String image) {
        this.title = title;
        this.url = url;
        this.image = image;
    }
}
