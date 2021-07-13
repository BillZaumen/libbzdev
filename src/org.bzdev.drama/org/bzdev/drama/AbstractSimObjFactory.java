package org.bzdev.drama;
import org.bzdev.devqsim.*;

/**
 * Simulation factory for additional simulation objects.
 * This class is provided for convenience.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of DramaSimObject, each
 * responsible for providing parameters for the corresponding
 * DramaSimObject subclass, plus a factory that is not abstract for
 * each class that can be created.
 * <P>
 * The parameters inherited from superclasses are:
 * <ul>
 *   <li> "timeline" - an integer-keyed set of values that define
 *         changes in the object's configuration. Subclasses may provide
 *         additional parameters.  The default parameters are:
 *        <ul>
 *           <li> "timeline.time" - the time at which timeline parameters
 *                are to change. This parameter must be provided if a
 *                timeline entry exists.  The units are those used by the
 *                double-precision time unit for the simulation (for
 *                animations, this is generally seconds).
 *           <li> "timeline.traceSetMode" - indicates how the parameter
 *                "timeline.traceSets" is interpreted. the values are
 *                enumeration constants of type
 *                {@link org.bzdev.devqsim.TraceSetMode} and are used as
 *                follows:
 *                <ul>
 *                  <li> <code>KEEP</code> - keep the existing trace sets,
 *                       adding additional ones specified by the
 *                       parameter "timeline.traceSets".
 *                  <li> <code>REMOVE</code> - remove the trace sets specified
 *                       by the parameter "timeline.traceSets".
 *                  <li> <code>REPLACE</code> - remove all existing trace sets
 *                       and replace those with the ones specified by
 *                       the timeline.traceSets parameter.
 *                </ul>
 *           <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
 *        </ul>
 *   <li> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 * </ul>
 */
abstract public class AbstractSimObjFactory
    <Obj extends DramaSimObject>
    extends SimObjectFactory<AbstractSimObjFactory<Obj>, DramaSimulation, Obj>
{
    /**
     * Constructor.
     * Subclasses must call this constructor.
     * @param sim the simulation
     */
    protected AbstractSimObjFactory (DramaSimulation sim)
    {
	super(sim);
    }
}

//  LocalWords:  subclasses DramaSimObject superclasses ul li
//  LocalWords:  traceSetMode traceSets TraceSet SimObject
