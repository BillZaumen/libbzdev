package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Class representing real-valued conditions.
 */
public class DoubleCondition extends Condition
{
    double value = 0.0;

    /**
     * Get a condition's value.
     * @return the value of the condition
     */
    public double getValue() {return value;}

    /**
     * Set a condition's value.
     * Calling this method does not notify domains of the change.
     * A subclass may have to override this method as follows:
     * <blockquote><pre><code>
     *      protected void setValue(double value) {
     *          super.setValue(value);
     *      }
     * </CODE></PRE></blockquote>
     * in order to make the method accessible to classes in the
     * subclass' package.
     * @param value the new value
     */
    protected void setValue(double value) {
	this.value = value;
    }


    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the condition; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public DoubleCondition(DramaSimulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Constructor with an initial value.
     * @param sim the simulation
     * @param name the name of the condition; null if generated
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param value the initial value of the condition
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public DoubleCondition(DramaSimulation sim, String name, boolean intern,
			   double value)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.value = value;
    }
}

//  LocalWords:  blockquote pre setValue IllegalArgumentException
