package org.bzdev.bin.yrunner;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.bin.yrunner.lpack.CheckOncePerJVM

/**
 * Check that code points have been executed at most once.
 * This class is intended to prevent recursive calls to the
 * main method of a program, which is static.
 */
class CheckOncePerJVM {
    private static boolean allowOnceCalled = false;

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.bin.yrunner.lpack.CheckOncePerJVM");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    /**
     * Check that this method has not been previously called.
     * 
     * @exception SecurityException this method has been called previously
     */
    static synchronized void check() throws SecurityException {
	if (allowOnceCalled) {
	    if (System.getSecurityManager() != null) {
		throw new SecurityException(errorMsg("secOnce"));
		/*
		throw new SecurityException("security policy allows a single "
					    + "call to a set of methods");
		*/
	    } else {
		throw new SecurityException(errorMsg("onlyOnce"));
		/*
		throw new SecurityException("only a single call to a set "
					    + "of methods allowed");
		*/
	    }
	}
	allowOnceCalled = true;
    }
}
