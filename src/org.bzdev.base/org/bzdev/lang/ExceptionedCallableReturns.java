package org.bzdev.lang;

/**
 * Interface for executing code that returns a value.
 * An ExceptionedCallableReturns object can be constructed inside an
 * object's methods so that otherwise hidden fields and methods are
 * accessible.  For example:
 * <pre><code>
 *  Map&lt;String, ExceptionedCallableReturns&lt;Integer&gt;&gt; table = ...;
 *  void foo(final int i) {
 *     CallableReturns&lt;Integer&gt; callable
 *       = new ExceptionedCallableReturns&lt;Integer&gt;() {
 *                 public Integer call() throws Exception {
 *                     if (i &lt; 0) throw new Exception("negative value");
 *                     return Integer.valueOf(i);
 *                 }
 *             };
 *     table.put("foo", callable);
 * }
 *
 * void bar() {
 *     try {
 *         System.out.println(table.get("foo").call());
 *     } catch (Exception e) {
 *         e.printStackTrace();
 *         System.exit(1);
 *     }
 * }
 * </code></pre>
 */
@FunctionalInterface
public interface ExceptionedCallableReturns<T> {
    /**
     * The method to call.
     * @return an object of type T providing the results of the call
     * @exception Exception an exception occurred
     */
    T call() throws Exception;
}

//  LocalWords:  ExceptionedCallableReturns pre lt CallableReturns
//  LocalWords:  valueOf printStackTrace
