package org.bzdev.lang;

/**
 * Interface for executing code with arguments that does not return a value.
 * Similar to Runnable, but not used to program threads.
 * A Callable can be constructed inside an object's methods
 * so that otherwise hidden fields and methods are
 * accessible.  For example:
 * <pre><code>
 *  Map&lt;String, Callable&gt; table = ...;
 *  void foo() {
 *     final int i = 10;
 *     Callable callable = new Callable() {
 *         public void call(Object... args) {
 *           int j = (args.length == 0)? 0: (Integer)args[0];
 *           System.out.println("i = " + i + ", j = " + j);
 *         }
 *       };
 *     table.put("foo", callable);
 * }
 *
 * void bar(int j) {
 *     table.get("foo").call(Integer.valueOf(j));
 *  }
 * </code></pre>
 * <p>
 * Applications can be coded so that the Runnable interface is
 * used when code encapsulated by a Runnable is to be run in its
 * own thread and a Callable can be used when the code that was
 * encapsulated is executed in the caller's thread.
 *        
 */
@FunctionalInterface
public interface CallableArgs<T> {
    /**
     * The method to call.
     * @param args additional arguments supplied by the caller when
     *        this method is called
     */
    @SuppressWarnings("unchecked")
    void call(T... args);
}

//  LocalWords:  Runnable pre lt args
