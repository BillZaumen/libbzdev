package org.bzdev.ejws;

/*
 * EJWS Exception class.
 * This class is provided so that the method
 * {@link WebMap#getInfoFromPath(String,String,String,String,WebMap.RequestInfo)}
 * can simply throw an exception to indicate an error. It's caller
 * will then generate a standard error message and set an appropriate
 * response status code.
 */
public class EjwsException extends Exception {

    /**
     * Constructor.
     */
    public EjwsException() {
	super();
    }

    /**
     * Constructor with a message.
     * @param message the message
     */
    public EjwsException(String message) {
	super(message);
    }

    /**
     * Constructor with a message and cause.
     * @param message the message
     * @param cause the exception that caused this exception; null if not
     *        known
     */
    public EjwsException(String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * Constructor with a message and cause, suppression enabled or disabled,
     * and a writable stack trace enabled or disabled.
     * @param message the message
     * @param cause the exception that caused this exception; null if not
     *        known
     * @param enableSuppression true if suppression is enabled; false if it
     *        is disabled
     * @param writableStackTrace true if the stack trace should be
     *        writable; false otherwise
     */
    protected EjwsException(String message, Throwable cause,
			boolean enableSuppression,
			boolean writableStackTrace)
    {
	super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructor given a cause.
     * @param cause the cause; null if the cause is not known
     */
    public EjwsException(Throwable cause) {
	super(cause);
    }
}

//  LocalWords:  EJWS WebMap getInfoFromPath RequestInfo
//  LocalWords:  enableSuppression writableStackTrace
