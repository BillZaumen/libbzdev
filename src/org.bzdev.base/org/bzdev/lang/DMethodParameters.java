package org.bzdev.lang;
//@exbundle org.bzdev.lang.lpack.Lang

/**
 * Configuration parameters for dynamic methods.
 */
public class DMethodParameters {

    static String errorMsg(String key, Object... args) {
	return LangErrorMsg.errorMsg(key, args);
    }
    
    static Appendable out = System.out;

    /**
     * Set the output to use for tracing when method tracing is enabled.
     * The default is System.out.
     */
    public static void setTracingOutput(Appendable out) {
	if (out == null) out = System.out;
	DMethodParameters.out = out;
    }

    /**
     * Get the output to use for tracing when method tracing is enabled.
     * The default is System.out.
     * @return the appendable to use for tracing
     */
    public static Appendable getTracingOutput() {
	return out;
    }

    /**
     * The default value for the cache limit.
     * This value will be used unless changed by calling
     * {@link #setDefaultCacheLimit(int) setDefaultCacheLimit}.
     */
    public static final int DEFAULT_LIMIT = 128;

    static int defaultLimit = DEFAULT_LIMIT;

    /**
     * Set the default limit for a helper's cache size.
     * The limit is read when a helper class is initialized.
     * @param value a positive integer giving the limit; 0 to
     *        use the default value (DEFAULT_LIMIT)
     * @see #DEFAULT_LIMIT
     */
    public static void setDefaultCacheLimit(int value) {
	if (value < 0)
	    throw new IllegalArgumentException
		(errorMsg("needPositive", value));
	if (value == 0) {
	    defaultLimit = DEFAULT_LIMIT;
	} else {
	    defaultLimit = value;
	}
    }

    /**
     * Get the default limit for a helper's cache size.
     * @return the default limit
     */
    public static int getDefaultCacheLimit() {return defaultLimit;}

}
//  LocalWords:  exbundle appendable setDefaultCacheLimit
//  LocalWords:  needPositive
