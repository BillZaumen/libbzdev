package org.bzdev.math;

/**
 * Exception for errors in computing mathematical functions.
 * A MathException should be used in cases where a user supplies
 * a function and an error occurs in evaluating it.  For example,
 * the class {@link org.bzdev.math.RootFinder.Newton RootFinder.Newton} is
 * abstract because the user must supply a function and its first
 * derivative.  A MathException will occur if there is an error in
 * evaluating these, with the exception that caused the failure provided
 * as a cause.
 */
public class MathException extends RuntimeException {
    /**
     * Constructor.
     * Constructs a new math exception. The cause is not
     * initialized, and may subsequently be initialized by a call
     * to {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
     */
    public MathException() {
	super();
    }

    /**
     * Constructs a new math exception with the specified detailed
     * message.
     * The cause is not initialized, and may subsequently be
     * initialized by a call to
     * {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
     * @param msg the detail message; the detail message is saved for
     * later retrieval by a call to
     * {@link java.lang.Throwable#getMessage() Throwable.getMessage()}
     */
    public MathException(String msg) {
	super(msg);
    }

    /**
     * Constructs a new math exception with the specified detailed
     * message and cause.
     * The cause is not initialized, and may subsequently be
     * initialized by a call to
     * {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
     * @param msg the detail message; the detail message is saved for
     * later retrieval by a call to
     * {@link java.lang.Throwable#getMessage() Throwable.getMessage()}
     * @param cause the cause
     */
    public MathException(String msg, Throwable cause) {
	super(msg, cause);
    }

    /**
     * Constructs a new math exception with the specified cause
     * and a detail message of (cause==null ? null :
     * cause.toString()) (which typically contains the class and
     * detail message of cause).
     * This constructor is useful for math exceptions that are
     * little more than wrappers for other throwables.
     * @param cause the cause; null if the cause is nonexistent or unknown
     */
    public MathException(Throwable cause) {
	super(cause);
    }
}

//  LocalWords:  MathException RootFinder initCause msg getMessage
//  LocalWords:  Throwable toString throwables
