package org.bzdev.net.calendar;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class CalendarErrorMsg {

    private static ResourceBundle exbundle =
	ResourceBundle.getBundle("org.bzdev.net.calender.lpack.Calendar");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
