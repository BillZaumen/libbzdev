package org.bzdev.lang.annotations;
import java.lang.annotation.*;

/**
 * Options for dynamic methods.
 * A method annotated with a DynamicMethod annotation may
 * also be annotated with a DMethodOptions annotation. This
 * annotation set various options that affect performance.
 */
@Retention(RetentionPolicy.SOURCE)
/**
 * Dynamic method options.  
 * This annotation should be used only on methods annotated with the
 * <code>DynamicMethod</code> annotation; otherwise it has no effect.
 * It makes it possible to specify options that affect performance.
 */
@Target(ElementType.METHOD)
public @interface DMethodOptions {
    /**
     * Enumeration of locking modes for dynamic methods.
     * The implementation of dynamic methods uses a table that
     * maps argument types to code that will be called to perform an
     * operation.  Because Java loads classes when needed, the table
     * may be read or modified concurrently (modifications involve
     * adding new entries).  The type of locking used can be set
     * at compile time.  This enumeration specifies the choices.
     */
    public enum Locking {
	/**
	 * No locking is required.  This is appropriate for use in an
	 * application that is single-threaded, or when one can ensure
	 * that all uses of dynamic methods associated with a class or
	 * interface occur in the same thread until all classes used in
	 * the application and that implement those dynamic methods are
	 * initialized.
	 */
	NONE,
	/**
	 * The default locking mode is MUTEX but may be overridden via
	 * a compiler option.
	 */
	DEFAULT,
	/**
	 * A mutual-exclusion lock is used for locking.
	 */
        MUTEX,
	/**
	 * A read-write lock is used. This is more expensive than a mutual
	 * exclusion lock but allows more concurrency.  It may be useful
	 * in cases where lock contention is very high.
	 */
	RWLOCK
    }
    /**
     * Set the type of locking used by the helper class.
     * Locking protects data structures used when a dynamic
     * method is run and when a dynamic method's class (and that
     * class' subclasses) are loaded.  Java loads classes immediately
     * before those classes are used.
     * Values are 
     * <ul>
     *   <li> <code>DMethodOptions.Locking.NONE</code> (no locking)
     *   <li> <code>DMethodOptions.Locking.DEFAULT</code> (use the 
     *        default, typically <code>DMethodOptions.Locking.MUTEX</code>)
     *   <li> <code>DMethodOptions.Locking.MUTEX</code> (use a
     *              mutual-exclusion lock)
     *   <li> <code>DMethodOptions.Locking.RWLOCK</code> (use a 
     *        read-write lock)
     * </ul>
     * <P>
     * The option <code>DMethodOptions.Locking.DEFAULT</code> picks a
     * system-wide default (<code>DMethodOptions.Locking.MUTEX</code>,
     * unless overridden by compiler flags).  The option
     * <code>DMethodOptions.Locking.NONE</code> should be used when
     * all uses of a dynamic method, including loading the method's
     * class and any subclass that implements the dynamic method, are
     * single threaded.  The option
     * <code>DMethodOptions.Locking.MUTEX</code> (the default) is best
     * in nearly all cases where locking is needed.  The option
     * <code>DMethodOptions.Locking.RWLOCK</code> reduces lock
     * contention in multithreaded applications when a very large
     * number of threads may call the same method concurrently.  The
     * lock is held only during method searching and cache-maintenance
     * operations, so the time a method executes is not relevant.  One
     * should not, however, that method search time grows with both
     * the complexity of the class hierarchy and with the number of
     * arguments searched.
     * <p> 
     * With a trivial dynamic method that has a single argument, a
     * read-write lock will account for about half of the execution
     * time while a mutex will account for about a quarter of the
     * execution time when there is no lock contention.
     * <p>
     * To set the default option for the compiler, the option name
     * is <code>org.bzdev.lang.DMethodOption.lockingMode</code> and 
     * the legal values are <code>NONE</code>, <code>MUTEX</code>,
     * and <code>RWLOCK</code>. For example, to turn off locking as 
     * the default with javac, use
     * <pre><code>
     *    javac -Aorg.bzdev.lang.DMethodOptions.lockingMode=NONE ...  FILE.java
     * </code></pre>
     * @return the value of this element
     */
    Locking lockingMode() default Locking.MUTEX;

    /**
     * Set whether or not to trace a method search.
     * A value of true indicates that a trace of the search for an
     * appropriate set of arguments and implementing class should be
     * printed on standard output when the dynamic method annotated with
     * this annotation is used; false if no tracing should be done.
     * The default is false.
     * <P>
     * This should only be set to true for debugging.
     * @return the value of this element
     */
    boolean traceMode() default false;

    /**
     * Provides a scale factor so that the cache size for a helper
     * will be the scale factor multiplied by the value of
     * {@link org.bzdev.lang.DMethodParameters#getDefaultCacheLimit() DMethodParameters.getDefaultCacheLimit()}.
     * When a search for the appropriate method is needed, the
     * the search results will be cached for later use.  The cache has a
     * finite size.  This option is intended for cases where one expects
     * that the default value will result in an inappropriate cache size
     * for a specific dynamic method. The method
     * {@link org.bzdev.lang.DMethodParameters#setDefaultCacheLimit(int) DMethodParameters.getDefaultCacheLimit(int)}
     * should be used to change the cache size for all dynamic
     * methods. The values should be changed from the default ones
     * only if performance measurements or tracing show that there is
     * a good reason to do that.
     * @return the value of this element
     */
    int limitFactor() default 1;
}

//  LocalWords:  DynamicMethod DMethodOptions MUTEX subclasses ul li
//  LocalWords:  multithreaded mutex RWLOCK javac pre
//  LocalWords:  getDefaultCacheLimit DMethodParameters
//  LocalWords:  setDefaultCacheLimit
