package org.bzdev.math.stats;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class StatsErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.math.stats.lpack.Stats");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
