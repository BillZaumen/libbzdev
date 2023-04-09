package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;

/**
 * Annotation for compound parameters.
 * The object annotated must be a field whose type matches a class
 * that has a CompoundParmType annotation.
 * This class must have a constructor with zero arguments and will
 * have some or all of its fields annotated by a PrimitiveParm
 * annotation.  An instance created using a zero-argument constructor
 * must be used in the initializer, which is mandatory. The initial
 * value may use a subclass.
 * For example,
 * <blockquote><pre>
 *    {@literal @}CompoundParmType
 *    class Value {
 *       {@literal @}PrimitiveParm("Size")
 *       int size = 10;
 *       {@literal @}PrimitiveParm("Base")
 *       int base = 0;
 *    }
 *
 *    {@literal @}FactoryParmManager("OurFactoryParmManager")
 *    public class OurFactory extends ... {
 *       {@literal @}CompoundParm("value")
 *       Value value = new Value();
 *       ...
 *    }
 * </pre></blockquote>
 * This will define factory parameters whose names are "value.size" and
 * "value.base" that will be initialized using a method named set. A 
 *  delimiter is provided, that will be inserted after
 * the string "map" in the name, with a default delimiter ".".  If no
 * delimiter is desired, set the delimiter to an empty string.
 * Thus, to set a value, one might use
 * <blockquote><pre>
 *   OurFactory factory;
 *   ...
 *   factory.set("value.base", 10);
 *   factory.set("value.size", 20);
 * </pre></blockquote>
 * <P>
 * As an example of using a subclass in an initializer
 * <blockquote><pre>
 *  {@literal @}CompoundParm("edgeColor")
 *  ColorParm edgeColor = new ColorParm.RED();
 * </pre></blockquote>
 * will configure edgeColor so its default value produces the
 * color red. By contrast, the definition
 * <blockquote><pre>
 *  {@literal @}CompoundParm("edgeColor")
 *  ColorParm edgeColor = new ColorParm(Color.RED);
 * </pre></blockquote>
 * will result in the  color provided by ColorParm's zero-argument
 * constructor, which is not what is desired: whenever edgeColor is
 * reset to its default, the value's constructor will be called.
 * @see org.bzdev.obnaming.annotations.PrimitiveParm
 * @see org.bzdev.obnaming.annotations.KeyedPrimitiveParm
 * @see org.bzdev.obnaming.annotations.CompoundParmType
 * @see org.bzdev.obnaming.annotations.KeyedCompoundParm
 * @see org.bzdev.obnaming.annotations.FactoryParmManager
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface CompoundParm {
    /**
     * The name of the key.
     * @return the key name
     */
    String value();
    /**
     * The delimiter separating the key's name from one of its value's
     * names. The delimiter must not be "."
     * @return the delimiter
     */
    String delimiter() default ".";
}

//  LocalWords:  CompoundParmType PrimitiveParm initializer pre
//  LocalWords:  blockquote FactoryParmManager OurFactoryParmManager
//  LocalWords:  OurFactory CompoundParm edgeColor ColorParm
//  LocalWords:  ColorParm's
