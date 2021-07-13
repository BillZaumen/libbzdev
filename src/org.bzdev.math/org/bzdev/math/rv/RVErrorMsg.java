package org.bzdev.math.rv;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class RVErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.math.rv.lpack.RV");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
