package org.bzdev.obnaming;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class ObnamingErrorMsg {

    private static ResourceBundle
	exbundle=ResourceBundle.getBundle("org.bzdev.obnaming.lpack.Obnaming");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
