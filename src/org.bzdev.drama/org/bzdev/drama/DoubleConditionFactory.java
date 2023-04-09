package org.bzdev.drama;
import org.bzdev.devqsim.*;

/**
 * Factory for creating instances of the class DoubleCondition.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/DoubleConditionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/DoubleConditionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class DoubleConditionFactory
    extends AbstractDoubleCondFactory<DoubleCondition>
{
    /**
     * Constructor.
     * @param sim the simulation
     */
    public DoubleConditionFactory(DramaSimulation sim) {
	super(sim);
	initInitialValueParm(null, false, null, false, 0.0);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public DoubleConditionFactory() {
	this(null);
    }

    @Override
    protected DoubleCondition newObject(String name) {
	return new DoubleCondition(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  DoubleCondition IFRAME SRC px steelblue HREF
