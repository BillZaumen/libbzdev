package org.bzdev.devqsim;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.RealValuedFunctTwoOps;
import org.bzdev.obnaming.NamedFunctionTwoOps;

/**
 * Factory for simulation objects in which an object represents
 * a RealValuedFunctionTwo.
 */
public final class SimFunctionTwo extends DefaultSimObject
    implements NamedFunctionTwoOps
{

    boolean nof = false;
    RealValuedFunctionTwo f;

    @Override
    public RealValuedFunctionTwo getFunction() {
	if (nof) return null;
	return f;
    }

    // method for factory - object is null if and only if fname, etc., are used.
    void setFunction(Object object, String fname, String f1name, String f2name,
		     String f11name, String f12name,
		     String f21name, String f22name)
    {
	if (object != null) {
	    if (object instanceof RealValuedFunctionTwo) {
		f = (RealValuedFunctionTwo) object;
	    } else if (object instanceof RealValuedFunctTwoOps) {
		f = new RealValuedFunctionTwo((RealValuedFunctTwoOps)object);
	    } else {
		f = new RealValuedFunctionTwo(getSimulationAsSimulation(),
					      object);
	    }
	} else {
	    f = new RealValuedFunctionTwo(getSimulationAsSimulation(),
					  fname, f1name, f2name,
					  f11name, f12name,
					  f21name, f22name);
	}
    }

    @Override
    public double getDomainMin1() {
	return f.getDomainMin1();
    }

    @Override
    public boolean domainMin1Closed() {
	return f.domainMin1Closed();
    }

    @Override
    public double getDomainMax1() {
	return f.getDomainMax1();
    }

    @Override
    public boolean domainMax1Closed() {
	return f.domainMax1Closed();
    }

    @Override
    public double getDomainMin2() {
	return f.getDomainMin2();
    }

    @Override
    public boolean domainMin2Closed() {
	return f.domainMin2Closed();
    }

    @Override
    public double getDomainMax2() {
	return f.getDomainMax2();
    }

    @Override
    public boolean domainMax2Closed() {
	return f.domainMax2Closed();
    }

    @Override
    public boolean isInDomain(double x, double y)
	 throws UnsupportedOperationException
    {
	return f.isInDomain(x, y);
    }

    @Override
    public double valueAt(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.valueAt(x, y);
    }
    
    @Override
	public double deriv1At(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.deriv1At(x, y);
    }

    @Override
	public double deriv2At(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.deriv2At(x, y);
    }

    @Override
	public double deriv11At(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.deriv11At(x, y);
    }

    @Override
	public double deriv12At(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.deriv12At(x, y);
    }

    @Override
	public double deriv21At(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.deriv21At(x, y);
    }

    @Override
	public double deriv22At(double x, double y)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	return f.deriv22At(x, y);
    }

    // constructor for factory
    SimFunctionTwo(Simulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Constructor given a RealValuedFunctionTwo or a scripting-language-defined
     * object.
     * SimFunctionTwo objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; false
     *        otherwise.
     * @param fobj the function specified as an instance of
     *        RealValuedFunctionTwo or
     *        a scripting-language-defined object; null to return a 
     *        SimFunctionTwo such that getFunction() will return null
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunctionTwo
     */
    public SimFunctionTwo(Simulation sim, String name, boolean intern,
			  Object fobj)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (fobj == null) {
	    this.f = new RealValuedFunctionTwo();
	    nof = true;
	} else 	if (fobj instanceof RealValuedFunctionTwo) {
	    RealValuedFunctionTwo f = (RealValuedFunctionTwo)fobj;
	    this.f = f;
	} else {
	    this.f = new RealValuedFunctionTwo(sim, fobj);
	}
    }

    /**
     * Constructor given a RealValuedFunctTwoOps object, which can be
     * provided by a lambda expression.
     * SimFunctionTwo objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation};
     *        false otherwise.
     * @param f the function providing this function's value; null to return a
     *        SimFunctionTwo such that getFunction() will return null
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunctionTwo
     */
    public SimFunctionTwo(Simulation sim, String name, boolean intern,
			  RealValuedFunctTwoOps f)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (f == null) {
	    this.f = new RealValuedFunctionTwo();
	    nof = true;
	} else if (f instanceof RealValuedFunctionTwo) {
	    this.f = (RealValuedFunctionTwo)f;
	} else {
	    this.f = new RealValuedFunctionTwo(f);
	}
    }

    /**
     * Constructor given RealValuedFunctTwoOps objects, which can be
     * provided by lambda expressions, to specify a function and its
     * first partial derivatives.
     * SimFunctionTwo objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * If the arguments f, f1, and f2 are all null,
     * {@link #getFunction()} will return null.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation};
     *        false otherwise.
     * @param f the function providing this function's value; null if a
     *        value cannot be computed
     * @param f1 a function providing this function's partial derivative
     *        &part;f/&part;x<sub>1</sub>; null if a value cannot be computed
     * @param f2 a function providing this function's partial derivative
     *        &part;f/&part;x<sub>2</sub>; null if a value cannot be computed
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunctionTwo
     */
    public SimFunctionTwo(Simulation sim, String name, boolean intern,
			  RealValuedFunctTwoOps f,
			  RealValuedFunctTwoOps f1,
			  RealValuedFunctTwoOps f2)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (f == null && f1 == null && f2 == null) {
	    this.f = new RealValuedFunctionTwo();
	    nof = true;
	} else {
	    this.f = new RealValuedFunctionTwo(f, f1, f2);
	}
    }

    /**
     * Constructor given RealValuedFunctTwoOps objects, which can be
     * provided by lambda expressions, to specify a function and its
     * first and second partial derivatives.
     * SimFunctionTwo objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * If the arguments f, f1, f2, f11, f12, f21, and f22 are all null,
     * {@link #getFunction()} will return null.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation};
     *        false otherwise.
     * @param f the function providing this function's value; null if a
     *        value cannot be computed
     * @param f1 a function providing this function's partial derivative
     *        &part;f/&part;x<sub>1</sub>; null if a value cannot be computed
     * @param f2 a function providing this function's partial derivative
     *        &part;f/&part;x<sub>2</sub>; null if a value cannot be computed
     * @param f11 a function providing this function's partial derivative
     *        (&part;<sup>2</sup>;f)/(&part;x<sub>1</sub><sup>2</sup>);
     *         null if a value cannot be computed
     * @param f12 a function providing this function's partial derivative
     *        (&part;<sup>2</sup>;f)/(&part;x<sub>1</sub>&part;x<sub>2</sub>);
     *         null if a value cannot be computed
     * @param f21 a function providing this function's partial derivative
     *        (&part;<sup>2</sup>;f)/(&part;x<sub>2</sub>&part;x<sub>1</sub>);
     *         null if a value cannot be computed
     * @param f22 a function providing this function's partial derivative
     *        (&part;<sup>2</sup>;f)/(&part;x<sub>2</sub><sup>2</sup>);
     *         null if a value cannot be computed
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.math.RealValuedFunctionTwo
     */
    public SimFunctionTwo(Simulation sim, String name, boolean intern,
			  RealValuedFunctTwoOps f,
			  RealValuedFunctTwoOps f1,
			  RealValuedFunctTwoOps f2,
			  RealValuedFunctTwoOps f11,
			  RealValuedFunctTwoOps f12,
			  RealValuedFunctTwoOps f21,
			  RealValuedFunctTwoOps f22)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	if (f == null && f1 == null && f2 == null && f11 == null
	    && f12 == null && f21 == null && f22 == null) {
	    this.f = new RealValuedFunctionTwo();
	    nof = true;
	} else {
	    this.f = new RealValuedFunctionTwo(f, f1, f2,
					       f11, f12,
					       f21, f22);
	}
    }

    /**
     * Constructor given the names of scripting-language-defined functions
     * that will implement a function and its first and second derivatives.
     * SimFunctionTwo objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; false
     *        otherwise.
     * @param fname the name of the scripting-language-defined function giving
     *              the value of this function
     * @param f1name the name of the scripting-language-defined function giving
     *        the partial derivative &part;f / &part;x<sub>1</sub>
     *        for the function f(x<sub>1</sub>x<sub>2</sub>) defined by
     *        this object; null if the derivative is not provided.
     * @param f2name the name of the scripting-language-defined function giving
     *        the partial derivative &part;f / &part;x<sub>2</sub>
     *        for the function f(x<sub>1</sub>x<sub>2</sub>) defined by
     *        this object; null if the derivative is not provided.
     * @param f11name the name of the scripting-language-defined function giving
     *        the partial derivative
     *       &part;<sup>2</sup>f / &part;x<sub>1</sub><sup>2</sup>
     *        for the function f(x<sub>1</sub>x<sub>2</sub>) defined by
     *        this object; null if the derivative is not provided.
     * @param f12name the name of the scripting-language-defined function giving
     *        the partial derivative
     *        &part;<sup>2</sup>f / (&part;x<sub>1</sub>&part;x<sub>2</sub>)
     *        for the function f(x<sub>1</sub>x<sub>2</sub>) defined by
     *        this object; null if the derivative is not provided.
     * @param f21name the name of the scripting-language-defined function giving
     *        the partial derivative
     *        &part;<sup>2</sup>f / (&part;x<sub>2</sub>&part;x<sub>1</sub>)
     *        for the function f(x<sub>1</sub>x<sub>2</sub>) defined by
     *        this object; null if the derivative is not provided.
     * @param f22name the name of the scripting-language-defined function giving
     *        the partial derivative
     *       &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     *        for the function f(x<sub>1</sub>x<sub>2</sub>) defined by
     *        this object; null if the derivative is not provided
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public SimFunctionTwo(Simulation sim, String name, boolean intern,
			  String fname, String f1name, String f2name,
			  String f11name, String f12name,
			  String f21name, String f22name)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	f = new RealValuedFunctionTwo(sim, fname, f1name, f2name,
				      f11name, f12name,
				      f21name, f22name);
    }
}

//  LocalWords:  RealValuedFunctionTwo fname SimFunctionTwo fobj
//  LocalWords:  getFunction IllegalArgumentException getObject
//  LocalWords:  RealValuedFunctTwoOps
