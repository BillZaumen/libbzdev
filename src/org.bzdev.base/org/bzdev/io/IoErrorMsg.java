package org.bzdev.io;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class IoErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.io.lpack.IO");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
