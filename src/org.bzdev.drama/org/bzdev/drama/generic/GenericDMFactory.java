package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;

import org.bzdev.devqsim.*;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Map;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Abstract class for domain member factories.
 * This class includes the parameters necessary to initialize
 * a domain member with a set of domains.
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
 *  <li> <code>domain</code>.  Used to specify a set of domains that can added
 *       or removed from the domain member's domain set. This parameter uses
 *       a key to specify a domain and a boolean value indicating if that
 *       domain should track conditions or not for this domain factory.
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
abstract public class GenericDMFactory<
    OF extends GenericDMFactory<OF,S,A,C,D,DM,F,G,Obj>,
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>,
    Obj extends DM>
    extends SimObjectFactory<OF,S,Obj>
{
    S sim;
    // LinkedList<D> domains = new LinkedList<D>();
    LinkedHashMap<D,Boolean> domainMap = new LinkedHashMap<D,Boolean>();

    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	Object obj = super.clone();
	GenericDMFactory<OF,S,A,C,D,DM,F,G,Obj> obj1 =
	    (GenericDMFactory<OF,S,A,C,D,DM,F,G,Obj>) obj;
	obj1.domainMap = (LinkedHashMap<D,Boolean>)(domainMap.clone());
	return obj;
    }

    private void setParm1(Class domainClass) {
	parms[1] = new
	    Parm("domain",
		 domainClass, null,
		 new ParmParser() {
		     @SuppressWarnings("unchecked")
		      public void parse(org.bzdev.obnaming.NamedObjectOps key,
					boolean value)
			 throws IllegalArgumentException
		     {
			 if (key instanceof GenericDomain) {
			     try {
				 D mapkey = (D)key;
				 domainMap.put(mapkey, value);
			     } catch (Exception e) {
				 throw new IllegalArgumentException
				     (errorMsg("notDomain1", getParmName()));
				     /* ("key not a domain");*/
			     }
			 }
			 /*
			 D d = sim.getDomain(value);
			 if (d == null) {
			     throw new
				 IllegalArgumentException("value not a domain");
			 }
			 domains.add(d);
			 */
		     }
		     @SuppressWarnings("unchecked")
		     public void clear(org.bzdev.obnaming.NamedObjectOps key) {
			 if (key instanceof GenericDomain) {
			     try {
				 D mapkey = (D) key;
				 domainMap.remove(mapkey);
			     } catch (Exception e) {
				 throw new IllegalArgumentException
				     ("key not a domain");
			     }
			 }
			 // D d = sim.getDomain(key);
			 // domains.remove(d);
		     }
		     public void clear() {
			 domainMap.clear();
		     }
		 },
		 java.lang.Boolean.class);

    }


    Parm parms[] = {
	new Parm("domains", null, null,
		 new ParmParser() {
		     public void clear() {
			 domainMap.clear();
		     }
		 },
		 null),
	null,
    };

    public void clear() {
	super.clear();
	domainMap.clear();
    }


    /**
     * Constructor.
     * Subclasses must call this constructor.
     *<P>
     * Note: this is different than most of the named-object factory classes
     * in that the constructor takes the domainClass argument.  The reason is
     * that the common superclasses for domains for a specific simulation
     * flavor has to be provided by the subclass, but is needed when the
     * constructor is called.
     * @param sim the simulation
     * @param domainClass the simulation flavor's common domain class
     */
    protected GenericDMFactory (S sim,
				 Class<D> domainClass) {
	super(sim);
	this.sim = sim;
	initFactory(domainClass);
    }

    private void initFactory(Class<D> domainClass) {
	// fix up the type to make it more specific
	setParm1(domainClass);
	// parms[1].keyType = domainClass;
	initParms(parms, GenericDMFactory.class);
	addLabelResourceBundle("*.lpack.DMLabels", GenericDMFactory.class);
	addTipResourceBundle("*.lpack.DMTips", GenericDMFactory.class);
	addDocResourceBundle("*.lpack.DMDocs", GenericDMFactory.class);
    }

    @Override
    protected void initObject(Obj dm) {
	super.initObject(dm);
	for (Map.Entry<D,Boolean> entry: domainMap.entrySet()) {
	    D d = entry.getKey();
	    boolean value = entry.getValue();
	    dm.joinDomain(d, value);
	}
    }
}

//  LocalWords:  exbundle subclasses ul li boolean superclasses parms
//  LocalWords:  traceSetMode traceSets TraceSet SimObject LinkedList
//  LocalWords:  notDomain getDomain IllegalArgumentException keyType
//  LocalWords:  domainClass
