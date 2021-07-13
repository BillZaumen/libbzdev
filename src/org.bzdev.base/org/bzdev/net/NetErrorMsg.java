package org.bzdev.net;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class NetErrorMsg {

    private static ResourceBundle
	exbundle=ResourceBundle.getBundle("org.bzdev.net.lpack.Net");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
