package org.bzdev.math.rv;
import java.util.Spliterator;

/**
 * Random variable that generates a deterministic sequence of boolean values.
 */
public class DetermBooleanRV extends BooleanRandomVariable {
    private boolean[] values;
    private boolean finalValue;
    private boolean repeat = false;
    int index = 0;

    /**
     * Get the values.
     * @return an array of the values, excluding the final value
     */
    public boolean[] getValues() {
	return values.clone();
    }

    /**
     * Get the final value
     * @return the final value; undefined if the sequence repeats forever
     */
    public boolean getFinalValue() {
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

    /**
     * Constructor for a repeating sequence.
     * @param values the values of the random variable in the order in which
     *        these are returned
     */
    public DetermBooleanRV(boolean[] values) {
	super();
	repeat = true;
	this.values = values;
    }


    /**
     * Constructor given a starting value and a final value.
     * @param value the starting value of the random variable
     * @param finalValue the subsequent value of the random variable
     */
    public DetermBooleanRV(boolean value, boolean finalValue) {
	super();
	boolean[] values = new boolean[1];
	values[0] = value;
	this.values = values;
	this.finalValue = finalValue;
    }

    /**
     * Constructor given initial values and a final value.
     * @param values the initial sequence of values for the random variable
     * @param finalValue the subsequent value of the random variable after
     *        the initial sequence is used
     */
    public DetermBooleanRV(boolean[] values, boolean finalValue) {
	super();
	this.values = values;
	this.finalValue = finalValue;
    }


    public Boolean next() {
	if (!repeat && index == values.length) return finalValue;
	try {
	    return values[index++];
	} finally {
	    if (repeat && index == values.length) index = 0;
	}
    }

    public String toString() {
	return "DetermBooleanRV(" + values 
	    + (repeat? "": "," + finalValue) + ")";
    }

    @Override
    protected int getCharacteristics() {
	return Spliterator.IMMUTABLE | Spliterator.NONNULL
	    | Spliterator.ORDERED;
    }

}

//  LocalWords:  boolean finalValue DetermBooleanRV
