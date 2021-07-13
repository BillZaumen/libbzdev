package org.bzdev.devqsim;
import java.lang.reflect.Method;

import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.obnaming.NamedFunctionOps;
import org.bzdev.obnaming.ObjectNamerLauncher;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Factory for simulation objects in which an object represents
 * a RealValuedFunction.
 */
public final class SimFunction extends DefaultSimObject
    implements NamedFunctionOps
{

    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }


    boolean nof = false;
    RealValuedFunction f;

    @Override
    public RealValuedFunction getFunction() {
	if (nof) return null;
	return f;
    }

    private static Method findRVFMethod(String name)
	throws IllegalStateException
    {
	try {
	    return ObjectNamerLauncher.findRVFMethod(name);
	} catch (Exception e) {
	    String msg = errorMsg("rvfNoMethod", name);
	    throw new IllegalStateException(msg, e);
	}
    }

    // method for factory - object is null if and only if fname, etc., are used.
    void setFunction(Object object, String fname, String fpname, String fppname)
    {
	Simulation sim = getSimulationAsSimulation();
	if (object != null) {
	    if (object instanceof RealValuedFunction) {
		f = (RealValuedFunction) object;
	    } else if (object instanceof RealValuedFunctOps) {
		f = new RealValuedFunction((RealValuedFunctOps)object);
	    } else {
		f = new RealValuedFunction(getSimulationAsSimulation(), object);
	    }
	} else if (sim.hasScriptEngine()) {
	    f = new RealValuedFunction(getSimulationAsSimulation(),
				       fname, fpname, fppname);
	} else {
	    Method m = (fname == null)? null: findRVFMethod(fname);
	    Method mp = (fpname == null)? null: findRVFMethod(fpname);
	    Method mpp = (fppname == null)? null: findRVFMethod(fppname);
	    f = new RealValuedFunction
		(((m == null)? null:
		  (x) -> {
		      try {
			  return (Double)m.invoke(null, x);
		      } catch (Exception e) {
			  String cause = e.getMessage();
			  String msg = errorMsg("rvfFailed", fname, x, cause);
			  throw new IllegalStateException(msg, e);
		      }
		  }),
		 ((mp == null)? null:
		   (y) -> {
		       try {
			   return (Double)mp.invoke(null, y);
		       } catch (Exception e) {
			   String cause = e.getMessage();
			   String msg = errorMsg("rvfFailed", fname, y, cause);
			   throw new IllegalStateException(msg, e);
		       }
		   }),
		  ((mpp == null)? null:
		   (z) -> {
		      try {
			  return (Double)mpp.invoke(null, z);
		      } catch (Exception e) {
			  String cause = e.getMessage();
			  String msg = errorMsg("rvfFailed", fname, z, cause);
			  throw new IllegalStateException(msg, e);
		      }
		  }));
	}
    }

    @Override
    public double getDomainMin() {
	return f.getDomainMin();
    }

    @Override
    public boolean domainMinClosed() {
	return f.domainMinClosed();
    }

    @Override
    public double getDomainMax() {
	return f.getDomainMax();
    }

    @Override
    public boolean domainMaxClosed() {
	return f.domainMaxClosed();
    }

    @Override
    public double valueAt(double x)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.valueAt(x);
    }
    
    @Override
    public double derivAt(double x)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.derivAt(x);
    }

    @Override
    public double secondDerivAt(double x)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.secondDerivAt(x);
    }

    // constructor for factory
    SimFunction(Simulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Constructor given a RealValuedFunction or a scripting-language-defined
     * object.
     * SimFunction objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically
     *        generated name
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; false
     *         otherwise.
     * @param fobj the function specified as an instance of RealValuedFunction or
     *        a scripting-language-defined object; null to return a SimFunction
     *        such that getFunction() will return null
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunction
     */
    public SimFunction(Simulation sim, String name, boolean intern, Object fobj)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (fobj == null) {
	    this.f = new RealValuedFunction();
	    nof = true;
	} else 	if (fobj instanceof RealValuedFunction) {
	    RealValuedFunction f = (RealValuedFunction)fobj;
	    this.f = f;
	} else {
	    this.f = new RealValuedFunction(sim, fobj);
	}
    }

    /**
     * Constructor given a RealValuedFunctOps, provided so that the
     * function can be defined using a lambda expression that takes
     * one argument, or given an instance of RealValuedFunction.
     * SimFunction objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation};
     *        false otherwise.
     * @param f the function specified; null to return a SimFunction
     *        such that getFunction() will return null
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunction
     */
    public SimFunction(Simulation sim, String name, boolean intern,
		       RealValuedFunctOps f)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (f == null) {
	    this.f = new RealValuedFunction();
	    nof = true;
	} else if (f instanceof RealValuedFunction) {
	    this.f = (RealValuedFunction)f;
	} else {
	    this.f = new RealValuedFunction(f);
	}
    }

    /**
     * Constructor given a RealValuedFunctOps, provided so that the
     * function can be defined using a lambda expression that takes
     * one argument.
     * SimFunction objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; false
     *        otherwise.
     * @param f the function specified; null to return a SimFunction
     *        such that getFunction() will return null
     * @param fp a function providing the the first derivative of
     *        the function specified; null if the first derivative is
     *        not provided
     * @param fpp a function providing the second derivative of
     *        the function specified; null if the first derivative is
     *        not provided
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunction
     */
    public SimFunction(Simulation sim, String name, boolean intern,
		       RealValuedFunctOps f,
		       RealValuedFunctOps fp,
		       RealValuedFunctOps fpp)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (f == null) {
	    this.f = new RealValuedFunction();
	    nof = true;
	} else {
	    this.f = new RealValuedFunction(f, fp, fpp);
	}
    }


    /**
     * Constructor given the names of scripting-language-defined functions
     * that will implement a function and its first and second derivatives.
     * SimFunction objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; false
     *        otherwise.
     * @param fname the name of the scripting-language-defined function giving
     *              the value of this function
     * @param fpname the name of the scripting-language-defined function giving
     *              the value of this function's first derivative; null if
     *              a first derivative is not provided.
     * @param fppname the name of the scripting-language-defined function giving
     *              the value of this function's second derivative; null if
     *              a second derivative is not provided.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public SimFunction(Simulation sim, String name, boolean intern,
		       String fname, String fpname, String fppname)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	f = new RealValuedFunction(sim, fname, fpname, fppname);
    }
}

//  LocalWords:  exbundle RealValuedFunction rvfNoMethod fname fobj
//  LocalWords:  rvfFailed SimFunction getFunction getObject fp fpp
//  LocalWords:  IllegalArgumentException RealValuedFunctOps fpname
//  LocalWords:  fppname
