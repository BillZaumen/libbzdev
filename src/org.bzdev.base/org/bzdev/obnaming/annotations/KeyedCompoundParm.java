package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;

/**
 * Annotation for keyed parameters.
 * The object annotated must be a field whose type implements the
 * interface java.util.Map. with both type parameters provided.  The
 * type of the map's key must be the type of a named object, an
 * Integer, a String, or an Enum (with a type parameter). The type of
 * a value must be a class annotated with a {@literal @}CompoundParmType
 * annotation.
 * This class must have a constructor with zero arguments and will
 * have some or all of its fields annotated by a {@literal @}PrimitiveParm
 * annotation.
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
 *    public class OurFactory<Obj extends NamedObject1> extends ... {
 *       {@literal @}KeyedCompoundParm("map")
 *       Map&lt;OurNamedObject,MapValue&gt; hashmap = new HashMap&lt;&gt;();
 *       ...
 *    }
 * </pre></blockquote>
 * This will define factory parameters whose names are "map",
 * "map.size" and "map.base". The parameters "map.size" and "map.base"
 * will be initialized using a method named set that has three
 * arguments: the parameter name, the key (of type OurNamedObject in
 * this case), and a value (an int in this case). A delimiter is
 * provided, that will be inserted after the string "map" in the name,
 * with a default delimiter ".".  If no delimiter is desired, set the
 * delimiter to an empty string. For the parameter whose name is "map",
 * the add method will create default entry for the keys, the remove
 * method will allow all entries associated with a key to be removed,
 * and the clear method with the parameter name will clear the table.
 * For example,
 * <blockquote><pre>
 *    factory.set("object.base", 1, 20);
 *    factory.add("object", 2);
 *    factory.set("object.base", 3, 30);
 * </pre></blockquote>
 * will create the entries show in the following table:
 * <table>
 *  <tr><th>key</th><th>object.size</th><th>object.base</th></tr>
 *  <tr><td>1</td><td>10</td><td>20</td></tr>
 *  <tr><td>2</td><td>10</td><td>0</td></tr>
 *  <tr><td>3</td><td>10</td><td>30</td></tr>
 * </table>
 * Calling
 * <blockquote><pre>
 *    factory.remove("object", 1);
 * </pre></blockquote>
 * will remove the values whose key is 1. Calling
 * <blockquote><pre>
 *    factory.clear("object")
 * </pre></blockquote>
 * will remove all the keys associated with "object".
 * <P>
 * If a subclass of a factory defines the same keyed compound parameter,
 * and the ParmManager defined is used in each factory's constructor,
 * the primitive values set are the ones specified by both.
 * For example,
 * <blockquote><pre>
 *    {@literal @}CompoundParmType
 *    class MapValue2 {
 *       {@literal @}PrimitiveParm("size2")
 *       int size2 = 100;
 *       {@literal @}PrimitiveParm("base2")
 *       int base2 = 200;
 *    }
 *
 *    {@literal @}FactoryParmManager("OurFactory2ParmManager")
 *    public class OurFactory2<Obj extends NamedObject2>
 *       extends OurFactory<Obj>
 *    {
 *       {@literal @}KeyedCompoundParm("map")
 *       Map&lt;OurNamedObject,MapValue2&gt; hashmap2 = new HashMap&lt;&gt;();
 *       ...
 *    }
 * </pre></blockquote>
 * will provide a factory with compound keys "object.size", "object.base",
 * "object.size2", and "object.base2". If we set one entry for a key
 * the remaining entries will be set to their default values. Thus, if
 * the object <code>factory</code> is an instance of <code>OurFactory2</code>
 * <blockquote><pre>
 *    factory.set("object.base", 1, 20);
 *    factory.add("object", 2);
 *    factory.set("object.base", 3, 30);
 * </pre></blockquote>
 * will result in the values in the following table:
 * <table>
 *  <tr><th>key</th><th>object.size</th><th>object.base</th>
 *       <th>object.size2</th><th>object.base2</th></tr>
 *  <tr><td>1</td><td>10</td><td>20<td><td>100</td><td>200</td></tr4>
 *  <tr><td>2</td><td>10</td><td>0<td><td>100</td><td>200</td></tr>
 *  <tr><td>3</td><td>10</td><td>30<td><td>100</td><td>200</td></tr>
 * </table>
 * Similarly, the use of "remove" or "clear" will eliminate the same
 * keys as in the previous example.
 * @see org.bzdev.obnaming.annotations.PrimitiveParm
 * @see org.bzdev.obnaming.annotations.CompoundParm
 * @see org.bzdev.obnaming.annotations.FactoryParmManager
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface KeyedCompoundParm {
    /**
     * The name of the key.
     */
    String value();
    /**
     * The delimiter separating the key's name from one of its value's
     * names. The delimiter must not be "."
     */
    String delimiter() default ".";
}

//  LocalWords:  Enum CompoundParmType PrimitiveParm blockquote pre
//  LocalWords:  MapValue FactoryParmManager OurFactoryParmManager lt
//  LocalWords:  OurFactory NamedObject KeyedCompoundParm hashmap td
//  LocalWords:  OurNamedObject HashMap ParmManager
