package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV


/**
 * Random variable that always generates the same double-precision value.
 * Useful when passing a constant to a method that expects a
 * random variable.
 */
public class FixedDoubleRV extends DoubleRandomVariable {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double value;

    /**
     * Get the value of this random variable, which is a constant.
     * @return the value
     */
    public double getValue() {
	return value;
    }

    public void setMinimum(double min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.setMinimum(min, closed);
    }

    public void tightenMinimum(Double min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.tightenMinimum(min, closed);
    }


    public void setMaximum(double max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < value):(max <= value))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    public void tightenMaximum(Double max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < value):(max <= value))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.tightenMaximum(max, closed);
    }

    /**
     * Constructor.
     * @param value the value of the random variable
     */
    public FixedDoubleRV(double value) {
	super();
	this.value = value;
    }

    public Double next() {
	return value;
    }

    public String toString() {
	return "FixedDoubleRV(" + value + ")";
    }
}

//  LocalWords:  exbundle tooLarge tooSmall FixedDoubleRV
