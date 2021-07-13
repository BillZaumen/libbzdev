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
    String helper() default "";
    String baseType() default "";
    String[] baseArgType() default {};
    String[] baseThrowables() default {};
    String baseReturnType() default "";
    int[] baseOrder() default {};
    boolean baseIsVarArgs() default false;
    DMethodOptions.Locking baseLockingMode() default
	DMethodOptions.Locking.MUTEX;
    boolean traceMode() default false;
    int limitFactor() default 1;
}

//  LocalWords:  RUNTIME
