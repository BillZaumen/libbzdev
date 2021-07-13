package org.bzdev.lang;

/**
 * Interface for executing code that returns a value.
 * A CallableReturns object can be constructed inside an object's methods
 * so that otherwise hidden fields and methods are
 * accessible.  For example
 * <pre><code>
 *  Map&lt;String, CallableReturns&lt;Integer&gt;&gt; table = ...;
 *  void foo(final int i) {
 *     CallableReturns&lt;Integer&gt; callable
 *       = new CallableReturns&lt;Integer&gt;() {
 *                 public Integer call() {
 *                     return Integer.valueOf(i);
 *                 }
 *             };
 *     table.put("foo", callable);
 * }
 *
 * void bar() {
 *     System.out.println(table.get("foo").call());
 *  }
 * </code></pre>
 * Note: this class was written independently of the class
 * {@link java.util.concurrent.Callable} and was subsequently
 * modified to extend {@link java.util.concurrent.Callable} for
 * interoperability reasons. It is not being deprecated because
 * the naming convention fits a pattern used in this package.
 *
 */
@FunctionalInterface
public interface CallableReturns<T> extends java.util.concurrent.Callable<T> {
    /**
     * The method to call.
     * @return an object of type T providing the results of the call
     */
    T call();
}

//  LocalWords:  CallableReturns pre lt valueOf
