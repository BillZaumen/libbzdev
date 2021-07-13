package org.bzdev.math.rv;

/**
 * Exception for Random Variables.
 * Intended for use in the rare case where a random variable cannot
 * return a value.  Normally this might occur when a random variable's
 * values are themselves random variables with a range restriction that
 * cannot be satisfied.
 *
 * The constructors use the same arguments as those for a RuntimeException
 * (and the documentation is more or less transcribed from the Javadoc
 * documentation for RuntimeException to emphasize that the usage is
 * identical).
 */
public class RandomVariableException extends RuntimeException {
    /**
     * Constructs a new runtime exception with null as its detail message. 
     * The cause is not initialized, and may subsequently be initialized by 
     * a call to 
     * <code>{@link java.lang.Throwable#initCause(java.lang.Throwable) initCause(java.lang.Throwable)}</code>.
     */
    public  RandomVariableException() {
	super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by 
     * a call to 
     * <code>{@link java.lang.Throwable#initCause(java.lang.Throwable) initCause(java.lang.Throwable)}</code>
     * @param message the detail message
     */
    public  RandomVariableException(String message) {
	super(message);
    }
    
    /**
     * Constructs a new runtime exception with the specified detail
     * message and cause.  Note that the detail message associated
     * with cause is not automatically incorporated in this runtime
     * exception's detail message.
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the 
     *        {@link java.lang.Throwable#getCause()  <code>getCause()</code>}
     *        method)(A null value is permitted, and indicates that the 
     *        cause is nonexistent or unknown)
     */
    public  RandomVariableException (String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a 
     * detail message of <code>(cause==null ? null : cause.toString())</code>
     * (which typically contains the class and detail message of cause). 
     * This constructor is useful for runtime exceptions that are
     * little more than wrappers for other throwables.
     * @param cause the cause (which is saved for later retrieval by the 
     *        {@link java.lang.Throwable#getCause()  <code>getCause()</code>}
     *        method)(A null value is permitted, and indicates that the 
     *        cause is nonexistent or unknown)
     */
    public  RandomVariableException(Throwable cause) {
	super(cause);
    }
}

//  LocalWords:  RuntimeException Javadoc runtime initCause getCause
//  LocalWords:  throwables
