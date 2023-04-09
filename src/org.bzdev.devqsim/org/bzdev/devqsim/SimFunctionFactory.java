package org.bzdev.devqsim;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.NamedObjectFactory.ConfigException;
import org.bzdev.util.SafeFormatter;

import javax.script.ScriptException;
import java.util.*;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Factory for simulation objects providing functions.
 * Unlike most named-object factories, the values passed
 * as the final argument to 'set' methods can be cast to
 * the type Object, either explicitly or due to variable
 * assignment or the return value of a some method.  This
 * flexibility is provided so that the value of the "object"
 * parameter can be an instance of RealValuedFunction or
 * some object defined in a scripting language.
 * <P>
 * The parameters this class supports are the following:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/SimFunctionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFunctionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

public class SimFunctionFactory extends DefaultSimObjectFactory<SimFunction>
{
    Simulation sim;

    Object object = null;
    String fname = null;
    String fpname = null;
    String fppname = null;

    // resource bundle for messages used by exceptions and errors
    // (the bundles is the one for ParmParser - we just added one
    // additional entry as a special case).
    static ResourceBundle
      exbundle=ResourceBundle.getBundle("org.bzdev.obnaming.lpack.ParmParser");
    private static String errorMsg(String key, Object... args)
	throws
	    NullPointerException, MissingResourceException, ClassCastException
    {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }



    Parm[] parms = {
	new Parm("object", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) {
			     value = null;
			     return;
			 } else {
			     try {
				 object = sim.evalScript(value);
			     } catch (ScriptException e) {
				 // need outer quotes for a test
				 String msg =
				     (errorMsg("badScript", getParmName()));
				 throw new IllegalArgumentException(msg, e);
				     /*("bad script", e);*/
			     }
			 }
		     }
		     public void clear() {
			 fname = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("fName", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 fname = value;
		     }
		     public void clear() {
			 fname = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("fpName", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 fpname = value;
		     }
		     public void clear() {
			 fpname = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("fppName", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 fppname = value;
		     }
		     public void clear() {
			 fppname = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
    };

    public void clear() {
	super.clear();
	object = null;
	fname = null;
	fpname = null;
	fppname = null;
    }


    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public SimFunctionFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
	initParms(parms, SimFunctionFactory.class);
 	addLabelResourceBundle("*.lpack.SimFunctionLabels",
			       SimFunctionFactory.class);
	addTipResourceBundle("*.lpack.SimFunctionTips",
			     SimFunctionFactory.class);
	addDocResourceBundle("*.lpack.SimFunctionDocs",
			     SimFunctionFactory.class);
   }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * jst calls the default constructor with a null argument.
     */
    public SimFunctionFactory() {
	this(null);
    }

    /**
     * Set a value provided as a object.
     * This customizes the factory for the "object" parameter.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     */
    public void set(String name, Object value) throws ConfigException {
	try {
	    if (name.equals("object")) {
		object = value;
		if (value instanceof String) {
		    try {
			object = sim.evalScript((String)value);
		    } catch (ScriptException e) {
			// need outer quotes for a test
			String msg = (errorMsg("badScript", name));
			throw new IllegalArgumentException(msg, e);
			/*("bad script", e);*/
		    }
		} else {
		    object = value;
		}
	    } else if (value instanceof String) {
		super.set(name, (String)value);
	    } else {
		// Of the normal value types for a factory, this one
		// only accepts a string.  We allow an object to be
		// passed for one special case.
		throw new UnsupportedOperationException
		    (errorMsg("wrongType1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw newConfigExceptionInstance(name, e);
	} catch (IllegalStateException e) {
	    throw newConfigExceptionInstance(name, e);
	} catch (UnsupportedOperationException e) {
	    throw newConfigExceptionInstance(name, e);
	}
    }

    @Override
    protected final SimFunction newObject(String name) {
	return new SimFunction(sim, name, willIntern());
    }

    @Override
    protected void initObject(SimFunction simFunction) {
	super.initObject(simFunction);
	simFunction.setFunction(object, fname, fpname, fppname);
    }
}

//  LocalWords:  exbundle RealValuedFunction IFRAME SRC px steelblue
//  LocalWords:  HREF ParmParser badScript fName fpName fppName jst
//  LocalWords:  ConfigException IllegalArgumentException wrongType
//  LocalWords:  UnsupportedOperationException IllegalStateException
