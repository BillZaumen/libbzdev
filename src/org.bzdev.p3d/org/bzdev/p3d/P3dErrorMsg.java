package org.bzdev.p3d;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class P3dErrorMsg {

    private static ResourceBundle
	exbundle=ResourceBundle.getBundle("org.bzdev.p3d.lpack.P3d");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
