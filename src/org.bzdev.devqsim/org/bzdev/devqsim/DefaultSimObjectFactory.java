package org.bzdev.devqsim;


/**
 * Abstract factory for constructing subclasses of DefaultSimObject.
 * This class merely specializes some generic types. It provides
 * no additional parameters and inherits the parameters
 * timeline, timeline.time, timeline.traceSetMode, timeline.traceSets,
 * and traceSets.
 */
public abstract class DefaultSimObjectFactory<
    OBJ extends DefaultSimObject>
    extends SimObjectFactory<DefaultSimObjectFactory<OBJ>,Simulation,OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected DefaultSimObjectFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses DefaultSimObject traceSetMode traceSets
