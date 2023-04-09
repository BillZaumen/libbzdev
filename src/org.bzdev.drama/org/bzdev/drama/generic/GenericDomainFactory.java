package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.devqsim.*;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collections;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Abstract class for domain factories.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <P>
 * This class includes the common parameters necessary to initialize
 * domains.
 * <P>
 * Parameters:
 * <ul>
 *   <li> <code>priority</code>. The domain's priority level.
 *   <li> <code>parent</code>. The domain's parent domain.
 *   <li> <code>communicationDomainType</code>. The name of a communication
 *        domain type when no parent is provided.
 *   <li> <code>additionalCommDomainType</code>  One of the additional
 *        communication domain types, represented as a string. Multiple
 *        types are allowed.
 *   <li> <code>condition</code>. One of the conditions associated with
 *        a domain.  Multiple conditions are allowed.
 *   <li> <code>msgForwardingInfo</code>. An instance of
 *        <code>GenericMsgFrwdngInfo</code> used to control message
 *        forwarding for communication domains.
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
abstract public class GenericDomainFactory<
    OF extends GenericDomainFactory<OF,S,A,C,D,DM,F,G,Obj>,
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>,
    Obj extends D>
    extends SimObjectFactory<OF,S,Obj>
{

    S sim;
    // static int DEFAULT_PRIORITY = 0;
    static final int DEFAULT_PRIORITY = (0);
    int priority = DEFAULT_PRIORITY;

    CommDomainType communicationDomainType = null;

    Set<CommDomainType> commDomainTypeSet =
	new LinkedHashSet<CommDomainType>();


    /**
     * Get the priority.
     * @return the priority
     */
    public int getPriority() {return priority;}

    Set<C> conditionSet = new LinkedHashSet<C>();

    /**
     * Get the set of conditions associated with this domain factory
     * @return a set of conditions
     */
    public Set<C> getConditionSet() {
	return Collections.unmodifiableSet(conditionSet);
    }

    D parent = null;

    GenericMsgFrwdngInfo<S,A,C,D,DM,F,G> msgForwardingInfo = null;
    /**
     * Get the parent domain.
     * @return the parent domain
     */
    public D getParent() {return parent;}

    Parm parms[] = {
	new Parm("priority",
		 null, null,
		 new ParmParser() {
		     public void parse(int value)
			 throws IllegalArgumentException
		     {
			 priority = value;
		     }
		     public void clear() {
			 priority = DEFAULT_PRIORITY;
		     }
		 },
		 int.class,
		 (Integer) null, false, (Integer) null, false),
	new Parm("communicationDomainType",
		 null, null,
		 new ParmParser() {
		     public void parse(String value)
			 throws IllegalArgumentException
		     {
			 communicationDomainType =
			     CommDomainType.findType(value);
		     }
		     public void clear() {
			 communicationDomainType = null;
		     }
		 },
		 String.class),

	new Parm("additionalCommDomainType",
		 java.lang.String.class, null,
		 new ParmParser() {
		     public void parse(String value) {
			 CommDomainType type = CommDomainType.findType(value);
			 commDomainTypeSet.add(type);
		     }
		     public void clear() {
			 commDomainTypeSet.clear();
		     }
		     public void clear(String value) {
			 CommDomainType type = CommDomainType.findType(value);
			 commDomainTypeSet.remove(type);
		     }
		 },
		 null,
		 null, true, null, true),
	new Parm("parent",
		 null, null,
		 new ParmParser() {
		     @SuppressWarnings("unchecked")
		     public void parse(org.bzdev.obnaming.NamedObjectOps value)
			 throws IllegalArgumentException, IllegalStateException
		     {
			 if (value instanceof GenericDomain) {
			     try {
				 D d = (D)value;
				 if (!d.isCommunicationDomain()) {
				     String n = getParmName();
				     throw new IllegalArgumentException
					 (errorMsg("notCommDomain1", n));
					 /*("value is not a communication domain");*/
				 }
				 parent = d;
			     } catch (IllegalArgumentException ee) {
				 throw ee;
			     } catch (Exception e) {
				 throw new IllegalArgumentException
				     (errorMsg("notDomain1", getParmName()));
				     /* ("value not a domain"); */
			     }
			 }
		     }
		     public void clear() {
			 parent = null;
		     }
		 },
		 GenericDomain.class),
	new Parm("msgForwardingInfo",
		 null, null,
		 new ParmParser() {
		     @SuppressWarnings("unchecked")
		     public void parse(org.bzdev.obnaming.NamedObjectOps value)
			 throws IllegalArgumentException, IllegalStateException
		     {
			 if (value instanceof GenericMsgFrwdngInfo) {
			     try {
				 GenericMsgFrwdngInfo<S,A,C,D,DM,F,G>
				     info =
				     (GenericMsgFrwdngInfo<S,A,C,D,DM,F,G>)
				     value;
				 msgForwardingInfo = info;
			     } catch (Exception e) {
				 String n = getParmName();
				 throw new IllegalArgumentException
				     (errorMsg("notMsgFrwdInfo", n));
				      /*("value does not reference "
					+ "message-forwarding info");*/
			     }
			 }
		     }
		     public void clear() {
			 msgForwardingInfo = null;
		     }
		 },
		 GenericMsgFrwdngInfo.class),
    };

    private boolean initConditionParmCalled = false;
    /**
     * Initialize the "condition" parameter.
     * This should be called by the first subclass for which the type
     * parameter C is an actual type. The call should appear in the
     * constructor immediately after "super" is called.  This will
     * set up a table of conditions that will be added to a domain.
     * This method must be called only once, and is called by
     * org.bzdev.drama.AbstractDomainFactory.
     * @param condClass the actual class corresponding to the type parameter C
     */
    protected final void initConditionParm(final Class<C> condClass) {
	if (initConditionParmCalled) {
	    throw new IllegalStateException("initConditionParm already called");
	}
	initConditionParmCalled = true;
	initParm(new
		 Parm("condition", condClass, null,
		      new ParmParser() {
			  public void
			  parse(org.bzdev.obnaming.NamedObjectOps value)
			  {
			      if (condClass
				  .isAssignableFrom(value.getClass())) {
				  conditionSet.add(condClass.cast(value));
			      } else {
				  throw new IllegalArgumentException
				      (errorMsg("wrongType1", getParmName()));
				      /*("wrong type");*/
			      }
			  }
			  public void clear() {
			      conditionSet.clear();
			  }
			  public void
			      clear(org.bzdev.obnaming.NamedObjectOps value)
			  {
			      conditionSet.remove(value);
			  }
		      },
		      null,
		      null, true, null, true),
		 GenericDomainFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	priority = DEFAULT_PRIORITY;
	parent = null;
	conditionSet.clear();
	communicationDomainType = null;
	msgForwardingInfo = null;
	commDomainTypeSet.clear();
    }

    /**
     * Constructor.
     * Subclasses must call this constructor.
     * @param sim the simulation
     */
    protected GenericDomainFactory(S sim) {
	super(sim);
	this.sim = sim;
	initParms(parms, GenericDomainFactory.class);
	addLabelResourceBundle("*.lpack.DomainLabels",
			       GenericDomainFactory.class);
	addTipResourceBundle("*.lpack.DomainTips", GenericDomainFactory.class);
	addDocResourceBundle("*.lpack.DomainDocs", GenericDomainFactory.class);
    }

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	object.setPriority(priority);
	if (parent != null) {
	    object.setParent(parent);
	    if (object.isCommunicationDomain()
		&& !commDomainTypeSet.isEmpty()) {
		object.addCommDomainTypeSet(commDomainTypeSet);
	    }
	    object.setMessageForwardingInfo(msgForwardingInfo);
	} else if (communicationDomainType != null) {
	    object.configureAsCommunicationDomain(communicationDomainType);
	    if (!commDomainTypeSet.isEmpty()) {
		object.addCommDomainTypeSet(commDomainTypeSet);
	    }
	    object.setMessageForwardingInfo(msgForwardingInfo);
	}
	for (C condition: conditionSet) {
	    object.addCondition(condition);
	}
    }
}

//  LocalWords:  exbundle subclasses ul li communicationDomainType
//  LocalWords:  additionalCommDomainType msgForwardingInfo traceSets
//  LocalWords:  GenericMsgFrwdngInfo superclasses traceSetMode
//  LocalWords:  TraceSet SimObject notCommDomain notDomain condClass
//  LocalWords:  notMsgFrwdInfo initConditionParm wrongType
