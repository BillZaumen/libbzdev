package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;

/**
 * Annotation for creating a named object's helper class.
 * <P>
 * The design pattern used in conjunction with the
 * org.bzdev.obnaming.annotations package to create an object namer class
 * and the corresponding named object class provides implementations
 * of  two interfaces, {@link org.bzdev.obnaming.NamedObjectOps} and
 * {@link org.bzdev.obnaming.ObjectNamerOps}, by creating helper classes
 * that are superclasses of a named-object class and a corresponding
 * object-namer class.
 * <P>
 * <B>The use of this annotation requires a particular design pattern.
 * Please see
 * <A HREF="doc-files/NamedObject.html">the extended description</A>
 * for a detailed description.</B>
 * @see ObjectNamer
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface NamedObject {
    /**
     * Constructor types.
     * Provides information about the types of a constructor's arguments.
     * <P>
     * <B>The use of this annotation requires a particular design pattern.
     * Please see
     * <A HREF="doc-files/NamedObject.html">the extended description</A>
     * for a detailed description.</B>
     */
    public @interface ConstrTypes {
	/**
	 * Types for a constructor.
	 * The value is an array of strings, each providing the
	 * type of a constructor's arguments. The order is the same
	 * as the order of the constructor's arguments.
	 */
	String[] value() default {};
	/**
	 * Exceptions a constructor may throw.
	 * The value is an array of strings, each containing the class
	 * name of an exception.
	 */
	String[] exceptions() default{};
    };

    /**
     * The named-object class' helper class.
     */
    String helperClass();

    /*
     *
    String helperTypeParms() default "";
     */

    /**
     * The superclass of the named-object helper class.
     * This must be a Java class name.
     */
    String helperSuperclass() default "";

    /**
     * Type parameters for the helper superclass.
     * These must be actual types, not parameters.  The value must
     * include the delimiting '&lt;' and '&gt;' characters and the types
     * must be separated by commas.
     */
    String helperSuperclassTypeParms() default "";

    /**
     * Description of superclass constructors.
     * Each array entry contains a list of the types for a helper's 
     * superclass constructor's arguments, in the order in which they
     * appear. An optional member also provides an array of the exceptions
     * that might be thrown. The default provides a constructor with zero
     * arguments, and such a constructor, if desired, should be provided
     * if this argument is used.
     */
    NamedObject.ConstrTypes[] helperSuperclassConstrTypes() default {
	@NamedObject.ConstrTypes()
    };

    /**
     * The name of the object-namer class.
     */
    String namerHelperClass();

    /**
     * The namer class.
     * The named object will have a protected method named getObjectNamer()
     * that returns an instance of this class.
     */
    String namerClass();
}

//  LocalWords:  namer superclasses HREF ObjectNamer helperTypeParms
//  LocalWords:  superclass lt getObjectNamer
