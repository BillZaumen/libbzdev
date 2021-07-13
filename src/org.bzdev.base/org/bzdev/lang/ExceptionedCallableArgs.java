package org.bzdev.lang;

/**
 * Interface for executing code that does not return a value but may throw
 * a checked exception.
 * This Interface is identical to {@link CallableArgs CallableArgs} except for
 * the method {@link #call(Object...) call}() being able to throw a checked
 * exception.
 */
@FunctionalInterface
public interface ExceptionedCallableArgs<T> {
    /**
     * The method to call.
     * @param args additional arguments supplied by the caller when
     *        this method is called
     * @exception an exception was thrown
     */
    @SuppressWarnings("unchecked")
    void call(T... args) throws Exception;
}

//  LocalWords:  CallableArgs args
