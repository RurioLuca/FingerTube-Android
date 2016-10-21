package org.fingerlinks.mobile.android.fingertube.tv;

import android.app.Application;

import com.tumblr.remember.Remember;

/**
 * Created by raphaelbussa on 21/10/16.
 */

public class FingerTubeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Remember.init(getApplicationContext(), R.class.getPackage().getName());
        if (Remember.getString("tv_id", "").isEmpty()) {
            Remember.putString("tv_id", Utils.getLoginCode());
        }
    }
}
