package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that always generates the same interarrival time.
 * Useful when passing a constant to a method that expects a
 * random variable.
 */
public class FixedIATimeRV extends InterarrivalTimeRV {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private long value;

    /**
     * Get the value this random variable always produces.
     * @return the value
     */
    public long getValue() {
	return value;
    }

    @Override
    public void setMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException
		(errorMsg("tooLarge", min));
	super.setMinimum(min, closed);
    }

    @Override
    public void setMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < value):(max <= value))
	    throw new IllegalArgumentException
		(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    /**
     * Constructor.
     * @param value the value of the random variable
     */
    public FixedIATimeRV(long value) throws IllegalArgumentException {
	super();
	if (value < 0) 
	    throw new IllegalArgumentException
		(errorMsg("valNotNegative", value));
	this.value = value;
    }

    public Long next() {
	return value;
    }

    public String toString() {
	return "FixedIATimeRV(" +getValue() + ")";
    }
}

//  LocalWords:  exbundle interarrival tooLarge tooSmall
//  LocalWords:  valNotNegative FixedIATimeRV
