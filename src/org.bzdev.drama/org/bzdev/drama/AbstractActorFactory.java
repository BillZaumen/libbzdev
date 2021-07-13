package org.bzdev.drama;
import org.bzdev.drama.generic.GenericActorFactory;

/**
 * Abstract Actor factory.
 * This class is the base class for factories that create subclasses
 * of org.bzdev.drama.Actor.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of Actor, each
 * responsible for providing parameters for the corresponding Actor
 * subclass, plus a factory that is not abstract for each class that
 * can be created.
 * <P>
 * The parameters defined by this factory are:
 * <ul>
 *  <li> "domainMember".  Used to configure an instance of
 *       DomainMember for handling domain membership.
 *  <li> "domains".  Used to clear a set of domains explicitly
 *       provided, but cannot be used to set or add domains.
 *  <li> "domain".  Used to specify a domain that can added
 *       or removed from the actor's domain set. For this parameter,
 *       the key is the domain and the value is a boolean
 *       that is true if conditions for that domain should be tracked;
 *       false otherwise. If a specified domain was already joined by
 *       a shared domain member, an explicit request to join that
 *       domain will be ignored when an actor is created.
 * </ul>
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
 *                  <li> "KEEP" - keep the existing trace sets,
 *                       adding additional ones specified by the
 *                       parameter "timeline.traceSets".
 *                  <li> "REMOVE" - remove the trace sets specified
 *                       by the parameter "timeline.traceSets".
 *                  <li> "REPLACE" - remove all existing trace sets
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
abstract public class AbstractActorFactory<Obj extends Actor>
    extends GenericActorFactory<AbstractActorFactory<Obj>, DramaSimulation,
	    Actor,Condition,Domain,DomainMember,DramaFactory,Group,Obj>
{
    /**
     * Constructor.
     * @param sim the simulation associated with this factory
     */
    protected AbstractActorFactory(DramaSimulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses ul li domainMember boolean superclasses
//  LocalWords:  traceSetMode traceSets TraceSet SimObject
