package org.bzdev.drama.generic;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import org.bzdev.util.*;
import org.bzdev.util.SafeFormatter;

import java.util.*;
import javax.script.ScriptException;

//@exbundle org.bzdev.drama.generic.lpack.GenericSimulation

/**
 * Base simulation class.
 * All flavors of simulations defined in other packages should be
 * subclasses of this class.  The simulation package includes a
 * number of classes that make use of generics. The type parameters
 * should match the specific classes used in each simulation's flavor,
 * where this use of generics will be hidden.
 * <p>
 * The idea is that each flavor of simulation will want its own top-level
 * classes, and access methods should declare their return values to
 * match those classes, not the ones defined in the current package.
 * <p>
 * While GenericSimulation merely provides access methods to look up
 * flavor-specific instances of GenericActor, GenericCondition, etc.,
 * its superclass Simulation provides the mechanisms necessary managing
 * the event queue and mapping names to objects.
 */
abstract public class GenericSimulation<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends Simulation
{

    private static ResourceBundle
      exbundle = ResourceBundle.getBundle("org.bzdev.drama.generic.lpack.GenericSimulation");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    F factory;

    /**
     * Get the simulation's factory.
     * A simulation's factory provides methods that construct objects
     * with the subtype of the object determined at run time. It is
     * needed in part because type parameters cannot be used as the
     * names of constructors.
     * @return the simulation's factory
     */
    public F getFactory() {return factory;}

    /**
     * Constructor.
     * A simulation's factory provides methods that construct objects
     * with the subtype of the object determined at run time. It is
     * needed in part because type parameters cannot be used as the
     * names of constructors. Subclasses will provide the appropriate
     * factory as a default, but may allow that choice to be overridden.
     * @param factory the simulation factory
     */
    @SuppressWarnings("unchecked")
    protected GenericSimulation(F factory) {
	super();
	this.factory = factory;
	factory.setSimulation((S)this);
    }

    /**
     * Constructor with a parent.
     * When a simulation has a parent, the simulation's scripting context
     * is that of its parent unless specific methods are overridden.
     * When a simulation has a parent that is also a Simulation, the
     * parent's event queue is used instead of the simulation's event
     * queue and time structures. This allows multiple simulations,
     * perhaps with different flavors, to be combined into a single
     * simulation. While the event queue and simulation time are
     * shared, tables of simulation objects are not.  Running any of
     * the simulations sharing a parent will run all of them.
     * <P>
     * A simulation's factory provides methods that construct objects
     * with the subtype of the object determined at run time. It is
     * needed in part because type parameters cannot be used as the
     * names of constructors. Subclasses will provide the appropriate
     * factory as a default, but may allow that choice to be overridden.
     * @param parent the parent simulation.
     * @param factory the simulation factory
     */
    @SuppressWarnings("unchecked")
    protected GenericSimulation(ScriptingContext parent, F factory) {
	super(parent);
	this.factory = factory;
	factory.setSimulation((S)this);
    }

    /**
     * Constructor with parent simulation and time unit specification.
     * When a simulation has a parent, the simulation's scripting context
     * is that of its parent unless specific methods are overridden.
     * When a simulation has a parent that is also a Simulation, the
     * parent's event queue is used instead of the simulation's event
     * queue and time structures. This allows multiple simulations,
     * perhaps with different flavors, to be combined into a single
     * simulation. While the event queue and simulation time are
     * shared, tables of simulation objects are not.  Running any of
     * the simulations sharing a parent will run all of them. In
     * addition, the parent simulation will be added an an alternative
     * object namer, so that the set/add methods of factory classes
     * can find objects are defined by a parent simulation.
     * <P>
     * A simulation's factory provides methods that construct objects
     * with the subtype of the object determined at run time. It is
     * needed in part because type parameters cannot be used as the
     * names of constructors. Subclasses will provide the appropriate
     * factory as a default, but may allow that choice to be overridden.
     *
     * @param parent the simulation's parent; null if there is none
     * @param factory the simulation factory
     * @param ticksPerUnitTime the number of ticks per unit time
     */
    @SuppressWarnings("unchecked")
    protected GenericSimulation(ScriptingContext parent, F factory,
			     double ticksPerUnitTime)
    {
	super(parent, ticksPerUnitTime);
	this.factory = factory;
	factory.setSimulation((S)this);
    }

