package org.bzdev.lang.annotations;
// import org.bzdev.lang.annotations.DMethodOptions;
// import org.bzdev.lang.annotations.DMethodOptions.Locking;
import java.lang.annotation.*;

// annotation used to tag helpers with data needed for
// separate compilation.  The RUNTIME retention policy is
// used because we look up the annotation from the class
// file to support separate compilation.

/**
 * Annotation used by the dynamic method implementation.
 * This annotation is public because it has to be
 * used by multiple packages, but is intended for internal
 * use only (to support separate compilation).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DMethodHelperInfo {
    /**
     * Private element.
     * @return the value of this element
     */
    String helper() default "";
    /**
     * Private element.
     * @return the value of this element
     */
    String baseType() default "";
    /**
     * Private element.
     * @return the value of this element
     */
    String[] baseArgType() default {};
    /**
     * Private element.
     * @return the value of this element
     */
    String[] baseThrowables() default {};
    /**
     * Private element.
     * @return the value of this element
     */
    String baseReturnType() default "";
    /**
     * Private element.
     * @return the value of this element
     */
    int[] baseOrder() default {};
    /**
     * Private element.
     * @return the value of this element
     */
    boolean baseIsVarArgs() default false;
    /**
     * Private element.
     * @return the value of this element
     */
    DMethodOptions.Locking baseLockingMode() default
	DMethodOptions.Locking.MUTEX;
    /**
     * Private element.
     * @return the value of this element
     */
    boolean traceMode() default false;
    /**
     * Private element.
     * @return the value of this element
     */
    int limitFactor() default 1;
}

//  LocalWords:  RUNTIME
