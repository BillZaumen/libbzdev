package org.bzdev.drama;
import org.bzdev.devqsim.*;

/**
 * Factory for creating instances of the class IntegerCondition.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/IntegerConditionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/IntegerConditionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class IntegerConditionFactory
    extends AbstractIntegerCondFactory<IntegerCondition>
{
    /**
     * Constructor.
     * @param sim the simulation
     */
    public IntegerConditionFactory(DramaSimulation sim) {
	super(sim);
	initInitialValueParm(null, false, null, false, 0);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public IntegerConditionFactory() {
	this(null);
    }

    @Override
    protected IntegerCondition newObject(String name) {
	return new IntegerCondition(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  IntegerCondition IFRAME SRC px steelblue HREF
