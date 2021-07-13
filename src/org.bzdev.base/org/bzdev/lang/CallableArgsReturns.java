package org.bzdev.lang;

/**
 * Interface for executing code with arguments that returns a value.
 * A CallableArgsReturns object can be constructed inside an object's methods
 * so that otherwise hidden fields and methods are
 * accessible.  For example
 * <pre><code>
 *  Map&lt;String, CallableArgsReturns&lt;Integer,Integer&gt;&gt; table = ...;
 *  void foo(final int i) {
 *     CallableArgsReturns&lt;Integer&gt; callable
 *       = new CallableArgsReturns&lt;Integer,Integer&gt;() {
 *                 public Integer call(Integer... args) {
 *                     int j = (args.length == 0)? 0: args[0];
 *                     return Integer.valueOf(i + j);
 *                 }
 *             };
 *     table.put("foo", callable);
 * }
 *
 * void bar(int j) {
 *     System.out.println(table.get("foo").call(Integer.valueOf(j)));
 *  }
 * </code></pre>
 */
@FunctionalInterface
public interface CallableArgsReturns<T,Arg> {
    /**
     * The method to call.
     * @param args additional arguments supplied by the caller when
     *        this method is called
     * @return an object of type T providing the results of the call
     */
    @SuppressWarnings("unchecked")
    T call(Arg... args);
}

//  LocalWords:  CallableArgsReturns pre lt args valueOf
