package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;


/**
 * Class providing delays and message filters.
 * The class {@link GenericDomain GenericDomain} contains methods for
 * finding delays and message filters for messages sent between actors
 * and groups for cases in which the delay is not given explicitly.
 * The source and destination of a message, however, may not be in the
 * same domain.  If not, parent domains are checked until one finds a
 * source domain and a destination domain with a common parent, and
 * with all three domains, plus any in between, constrained to being
 * communication domains, possibly with a set of possible
 * communication-domain types specified.  These domains are used to
 * compute the delay and any applicable message filters.
 * <P>
 * Instances of GenericMsgFrwdngInfo are used to allow each domain in
 * the path between a source and destination to provide a contribution
 * to the delay and message filter.  This is done by summing individual
 * delays and by creating instances of 
 * {@link CompoundMessageFilter CompoundMessageFilter}, which allows
 * multiple message filters to be applied in sequence.
 * <P>
 * As an example. suppose we have 8 domains, with the domain "topdomain"
 * not having a parent domain. the domains "domain" and "subdomain5" have
 * "topdomain" as their parents.  The domain "subdomain6" has "subdomain5"
 * as its parent.  In addition, the domains "subdomain1", "subdomain2",
 * subdomain3", and "subdomain4" have "domain" as their parent.  Also 
 * suppose that actor "a1" joins "subdomain1", actor "a2" joins "subdomain3",
 * actor "a3"joins "subdomain3", actor a4 joins "subdomain4", actor "a6"
 * joins "subdomain6", actor "adom" joins "domain", and actor atop joins
 * "topdomain". Then
 * <ul> 
 *   <li> if a2  sends a message to a3 :
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>subdomain3</dt><td>a2</dt><td></dt>a3</tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if a1 sends a message to adom:
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>subdomain1</dt><td>a1</dt><td>domain</dt></tr>
 *           <tr><td>domain</dt><td>subdomain1</dt><td>adom</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if a1 sends a message to a2 :
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>subdomain1</dt><td>a1</dt><td>domain</dt></tr>
 *           <tr><td>domain</dt><td>subdomain1</dt><td>subdomain3</dt></tr>
 *           <tr><td>subdomain3</dt><td>domain</dt><td>a2</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if a1 sends a message to atop :
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>subdomain1</dt><td>a1</dt><td>domain</dt></tr>
 *           <tr><td>domain</dt><td>subdomain1</dt><td>topdomain</dt></tr>
 *           <tr><td>topdomain</dt><td>domain</dt><td>atop</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if a1 sends a message to a5:
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>subdomain1</dt><td>a1</dt><td>domain</dt></tr>
 *           <tr><td>domain</dt><td>subdomain1</dt>topdomain<td></dt></tr>
 *           <tr><td>topdomain</dt>domain<td></dt><td>subdomain5</dt></tr>
 *           <tr><td>subdomain5</dt><td>topdomain</dt><td>a5</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if a1 sends a message to a6:
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>subdomain1</dt><td>a1</dt><td>domain</dt></tr>
 *           <tr><td>domain</dt><td>subdomain1</dt><td>topdomain</dt></tr>
 *           <tr><td>topdomain</dt><td>domain</dt><td>subdomain5</dt></tr>
 *           <tr><td>subdomain5</dt><td>domain</dt><td>subdomain6</dt></tr>
 *           <tr><td>subdomain6</dt><td>subdomain5</dt><td>a6</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if atop sends a message to a5:
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>todomain</dt><td>atop</dt><td>subdomain5</dt></tr>
 *           <tr><td>subdomain5</dt><td>topdomain</dt><td>a5</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 *   <li> if atop sends a message to a6:
 *   <div style="margin-left: 25%; margin-right: 25%">
 *   <table border="1">
 *     <caption>&nbsp;</caption>
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <colgroup align="center">
 *           <thead>
 *           <tr><th>localDelay<BR>First Argument</th>
 *               <th>localDelay<BR>Second Argument</th>
 *               <th>localDelay<BR>Fourth Argument</th>
 *           </tr>
 *           </thead>
 *           <tbody>
 *           <tr><td>topdomain</dt><td>atop</dt><td>subdomain5</dt></tr>
 *           <tr><td>subdomain5</dt><td>topdomain</dt><td>subdomain6</dt></tr>
 *           <tr><td>subdomain6</dt><td>subdomain5</dt><td>a6</dt></tr>
 *           </tbody>
 *   </table>
 *   </div>
 * </ul>
 * The method getMessageFilter is handled similarly, with calls to
 * localMessageFilter instead of localDelay. While the examples just
 * use actor, groups are treated the same way.
 */
public class GenericMsgFrwdngInfo <
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericSimObject<S,A,C,D,DM,F,G>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the belief; null if one should be chosen
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericMsgFrwdngInfo(S sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * source actor, and destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, A src, Object msg, A dest) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * source actor, and destination group.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, A src, Object msg, G dest) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * source actor, and domain that will forward a message
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param next the domain that will forward the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, A src, Object msg, D next) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * source group, and destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, G src, Object msg, A dest) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * source group, and destination group.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, G src, Object msg, G dest) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * source group, and domain that will forward a message
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param next the domain that will forward the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, G src, Object msg, D next) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * a previous domain and the destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the domain that has forwarded the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, D src, Object msg, A dest) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * a previous domain and the destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the domain that has forwarded the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, D src, Object msg, G dest) {
	return 0;
    }

    /**
     * Get the contribution of a domain to the total delay for a message,
     * a previous domain and the next domain.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the domain that has forwarded the message
     * @param msg the message
     * @param next the domain tat will forward the message
     * @return the contribution of the domain to the delay
     */
    protected long localDelay(D domain, D src, Object msg, D next) {
	return 0;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * source actor and a destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       A src,
					       Object msg,
					       A dest)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * source actor and a destination group.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       A src,
					       Object msg,
					       G dest)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * source actor and a domain that will forward the message.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param next the domain that will forward the message message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       A src,
					       Object msg,
					       D next)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * source group and a destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       G src,
					       Object msg,
					       A dest)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * source group and a destination group.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       G src,
					       Object msg,
					       G dest)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * source group and a domain that will forward the message.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the source for the message
     * @param msg the message
     * @param next the domain that will forward the message message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       G src,
					       Object msg,
					       D next)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * domain that has forwarded the message and a destination actor.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the domain that has forwarded the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       D src,
					       Object msg,
					       A dest)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * domain that has forwarded the message and a destination group.
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the domain that has forwarded the message
     * @param msg the message
     * @param dest the destination for the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       D src,
					       Object msg,
					       G dest)
    {
	return null;
    }

    /**
     * Get the message filter provided by a domain for a message, a
     * domain that has forwarded the message and a domain that will
     * forward the message
     * @param domain the domain used to determine a contribution to the
     *        delay
     * @param src the domain that has forwarded the message
     * @param msg the message
     * @param next the domain that will forward the message
     * @return the message filter
     */
    protected MessageFilter localMessageFilter(D domain,
					       D src,
					       Object msg,
					       D next)
    {
	return null;
    }
}

//  LocalWords:  GenericDomain GenericMsgFrwdngInfo topdomain adom ul
//  LocalWords:  CompoundMessageFilter li colgroup thead th tbody td
//  LocalWords:  localDelay dt todomain getMessageFilter src msg dest
//  LocalWords:  localMessageFilter IllegalArgumentException
