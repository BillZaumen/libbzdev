package org.bzdev.geom;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class GeomErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.geom.lpack.Geom");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
