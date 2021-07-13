package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class to generate a sequence of LogNormal Random Variables.
 */
public class LogNormalRVRV extends DoubleRandomVariableRV<LogNormalRV> {
    DoubleRandomVariable muRV;
    DoubleRandomVariable sigmaRV;

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor.
     * @param muRV a random variable that determines the mean of the
     *        distribution for the logarithm of a LogNormal random variable
     * @param sigmaRV a random variable that determines the standard
     *        deviation of the distribution for the logarithm of a LogNormal
     *        random variable
     */
    public LogNormalRVRV(DoubleRandomVariable muRV,
			DoubleRandomVariable sigmaRV)
    {
	// clone in case setMinimum or similar methods called.
	Class<?> clasz = muRV.getClass();
	try {
	    this.muRV = (DoubleRandomVariable)muRV.clone();
	    clasz = sigmaRV.getClass();
	    this.sigmaRV = (DoubleRandomVariable)sigmaRV.clone();
	    this.sigmaRV.tightenMinimum(0.0, false);
	    determineIfOrdered(muRV, sigmaRV);
	}  catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", clasz.getName());
	    throw new RandomVariableException(msg, e);
	}
    }

    protected LogNormalRV doNext() throws RandomVariableException {
	return new LogNormalRV(muRV.next(), sigmaRV.next());
    }
}

//  LocalWords:  exbundle LogNormal muRV sigmaRV setMinimum noClone
