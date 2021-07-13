package org.bzdev.lang;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

class LangErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.lang.lpack.Lang");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}
