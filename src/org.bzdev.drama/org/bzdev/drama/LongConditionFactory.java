package org.bzdev.drama;
import org.bzdev.devqsim.*;

/**
 * Factory for creating instances of the class LongCondition.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/LongConditionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/LongConditionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class LongConditionFactory
    extends AbstractLongCondFactory<LongCondition>
{
    /**
     * Constructor.
     * @param sim the simulation
     */
    public LongConditionFactory(DramaSimulation sim) {
	super(sim);
	initInitialValueParm(null, false, null, false, 0L);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public LongConditionFactory() {
	this(null);
    }

    @Override
    protected LongCondition newObject(String name) {
	return new LongCondition(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  LongCondition IFRAME
