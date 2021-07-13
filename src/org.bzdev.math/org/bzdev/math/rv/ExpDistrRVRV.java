package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class to generate a sequence of random variables, each of which
 * generates exponentially distributed values.
 */
public class ExpDistrRVRV extends DoubleRandomVariableRV<ExpDistrRV> {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    DoubleRandomVariable meanRV;
    /**
     * Constructor.
     * @param meanRV a random variable that determines the mean of the
     *        distribution for a random variable that generates
     *        exponentially distributed values
     */
    public ExpDistrRVRV(DoubleRandomVariable meanRV)
    {
	// clone in case setMinimum or similar methods called.
	try {
	    this.meanRV = (DoubleRandomVariable)meanRV.clone();
	    this.meanRV.tightenMinimum(0.0, false);
	    determineIfOrdered(meanRV);
	}  catch (CloneNotSupportedException e) {
	    String msg =
		errorMsg("noClone", meanRV.getClass().getName());
	    throw new RandomVariableException(msg, e);
	}
    }

    protected ExpDistrRV doNext() throws RandomVariableException {
	return new ExpDistrRV(meanRV.next());
    }
}

//  LocalWords:  exbundle meanRV setMinimum noClone
