package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class to generate a sequence of random variables, each of which
 * generates Poisson-distributed interarrival times.
 */
public class PoissonIATimeRVRV extends InterarrivalTimeRVRV<PoissonIATimeRV> {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    DoubleRandomVariable meanRV;
    /**
     * Constructor.
     * @param meanRV a random variable that determines the mean of the
     *        distribution for a random variable that generates
     *        Poisson-distributed interarrival times
     */
    public PoissonIATimeRVRV(DoubleRandomVariable meanRV)
    {
	// clone in case setMinimum or similar methods called.
	try {
	    this.meanRV = (DoubleRandomVariable)meanRV.clone();
	    this.meanRV.tightenMinimum(0.0, false);
	    determineIfOrdered(meanRV);
	}  catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", meanRV.getClass().getName());
	    throw new RandomVariableException(msg, e);
	}
    }

    protected PoissonIATimeRV doNext() throws RandomVariableException {
	return new PoissonIATimeRV(meanRV.next());
    }
}

//  LocalWords:  exbundle interarrival meanRV Poisson setMinimum
//  LocalWords:  noClone