    /**
     * Constructor give a time-unit specification.
     * A simulation's factory provides methods that construct objects
     * with the subtype of the object determined at run time. It is
     * needed in part because type parameters cannot be used as the
     * names of constructors. Subclasses will provide the appropriate
     * factory as a default, but may allow that choice to be overridden.
     * @param factory the simulation factory
     * @param ticksPerUnitTime the number of ticks per unit time when
     *        time is provided as a double-precision number
     */
    protected GenericSimulation(F factory, double ticksPerUnitTime) {
	this((Simulation) null, factory, ticksPerUnitTime);
    }

    /**
     * Get an actor.
     * @param name the actor's name
     * @return the actor; null if none with that name
     */
    abstract public A getActor(String name);

    /**
    * Get actor names.
    * @return a set of the names of all actors interned in the
    *         simulation's name table
    */
    public Set<String> getActorNames() {
     	return getObjectNames(GenericActor.class);
    }
    //
    //     /**
    //      * Get an condition.
    //      * @param name the condition's name
    //      * @return the condition; null if none with that name
    //      */
    //     public C getCondition(String name) {
    // 	return (C)(Object) getObject(name, GenericCondition.class);
    //     }
    //
    /**
     * Get condition names.
     * @return a set of the names of all conditions interned in the
     *         simulation's name table
     */
    public Set<String> getConditionNames() {
     	return getObjectNames(GenericCondition.class);
    }

    /**
     * Get a domain.
     * @param name the domain's name
     * @return the domain; null if none with that name
     */
    abstract public D getDomain(String name);
    // {
    // 	return (D)(Object) getObject(name, GenericDomain.class);
    //     }
    //
    /**
     * Get domain names.
     * @return a set of the names of all domains interned in the
     *         simulation's name table
     */
    public Set<String> getDomainNames() {
	return getObjectNames(GenericDomain.class);
    }

    /**
     * Get a domain member.
     * @param name the domain member's name
     * @return the domain member; null if none with that name
     */
    abstract public DM getDomainMember(String name);
    //     {
    // 	return (DM) (Object) getObject(name, GenericDomainMember.class);
    //     }
    //
    /**
     * Get domain-member names.
     * @return a set of the names of all domain members interned in the
     *         simulation's name table
     */
    public Set<String> getDomainMemberNames() {
	return getObjectNames(GenericDomainMember.class);
    }

    /**
     * Get a group.
     * @param name the group's name
     * @return the group; null if none with that name
     */
    abstract public G getGroup(String name);
    // {
    // 	return (G) (Object) getObject(name, GenericGroup.class);
    //     }
    //
    /**
     * Get group names.
     * @return a set of the names of all groups interned in the
     *         simulation's name table
     */
    public Set<String> getGroupNames() {
	return getObjectNames(GenericGroup.class);
    }


