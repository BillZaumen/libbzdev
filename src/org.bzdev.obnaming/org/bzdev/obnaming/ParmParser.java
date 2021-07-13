package org.bzdev.obnaming;
import org.bzdev.math.rv.*;
import java.util.Iterator;
import java.util.*;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Named-Object Factory Parameter Parser.
 * Each instance of this class may be associated with only a
 * single instance of Parm, the class representing parameters.
 */
public class ParmParser {

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.obnaming.lpack.ParmParser");

    /**
     * Get an error message with standard formats.
     * The formats are in the resource org.bzdev.obnaming.lpack.ParmParser
     * A resource bundle is used so that the values can be localized.
     * The jar file entry is
     * <blockquote>
     * org.bzdev.obnaming.lpack.ParmParser.properties
     * </blockquote>
     * and any locale-related entries.  The keys are:
     * <blockquote>
     * </blockquote>
     * To override this method in order to add additional keys,
     * use code such as the following:
     * <blockquote><code>
     *  static ResourceBundle mybundle =
     *     ResourceBundle.getBundle("...");
     *  {@literal @}Override
     *  protected String errorMsg(String key, Object... args)
     *      throws NullPointerException,
     *             MissingResourceException,
     *             ClassCastException
     *  {
     *      try {
     *          return super.errorMsg(key, args);
     *      } catch (Exception e) {
     *          return String.format(mybundle.getString(key), args);
     *      }
     *  }
     * </code></blockquote>
     * @param key the key naming the message format
     * @param args  the arguments used to format the message
     * @exception NullPointerException if the key is null
     * @exception MissingResourceException if no object for the given
     *            key can be found
     * @exception ClassCastException if the object found for the given
     *            key is not a string
     */
    protected String errorMsg(String key, Object... args)
	throws
	    NullPointerException, MissingResourceException, ClassCastException
    {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    /**
     * Format a key as a string.
     * Keys can be composed of named objects, enums, integers,
     * strings, or long integers. A named object will be represented
     * by the object's name. The other types are represented by using
     * the toString() method for the corresponding object.
     * Subkeys are separated by periods.
     * <P>
     * This method is provided in order provide values for messages
     * associated with exceptions that a parser may throw.
     * @param key the key
     * @return a string representing the key
     */
    protected final String keyString(Object key) {
	if (key instanceof NamedObjectOps) {
	    return ((NamedObjectOps)key).getName();
	} else if (key instanceof Object[]) {
	    Object[] array = (Object[]) key;
	    String result = "";
	    boolean first = true;
	    for (Object k: array) {
		result = result + (first? "": ".") +  keyString(k);
		first = false;
	    }
	    return result;
	} else {
	    return key.toString();
	}
    }


    ParmParser altParmParser = null;

    String parmName = null;
    // used to set the parm name during initialization by a factory
    void setParmName(String name) {
	if (parmName != null) {
	    throw new IllegalStateException
		("There must be only one Parm per ParmParser");
	}
	parmName = name;
    }

    /**
     * Get the name of the parameter associated with this object.
     * This is intended for use in generating messages when throwing
     * exceptions. In particular, when parameters are initialized
     * using scripts, the scripting engine may give a file and line
     * number associated with a statement, not the line matching the
     * parameter being set. For this reason, the exception's message
     * should include the parameter name, and possibly a key, in order
     * to help the user find the error.
     * @return the name of the parameter whose value this ParmParser
     *         sets
     */
    protected final String getParmName() {return parmName;}

    /**
     * Get the alternate Parm.
     * This is used so that a ParmParser has access the parent Parm
     * of the Parm associated with the parser. In some cases associated
     * with a Parm defined via {@literal @}KeyedCompoundParm, a subclass
     * will add a new {@literal @}KeyedCompoundParm with the same value
     * (the parameter name) in order to add additional entries. Having
     * access to the original Parm is necessary for some operations.
     * @return the alternate Parm.
     */
    protected ParmParser getAltParmParser() {
	return altParmParser;
    }

    /**
     * Constructor.
     */
    public ParmParser() {}

    /**
     * Constructor given an alternate Parm.
     * This is provided so that a ParmParser has access to the ParmParser
     * for  the parent Parm of the Parm associated with the parser.
     * This alternate ParmParser is obtained by calling the method
     * {@link #getAltParmParser()}. For a non-null value to be returned
     * by {@link #getAltParmParser()}, there must be an existing parameter
     * with the same name as the name provided in this constructor; the
     * keyType argument must be non-null and must match the key type used
     * by the existing parameter, and the keyType must not be a ParmKeyType.
     * <P>
     * The rationale is that in some cases associated with a Parm defined via
     * {@literal @}KeyedCompoundParm, a subclass will add a new
     * {@literal @}KeyedCompoundParm with the same value
     * (the parameter name) in order to add additional entries. Having
     * access to the original Parm is necessary for some operations such
     * as clearing all subparameters associated with the parameter name.
     * <P>
     * For example, the anim2d package has factories that support a
     * keyed compound parameter whose name is "timeline", where subclasses
     * add their own timeline parameters. one can
     * remove a key for "timeline" to remove all the entries that were
     * defined for a specified key.  For the "timeline" parameter alone,
     * with no specified subparameter, the parser implements the
     * {@link #parse(int)}, {@link #clear()} and {@link #clear(int)}
     * methods ("timeline" has a integer-valued key). Each method
     * cleans up or creates its own entries and then calls
     * {@link #getAltParmParser()}. if {@link #getAltParmParser()} returns
     * a non-null value, the same method is called on that non-null value.
     * A typical code fragment is the following:
     * <blockquote>
     * <code><pre>
     *         public void clear(int key) {
     *           factory.timelineMap.remove(key);
     *           ParmParser altParser = getAltParmParser();
     *           if (altParser != null) {
     *              altParser.clear(key);
     *           }
     *        }
     * </pre></code>
     * </blockquote>
     * Since all subclasses do the same thing, and because each
     * parm parser has its own alternate parm parser, the result
     * is to traverse all the superclasses so that each gets a
     * chance to clear its table for the given key.
     * <P>
     * This constructor is used in code generated by the
     * annotation processor that supports the annotations in
     * the package
     * <a href ="@{docRoot}/org/bzdev/obnaming/annotations/package-summary.html">
     * org.bzdev.obnaming.annotations</a>. The example above
     * is for the benefit of programmers who choose to implement
     * this behavior explicitly.
     * @param factory the factory to which this object belongs
     * @param name the name of the parameter being defined
     * @param keyType the type of the key; null if there is none
     */
    public ParmParser(NamedObjectFactory factory, String name,
		      Class<?> keyType) {
	Parm altParm = factory.getParm(name);
	if (altParm != null) {
	    if (altParm.keyType == keyType && keyType != null
		&& keyType != ParmKeyType.class) {
		altParmParser = altParm.getParser();
	    }
	}
    }


    /**
     * Parse a string, run a validity check on the object created, and
     * store the value.
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(String value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new
	    UnsupportedOperationException(errorMsg("unsupported2", n, cn));
    }

    /**
     * Parse an array, run a validity check, and, store the value.
     * This will be called in response to an 'add' method of a factory.
     * @param value the array to process
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(Object[] value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = "[";
	boolean ft = true;
	for (Object obj: value) {
	    cn = (ft? "": ", ") + obj.getClass().getName();
	}
	cn = cn + "]";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Parse a named object and store the value.
     * @param value the array to process
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(NamedObjectOps value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Parse an instance of an enumeration and store the value.
     * @param value the array to process
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(Enum<?> value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }


    /**
     * Process an int and store it.
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in a
     *            state for which this value may be parsed and entered
     */
    public void parse(int value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = "int";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process an integer-valued random variable and store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in a
     *            state for which this value may be parsed and entered
     */
    public void parse(IntegerRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process an integer-random-variable-valued random variable and
     * store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in a
     *            state for which this value may be parsed and entered
     */
    public void parse(IntegerRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }


    /**
     * Process a long and store it.
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(long value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = "long";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process a long-valued random variable and store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(LongRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process a long-random-variable-valued random variable and store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(LongRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }


    /**
     * Process a double and store it.
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(double value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = "double";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process a double-valued random variable and store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(DoubleRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }


    /**
     * Process a double-random-variable-valued random variable and store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(DoubleRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process a boolean and store it.
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(boolean value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = "boolean";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process a boolean-valued random variable and store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(BooleanRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Process a boolean-random-variable-valued random variable and
     * store it.
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void parse(BooleanRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2", n, cn));
    }

    /**
     * Parse a named object and run a validity check on the object created,
     * storing it in the location specified by an index.
     * @param index the index of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, NamedObjectOps value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse an enumeration and run a validity check on the object created,
     * storing it in the location specified by an index.
     * @param index the index of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, Enum<?>value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a string and run a validity check on the object created,
     * storing it in the location specified by an index.
     * @param index the index of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, String value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an int and store it in the location specified
     * by an index.
     * @param index the index of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, int value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = "int";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-valued random variable and store it in the
     * location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, IntegerRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-random-variable-valued random variable and
     * store it in the location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, IntegerRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a long and store it in the location specified
     * by an index.
     * @param index the index of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, long value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = "long";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-valued random variable and store it in the
     * location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, LongRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-random-variable-valued random variable and
     * store it in the location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, LongRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double and store it in the location specified
     * by an index.
     * @param index the index of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, double value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = "double";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double-valued random variable and store it in the
     * location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, DoubleRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a double-random-variable-valued random variable and
     * store it in the location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, DoubleRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean and store it in the location specified
     * by an index.
     * @param index the index of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, boolean value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = "boolean";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-valued random variable and store it in
     * the location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, BooleanRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-random-variable-valued random variable and
     * store it in the location specified by an index.
     * @param index the index of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(int index, BooleanRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a named object and run a validity check on the object created,
     * storing it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, NamedObjectOps value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse an enumeration and run a validity check on the object created,
     * storing it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, Enum<?>value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a string and run a validity check on the object created,
     * storing it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, String value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an int and store it in the location specified
     * by a named-object key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, int value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "int";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-valued random variable and store it in the
     * location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, IntegerRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-random-variable-valued random variable and
     * store it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, IntegerRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a long and store it in the location specified
     * by a named-object key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, long value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "long";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-valued random variable and store it in the
     * location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, LongRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-random-variable-valued random variable and
     * store it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, LongRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double and store it in the location specified
     * by a named-object key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, double value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "double";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double-valued random variable and store it in the
     * location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, DoubleRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a double-random-variable-valued random variable and
     * store it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, DoubleRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean and store it in the location specified
     * by a named-object key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, boolean value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "boolean";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-valued random variable and store it in
     * the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, BooleanRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-random-variable-valued random variable and
     * store it in the location specified by a named-object key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(NamedObjectOps key, BooleanRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a named object and run a validity check on the object created,
     * storing it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, NamedObjectOps value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse an enumeration and run a validity check on the object created,
     * storing it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, Enum<?>value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a string and run a validity check on the object created,
     * storing it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, String value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an int and store it in the location specified
     * by an enumeration key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, int value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "int";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-valued random variable and store it in the
     * location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, IntegerRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-random-variable-valued random variable and
     * store it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, IntegerRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a long and store it in the location specified
     * by an enumeration key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, long value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "long";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-valued random variable and store it in the
     * location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, LongRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-random-variable-valued random variable and
     * store it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, LongRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double and store it in the location specified
     * by an enumeration key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, double value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "double";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double-valued random variable and store it in the
     * location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, DoubleRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a double-random-variable-valued random variable and
     * store it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, DoubleRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean and store it in the location specified
     * by an enumeration key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, boolean value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "boolean";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-valued random variable and store it in
     * the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, BooleanRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-random-variable-valued random variable and
     * store it in the location specified by an enumeration key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Enum<?> key, BooleanRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a named object and run a validity check on the object created,
     * storing it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, NamedObjectOps value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse an enumeration and run a validity check on the object created,
     * storing it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, Enum<?>value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a string and run a validity check on the object created,
     * storing it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, String value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an int and store it in the location specified
     * by a compound key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, int value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "int";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-valued random variable and store it in the
     * location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, IntegerRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-random-variable-valued random variable and
     * store it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, IntegerRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a long and store it in the location specified
     * by a compound key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, long value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "long";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-valued random variable and store it in the
     * location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, LongRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-random-variable-valued random variable and
     * store it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, LongRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double and store it in the location specified
     * by a compound key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, double value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "double";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double-valued random variable and store it in the
     * location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, DoubleRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a double-random-variable-valued random variable and
     * store it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, DoubleRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean and store it in the location specified
     * by a compound key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, boolean value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "boolean";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-valued random variable and store it in
     * the location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, BooleanRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-random-variable-valued random variable and
     * store it in the location specified by a compound key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception KeyOutOfBoundsException the key is not in a legal
     *            range
     */
    public void parse(Object[] key, BooleanRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a string and run a validity check on the object created,
     * storing it in the location specified by a key.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, String value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse a named object and run a validity check on the object created,
     * storing it in the location specified by an index.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(String key, NamedObjectOps value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Parse an enumeration and run a validity check on the object created,
     * storing it in the location specified by an index.
     * @param key the key of the object created
     * @param value the value to parse and check
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the index is not in a legal
     *            range
     */
    public void parse(String key, Enum<?>value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an int and store it in the location specified
     * by a key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, int value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "int";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-valued random variable  and store it
     * in the location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, IntegerRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process an integer-random-variable-valued random variable
     * and store it in the location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, IntegerRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long and store it in the location specified
     * by a key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, long value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "long";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-valued random variable and store it in the
     * location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, LongRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a long-random-variable-valued random variable and
     * store it in the location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, LongRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double and store it in the location specified
     * by a key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, double value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "double";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double-valued random variable and store it in the
     * location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, DoubleRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a double-random-variable-valued random variable and
     * store it in the location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, DoubleRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean and store it in the location specified
     * by a key.
     * @param key the key of the object created
     * @param value the value to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, boolean value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = "boolean";
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }

    /**
     * Process a boolean-valued random variable and store it in the
     * location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, BooleanRandomVariable value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Process a boolean-random-variable-valued random variable and
     * store it in the location specified by a key.
     * @param key the key of the object created
     * @param value the random variable to store
     * @exception IllegalArgumentException the argument was not
     *            valid
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception IndexOutOfBoundsException the key is an index that is
     *            not in the legal range
     */
    public void parse(String key, BooleanRandomVariableRV value)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IndexOutOfBoundsException, IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	String cn = value.getClass().getName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported3", n, k, cn));
    }


    /**
     * Removes the effect of calling parse.
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is in a state for
     *            which an entry cannot be cleared, typically the result
     *            of another entry being dependent on the current entry
     */
    public void clear()
	throws UnsupportedOperationException, IllegalStateException
    {
	String n = getParmName();
	throw new UnsupportedOperationException
	    (errorMsg("unsupported1", n));
    }

    /**
     * Removes the effect of calling parse with an index.
     * @param index the index
     * @exception IllegalArgumentException the argument has an
     *            illegal value
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is in a state for
     *            which an entry cannot be cleared, typically the result
     *            of another entry being dependent on the current entry
     */
    public void clear(int index)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString((Integer)index);
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2c", n, k));
    }

    /**
     * Removes the effect of calling parse with a key.
     * @param key the key
     * @exception IllegalArgumentException the argument has an
     *            illegal value
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is in a state for
     *            which an entry cannot be cleared, typically the result
     *            of another entry being dependent on the current entry
     */
    public void clear(String key)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2c", n, k));
    }


    /**
     * Removes the effect of calling parse with a key that is a named object.
     * @param key the key
     * @exception IllegalArgumentException the argument has an
     *            illegal value
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is in a state for
     *            which an entry cannot be cleared, typically the result
     *            of another entry being dependent on the current entry
     */
    public void clear(NamedObjectOps key)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2c", n, k));
    }

    /**
     * Removes the effect of calling parse with a key that is an enumeration.
     * @param key the key
     * @exception IllegalArgumentException the argument has an
     *            illegal value
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is in a state for
     *            which an entry cannot be cleared, typically the result
     *            of another entry being dependent on the current entry
     */
    public void clear(Enum<?> key)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2c", n, k));
    }

    /**
     * Removes the effect of calling parse with a compound key.
     * @param key the key
     * @exception IllegalArgumentException the argument has an
     *            illegal value
     * @exception UnsupportedOperationException the parameter does not allow
     *            the use of this method
     * @exception IllegalStateException the factory is in a state for
     *            which an entry cannot be cleared, typically the result
     *            of another entry being dependent on the current entry
     */
    public void clear(Object[] key)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	String n = getParmName();
	String k = keyString(key);
	throw new UnsupportedOperationException
	    (errorMsg("unsupported2c", n, k));
    }
}

//  LocalWords:  exbundle Parm blockquote ResourceBundle mybundle pre
//  LocalWords:  getBundle errorMsg args NullPointerException enums
//  LocalWords:  MissingResourceException ClassCastException toString
//  LocalWords:  Subkeys parm ParmParser KeyedCompoundParm keyType
//  LocalWords:  getAltParmParser ParmKeyType subparameters anim href
//  LocalWords:  assocaiated subclasses subparameter altParser
//  LocalWords:  superclasses IllegalArgumentException boolean
//  LocalWords:  UnsupportedOperationException IllegalStateException
//  LocalWords:  IndexOutOfBoundsException KeyOutOfBoundsException
