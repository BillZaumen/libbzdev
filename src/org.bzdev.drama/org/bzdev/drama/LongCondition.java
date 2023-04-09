package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Class for long-valued conditions.
 */
public class LongCondition extends Condition {
    long value = 0;

    /**
     * Get the value associated with this condition.
     * Does not notify domains of a change in value.
     * @return the condition's value
     */
    public long getValue() {return value;}

    /**
     * Set the value associated with this condition
     * Calling this method does not notify domains of the change.
     * A subclass may have to override this method as follows:
     * <blockquote><pre><code>
     *      protected void setValue(long value) {
     *          super.setValue(value);
     *      }
     * </CODE></PRE></blockquote>
     * in order to make the method accessible to classes in the
     * subclass' package.
     * @param value the new value
     */
    protected void setValue(long value) {
	this.value = value;
    }


    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the condition; null if generated
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public LongCondition(DramaSimulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.value = value;
    }

    /**
     * Constructor with an initial value.
     * @param sim the simulation
     * @param name the name of the condition; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param value the initial value for the condition
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public LongCondition(DramaSimulation sim, String name, boolean intern,
			    long value)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.value = value;
    }
}

//  LocalWords:  blockquote pre setValue IllegalArgumentException
