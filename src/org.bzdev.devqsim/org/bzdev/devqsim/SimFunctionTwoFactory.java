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
 * parameter can be an instance of
 * {@link org.bzdev.math.RealValuedFunctionTwo} or
 * some object defined in a scripting language.
 * <P>
 * The parameters this class supports are the following:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/SimFunctionTwoFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFunctionTwoFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

public class SimFunctionTwoFactory extends DefaultSimObjectFactory<SimFunctionTwo>
{
    Simulation sim;

    Object object = null;
    String fname = null;
    String f1name = null;
    String f2name = null;
    String f11name = null;
    String f12name = null;
    String f21name = null;
    String f22name = null;

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
	new Parm("f1Name", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 f1name = value;
		     }
		     public void clear() {
			 f1name = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("f2Name", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 f2name = value;
		     }
		     public void clear() {
			 f2name = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("f11Name", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 f11name = value;
		     }
		     public void clear() {
			 f11name = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("f12Name", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 f12name = value;
		     }
		     public void clear() {
			 f12name = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("f21Name", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 f21name = value;
		     }
		     public void clear() {
			 f21name = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true),
	new Parm("f22Name", null,
		 new ParmParser() {
		     public void parse(String value) {
			 if (value.length() == 0) value = null;
			 f22name = value;
		     }
		     public void clear() {
			 f22name = null;
		     }
		 },
		 java.lang.String.class,
		 null, true, null, true)
    };

    public void clear() {
	super.clear();
	object = null;
	fname = null;
	f1name = null;
	f2name = null;
	f11name = null;
	f12name = null;
	f21name = null;
	f22name = null;
    }


    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public SimFunctionTwoFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
	initParms(parms, SimFunctionTwoFactory.class);
 	addLabelResourceBundle("*.lpack.SimFunctionTwoLabels",
			       SimFunctionTwoFactory.class);
	addTipResourceBundle("*.lpack.SimFunctionTwoTips",
			     SimFunctionTwoFactory.class);
	addDocResourceBundle("*.lpack.SimFunctionTwoDocs",
			     SimFunctionTwoFactory.class);
   }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public SimFunctionTwoFactory() {
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
    protected final SimFunctionTwo newObject(String name) {
	return new SimFunctionTwo(sim, name, willIntern());
    }

    @Override
    protected void initObject(SimFunctionTwo simFunction) {
	super.initObject(simFunction);
	simFunction.setFunction(object, fname, f1name, f2name,
				f11name, f12name,
				f21name, f22name);
    }
}

//  LocalWords:  exbundle IFRAME SRC px steelblue HREF ParmParser
//  LocalWords:  badScript fName ConfigException wrongType
//  LocalWords:  IllegalArgumentException IllegalStateException
//  LocalWords:  UnsupportedOperationException
