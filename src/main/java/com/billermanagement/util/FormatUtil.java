package com.billermanagement.util;

import java.util.Calendar;
import java.util.TimeZone;

public class FormatUtil {
    public static String getTime(String format) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        return formatter.format(cal.getTime());
    }

    public static String getDateTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mJam = calendar.get(Calendar.HOUR_OF_DAY);
        int mMenit = calendar.get(Calendar.MINUTE);
        int mDetik = calendar.get(Calendar.SECOND);
        int mMilis = calendar.get(Calendar.MILLISECOND);

        return mYear + "-" + mMonth + "-" + mDay + " " + mJam + ":" + mMenit + ":" + mDetik + "." + mMilis;
    }
}
