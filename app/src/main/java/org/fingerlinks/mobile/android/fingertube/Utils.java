package org.fingerlinks.mobile.android.fingertube;

import android.content.Context;
import android.text.format.DateUtils;

import java.security.MessageDigest;
import java.util.Date;

/**
 * Created by raphaelbussa on 21/10/16.
 */

public class Utils {

    public static String gravatarUrl(String mail) {
        return "http://www.gravatar.com/avatar/" + convertToMd5(mail) + "?s=64";
    }

    public static String convertToMd5(String md5) {
        try {
            MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String dataRelativa(Context context, Date date) {
        return (String) DateUtils.getRelativeDateTimeString(context, date.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
    }

}
