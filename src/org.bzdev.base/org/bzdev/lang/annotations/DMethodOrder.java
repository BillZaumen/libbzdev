package org.bzdev.lang.annotations;
import java.lang.annotation.*;

/**
 * Search order for arguments to dynamic methods.
 * This annotation is applicable only for methods
 * annotated with the <code>DynamicMethod</code>
 * annotation.
 * <p>
 * When there are more than one arguments for a dynamic
 * method, multiple methods may match.  This annotation
 * determines the search order. The first match will be
 * used.  In searching, the implementation starts with
 * most specific class for the object whose method is
 * being executed, and the classes of each argument.
 * If there isn't a match, the class of the last
 * argument in the order list is replaced with its
 * superclass, repeating until either a match is found
 * or a base type (class or interface) is reached. If
 * there is still no match, the next to last argument in
 * the priority list is replaced with its superclass and
 * the last argument in the priority list is set to the
 * class of the last argument.  If there is still no
 * match, the next to last argument's current class is
 * replaced with its superclass, repeating as before until
 * a base class is reached.  If there is still no match,
 * the third form the last argument is varied, following
 * the same pattern until all combinations of superclasses
 * are tried or until a match is found.
 * <p>
 * For example, given the classes
 * <pre><code>
 * public class Foo1 {
 *    int a;
 * }
 * public class Foo2 extends Foo1 {
 *   int b;
 * }
 *
 * public class Foo3 extends Foo2 {
 *   int b;
 * }
 * 
 *
 * public class Bar1 {
 *   int c;
 * }
 * public class Bar2 extends Bar1 {
 *   int d;
 * {
 * public class Bar3 extends Bar2 {
 *   int d;
 * {
 *
 * public class FooBar {
 *    &#064;DynamicMethod("FooBarHelper")
 *    &#064;DMethodOrder({1,2})
 *    public class foobar(Foo1 foo, Bar1 bar) {
 *        FooBarHelper.dispatch(foo, bar);
 *    }
 * }
 *
 * &#064;DMethodContext("FooBarHelper", "FooBarHelper1")
 * public class FooBar1 extends FooBar {
 *
 *   &#064;DMethodImpl("FooBarHelper")
 *   foobarImpl(Foo1 foo, Bar1 bar) {...}
 *
 *   &#064;DMethodImpl("FooBarHelper")
 *   foobarImp(Foo2 foo, Bar2 bar) {...}
 * }

 * </code></pre>
 * Then the order in which class will be searched will be when arguments
 * of type <code>Foo3</code> and <code>Bar3</code> are passed to
 * <code>obj.foobar</code>, where obj is an instance of a subclass of
 * <code>FooBar</code>.
 * <pre><code>
 *     Foo3  Bar3
 *     Foo3  Bar2
 *     Foo3  Bar1
 *     Foo2  Bar3
 *     Foo2  Bar2
 *     Foo2  Bar1
 *     Foo1  Bar3
 *     Foo1  Bar2
 *     Foo1  Bar1
 * </code></pre>
 * with the search stopping at the first match. If there is no match,
 * the search is repeated for the superclass of the object continuing
 * until class <code>FooBar</code> is reached. Arguments that are
 * arrays (including a variable-argument list) or primitive types do
 * not participate in the search and their priority values must be
 * zero or negative.  A zero or negative argument can also be used for
 * arguments whose type is a class or interface, in which case the
 * compile-time type of the argument will be used in determining a match.
 * For example,
 * <pre><code>
 * public class FooBar {
 *    &#064;DynamicMethod("FooBarHelper")
 *    &#064;DMethodOrder({1,0,0,2})
 *    public class foobar(Foo1 foo,int index, Foo[] array, Bar1 bar) {
 *        FooBarHelper.dispatch(foo, bar);
 *    }
 * }
 * </code></pre>
 * In this case, the search for arguments <code>foo</code> and
 * <code>bar</code> occur just as in the previous example, while
 * the arguments <code>index</code> and <code>array</code> have
 * their types determined at compile time.
 *
 * @see org.bzdev.lang.annotations.DynamicMethod
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface DMethodOrder {
    /**
     * A list of argument priorities, starting from 1, denoting the
     * order in which the arguments' superclasses will be searched
     * until there is a match.  If an argument should be ignored, its
     * priority value should be 0 or negative. The argument
     * corresponding to the highest priority value in the array will
     * have its superclass searched first.  The priority value must
     * be zero or negative for arrays and primitive types.
     * @return the value of this element
     */
    int[] value();
}

//  LocalWords:  DynamicMethod superclass superclasses pre FooBar
//  LocalWords:  FooBarHelper DMethodOrder DMethodContext DMethodImpl
//  LocalWords:  foobarImpl foobarImp
