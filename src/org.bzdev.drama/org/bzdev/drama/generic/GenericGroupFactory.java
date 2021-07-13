package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;

import org.bzdev.devqsim.*;
import java.util.List;
import java.util.LinkedList;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Abstract class for group factories.
 * This class includes the common parameters necessary to initialize
 * groups.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <P>
 * Parameters:
 * <ul>
 *  <li> <code>domains</code>.  Used to clear a set of domains explicitly
 *       provided, but cannot be used to set or add domains.
 *  <li> <code>domain</code>.  Used to specify a domain that can added
 *       or removed from the group's domain set.
 * </ul>
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
abstract public class GenericGroupFactory<
    OF extends GenericGroupFactory<OF,S,A,C,D,DM,F,G,Obj>,
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>,
    Obj extends G>
    extends SimObjectFactory<OF,S,Obj>
{
    S sim;

    LinkedList<D> domains = new LinkedList<D>();

    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	Object obj = super.clone();
	GenericGroupFactory<OF,S,A,C,D,DM,F,G,Obj> obj1
	    = (GenericGroupFactory<OF,S,A,C,D,DM,F,G,Obj>) obj;
	obj1.domains = (LinkedList<D>)(domains.clone());
	return obj;
    }


    Parm parms[] = {
	new Parm("domains",
		 null, null,
		 new ParmParser() {
		     public void clear() {
			 domains.clear();
		     }
		 },
		 null),
	new Parm("domain",
		 GenericDomain.class, null,
		 new ParmParser() {
		     public void parse(String value)
			 throws IllegalArgumentException
		     {
			 D d = sim.getDomain(value);
			 if (d == null) {
			     throw new
				 IllegalArgumentException
				 (errorMsg("notDomain1", getParmName()));
				 /*("value not a domain");*/
			 }
			 domains.add(d);
		     }
		     public void clear(String key) {
			 D d = sim.getDomain(key);
			 domains.remove(d);
		     }
		 },
		 null),
    };

    public void clear() {
	super.clear();
	domains.clear();
    }

    /**
     * Constructor.
     * Subclasses must call this constructor.
     * @param sim the simulation
     */
    protected GenericGroupFactory(S sim)
    {
	super(sim);
	this.sim = sim;
	initParms(parms, GenericGroupFactory.class);
	addLabelResourceBundle("*.lpack.GroupLabels",
			       GenericGroupFactory.class);
	addTipResourceBundle("*.lpack.GroupTips", GenericGroupFactory.class);
	addDocResourceBundle("*.lpack.GroupDocs", GenericGroupFactory.class);
    }

    @Override
    protected void initObject(Obj group) {
	super.initObject(group);
	for (D d: domains) {
	    group.joinDomain(d);
	}
    }
}

//  LocalWords:  exbundle subclasses ul li superclasses traceSetMode
//  LocalWords:  traceSets TraceSet SimObject notDomain
