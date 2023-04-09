package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Class for boolean-valued conditions.
 */

public class BooleanCondition extends Condition
{
    boolean value = false;

    /**
     * Get the value for the condition.
     * @return the value
     */
    public boolean getValue() {return value;}


    /**
     * Set the value for the condition.
     * Calling this method does not notify domains of the change.
     * A subclass may have to override this method as follows:
     * <blockquote><pre><code>
     *      protected void setValue(boolean value) {
     *          super.setValue(value);
     *      }
     * </CODE></PRE></blockquote>
     * in order to make the method accessible to classes in the
     * subclass' package.
     * @param value the value
     */
    protected void setValue(boolean value) {
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
    public BooleanCondition(DramaSimulation sim, String name, boolean intern)
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
     * @param value the initial value for the condition
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public BooleanCondition(DramaSimulation sim, String name, boolean intern,
			    boolean value)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.value = value;
    }
}

//  LocalWords:  boolean blockquote pre setValue
//  LocalWords:  IllegalArgumentException
