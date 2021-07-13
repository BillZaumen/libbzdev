package org.bzdev.ejws.maps;
import java.util.*;
import org.bzdev.util.SafeFormatter;


class WebMapErrorMsg {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.maps.lpack.WebMap");
    
    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
}