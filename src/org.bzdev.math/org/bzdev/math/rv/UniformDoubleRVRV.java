package org.bzdev.math.rv;

/**
 * Random variable that generates the same integer-valued random variable 
 * repeatedly.
 */
public class UniformDoubleRVRV
    extends DoubleRandomVariableRV<UniformDoubleRV>
{
    DoubleRandomVariable lowerLimitRV;
    boolean lowerClosed;
    DoubleRandomVariable upperLimitRV;
    boolean upperClosed;
    
    /**
     * Constructor.
     * @param lowerLimitRV random variable for the lower limit for the random
     *        variable produced
     * @param upperLimitRV random variable for the lower limit for the random
     *        variable produced
     */
    public UniformDoubleRVRV(DoubleRandomVariable lowerLimitRV,
			      DoubleRandomVariable upperLimitRV) {
	this(lowerLimitRV, true, upperLimitRV, false);
    }

    /**
     * Constructor specifying random variables for range type 
     * (open, closed, etc.).
     * @param lowerLimitRV random variable for the lower limit for the random
     *        variable produced
     * @param lowerClosed if the lower limit is included in the range
     *        and false if it is not
     * @param upperLimitRV random variable for the lower limit for the random
     *        variable produced
     * @param upperClosed true if the upper limit is included in the
     *        range and false if it is not
     *
     */
    public UniformDoubleRVRV(DoubleRandomVariable lowerLimitRV,
			    boolean lowerClosed,
			    DoubleRandomVariable upperLimitRV,
			    boolean upperClosed)
    {
	try {
	    this.lowerLimitRV = (DoubleRandomVariable)(lowerLimitRV.clone());
	    this.lowerClosed = lowerClosed;
	    this.upperLimitRV = (DoubleRandomVariable)(upperLimitRV.clone());
	    this.upperClosed = upperClosed;
	    determineIfOrdered(lowerLimitRV, upperLimitRV);
	} catch (CloneNotSupportedException cnse) {
	    throw new RuntimeException("could not clone", cnse);
	}
    }


    protected UniformDoubleRV doNext() throws RandomVariableException {
	return new UniformDoubleRV(lowerLimitRV.next(),
				    lowerClosed,
				    upperLimitRV.next(),
				    upperClosed);
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	UniformDoubleRVRV obj = (UniformDoubleRVRV) super.clone();
	return obj;
    }
}

//  LocalWords:  lowerLimitRV upperLimitRV lowerClosed upperClosed
