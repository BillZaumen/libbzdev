package org.bzdev.math;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class MathErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.math.lpack.Math");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
