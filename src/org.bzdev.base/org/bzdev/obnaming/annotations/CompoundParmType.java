package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;


/**
 * Annotation denoting that a class can be the value parameter of a Map
 * or the type of a variable denoting a group of parameters.
 * The corresponding map will be annotated by the
 * {@literal @}KeyedCompoundParm annotation.
 * For example,
 * <blockquote><pre>
 *    {@literal @}CompoundParmType
 *    class MapValue {
 *       {@literal @}PrimitiveParm("size")
 *       int size = 10;
 *       {@literal @}PrimitiveParm("base")
 *       int base = 0;
 *    }
 *
 *    {@literal @}FactoryParmManager("OurFactoryParmManager")
 *    public class OurFactory extends ... {
 *       {@literal @}KeyedParm("map")
 *       Map&lt;OurNamedObject,MapValue&gt; hashmap = new HashMap&lt;&gt;();
 *       ...
 *    }
 * </pre></blockquote>
 * This will define factory parameters whose names are "map.size" and
 * "map.base" that will be initialized using a method whose name is "set"
 * that has three arguments.
 * <P>
 * Additional elements can be provided for CompoundParmType in order
 * to specify resource bundles used for tips, labels, and parameter
 * documentation for the class being annotated. The resource bundles
 * are defined by property files that use annotation <CODE>value</CODE>
 * elements as keys. Annotations that provide such keys are
 * {@link PrimitiveParm PrimitiveParm},
 * {@link KeyedPrimitiveParm KeyedPrimitiveParm}
 * {@link CompoundParm CompoundParm}, and
 * {@link KeyedCompoundParm KeyedCompoundParm}.
 * <P>
 * For a string associated with a key in a label or tip resource
 * bundle to contain HTML constructs, the string must start with
 * <CODE>&lt;html&gt;</CODE> and end with <CODE>&lt;/html&gt;</CODE>.
 * For the HTML case when the <CODE>lsnof</CODE> program is used,
 * each <CODE>&lt;br&gt;</CODE> element will be replaced
 * with a space for these two resource bundles, and
 * the <CODE>&lt;/html&gt;</CODE> and <CODE>&lt;/html&gt;</CODE>
 * elements will be removed from the start and end of the  string
 * respectively.  Strings associated with parameter-documentation
 * resource bundles are assumed to be HTML fragments with one additional
 * element. These can be
 * inserted as-is into HTML documents and  must be
 * formatted so that they can fit between a <CODE>&lt;DIV&gt;</CODE>
 * element and a matching <CODE>&lt;/DIV&gt;</CODE>. The additional
 * element is named JDOC and its contents use the same convention
 * as the Javadoc {@literal @}link directive. The class name and optional
 * method or field will be turned into a link to the corresponding API
 * documentation.
 * <P>
 * In some cases, the  parameter name consists of those provided by
 * multiple annotations, with a delimiter ("." by default) separating
 * each component.  It is worth
 * noting that the simulation and animation classes in the BZDev class
 * library, when used with a scripting environment, assume that the
 * delimiter will be ".".
 * <P>
 * For example, property files will use the parameter name  "size" and "base"
 * as keys in the annotation shown above.
 * @see org.bzdev.obnaming.annotations.PrimitiveParm
 * @see org.bzdev.obnaming.annotations.KeyedPrimitiveParm
 * @see org.bzdev.obnaming.annotations.CompoundParm
 * @see org.bzdev.obnaming.annotations.KeyedCompoundParm
 * @see org.bzdev.obnaming.annotations.FactoryParmManager
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CompoundParmType {
    /**
     * The class name for a resource bundle for tips.
     * The name must be either a simple name, a qualified class name
     * starting with "*." or a fully-qualified name whose package
     * component either matches the package of the class being
     * annotated or starts with the package of the class being
     * annotated but followed by a package component named "lpack".
     * The '*." form is meant to suggest a wildcard in which the '*'
     * will be replaced with the package name of the class being annotated.
     * If no value is provided or the value is an empty string, a tip
     * resource bundle is not configured.
     * @return the class name
     */
    String tipResourceBundle() default "";

    /**
     * The class name for a resource bundle for labels.
     * The name must be either a simple name, a qualified class name
     * starting with "*." or a fully-qualified name whose package
     * component either matches the package of the class being
     * annotated or starts with the package of the class being
     * annotated but followed by a package component named "lpack".
     * The '*." form is meant to suggest a wildcard in which the '*'
     * will be replaced with the package name of the class being annotated.
     * If no value is provided or the value is an empty string, a label
     * resource bundle is not configured.
     * @return the class name
     */
    String labelResourceBundle() default "";

    /**
     * The class name for a resource bundle for parameter documentation.
     * The name must be either a simple name, a qualified class name
     * starting with "*." or a fully-qualified name whose package
     * component either matches the package of the class being
     * annotated or starts with the package of the class being
     * annotated but followed by a package component named "lpack".
     * The '*." form is meant to suggest a wildcard in which the '*'
     * will be replaced with the package name of the class being annotated.
     * If no value is provided or the value is an empty string, a label
     * resource bundle is not configured.
     * @return the class name
     */
    String docResourceBundle() default "";
}

//  LocalWords:  KeyedCompoundParm blockquote pre CompoundParmType lt
//  LocalWords:  MapValue PrimitiveParm FactoryParmManager OurFactory
//  LocalWords:  OurFactoryParmManager KeyedParm OurNamedObject html
//  LocalWords:  hashmap HashMap KeyedPrimitiveParm CompoundParm br
//  LocalWords:  lsnof BZDev lpack wildcard
