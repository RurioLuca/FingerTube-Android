package org.fingerlinks.mobile.android.fingertube.tv;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

/**
 * Created by raphaelbussa on 20/10/16.
 */

public class Utils {

    @SuppressWarnings("SpellCheckingInspection")
    public static String getLoginCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 6) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString().toLowerCase();
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (source.isEmpty()) {
            source = "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

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
