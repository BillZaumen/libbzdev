package org.bzdev.obnaming;

/**
 * Specify the type of a compound key.
 * This class is used to represent the type of a compound key by providing
 * the types of its components. When a key is represented as a
 * <code>String</code>, the subkeys are separated by
 * a period, with each component having a type constraint.  Otherwise
 * the keys are represented as an array of objects.  The string
 * representation is handled automatically by the <code>set</code>,
 * <code>add</code>,<code>unset</code>, and <code>remove</code> methods
 * provided by named-object factories. These methods convert the string
 * to an appropriate array.
 * <P>
 * A ParmKeyType specifies the types of the objects in the array
 * representation. A ParmKeyType can also be declared to be addable
 * (this flag is specified in a constructor). When a ParmKeyType is
 * addable, one of a factory's <code>add</code> methods can be used
 * with this key, as can the factory's <code>remove</code> methods,
 * but the <code>set</code> and <code>unset</code> methods cannot be
 * used.
 */
public class ParmKeyType {
    Class<?>[] classArray;
    boolean addable;
    /**
     * Constructor.
     * @param classArray an array of classes, each naming the type of
     *        a component of a key
     */
    public ParmKeyType(Class<?>[] classArray) {
	this.classArray = classArray;
	this.addable = true;
    }
	
    /**
     * Constructor with an addable flag.
     * @param classArray an array of classes, each naming the type of
     *        a component of a key
     * @param addable true if the key can be added; false
     *                 otherwise.
     */
    public ParmKeyType(Class<?>[] classArray, boolean addable) {
	this.classArray = classArray;
	this.addable = addable;
    }

    /**
     * Get an array of the ParmKeyType components
     * @return an array of components listed in order
     */
    public Class<?>[] getComponents() {
	return (Class<?>[])(classArray.clone());
    }

    /**
     * Determine of a ParmKeyType is addable.
     * A ParmKeyType is addable if one of a factory's <code>add</code>
     * methods can be used. Addable entries can be used in some cases
     * to add a default entry to a keyed table or to represent a set
     * of objects.
     * @return true if this ParmKeyType is addable; false if it is not addable.
     */
    public boolean isAddable() {
	return addable;
    }

    /**
     * Describe a ParmKeyType.
     * If there is only one component, that component's
     * fully qualified class name is returned.  Otherwise, the class
     * name of each component is delimited by angle brackets ("&lt;" and
     * "&gt;"), with each component separated by a period.
     * @return a description listing the components of a ParmKeyType
     */
    public String description() {
	int i;
	if (classArray.length == 0) return "";
	String result = "";
	if (classArray.length == 1) {
	    return classArray[0].getName();
	}
	for (i = 0; i < classArray.length; i++) {
	    result = result + ((i==0)?"":".") 
		+ "<" + classArray[i].getName() +">";
	}
	return result;
    }
}

//  LocalWords:  subkeys ParmKeyType classArray lt
