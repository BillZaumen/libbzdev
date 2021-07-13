package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;

import org.bzdev.devqsim.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Abstract class for actor factories.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <P>
 * This class includes the common parameters necessary to initialize
 * actors.
 * <P>
 * Parameters:
 * <ul>
 *  <li> <code>domainMember</code>.  Used to configure an instance of
 *       DomainMember as a shared domain member for handling domain membership.
 *  <li> <code>domains</code>.  Used to clear a set of domains explicitly
 *       provided, but cannot be used to set or add domains.
 *  <li> <code>domain</code>.  Used to specify a domain that can added
 *       or removed from the actor's domain set. For this parameter,
 *       the key is the domain and the value is a boolean
 *       that is true if conditions for that domain should be tracked;
 *       false otherwise. If a specified domain was already joined by
 *       a shared domain member, an explicit request to join that
 *       domain will be ignored when an actor is created.
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

abstract public class GenericActorFactory<
    OF extends GenericActorFactory<OF,S,A,C,D,DM,F,G,Obj>,
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>,
    Obj extends A>
    extends SimObjectFactory<OF,S,Obj>
{
    S sim;
    // LinkedList<D> domains = new LinkedList<D>();
    LinkedHashMap<D,Boolean> domainMap = new LinkedHashMap<D,Boolean>();

    DM domainMember = null;

    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	Object obj = super.clone();
	GenericActorFactory<OF,S,A,C,D,DM,F,G,Obj> obj1
	    = (GenericActorFactory<OF,S,A,C,D,DM,F,G,Obj>) obj;
	// obj1.domains = (LinkedList<D>)(domains.clone());
	obj1.domainMap = (LinkedHashMap<D,Boolean>)(domainMap.clone());
	return obj;
    }

    Parm parms[] = {
	new
	Parm("domainMember",
	      null, null,
	      new ParmParser() {
		  @SuppressWarnings("unchecked")
		  public void parse(org.bzdev.obnaming.NamedObjectOps value)
		      throws IllegalArgumentException, IllegalStateException
		  {
		      if (value instanceof GenericDomainMember) {
			  try {
			      DM dm = (DM)value;
			      domainMember = dm;
			  } catch (Exception e) {
			      throw new IllegalArgumentException
				  (errorMsg("notDomainMember", getParmName()));
				  /*("value not a domain member");*/
			  }
		      }
		      /*
		      DM dm = sim.getDomainMember(name);
		      if (dm == null) {
			  throw new
			      IllegalArgumentException("domain member \""
						       +dm
						       +"\" does not exist");
		      }
		      domainMember = dm;
		      */
		  }
		  public void clear() {
		      domainMember = null;
		  }
	      },
	      GenericDomainMember.class),
	new
	Parm("domains",
	      null, null,
	      new ParmParser() {
		  public void clear() {
		      domainMap.clear();
		  }
	      },
	      null),
	new
	Parm("domain",
	      GenericDomain.class, null,
	      new ParmParser() {
		  @SuppressWarnings("unchecked")
		  public void parse(org.bzdev.obnaming.NamedObjectOps key,
				    boolean value)
		      throws IllegalArgumentException, IllegalStateException
		  {
		      if (key instanceof GenericDomain) {
			  try {
			      D mapkey = (D)key;
			      if (domainMember != null
				  && domainMember.inDomain(mapkey)) {
				  String k = keyString(key);
				  String n = getParmName();
				  throw new IllegalStateException
				      (errorMsg("domainMemberLoop", k, n));
				      /*("domain member exists and contains the "
					+ "domain being set");*/
			      }
			      domainMap.put(mapkey, value);
			  } catch (Exception e) {
			      String k = keyString(key);
			      String n = getParmName();
			      throw new IllegalArgumentException
				  (errorMsg("notDomain2", k, n));
				  /*("key not a domain");*/
			  }
		      } else {
			  String k = keyString(key);
			  String n = getParmName();
			  throw new IllegalArgumentException
			      (errorMsg("notDomain2", k, n));
			  /*throw new IllegalArgumentException
			    ("key is not a domain");*/
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
			      String k = keyString(key);
			      String n = getParmName();
			      throw new IllegalArgumentException
				  (errorMsg("notDomain2", k, n));
			      /*throw new IllegalArgumentException
				("key not a domain");*/
			  }
		      }
		      // D d = sim.getDomain(key);
		      // domains.remove(d);
		  }
		  public void clear() {
		      domainMap.clear();
		  }
	      },
	     java.lang.Boolean.class),
    };

    public void clear() {
	super.clear();
	//domains.clear();
	domainMap.clear();
	domainMember = null;
    }

    /**
     * Constructor.
     * Subclasses must call this constructor.
     * @param sim the simulation
     */
    protected GenericActorFactory (S sim) {
	super(sim);
	this.sim = sim;
	initParms(parms, GenericActorFactory.class);
	addLabelResourceBundle("*.lpack.ActorLabels",
			       GenericActorFactory.class);
	addTipResourceBundle("*.lpack.ActorTips",
			     GenericActorFactory.class);
	addDocResourceBundle("*.lpack.ActorDocs",
			     GenericActorFactory.class);
    }

    @Override
    protected void initObject(Obj actor) {
	super.initObject(actor);
	if (domainMember != null) {
	    actor.setSharedDomainMember(domainMember);
	}

	for (Map.Entry<D,Boolean> entry: domainMap.entrySet()) {
	    D d = entry.getKey();
	    boolean value = entry.getValue();
	    actor.joinDomain(d, value);
	}
    }
}

//  LocalWords:  exbundle subclasses ul li domainMember boolean DM dm
//  LocalWords:  superclasses traceSetMode traceSets TraceSet
//  LocalWords:  SimObject LinkedList notDomainMember getDomainMember
//  LocalWords:  IllegalArgumentException domainMemberLoop notDomain
//  LocalWords:  getDomain
