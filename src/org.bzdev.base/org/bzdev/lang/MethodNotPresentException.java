package org.bzdev.lang;

/**
 * Thrown when a dynamic method invocation failed to find a method
 * implementing the dynamic method.
 * One should either define a method whose signature matches that of
 * the dynamic method (in which case this exception can never be thrown)
 * or catch this exception and handle it explicitly in the method tagged
 * with the &#064;DynamicMethod annotation, unless the dynamic method's
 * 'throws' clause contains java.lang.MethodNotPresentException.
 * <p>
 * Note: a programming error that can cause this exception to be
 * thrown is the failure to call a local helper's
 * <code>register()</code> method when a class associated with that 
 * local helper is loaded.  Calling the <code>register()</code> method
 * is required because Java does not load classes until needed.
 * <P>
 * Normally instances of this method should not be created by users
 * of this class library: this exception is thrown by code generated
 * by an annotation processor.
 */
public class MethodNotPresentException extends RuntimeException {
    /**
     * Constructor.
     */
    public MethodNotPresentException() {}
}

//  LocalWords:  DynamicMethod
