package org.fingerlinks.mobile.android.fingertube.tv;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.Random;

/**
 * Created by raphaelbussa on 20/10/16.
 */

class Utils {

    @SuppressWarnings("SpellCheckingInspection")
    static String getLoginCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 6) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("deprecation")
    static Spanned fromHtml(String source) {
        if (source.isEmpty()) {
            source = "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

}
