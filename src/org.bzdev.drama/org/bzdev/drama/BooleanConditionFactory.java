package org.bzdev.drama;
import org.bzdev.devqsim.*;

/**
 * Factory for creating instances of the class BooleanCondition.
 * <P>
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/BooleanConditionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/BooleanConditionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class BooleanConditionFactory
    extends AbstractBooleanCondFactory<BooleanCondition>
{
    /**
     * Constructor.
     * @param sim the simulation
     */
    public BooleanConditionFactory(DramaSimulation sim) {
	super(sim);
	initInitialValueParm(false);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public BooleanConditionFactory() {
	this(null);
    }

    @Override
    protected BooleanCondition newObject(String name) {
	return new BooleanCondition(getSimulation(), name, willIntern());
    }


}

//  LocalWords:  BooleanCondition IFRAME SRC px steelblue HREF
