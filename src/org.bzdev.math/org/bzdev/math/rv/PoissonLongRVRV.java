package org.bzdev.math.rv;

/**
 * Class to generate a sequence of long-integer-valued Poisson Random Variables.
 */
public class PoissonLongRVRV
    extends LongRandomVariableRV<PoissonLongRV> {
    DoubleRandomVariable lambdaRV;
    boolean mode;

   /**
     * Constructor.
     * @param lambdaRV a random variable that determines the parameter
     *        &lambda; which gives the mean of the
     *        distribution for a Poisson random variable
     */
    public PoissonLongRVRV(DoubleRandomVariable lambdaRV) {
	this(lambdaRV, false);
    }

   /**
     * Constructor with a mode to allow tables to be allocated.
     * @param lambdaRV a random variable that determines the parameter
     *        &lambda; which gives the mean of the
     *        distribution for a Poisson random variable
     * @param mode true if tables should be allocated to improve execution
     *        speed when a PoissonLongRV is created; false otherwise
     * @see PoissonLongRV
     */
    public PoissonLongRVRV(DoubleRandomVariable lambdaRV, boolean mode)
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

    protected PoissonLongRV doNext() throws RandomVariableException {
	return new PoissonLongRV(lambdaRV.next(), mode);
    }
}

//  LocalWords:  lambdaRV PoissonLongRV setMinimum
