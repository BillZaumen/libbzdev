package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that always generates the same integer value.
 * Useful when passing a constant to a method that expects a
 * random variable.
 */
public class FixedIntegerRV extends IntegerRandomVariable {
    private int value;

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    /**
     * Get the value of this random variable, which is a constant.
     * @return the value
     */
    public int getValue() {
	return value;
    }

    public void setMinimum(Integer min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.setMinimum(min,closed);
    }

    public void tightenMinimum(Integer min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > value): (min >= value)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.tightenMinimum(min,closed);
    }

    public void setMaximum(Integer max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < value):(max <= value))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    public void tightenMaximum(Integer max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < value):(max <= value))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    /**
     * Constructor.
     * @param value the value of the random variable
     */
    public FixedIntegerRV(int value) {
	super();
	this.value = value;
    }

    public Integer next() {
	return value;
    }

    public String toString() {
	return "FixedIntegerRV(" + value + ")";
    }
}

//  LocalWords:  exbundle tooLarge tooSmall FixedIntegerRV
