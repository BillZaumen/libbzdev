package org.bzdev.lang.annotations;
import java.lang.annotation.*;

/**
 * Dynamic method implementation annotation.
 * This annotation marks methods that implement a dynamic method
 * for a specific argument type, which must be a subclass of the
 * argument type for the matching <code>&#064;DynamicMethod</code>
 * annotation. The class that this member is a part of must
 * be annotated with a <code>&#064;DMethodContext</code> or
 * <code>&#064;DMethodContexts</code> annotation to associate a
 * local-helper class (the source code for this class will be
 * automatically generated) with the top-level  helper class.
 * the local-helper class and the helper class must not be
 * the same class.
 * <p>
 * Unless a class loader that is aware of dynamic methods, such as
 * {@link org.bzdev.lang.DMClassLoader org.bzdev.lang.DMClassLoader}, is
 * used,
 * a class containing a <code>DMethodImpl</code> annotation must include the
 * following initialization code:
 * <pre><code>
 *    static {
 *	LOCALHELPER.register();
 *    }
 * </code></pre>
 * where LOCALHELPER is the name of the local-helper class (the 
 * <code>DMethodImpl</code> annotation's <code>localHelper</code> field's
 * value).
 * For example,
 * <pre><code>
 *    class Foo {
 *        &#064;DynamicMethod("Helper")
 *        void method(Bar arg) {
 *           Helper.getHelper().dispatch(this, arg);
 *        }
 *    }
 *   
 *   class Bar1 extends Bar {...}
 *   class Bar2 extends Bar {...}
 *
 *    &#064;DMethodContext(helper="Helper", localHelper="LocalHelper")
 *    class Foo1 extends Foo {
 *        static {
 *	   LocalHelper.register();
 *        }
 *        ...
 *        &#064;DMethodImpl("Helper")
 *        void doMethod(Bar1 arg) {
 *          ...
 *        }
 *        &#064;DMethodImpl("Helper")
 *        void doMethod(Bar2 arg) {
 *          ...
 *        }
 *    }
 * </code></pre>
 * Note that if (in the example) class <code>Foo</code> contained a
 * <code>DMethodImpl</code> annotation, then <code>Foo</code> would
 * itself require a <code>DMethodContext</code> annotation with a
 * local-helper class different from the helper class.
 * <p>
 * If the method returns a value, the above example becomes
 * <pre><code>
 *    class Foo {
 *        &#064;DynamicMethod("Helper")
 *        void FooBar method(Bar arg) {
 *           Helper.getHelper().dispatch(this, arg);
 *        }
 *    }
 *   
 *   class Bar1 extends Bar {...}
 *   class Bar2 extends Bar {...}
 *
 *    &#064;DMethodContext(helper="Helper", localHelper="LocalHelper")
 *    class Foo1 extends Foo {
 *        static {
 *	   LocalHelper.register();
 *        }
 *        ...
 *        &#064;DMethodImpl("Helper")
 *        FooBar doMethod(Bar1 arg) {
 *          ...
 *        }
 *        &#064;DMethodImpl("Helper")
 *       FooBar doMethod(Bar2 arg) {
 *          ...
 *        }
 *    }
 * </code></pre>
 *
 * @see org.bzdev.lang.annotations.DynamicMethod
 * @see org.bzdev.lang.annotations.DMethodContext
 * @see org.bzdev.lang.annotations.DMethodContexts
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface DMethodImpl {
    /**
     * Class name of the top-level helper class.
     * This class name is used as a key to distinguish various
     * dynamic methods.  It must be a fully qualified name if
     * the helper is not in the current package.
     */
    String value();
}

//  LocalWords:  DynamicMethod DMethodContext DMethodContexts pre arg
//  LocalWords:  DMethodImpl LOCALHELPER localHelper doMethod FooBar
