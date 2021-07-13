package org.bzdev.drama.common;
import org.bzdev.util.SafeFormatter;

import java.util.*;

class ExceptionString {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.drama.common.lpack.ExceptionString");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}

