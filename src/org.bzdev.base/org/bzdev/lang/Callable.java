package org.bzdev.lang;

/**
 * Interface for executing code that does not return a value.
 * Similar to Runnable, but not used to program threads.
 * A Callable can be constructed inside an object's methods
 * so that otherwise hidden fields and methods are
 * accessible.  For example:
 * <pre><code>
 *  Map&lt;String, Callable&gt; table = ...;
 *  void foo() {
 *     final int i = 10;
 *     Callable callable = new Callable() {
 *         public void call() {
 *           System.out.println("i = " +i);
 *         }
 *       };
 *     table.put("foo", callable);
 * }
 *
 * void bar() {
 *     table.get("foo").call();
 *  }
 * </code></pre>
 * <p>
 * Applications can be coded so that the Runnable interface is
 * used when code encapsulated by a Runnable is to be run in its
 * own thread and a Callable can be used when the code that was
 * encapsulated is executed in the caller's thread.
 */
@FunctionalInterface
public interface Callable {
    /**
     * The method to call.
     */
    void call();
}

//  LocalWords:  Runnable pre lt
