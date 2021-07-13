package org.bzdev.math.rv;

/**
 * Random variable that generates the same integer-valued random variable 
 * repeatedly.
 */
public class UniformIntegerRVRV
    extends IntegerRandomVariableRV<UniformIntegerRV>
{
    IntegerRandomVariable lowerLimitRV;
    boolean lowerClosed;
    IntegerRandomVariable upperLimitRV;
    boolean upperClosed;
    
    // static FixedBooleanRV trueRV = new FixedBooleanRV(true);
    // static FixedBooleanRV falseRV = new FixedBooleanRV(true);

    /**
     * Constructor.
     * @param lowerLimitRV random variable for the lower limit for the random
     *        variable produced
     * @param upperLimitRV random variable for the lower limit for the random
     *        variable produced
     */
    public UniformIntegerRVRV(IntegerRandomVariable lowerLimitRV,
			      IntegerRandomVariable upperLimitRV) {
	this(lowerLimitRV, true, upperLimitRV, false);
    }

    /**
     * Constructor specifying random variables for range type 
     * (open, closed, etc.).
     * @param lowerLimitRV random variable for the lower limit for the random
     *        variable produced
     * @param lowerClosed true if the lower limit is included in the
     *        range and false if it is not
     * @param upperLimitRV random variable for the lower limit for the random
     *        variable produced
     * @param upperClosed true if the upper limit is included in the
     *        range and false if it is not
     *
     */
    public UniformIntegerRVRV(IntegerRandomVariable lowerLimitRV,
			    boolean lowerClosed,
			    IntegerRandomVariable upperLimitRV,
			    boolean upperClosed)
    {
	try {
	    this.lowerLimitRV = (IntegerRandomVariable)(lowerLimitRV.clone());
	    this.lowerClosed = lowerClosed;
	    this.upperLimitRV = (IntegerRandomVariable)(upperLimitRV.clone());
	    this.upperClosed = upperClosed;
	    determineIfOrdered(lowerLimitRV, upperLimitRV);
	} catch (CloneNotSupportedException cnse) {
	    throw new RuntimeException("could not clone", cnse);
	}
    }


    protected UniformIntegerRV doNext() throws RandomVariableException {
	return new UniformIntegerRV(lowerLimitRV.next(),
				    lowerClosed,
				    upperLimitRV.next(),
				    upperClosed);
    }
    
    


    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	UniformIntegerRVRV obj = (UniformIntegerRVRV) super.clone();
	return obj;
    }
}

//  LocalWords:  FixedBooleanRV trueRV falseRV lowerLimitRV
//  LocalWords:  upperLimitRV lowerClosed upperClosed
