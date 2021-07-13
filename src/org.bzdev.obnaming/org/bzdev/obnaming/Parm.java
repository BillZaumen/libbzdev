package org.bzdev.obnaming;
import org.bzdev.math.rv.RandomVariable;

//@exbundle org.bzdev.obnaming.lpack.Obnaming

/**
 * Named-Object-factory parameter.
 * Parameters for a subclass of NamedObjectFactory are referenced by
 * unique names: each parameter that a factory recognizes must have
 * a different name.  A factory provides a series of 'set' methods
 * that allow values of parameters to be set.  The Parm class describes
 * each parameter.
 * <P>
 * Keyless parameters are the simplest ones, with appropriate parameters
 * supplied by the constructors
 * {@link #Parm(String,Class,ParmParser,Class) Parm(String,Class,ParmParser,Class)} 
 * and
 * {@link #Parm(String,Class,ParmParser,Class,Number,boolean,Number,boolean) Parm(String,...,Class,Number,boolean,Number,boolean)}.
 * In the constructor documentation,
 * <ul>
 *   <li> The <i>name</i> argument is the parameter name, which must be
 *        unique among all parameter names for a given factory, including
 *        parameters defined in a factory's superclasses.
 *   <li> The <i>rvClass</i> argument, if not null, specifies the type of
 *        a random variable that will generate a sequence of values whose
 *        types are <i>type</i>, one value per object created.  When null,
 *        the value provided is used as is.
 *   <li> The <i>parser</i> argument provides a parser that is responsible
 *        for storing a value and clearing the value (which typically means
 *        setting the variable to its default value).  A parser can be
 *        associated with only one instance of Parm.
 *   <li> The <i>type</i> defines the type of the value used to create or
 *        configure an object.
 *   <li> The <i>glb</i> argument provides a greatest lower bound on the value
 *        of an argument; null if there is none.
 *   <li> The <i>lbClosed</i> argument is true if the greatest lower bound is
 *        a value the parameter may have; false otherwise.
 *   <li> The <i>lub</i> argument provides a least upper bound on the value of
 *        an argument; null if there is none.
 *   <li> The <i>ubClosed</i> argument is true if the least upper bound is
 *        a value the parameter may have; false otherwise.
 * </ul>
 */
public class Parm {

    static String errorMsg(String key, Object... args) {
	return ObnamingErrorMsg.errorMsg(key, args);
    }

    String name;

    // used during initialization to let a ParmParser know the name
    // of a parameter (useful for generating error messages)
    String getParmName() {return name;}

    ParmParser parser;
    // make these two classes so we can make sure a string naming a
    // domain ,etc., actually matches a domain.
    // public enum KeyType {NOKEY, STRING, QNAME, INT}
    // public enum Type {DOUBLE, INT, LONG, STRING, QNAME, BOOLEAN, KEY}
    Class<?> keyType;
    boolean rvmode;
    Class<? extends RandomVariable<?>> rvClass;
    ParmKeyType parmKeyType = null;
    Class<?> type;
    Number glb = null;
    boolean lbClosed = true;
    Number lub = null;
    boolean ubClosed = true;
    boolean clearOnly = false;

    /**
     * Determine if this Parm instance supports clear methods, but
     * not add or set.
     * @return true if clear methods are supported but not add or set;
     *        false otherwise
     */
    public boolean isClearOnly() { return clearOnly;}

    /**
     * Assert that this parm parser is a clear-only parm parser.
     */
    protected void makeClearOnly() {
	clearOnly = true;
    }

    Class<?> factoryClass = null;
	
    void setFactoryClass(Class<?> clazz) {
	factoryClass = clazz;
    }

    /**
     * Constructor in non-append mode.
     * @param name the name of the parameter
     * @param rvClass the class of a random variable if values of type
     *        <code>type</code> are generated by a random variable; false
     *        otherwise
     * @param parser the parser for the parameter
     * @param type the type of the parameter
     * @exception IllegalStateException a parm parser was already in use
     */
    public Parm(String name, Class<? extends RandomVariable<?>> rvClass,
		ParmParser parser, Class type)
	throws IllegalStateException
    {
	this(name, null, rvClass, parser, type);
    }

    /**
     * Constructor in non-append mode.
     * @param name the name of the parameter
     * @param rvClass the class of a random variable if values of type
     *        <code>type</code> are generated by a random variable; null
     *        otherwise
     * @param parser the parser for the parameter (each parser may be
     *        associated with only one Parm object)
     * @param type the type of the parameter
     * @param glb the greatest lower bound for the range of acceptable
     *            values.
     * @param lbClosed true if the greatest lower bound is in the range
     *        of acceptable values; false if not; ignored if glb is null
     * @param lub the least upper bound  for the range of acceptable
     *            values.
     * @param ubClosed true if the least upper bound is in the range
     *        of acceptable values; false if not; ignored if lub is null
     * @exception IllegalStateException a parm parser was already in use
     */
    public Parm(String name, Class rvClass,
		ParmParser parser, Class type,
		Number glb, boolean lbClosed, Number lub, 
		boolean ubClosed)
	throws IllegalStateException
    {
	this(name, null, rvClass, parser, type,
	     glb, lbClosed, lub, ubClosed);
    }

