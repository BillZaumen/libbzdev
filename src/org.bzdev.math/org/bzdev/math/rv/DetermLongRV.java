package org.bzdev.math.rv;
import java.util.Spliterator;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates a deterministic sequence of long values.
 */
public class DetermLongRV extends LongRandomVariable {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private long maxvalue = Long.MIN_VALUE;
    private long minvalue = Long.MAX_VALUE;

    private long[] values;
    private long finalValue;
    private boolean repeat = false;
    int index = 0;
    
    /**
     * Get the values.
     * @return an array of the values, excluding the final value
     */
    public long[] getValues() {
	return values.clone();
    }

    /**
     * Get the final value
     * @return the final value; undefined if the sequence repeats forever
     */
    public long getFinalValue() {
	return finalValue;
    }

    /**
     * Determine if the sequence is a repeating one.
     * @return true if repeating, false otherwise.
     */
    public boolean isRepeating() {
	return repeat;
    }


    public void setMinimum(long min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > minvalue): (min >= minvalue)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.setMinimum(min, closed);
    }

    public void setMaximum(long max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < maxvalue):(max <= maxvalue))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    /**
     * Constructor.
     * When next is called, after the values in the first argument are
     * returned in order, the final value is returned persistently
     * from then on.
     * @param values the values of the random variable in the order in which
     *        these are returned
     * @param finalValue the final value of the random variable
     */
    public DetermLongRV(long[] values, long finalValue) {
	super();
	for (int i = 0; i < values.length; i++) {
	    long x = values[i];
	    if (x < minvalue) minvalue = x;
	    if (x > maxvalue) maxvalue = x;
	}
	this.values = values;
	this.finalValue = finalValue;
    }

    /**
     * Constructor for a repeating sequence.
     * @param values the values of the random variable in the order in which
     *        these are returned
     */
    public DetermLongRV(long[] values) {
	super();
	repeat = true;
	for (int i = 0; i < values.length; i++) {
	    long x = values[i];
	    if (x < minvalue) minvalue = x;
	    if (x > maxvalue) maxvalue = x;
	}
	this.values = values;
    }

    /**
     * Constructor given a starting value and a final value.
     * @param value the starting value of the random variable
     * @param finalValue the subsequent value of the random variable
     */
    public DetermLongRV(long value, long finalValue) {
	super();
	long[] values = new long[1];
	values[0] = value;
	minvalue = value;
	maxvalue = value;
	this.values = values;
	this.finalValue = finalValue;
    }

    public Long next() {
	if (!repeat && index == values.length) return finalValue;
	try {
	    return values[index++];
	} finally {
	    if (repeat && index == values.length) index = 0;
	}
    }

    public String toString() {
	return "DetermLongRV(" + values 
	    + (repeat? "": "," + finalValue) + ")";
    }

    @Override
    protected int getCharacteristics() {
	return Spliterator.IMMUTABLE | Spliterator.NONNULL
	    | Spliterator.ORDERED;
    }
}

//  LocalWords:  exbundle tooLarge tooSmall finalValue DetermLongRV
