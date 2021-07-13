package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;
/**
 * Random variable that generates true or false with equal probabilities.
 */
public class UniformBooleanRV extends BooleanRandomVariable {
    public Boolean next() {
	return StaticRandom.nextBoolean();
    }

    public String toString() {
	return "UniformBooleanRV()";
    }
}

//  LocalWords:  UniformBooleanRV
