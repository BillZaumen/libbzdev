package org.bzdev.drama;
import org.bzdev.drama.generic.*;


/**
 * Class providing delays and message filters.
 * The class {@link Domain Domain} contains methods for
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
 * Instances of MsgFrwdngInfo are used to allow each domain in
 * the path between a source and destination to provide a contribution
 * to the delay and message filter.  This is done by summing individual
 * delays and by creating instances of
 * {@link org.bzdev.drama.common.CompoundMessageFilter CompoundMessageFilter},
 *  which allows multiple message filters to be applied in sequence.
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
 * @see org.bzdev.drama.generic.GenericMsgFrwdngInfo
 */
public class MsgForwardingInfo
    extends GenericMsgFrwdngInfo<DramaSimulation,Actor,Condition,
	    Domain,DomainMember,DramaFactory,Group>
{

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public MsgForwardingInfo(DramaSimulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }
}

//  LocalWords:  MsgFrwdngInfo CompoundMessageFilter topdomain adom
//  LocalWords:  ul li colgroup thead th localDelay tbody td dt
//  LocalWords:  todomain getMessageFilter localMessageFilter
//  LocalWords:  IllegalArgumentException
