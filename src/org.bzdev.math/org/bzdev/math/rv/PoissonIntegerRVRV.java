package org.bzdev.math.rv;

/**
 * Class to generate a sequence of integer-valued Poisson Random Variables.
 */
public class PoissonIntegerRVRV
    extends IntegerRandomVariableRV<PoissonIntegerRV> {
    DoubleRandomVariable lambdaRV;
    boolean mode;

   /**
     * Constructor.
     * @param lambdaRV a random variable that determines the parameter
     *        &lambda; which gives the mean of the
     *        distribution for a Poisson random variable
     */
    public PoissonIntegerRVRV(DoubleRandomVariable lambdaRV) {
	this(lambdaRV, false);
    }

   /**
     * Constructor with a mode to allow tables to be allocated.
     * @param lambdaRV a random variable that determines the parameter
     *        &lambda; which gives the mean of the
     *        distribution for a Poisson random variable
     * @param mode true if tables should be allocated to improve execution
     *        speed when a PoissonIntegerRV is created; false otherwise
     * @see PoissonIntegerRV
     */
    public PoissonIntegerRVRV(DoubleRandomVariable lambdaRV, boolean mode)
    {
	// clone in case setMinimum or similar methods called.
	try {
	    this.lambdaRV = (DoubleRandomVariable)lambdaRV.clone();
	    this.lambdaRV.tightenMinimum(0.0, true);
	    this.mode = mode;
	    determineIfOrdered(lambdaRV);
	}  catch (CloneNotSupportedException e) {
	    throw new 
		RandomVariableException
		("cloning of random variable not possible", e);
	}
    }

    protected PoissonIntegerRV doNext() throws RandomVariableException {
	return new PoissonIntegerRV(lambdaRV.next(), mode);
    }
}

//  LocalWords:  lambdaRV PoissonIntegerRV setMinimum