    /**
     * Constructor for keyed types.
     * If both the keyType and type arguments (arguments 2 and 4)
     * are null, the factories "remove" method will call the
     * parser argument's <code>clear()</code> method, which must
     * remove the entries for all keys that match the name
     * @param name the name of the parameter
     * @param keyType the class of a simulation object that can
     *        be referenced by name, in which case a the type is
     *        a string; String.class for strings, Integer.class for
     *        integer indices; an instance of ParmKeyType if a qualified name;
     *        null if not a keyed entry
     * @param rvClass the class of a random variable if values of type
     *        <code>type</code> are generated by a random variable; false
     *        otherwise
     * @param parser the parser for the parameter (each parser may be
     *        associated with only one Parm object)
     * @param type the type of the parameter
     * @exception IllegalStateException a parm parser was already in use
     */
    public Parm(String name, Object keyType,
		Class rvClass,
		ParmParser parser, Class type)
	throws IllegalStateException
    {
	this(name, keyType, rvClass, parser, type, 
	     (Number)null, true, (Number)null, true);
    }

    /**
     * Constructor for a parameter used only to clear a compound parameter.
     * The parser should have only a clear() method implemented explicitly.
     * @param name the name of the parameter
     * @param parser the parser for the parameter (each parser may be
     *        associated with only one Parm object)
     */
    public Parm(String name, ParmParser parser) {
	this(name, null, null, parser, null,
	     (Number)null, true, (Number)null, true);
	makeClearOnly();
    }

    /**
     * Constructor for keyed types with bounds.
     * If both the keyType and type arguments (arguments 2 and 4)
     * are null, the factories "remove" method will call the
     * parser argument's <code>clear()</code> method, which must
     * remove the entries for all keys that match the name
     * @param name the name of the parameter
     * @param keyType the class of a simulation object that can
     *        be referenced by name, in which case a the type is
     *        a string; String.class for strings, Integer.class for
     *        integer indices; an instance of ParmKeyType if a qualified name;
     *        null if not a keyed entry
     * @param rvClass the class of a random variable if values of type
     *        <code>type</code> are generated by a random variable; false
     *        otherwise
     * @param parser the parser for the parameter (each parser may be
     *        associated with only one Parm object)
     * @param type the type of the parameter
     * @param glb the greatest lower bound for the range of acceptable
     *            values.
     * @param lbClosed true if the greatest lower bound is in the range
     *        of acceptable values; false if not; ignored if glb is null
     * @param lub the least upper bound  for the range of acceptable
     *            values.
     * @param ubClosed true if the least upper bound is in the range
     *        of acceptable values; false if not; ignored if lub is null
     * @exception IllegalStateException a parm parser was already in use
     */
    @SuppressWarnings("unchecked")
    public Parm(String name, Object keyType,
		Class rvClass,
		ParmParser parser, Class type,
		Number glb, boolean lbClosed, Number lub, 
		boolean ubClosed)
	throws IllegalStateException
    {
	this.name = name;
	parser.setParmName(name);
	if ((rvClass == null)
	    || RandomVariable.class.isAssignableFrom(rvClass)) {
	    this.rvClass = rvClass;
	} else {
	    throw new IllegalArgumentException(errorMsg("rvClassError"));
	}
	this.rvmode = (rvClass != null);
	if (keyType == null) {
	    this.keyType = null;
	} else if (keyType instanceof Class) {
	    this.keyType = (Class<?>) keyType;
	} else if (keyType instanceof ParmKeyType) {
	    this.keyType = ParmKeyType.class;
	    this.parmKeyType = (ParmKeyType)keyType;
	}

	this.parser = parser;
	this.type = type;

	this.glb = glb;
	this.lbClosed = lbClosed;
	this.lub = lub;
	this.ubClosed = ubClosed;
    }

    /**
     * Get the parameter parser.
     * @return the parser
     */
    public ParmParser getParser() {
	return parser;
    }
}

//  LocalWords:  exbundle NamedObjectFactory Parm Keyless ParmParser
//  LocalWords:  boolean ul li superclasses rvClass glb lbClosed lub
//  LocalWords:  ubClosed enum KeyType NOKEY QNAME parm keyType
//  LocalWords:  IllegalStateException ParmKeyType rvClassError
