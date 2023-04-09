package org.bzdev.math.rv;
import java.util.Spliterator;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates a deterministic sequence of long values.
 */
public class DetermIATimeRV extends InterarrivalTimeRV {

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
     * Get the values for this random variable.
     * @return the values in the order in which they are generated,
     *         excluding a final value for the non-repeating case
     */
    public long[] getValues() {
	return values.clone();
    }

    /**
     * Get the final value for this random variable.
     * @return the final value; 0 if this random variable repeats
     */
    public long getFinalValue() {
	return finalValue;
    }

    /**
     * Determine if this random variable repeats its sequence.
     * @return true if it is repeating; false otherwise
     */
    public boolean isRepeating() {return repeat;}


    @Override
    public void setMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > minvalue): (min >= minvalue)) 
	    throw new IllegalArgumentException
		(errorMsg("tooLarge", min));
	super.setMinimum(min,closed);
    }

    @Override
    public void setMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < maxvalue):(max <= maxvalue))
	    throw new IllegalArgumentException
		(errorMsg("tooSmall", max));
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
    public DetermIATimeRV(long[] values, long finalValue) {
	super();
	for (int i = 0; i < values.length; i++) {
	    long x = values[i];
	    if (x < 0) throw new IllegalArgumentException
			   (errorMsg("valAtIndNonNegative", x, i));
	    if (x < minvalue) minvalue = x;
	    if (x > maxvalue) maxvalue = x;
	}
	if (finalValue < 0)
	    throw new IllegalArgumentException
		(errorMsg("finalNotNegative", finalValue));
	this.values = values;
	this.finalValue = finalValue;
    }

    /**
     * Constructor for a repeating sequence.
     * @param values the values of the random variable in the order in which
     *        these are returned
     */
    public DetermIATimeRV(long[] values) {
	super();
	repeat = true;
	for (int i = 0; i < values.length; i++) {
	    long x = values[i];
	    if (x < 0) throw new IllegalArgumentException
			   (errorMsg("valAtIndNonNegative", x, i));
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
    public DetermIATimeRV(long value, long finalValue) {
	super();
	if (value < 0) throw new IllegalArgumentException
			   (errorMsg("valNotNegative", value));
	if (finalValue < 0) throw new IllegalArgumentException
				(errorMsg("finalNotNegative", finalValue));
	long[] values = new long[1];
	values[0] = value;
	minvalue = value;
	maxvalue = value;
	this.values = values;
	this.finalValue = finalValue;
    }

    @Override
    public Long next() {
	if (!repeat && index == values.length) return finalValue;
	try {
	    return values[index++];
	} finally {
	    if (repeat && index == values.length) index = 0;
	}
    }

    @Override
    public String toString() {
	return "DetermDetermIATime(" + values 
	    + (repeat? "": "," + finalValue) + ")";
    }

    @Override
    protected int getCharacteristics() {
	return Spliterator.IMMUTABLE | Spliterator.NONNULL
	    | Spliterator.ORDERED;
    }
}

//  LocalWords:  exbundle tooLarge tooSmall finalValue valNotNegative
//  LocalWords:  valAtIndNonNegative finalNotNegative
//  LocalWords:  DetermDetermIATime