    private String[] findMsgFrwdngInfo(CommDomainInfo<D>cdinfo) {
	if (cdinfo == null) return null;
	HashSet<String>
	    mset = new HashSet<>();
	D srcDomain = cdinfo.getSourceDomain();
	D destDomain = cdinfo.getDestDomain();
	D ancestor = cdinfo.getAncestorDomain();
	GenericMsgFrwdngInfo<S,A,C,D,DM,F,G> info = ancestor.getMFI();
	if (info != null) mset.add(info.getName());
	while (srcDomain != ancestor) {
	    info = srcDomain.getMFI();
	    if (info != null) mset.add(info.getName());
	    srcDomain = srcDomain.getParent();
	}
	while (destDomain != ancestor) {
	    info = destDomain.getMFI();
	    if (info != null) mset.add(info.getName());
	    destDomain = destDomain.getParent();
	}
	String[] result = new String[mset.size()];
	return mset.toArray(result);
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericActor,Set,GenericActor)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the actor that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the actor that will receive a message
     * @return the names of the MsgFrwdngInfo objects that would be used;
     *         null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(A src,
				      Set<CommDomainType> commDomainTypes,
				    A dest)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if an actor can communicate with another actor.
     * The test is unidirectional.
     * An argument provides the allowable
     * communication-domain types and a search of the src actor's domains
     * is used to find a suitable domain.
     * If a communication-domain-type set is provided (i.e., it is not null)
     * and a domain's communication-domain type is not a member
     * of this set, that domain is skipped. If all domains are
     * skipped, null is returned. Otherwise
     * {@link GenericDomain#communicationMatch(GenericActor,GenericActor)}
     * determines if communication is possible.
     * @param src the actor that will send a message
     * @param dest the actor that will receive a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(A src,
					    Set<CommDomainType> commDomainTypes,
					    A dest)
	throws IllegalArgumentException
    {

	CommDomainInfo<D>  match = null;

	Iterator<D> it = src.domainSet().iterator();
	while (it.hasNext()) {
	    D d1 = it.next();
	    if (commDomainTypes != null
		&& !commDomainTypes.contains(d1.getCommDomainType())) {
		continue;
	    }
	    if (d1.getPriority() == Integer.MAX_VALUE) return null;
	    if (d1.isCommunicationDomain() == false) continue;
	    match = d1.communicationMatch(src, dest);
	    if (match != null) break;
	    // if (d1.communicationMatch(src, dest) != null) return d1;
	}
	return match;
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericActor,Set,GenericActor,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the actor that will send a message
     * @param dest the actor that will receive a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param domain the domain that must allow communication
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that
     *         would be used; null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(A src,
				      Set<CommDomainType> commDomainTypes,
				      A dest,
				      D domain)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest,
						  domain);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if an actor can communicate with another actor using a
     * specific domain for communication.
     * The test is unidirectional.
     * An argument provides the allowable communication-domain types
     * and either the source actor or the destination actor must be a
     * member of the specified domain.  If a communication-domain-type
     * set is provided (i.e., it is not null) and the specified
     * domain's communication-domain type is not a member of this set,
     * this method returns null. Otherwise
     * {@link GenericDomain#communicationMatch(GenericActor,GenericActor)}
     * determines if communication is possible.
     * @param src the actor that will send a message
     * @param dest the actor that will receive a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param domain the domain that must allow communication
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(A src,
					    Set<CommDomainType> commDomainTypes,
					    A dest,
					    D domain)
	throws IllegalArgumentException
    {
	if (domain == null) return findCommDomain(src, commDomainTypes, dest);
	if (commDomainTypes != null &&
	    !commDomainTypes.contains(domain.getCommDomainType()))
	    return null;
	return domain.communicationMatch(src, dest);
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericActor,Set,GenericGroup)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the actor that will send a message
     * @param dest the group that will receive a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that
     *         would be used; null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(A src,
				      Set<CommDomainType> commDomainTypes,
				      G dest)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if an actor can communicate with a group.
     * The test is unidirectional.
     * An argument provides the allowable
     * communication-domain types and a search of the src actor's domains
     * is used to find a suitable domain.
     * If a communication-domain-type set is provided (i.e., it is not null)
     * and a domain's communication-domain type is not a member
     * The domain's method, that domain is skipped. If all domains are
     * skipped, null is returned. Otherwise
     * {@link GenericDomain#communicationMatch(GenericActor,GenericGroup)}
     * determines if communication is possible.
     * @param src the actor that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the group that will receive a message
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(A src,
					    Set<CommDomainType> commDomainTypes,
					    G dest)
	throws IllegalArgumentException
    {
	CommDomainInfo<D>  match = null;


	Iterator<D> it = src.domainSet().iterator();
	while (it.hasNext()) {
	    D d1 = it.next();
	    if (commDomainTypes != null
		&& !commDomainTypes.contains(d1.getCommDomainType())) {
		continue;
	    }
	    if (d1.getPriority() == Integer.MAX_VALUE) return null;
	    match = d1.communicationMatch(src, dest);
	    if (match != null) break;
	    // if (d1.communicationMatch(src, dest) != null) return d1;
	}
	return match;
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericActor,Set,GenericGroup,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the actor that will send a message
     * @param dest the group that will receive a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param domain the domain to use for communication
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that
     *         would be used; null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(A src,
				      Set<CommDomainType> commDomainTypes,
				      G dest, D domain)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest,
						  domain);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if an actor can communicate with a group using a specific
     * domain.
     * The test is unidirectional.
     * An argument provides the allowable communication-domain types
     * and either the source actor or the destination group must be a
     * member of the specified domain.  If a communication-domain-type
     * set is provided (i.e., it is not null) and the specified
     * domain's communication-domain type is not a member of this set,
     * this method returns null. Otherwise
     * {@link GenericDomain#communicationMatch(GenericActor,GenericGroup)}
     * determines if communication is possible.
     * @param src the actor that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the group that will receive a message
     * @param domain the domain to use for communication
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(A src,
					    Set<CommDomainType> commDomainTypes,
					    G dest,
					    D domain)
	throws IllegalArgumentException
    {
	
	if (domain == null) return findCommDomain(src, commDomainTypes, dest);
	if (commDomainTypes != null &&
	    !commDomainTypes.contains(domain.getCommDomainType()))
	    return null;
	return domain.communicationMatch(src, dest);
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericGroup,Set,GenericGroup)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the group that will receive a message
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that
     *         would be used; null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(G src,
				      Set<CommDomainType> commDomainTypes,
				      G dest)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if a group can communicate with a group.
     * The test is unidirectional.
     * An argument provides the allowable
     * communication-domain types and a search of the src group's domains
     * is used to find a suitable domain.
     * If a communication-domain-type set is provided (i.e., it is not null)
     * and a domain's communication-domain type is not a member
     * of this set, that domain is skipped. If all domains are
     * skipped, null is returned. Otherwise
     * {@link GenericDomain#communicationMatch(GenericGroup,GenericGroup)}
     * determines if communication is possible.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the group that will receive a message
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(G src,
					    Set<CommDomainType> commDomainTypes,
					    G dest)
	throws IllegalArgumentException
    {
	CommDomainInfo<D>  match = null;

	Iterator<D> it = src.domainSet().iterator();
	while (it.hasNext()) {
	    D d1 = it.next();
	    if (commDomainTypes != null
		&& !commDomainTypes.contains(d1.getCommDomainType())) {
		continue;
	    }
	    if (d1.getPriority() == Integer.MAX_VALUE) return null;
	    if (d1.isCommunicationDomain() == false) continue;
	    match = d1.communicationMatch(src, dest);
	    if (match != null) break;
	}
	return match;
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericGroup,Set,GenericGroup,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the group that will send a message
     * @param dest the group that will receive a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param domain the domain to use for communication
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that would be used;
     *         null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(G src,
				      Set<CommDomainType> commDomainTypes,
				      G dest, D domain)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest,
						  domain);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if a group can communicate with a group using a specific
     * domain.
     * The test is unidirectional.
     * An argument provides the allowable communication-domain types
     * and either the source group or the destination group must be a
     * member of the specified domain.  If a communication-domain-type
     * set is provided (i.e., it is not null) and the specified
     * domain's communication-domain type is not a member of this set,
     * this method returns null. Otherwise
     * {@link GenericDomain#communicationMatch(GenericGroup,GenericGroup)}
     * determines if communication is possible.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the group that will receive a message
     * @param domain the domain to use for communication
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(G src,
					    Set<CommDomainType> commDomainTypes,
					    G dest,
					    D domain)
	throws IllegalArgumentException
    {
	
	if (domain == null) return findCommDomain(src, commDomainTypes, dest);
	if (commDomainTypes != null &&
	    !commDomainTypes.contains(domain.getCommDomainType())) {
	    return null;
	}
	return domain.communicationMatch(src, dest);
    }

    /**
     * Find the names of the forwarding tables
     * {@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericGroup,Set,GenericActor)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the actor that will receive a message
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that would be used;
     *         null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(G src,
				      Set<CommDomainType> commDomainTypes,
				      A dest)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if a group can communicate with an actor using
     * a specific  domain.
     * The test is unidirectional.
      * An argument provides the allowable
     * communication-domain types and a search of the src group's domains
     * is used to find a suitable domain.
     * If a communication-domain-type set is provided (i.e., it is not null)
     * and a domain's communication-domain type is not a member
     * of this set, that domain is skipped. If all domains are
     * skipped, null is returned. Otherwise
     * {@link GenericDomain#communicationMatch(GenericGroup,GenericActor)}
     * determines if communication is possible.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the group that will receive a message
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(G src,
					    Set<CommDomainType> commDomainTypes,
					    A dest)
	throws IllegalArgumentException
    {
	CommDomainInfo<D>  match = null;

	Iterator<D> it = src.domainSet().iterator();
	while (it.hasNext()) {
	    D d1 = it.next();
	    if (commDomainTypes != null
		&& !commDomainTypes.contains(d1.getCommDomainType())) {
		continue;
	    }
	    if (d1.getPriority() == Integer.MAX_VALUE) return null;
	    if (d1.isCommunicationDomain() == false) continue;
	    match = d1.communicationMatch(src, dest);
	    if (match != null) break;
	}
	return match;
    }

    /**
     * Find the names of the forwarding tables
     * ({@link GenericMsgFrwdngInfo}) used by a call to
     * {@link #findCommDomain(GenericGroup,Set,GenericActor,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the actor that will receive a message
     * @param domain the domain to use for communication
     * @return the names of the {@link GenericMsgFrwdngInfo} objects that would be used;
     *         null if a route is not possible
     */
    public String[] findMsgFrwdngInfo(G src,
				      Set<CommDomainType> commDomainTypes,
				      A dest, D domain)
    {
	CommDomainInfo<D> cdinfo = findCommDomain(src,commDomainTypes, dest);
	return findMsgFrwdngInfo(cdinfo);
    }

    /**
     * Determine if a group can communicate with an actor using
     * a specific  domain.
     * The test is unidirectional.
      * An argument provides the allowable communication-domain types
     * and either the source group or the destination actor must be a
     * member of the specified domain.  If a communication-domain-type
     * set is provided (i.e., it is not null) and the specified
     * domain's communication-domain type is not a member of this set,
     * this method returns null. Otherwise
     * {@link GenericDomain#communicationMatch(GenericGroup,GenericActor)}
     * determines if communication is possible.
     * @param src the group that will send a message
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @param dest the actor that will receive a message
     * @param domain the domain to use for communication
     * @return an object specifying the domains needed to look up delays
     *         and message filters; null if there are none
     * @exception IllegalArgumentException src's domain set contains
     *            domains at a given priority level that have incompatible
     *            modes.
     */
    public CommDomainInfo<D> findCommDomain(G src,
					    Set<CommDomainType> commDomainTypes,
					    A dest,
					    D domain)
	throws IllegalArgumentException
    {
	if (domain == null) return findCommDomain(src, commDomainTypes, dest);
	if (commDomainTypes != null &&
	    !commDomainTypes.contains(domain.getCommDomainType())) {
	    return null;
	}
	return domain.communicationMatch(src, dest);
    }

    /**
     * Create a simulation listener based on a script object that implements an
     * adapter.  To respond to particular events, the script object must
     * implement one or more methods.  The arguments to these methods
     * are:
     * <ul>
     *   <li><code>sim</code>. The simulation that scheduled the event
     *   <li><code>q</code>. A task queue or server queue associated with the
     *       event.
     *   <li><code>server</code>. A queue server associated with the event.
     *   <li> <code>from</code>. The message recipient that sent a message.
     *   <li> <code>to</code>. The message recipient that received a message.
     *   <li> <code>msg</code> An arbitrary object representing a message.
     *   <li><code>obj</code>. A simulation object associated with the event
     *       by being responsible for generating it.  Methods with this
     *       argument are the result of events generated by calling certain
     *       methods of the class {@link SimObject} or its subclasses.
     *   <li><code>tag</code>. A tag labeling the event (for
     *       debugging/tracing purposes). The tag can be any object, but is
     *       typically a string or stack trace.
     * </ul>
     * Some methods use a subset of these arguments.
     * <P>
     * The adapters GenericSimulation recognizes two methods defined
     * for  a script object in addition to those defined by
     * {@link org.bzdev.devqsim.DefaultSimAdapter}.  These methods
     * are:
     * <ul>
     *   <li> messageReceiveStart(sim,from,to.msg). A message is about to be
     *        received.
     *   <li> messageReceiveEnd(sim,from,to.msg). A message has just been
     *        received and handled.
     * </ul>
     * <P>
     * The methods that DefaultSimAdapter provides are:
     * <ul>
     *   <li><code>simulationStart(sim)</code>. A simulation has started.
     *   <li><code>simulationStop(sim)</code>. A simulation has stopped
     *       (ended or paused).
     *   <li><code>callStart(sim,tag)</code>. A call scheduled by a
     *       simulation has started.  The call was scheduled using a
     *       simulation's method <code>scheduleCall</code>,
     *       <code>scheduleScript</code>, or
     *       <code>scheduleCallObject</code>, and is not attributed to
     *       any particular simulation object. The argument
     *       representing the call must be either a Callable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "call" method that has no
     *       arguments.
     *   <li><code>callEnd(sim, tag)</code>. A call scheduled by a
     *       simulation has ended.  The call was scheduled using a
     *       simulation's method <code>scheduleCall</code>,
     *       <code>scheduleScript</code>, or
     *       <code>scheduleCallObject</code>, and is not attributed to
     *       any particular simulation object. The argument
     *       representing the call must be either a Callable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "call" method that has no
     *       arguments.
     *   <li><code>callStartSimObject(sim,obj,tag)</code>. A call that
     *       a simulation object scheduled has started.  The call was
     *       created using a simulation object's protected method
     *       named <code>callableScript</code>,
     *       <code>scheduleScript</code>, <code>scheduleCall</code>,
     *       <code>scheduleCallObject</code>, or
     *       <code>bindCallable</code> and is attributed to that
     *       simulation object.
     *   <li><code>callEndSimObject(sim,obj,tag)</code>.  A call that
     *       an simulation object scheduled has ended.  The call was
     *       created using a simulation object's protected method
     *       named <code>callableScript</code>,
     *       <code>scheduleScript</code>, <code>scheduleCall</code>,
     *       <code>scheduleCallObject</code>, or
     *       <code>bindCallable</code> and is attributed to that
     *       simulation object.
     *   <li><code>taskStart(sim,tag)</code>. A task scheduled by a
     *       simulation has started.  The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskPause(sim,tag)</code>. A task scheduled by a
     *       simulation has paused. The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskResume(sim,tag)</code>. A task scheduled by a
     *       simulation has resumed.  The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskEnd(sim,tag)</code>. A task scheduled by a
     *       simulation has ended.  The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskStartSimObject(sim,obj,tag)</code>. A task that
     *       a simulation object scheduled has started.  The task was
     *       created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskPauseSimObject(sim,obj,tag)</code>. A task that
     *       a simulation object scheduled has paused.  The task was
     *       created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskResumeSimObject(sim,obj,tag)</code>. A task
     *       that a simulation object scheduled has resumed.  The task
     *       was created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskEndSimObject(sim,obj,tag)</code>. A task that a
     *       simulation object scheduled has ended.  The task was
     *       created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskQueueStart(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has started.
     *       Subsequent calls to other adapter methods may indicate
     *       details of that processing.
     *   <li><code>taskQueuePause(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has paused.
     *       Subsequent calls to other adapter methods may indicate
     *       details of that processing.
     *   <li><code>taskQueueResume(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has resumed.
     *       Subsequent calls to other adapter methods may indicate
     *       details of that processing.
     *   <li><code>taskQueueEnd(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has ended.
     *   <li><code>serverSelected(sim,q)</code>. A server was
     *       selected by a server queue in a simulation.
     *   <li><code>serverInteraction(sim,q,server,tag)</code>. A
     *       server associated with a server queue in a simulation
     *       begins interacting with the callable, runnable, or task
     *       it is serving.  For this method, the task or callable is
     *       not associated with a simulation object.
     *   <li><code>serverCallable(sim,q,server,tag)</code>. A server
     *       associated with a server queue in a simulation finishes
     *       its interaction and a callable is run to continue the
     *       simulation.  For this method, the task or callable is not
     *       associated with a simulation object.
     *   <li><code>serverRunnable(sim,q,server,tag)</code>. A server
     *       associated with a server queue in a simulation finishes
     *       its interaction and a Runnable starts execution to
     *       continue the simulation.  For this method, the task or
     *       callable is not associated with a simulation object.
     *   <li><code>serverTask(sim,q,server,tag)</code>. A server
     *       associated with a server queue in a simulation finishes
     *       its interaction and the task that was queued resumes
     *       execution.  For this method, the task or callable is not
     *       associated with a simulation object.
     *   <li><code>serverInteractionSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       begins interacting with the object it is serving.  The
     *       simulation object's protected method bindCallable or
     *       bindRunnable was used to create the Callable or Runnable
     *       and associate it with the simulation object, or a thread
     *       was created using the simulation object's
     *       unscheduledTaskThread or scheduleTask methods.
     *   <li><code>serverCallableSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       finishes its interaction and a callable associated with a
     *       simulation object is run to continue the simulation.  The
     *       simulation object's protected method bindCallable was
     *       used to create the Callable and associate it with the
     *       simulation object.
     *   <li><code>serverRunnableSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       finishes its interaction and a Runnable associated with a
     *       simulation object starts execution to continue the
     *       simulation.  The simulation object's protected method
     *       bindRunnable was used to create the Runnable and
     *       associate it with the simulation object.
     *   <li><code>serverTaskSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       finishes its interaction and a Task associated with a
     *       simulation object resumes execution to continue the
     *       simulation.  The task was associated with a simulation
     *       object when the task was created by creating the task by
     *       using one of the simulation object's
     *       unscheduledTaskThread or scheduleTask methods.
     * </ul>
     */
    @Override
    public SimulationListener createAdapter(Object object)
	throws IllegalArgumentException
    {
	if (object == null) return null;
	return new SimulationAdapter(this, object);
    }
}

//  LocalWords:  exbundle GenericSimulation GenericActor superclass
//  LocalWords:  GenericCondition namer ticksPerUnitTime getCondition
//  LocalWords:  getObject GenericDomain DM GenericDomainMember src
//  LocalWords:  GenericGroup communicationMatch dest commDomainTypes
//  LocalWords:  IllegalArgumentException src's FilteringIterator ul
//  LocalWords:  addToFilter li sim msg SimObject messageReceiveStart
//  LocalWords:  messageReceiveEnd DefaultSimAdapter simulationStart
//  LocalWords:  simulationStop callStart scheduleCall scheduleScript
//  LocalWords:  scheduleCallObject callEnd callStartSimObject
//  LocalWords:  callableScript bindCallable callEndSimObject taskEnd
//  LocalWords:  taskStart scheduleTask scheduleTaskScript taskPause
//  LocalWords:  scheduleTaskObject scheduleTaskWP startImmediateTask
//  LocalWords:  unscheduledTaskThread taskResume taskStartSimObject
//  LocalWords:  runnableScript runnableObject bindRunnable subtype
//  LocalWords:  taskPauseSimObject taskResumeSimObject taskQueueEnd
//  LocalWords:  taskEndSimObject taskQueueStart taskQueuePause
//  LocalWords:  taskQueueResume serverSelected serverInteraction
//  LocalWords:  serverCallable serverRunnable serverTask subclasses
//  LocalWords:  serverInteractionSimObject serverCallableSimObject
//  LocalWords:  serverRunnableSimObject serverTaskSimObject Runnable
//  LocalWords:  MsgFrwdngInfo findCommDomain GenericMsgFrwdngInfo
//  LocalWords:  runnable
