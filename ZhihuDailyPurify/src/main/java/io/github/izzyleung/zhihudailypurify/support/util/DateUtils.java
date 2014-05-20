package io.github.izzyleung.zhihudailypurify.support.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class DateUtils {
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    public static final Calendar birthDay = Calendar.getInstance();

    private DateUtils() {

    }

    static {
        try {
            birthDay.setTime(simpleDateFormat.parse("20130519"));
        } catch (ParseException ignored) {

        }
    }

    public static boolean isSameDay(Calendar first, Calendar second) {
        return (first.get(Calendar.YEAR) == second.get(Calendar.YEAR)) &&
                (first.get(Calendar.MONTH) == second.get(Calendar.MONTH)) &&
                (first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH));
    }
}
