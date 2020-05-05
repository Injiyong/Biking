package cau.injiyong.biking.Common;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Common {

    public static final String APP_ID = "eebf523696f7e0ac06c9bbf0c4311ed0";
    public static Location current_location = null;


    public static String convertUnixToDate(int dt) {
        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd EEE MM yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToHour(int dt) {
        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
