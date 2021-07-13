package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class to generate a sequence of Gaussian random variables, with the
 * values for each rounded to the nearest long integer to represent
 * interarrival times.
 */
public class GaussianIATimeRVRV extends InterarrivalTimeRVRV<GaussianIATimeRV> {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    DoubleRandomVariable meanRV;
    DoubleRandomVariable sdevRV;
    /**
     * Constructor.
     * @param meanRV a random variable that determines the mean of the
     *        distribution for a Gaussian random variable
     * @param sdevRV a random variable that determines the standard
     *        deviation of the distribution for a Gaussian random
     *        variable
     */
    public GaussianIATimeRVRV(DoubleRandomVariable meanRV,
			DoubleRandomVariable sdevRV)
    {
	// clone in case setMinimum or similar methods called.
	Class<?> clasz = null;
	try {
	    clasz = meanRV.getClass();
	    this.meanRV = (DoubleRandomVariable)meanRV.clone();
	    clasz = sdevRV.getClass();
	    this.sdevRV = (DoubleRandomVariable)sdevRV.clone();
	    this.sdevRV.tightenMinimum(0.0, false);
	    determineIfOrdered(meanRV, sdevRV);
	}  catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", clasz.getName());
	    throw new RandomVariableException(msg, e);
	}
    }

    @Override
    protected GaussianIATimeRV doNext() throws RandomVariableException {
	return new GaussianIATimeRV(meanRV.next(), sdevRV.next());
    }
}

//  LocalWords:  exbundle interarrival meanRV sdevRV setMinimum
//  LocalWords:  noClone
