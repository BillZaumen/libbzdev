package org.bzdev.lang;

/**
 * Interface for executing code that returns a value and that may raise a
 * checked exception.
 *
 * An ExceptionedCallableArgsReturns object can be constructed inside an
 * object's methods so that otherwise hidden fields and methods are
 * accessible.  For example:
 * <pre><code>
 *  Map&lt;String, ExceptionedCallableArgsReturns&lt;Integer,Integer&gt;&gt;
 *        table = ...;
 *  void foo(final int i) {
 *     CallableArgsReturns&lt;Integer,Integer&gt; callable
 *       = new ExceptionedCallableArgsReturns&lt;Integer,Integer&gt;() {
 *                 public Integer call(Integer... args) throws Exception {
 *                     if (i &lt; 0) throw new Exception("negative value");
 *                     int j = (args.length == 0)? 0: (args[0]);
 *                     return Integer.valueOf(i + j);
 *                 }
 *             };
 *     table.put("foo", callable);
 * }
 *
 * void bar(int j) {
 *     try {
 *         System.out.println(table.get("foo").call(Integer.valueOf(j)));
 *     } catch (Exception e) {
 *         e.printStackTrace();
 *         System.exit(1);
 *     }
 * }
 * </code></pre>
 */
@FunctionalInterface
public interface ExceptionedCallableArgsReturns<T,Args> {
    /**
     * The method to call.
     * @param args additional arguments supplied by the caller when
     *        this method is called
     * @return an object of type T providing the results of the call
     * @exception Exception an exception occurred
     */
    @SuppressWarnings("unchecked")
    T call(Args... args) throws Exception;
}

//  LocalWords:  ExceptionedCallableArgsReturns pre lt args valueOf
//  LocalWords:  CallableArgsReturns printStackTrace
