package org.bzdev.lang;

/**
 * Sometimes an exception may have to be caught but logically
 * can never occur (e.g., an IO exception in cases where an
 * IO stream has not been provided).  An UnexpectedExceptionError
 * can be thrown when handling this case, to indicate that a
 * logical error occurred in the program, but generally to
 * document that an exception should never occur.
 * <P>
 * Note: quite a few classes in the standard java class libraries
 * throw an InternalError when soemthing should never happen. The
 * problem with using an InternalError for this case is that the
 * documentation for InternalError states that it is thrown when
 * there is an error in the Java virtual machine. But the Java
 * virtual machine does not include the Java class libraries as
 * part of it.
 */
public class UnexpectedExceptionError extends Error {
    /**
     * Constructor.
     */
    public UnexpectedExceptionError() {
	super();
    }

    /**
     * Constructor giving a cause for the exception.
     * @param cause a throwable providing a cause for the error
     */
    public UnexpectedExceptionError(Throwable cause) {
	super(cause);
    }
}

//  LocalWords:  UnexpectedExceptionError
