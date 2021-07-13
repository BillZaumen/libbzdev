package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that always generates the same long integer.
 * Useful when passing a constant to a method that expects a
 * random variable.
 */
public class FixedLongRV extends LongRandomVariable {
    private long value;

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    /**
     * Get the value of this random variable, which is a constant.
     * @return the value
     */
    public long getValue() {
	return value;
    }

    public void setMinimum(Long min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.setMinimum(min, closed);
    }

    public void tightenMinimum(Long min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.tightenMinimum(min, closed);
    }

    public void setMaximum(Long max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < value):(max <= value))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    public void tightenMaximum(Long max, boolean closed) 
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
    public FixedLongRV(long value) {
	super();
	this.value = value;
    }

    public Long next() {
	return value;
    }

    public String toString() {
	return "FixedLongRV(" + value + ")";
    }
}

//  LocalWords:  exbundle tooLarge tooSmall FixedLongRV
