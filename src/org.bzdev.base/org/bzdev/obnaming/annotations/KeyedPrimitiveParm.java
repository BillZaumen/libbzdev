package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;

/**
 * Annotation for keyed parameters.  The object annotated must be a
 * field whose type implements the interface java.util.Map. with both
 * type parameters provided.  The type of the map's key must be the
 * type of a named object, an Integer, a String, or an
 * enumeration. The map's value is treated the same as the fields
 * for a {@link PrimitiveParm} annotation.
 * <P>
 * When the type of the map's value is a Set, the set's type parameter
 * can be a named-object type, an Integer, a String, or an
 * Enumeration.  In this case the factory is configured by using 'add'
 * methods (with three argument), which add entries to the set corresponding
 * to a key.
 *<P>
 * Otherwise the factory is configure using three-argument set methods, and
 * the type of a map's value is dependent on the value of the
 * rvmode() element. When rvmode() is false, the type of the map's value must
 * be either Boolean, Integer, Long, Double, String, an enumeration,
 * the class of a named object,
 * {@link org.bzdev.math.rv.BooleanRandomVariable},
 * {@link org.bzdev.math.rv.IntegerRandomVariable},
 * {@link org.bzdev.math.rv.LongRandomVariable}, or
 * {@link org.bzdev.math.rv.DoubleRandomVariable}.
 * When rvmode() is true, the type of the map's value must be either
 * {@link org.bzdev.math.rv.BooleanRandomVariable},
 *{@link org.bzdev.math.rv.IntegerRandomVariable},
 * {@link org.bzdev.math.rv.LongRandomVariable},
 * {@link org.bzdev.math.rv.DoubleRandomVariable},
 * {@link org.bzdev.math.rv.BooleanRandomVariableRV},
 * {@link org.bzdev.math.rv.IntegerRandomVariableRV},
 * {@link org.bzdev.math.rv.LongRandomVariableRV}, or
 * {@link org.bzdev.math.rv.DoubleRandomVariableRV}.
 * <P> 
 * Please see {@link KeyedPrimitiveParm#rvmode()} for a more detailed
 * description of the rvmode() element.
 *<P>
 * For example,
 * <blockquote><pre>
 *    {@literal @}FactoryParmManager("OurFactoryParmManager")
 *    public class OurFactory extends ... {
 *       {@literal @}KeyedPrimitiveParm("map")
 *       Map&lt;OurNamedObject,Double&gt; hashmap = new HashMap&lt;&gt;();
 *       ...
 *    }
 * </pre></blockquote>

 * This will define factory parameters whose name is "map", whose key
 * is an instance of OurNamedObject, and whose value is a
 * double-precision number. The following example shows the usage for
 * an instance <CODE>factory</CODE> of a class using this annotation:
 * <blockquote><pre>
 *     factory.set("map", ourNamedObject, 20.0);
 * </pre></blockquote>
 * or
 * <blockquote><pre>
 *     factory.set("map", "ourObject", 20.0);
 * </pre></blockquote>
 * where ourNamedObject is an instance of OurNamedObject with the
 * name "ourObject".
 * @see org.bzdev.obnaming.annotations.FactoryParmManager
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface KeyedPrimitiveParm {
    /**
     * The name of the parameter.
     */
    String value();
    /**
     * The parameter's random-variable mode. The object being annotated
     * is a map and the rvmode() element applies to that map's value,
     * not its key:
     * <UL>
     *   <LI> When rvmode() is false, the type of the parameter's
     *        value is the type of the map's value.
     *   <LI> When rvmode() is true, the map's value must be a random
     *        variable, and the type of this value is based on the
     *        type that the will be used as the third argument in the
     *        factory's corresponding 'set' method. When the expected type
     *        for the third argument for the 'set' method is one of
     *        the primitive types int, double, long, or boolean then
     *        the type of the value field for the map must be a
     *        subclass of IntegerRandomVariable, DoubleRandomVariable,
     *        LongRandomVariable, or BooleanRandomVariable
     *        respectively. Similarly, when the expected type for the
     *        third argument in the factory's corresponding 'set'
     *        method is the random-variable types
     *        IntegerRandomVariable, DoubleRandomVariable,
     *        LongRandomVariable, or BooleanRandomVariable, then the
     *        type of the value field for the map must be a
     *        IntegerRandomVariableRV, DoubleRandomVariableRV,
     *        LongRandomVariableRV, or BooleanRandomVariableRV respectively.
     * </UL>
     * <P>
     * From a user's viewpoint,
     * <UL>
     *  <LI> When rvmode() is false, the value is simply used as is,
     *       perhaps with some type conversions. For example, if the
     *       field is a random variable, a number will be converted to
     *       a random variable that repeats the same value, but in either
     *       case the field's value (a random number) will be used t
     *       configure the object, typically by passing the random number
     *       to the object durint the object's initialization.
     *  <LI> When rvmode() is true, for each random number, the factory
     *       will obtain its' next value and use that to configure an
     *       object.  This is useful primarily with factory methods
     *       that create arrays of random variables, where one wants
     *       each object to have a different value (the factory must
     *       be explicitly coded to call the random variable's next()
     *       method to do this - this behavior does not occur automatically).
     * </UL>
     */
    boolean rvmode() default false;
    /**
     * The upper bound for the the map's value; the string "null" if
     * there is none.
     * It is ignored for types that are not numbers or random variables
     * that generate numbers.
     */
    String upperBound() default "null";
    /**
     * A boolean that is true if the upper bound is included
     * in the map's value's range; false if it is not.
     */
    boolean upperBoundClosed() default true;
    /**
     * The lower bound for the map's value; the string "null" if
     * there is none.
     * It is ignored for types that are not numbers or random variables
     * that generate numbers.
     */
    String lowerBound() default "null";
    /**
     * A boolean that is true if the lower bound is included
     * in the map's value's range; false if it is not.
     * <P>
     * It is ignored for types that are not numbers or random variables
     * that generate numbers.
     */
    boolean lowerBoundClosed() default true;
}

//  LocalWords:  rvmode blockquote pre FactoryParmManager OurFactory
//  LocalWords:  OurFactoryParmManager KeyedPrimitiveParm lt hashmap
//  LocalWords:  OurNamedObject HashMap boolean IntegerRandomVariable
//  LocalWords:  DoubleRandomVariable LongRandomVariable variable's
//  LocalWords:  BooleanRandomVariable
