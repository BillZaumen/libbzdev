package org.bzdev.math.rv;

/**
 * Random variable that always generates the same boolean value.
 * Useful when passing a constant to a method that expects a
 * random variable.
 */
public class FixedBooleanRV extends BooleanRandomVariable {
    private boolean value;

    /**
     * Get the value of this random variable, which is a constant.
     * @return the value
     */
    public boolean getValue() {
	return value;
    }

    /**
     * Constructor.
     * @param value the value of this random variable
     */

    public FixedBooleanRV(boolean value) {
	this.value = value;
    }

    public Boolean next() {
	return value;
    }

    public String toString() {
	return "FixedBooleanRV(" + value + ")";
    }
}

//  LocalWords:  boolean FixedBooleanRV
