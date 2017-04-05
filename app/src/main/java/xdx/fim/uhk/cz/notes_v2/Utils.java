package xdx.fim.uhk.cz.notes_v2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by xDx on 29.2.2016.
 */
public class Utils {

    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);


    public static long formatDateAsLong(Calendar cal){
        return Long.parseLong(dateFormat.format(cal.getTime()));
    }

    public static Calendar getCalendarFromFormattedLong(long l){
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(dateFormat.parse(String.valueOf(l)));
            return c;

        } catch (ParseException e) {
            return null;
        }
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }




    public static final String DEFAULT = "dd/MM/yyyy";
    private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat(DEFAULT);

    public static String getFormattedDate(Calendar calendar){
        return getFormattedDate(DEFAULT, calendar.getTime());
    }

    public static String getFormattedDate(Date date){
        return getFormattedDate(DEFAULT, date);
    }

    public static String getFormattedDate(String format, Calendar calendar){
        return getFormattedDate(format, calendar.getTime());
    }

    public static String getFormattedDate(String format, Date d){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(d);
    }
}
