package org.bzdev.lang;

/**
 * Interface for executing code that does not return a value but may throw
 * a checked exception.
 * This Interface is identical to {@link Callable Callable} except for
 * the method {@link #call() call}() being able to throw a checked
 * exception
 */
@FunctionalInterface
public interface ExceptionedCallable {
    /**
     * The method to call.
     * @exception Exception an exception was thrown
     */
    void call() throws Exception;
}
