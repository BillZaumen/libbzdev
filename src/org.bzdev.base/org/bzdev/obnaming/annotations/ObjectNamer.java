package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;

/**
 * Annotation for creating an object namer's helper class.
 * <P>
 * The design pattern used in conjunction with the
 * org.bzdev.obnaming.annotations package to create an object namer class
 * and the corresponding named object class provides implementations
 * of  two interfaces, {@link org.bzdev.obnaming.NamedObjectOps} and
 * {@link org.bzdev.obnaming.ObjectNamerOps}, by creating helper classes
 * that are superclasses of a named-object class and a corresponding
 * object-namer class.
 * <P>
 * <B>Please see the
 * <A HREF="doc-files/ObjectNamer.html">extended description</A>
 * for a detailed description of how this design pattern is used.</B>
 * @see NamedObject
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ObjectNamer {
    /**
     * Constructor types.
     * This annotation provides information about the types of a
     * constructor's arguments.
     * <B>Please see the
     * <A HREF="doc-files/ObjectNamer.html">extended description</A>
     * for a detailed description of how this annotation is used.</B>
     */
    public @interface ConstrTypes {
	/**
	 * Types for a constructor.
	 * The value is an array of strings, each providing the
	 * type of a constructor's arguments. The order is the same
	 * as the order of the constructor's arguments.
	 * @return the types
	 */
	String[] value() default {};
	/**
	 * Exceptions a constructor may throw.
	 * The value is an array of strings, each containing the class
	 * name of an exception.
	 * @return the exceptions
	 */
	String[] exceptions() default{};
    };


    /**
     * The name of the helper class.
     * The Object Namer class must extend this class.
     * @return the helper class name
     */
    String helperClass();

    /**
     * The helper class' superclass.
     * This is the class the helper extends, and is an optional argument.
     * If missing, the helper class has no "extends" clause.
     * @return the name of the helper super class
     */
    String helperSuperclass() default "";

    /**
     * Type parameters for the helper's superclass.
     * These must be actual types, not parameters.  The value must
     * include the delimiting '&lt;' and '&gt;' characters and the types
     * must be separated by commas.
     * @return the type parameters for the helper's superclass
     */
    String helperSuperclassTypeParms() default "";

    /**
     * Types for constructors' arguments.
     * Each element in the array is a ConstrTypes annotation that
     * gives the types of one of the helper's superclass' constructors.
     * The helper will have a corresponding number of constructors,
     * with the same arguments. The default provides a constructor with zero
     * arguments, and such a constructor, if desired, should be provided
     * if this argument is used.
     * @return an array of annotations contaiing ConstrTypes annotations
     */
    ObjectNamer.ConstrTypes[] helperSuperclassConstrTypes() default {
	@ObjectNamer.ConstrTypes()
    };

    /**
     * The name of the helper class used by the named-object class.
     * @return the class name
     */
    String objectHelperClass();

    /**
     * The name of the named-object class.
     * @return the class name
     */
    String objectClass();

    /**
     * The path for a resource that will contain a
     * {@link java.util.Properties properties} file in XML format,
     * where the value for each key is a script and the key denotes a
     * scripting language. When present, and when the helper created
     * is a subclass of org.bzdev.scripting.ScriptingContext, The
     * object namer will be able to use a script to configure a
     * factory based on data provided in the scripting language. The
     * script must accept two arguments: the factory to be configured
     * and data in a format determined by the script to specify the
     * configuration. This element is ignored if scripting is not used.
     * @return the path for the specified resource
     * @see java.util.Properties
     */
    String factoryConfigScriptResource() default
	"/org/bzdev/obnaming/FactoryConfigScript.xml";

    /**
     * The path for a resource that will contain a
     * {@link java.util.Properties properties} file in XML format,
     * where the value for each key is a script and the key denotes a
     * scripting language. When present, and when the helper created
     * is a subclass of org.bzdev.scripting.ScriptingContext, The
     * object namer will be able to use a script to create
     * factories based on data provided in the scripting language. The
     * script must accept three arguments: the object namer, the name of
     * the package containing the factories, and an object with properties
     * whose names are the names of the variables to be created and whose
     * values are the name of the factory's class (excluding its package).
     * This element is ignored if scripting is not used.
     * @return the path for the specified resource
     * @see java.util.Properties
     */
    String factoryCreateScriptResource() default
	"/org/bzdev/obnaming/FactoryCreateScript.xml";
}

//  LocalWords:  namer's namer superclasses HREF NamedObject lt
//  LocalWords:  superclass ConstrTypes
