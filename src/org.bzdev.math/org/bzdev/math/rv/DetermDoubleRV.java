package org.bzdev.math.rv;
import java.util.Spliterator;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that always generates the same double-precision value.
 * Useful when passing a constant to a method that expects a
 * random variable.
 */
public class DetermDoubleRV extends DoubleRandomVariable {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double maxvalue = Double.NEGATIVE_INFINITY;
    private double minvalue = Double.POSITIVE_INFINITY;

    private double[] values;
    private double finalValue;
    private boolean repeat = false;
    private int index = 0;

    /**
     * Get the values.
     * @return an array of the values, excluding the final value
     */
    public double[] getValues() {
	return values.clone();
    }

    /**
     * Get the final value
     * @return the final value; undefined if the sequence repeats forever
     */
    public double getFinalValue() {
	return finalValue;
    }

    /**
     * Determine if the sequence is a repeating one.
     * @return true if repeating, false otherwise.
     */
    public boolean isRepeating() {
	return repeat;
    }

    /**
     * Set the minimum value for this random variable.
     * @param min the minimum value
     * @param closed true if the minimum value may be generated;
     *        false otherwise
     * @exception UnsupportedOperationException - if this operation is not
     *            supported
     * @exception  IllegalArgumentException - if an argument is out of range
     */
    public void setMinimum(double min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (min > minvalue): (min >= minvalue)) 
	    throw new IllegalArgumentException(errorMsg("tooLarge", min));
	super.setMinimum(min,closed);
    }

    /**
     * Set the maximum value for this random variable.
     * @param max the maximum value
     * @param closed true if the maximum value may be generated;
     *        false otherwise
     * @exception UnsupportedOperationException - if this operation is not
     *            supported
     * @exception  IllegalArgumentException - if an argument is out of range
     */
    public void setMaximum(double max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (closed? (max < maxvalue):(max <= maxvalue))
	    throw new IllegalArgumentException(errorMsg("tooSmall", max));
	super.setMaximum(max, closed);
    }

    /**
     * Constructor.
     * After the values are returned, the final value is returned and does
     * not change.
     * @param values the values of the random variable in the order in which
     *        these are returned
     * @param finalValue the final value of the random variable
     */
    public DetermDoubleRV(double[] values, double finalValue) {
	super();
	for (int i = 0; i < values.length; i++) {
	    double x = values[i];
	    if (x < minvalue) minvalue = x;
	    if (x > maxvalue) maxvalue = x;
	}
	this.values = values;
	this.finalValue = finalValue;
    }

    /**
     * Constructor.
     * The values will be repeated indefinitely.
     * @param values the values of the random variable in the order in which
     *        these are returned
     */
    public DetermDoubleRV(double[] values) {
	super();
	repeat = true;
	for (int i = 0; i < values.length; i++) {
	    double x = values[i];
	    if (x < minvalue) minvalue = x;
	    if (x > maxvalue) maxvalue = x;
	}
	this.values = values;
	this.finalValue = finalValue;
    }

    /**
     * Constructor given a starting value and a final value.
     * @param value the values of the random variable in the order in which
     *        these are returned
     * @param finalValue the final value of the random variable
     */
    public DetermDoubleRV(double value, double finalValue) {
	super();
	double[] values = new double[1];
	values[0] = value;
	minvalue = value;
	maxvalue = value;
	this.values = values;
	this.finalValue = finalValue;
    }



    public Double next() {
	if (!repeat && index == values.length) return finalValue;
	try {
	    return values[index++];
	} finally {
	    if (repeat && index == values.length) index = 0;
	}
    }

    public String toString() {
	return "DetermDoubleRV(" + values 
	    + (repeat? "": ("," + finalValue)) + ")";
    }

    @Override
    protected int getCharacteristics() {
	return Spliterator.IMMUTABLE | Spliterator.NONNULL
	    | Spliterator.ORDERED;
    }
}

//  LocalWords:  exbundle tooLarge tooSmall finalValue DetermDoubleRV
