package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialDoubleRV;

/**
 * Named random variable using a random variable that generates a
 * sequence of double-precision numbers with a binomial distribution.
 * @see org.bzdev.math.rv.BinomialDoubleRV
 */
public class SimBinomialDoubleRV extends SimDoubleRV<BinomialDoubleRV>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    protected SimBinomialDoubleRV(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    /**
     * Constructor given a random variable.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * @param rv the random variable itself
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public SimBinomialDoubleRV(Simulation sim, String name, boolean intern,
			 BinomialDoubleRV rv)
    {
	super(sim, name, intern, rv);
	super.setRV(rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
