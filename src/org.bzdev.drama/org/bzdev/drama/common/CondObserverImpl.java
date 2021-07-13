package org.bzdev.drama.common;
import org.bzdev.drama.generic.GenericCondObsrvrImpl;
import org.bzdev.drama.generic.GenericCondition;
import org.bzdev.devqsim.SimObject;

/**
 * Implement a condition observer. 
 * A class implementing CondObserver when the type parameter C is
 * Condition and the type parameter P is OurSimObject will typically
 * declare a field
 * <pre>
 *      CondObserverImpl&lt;Condition,OurSimObject&gt; impl
 *         = new CondObserverImpl&lt;Condition,OurSimObject&gt;(this) {
 *             protected void doConditionChange(Condition c,
 *                                              ConditionMode mode,
 *                                              SimObject source)
 *             {
 *               ...
 *             }
 *      };
 * </pre>
 * followed by a series of method declarations such as
 * <pre>
 *      public boolean addCondition(Condition c) {
 *         return impl.addCondition(c);
 *      }
 * </pre>
 * for each public method of CondObserver.  The use of an anonymous
 * inner class allows the doConditionChange method to be defined so that
 * the class implementing CondObserver can respond to a change in a
 * condition. While there is a default implementation for
 * doConditionChange(Map&lt;Condition,ConditionInfo&gt; cMap), which is used
 * when condition change notifications are queued, it may be necessary to
 * provide an implementation of this method for efficiency reasons (as is
 * done in the Generic Domain classes).
 * 
 */
public abstract class CondObserverImpl
    <C extends GenericCondition, P extends SimObject>
    extends GenericCondObsrvrImpl<C,P>
{

    /**
     * Constructor.
     * @param parent a simulation object (SimObject) that implements
     *        CondObserver
     */
    public CondObserverImpl(P parent) {
	super(parent);
	setCondObserverImpl(this);
    }
}

//  LocalWords:  CondObserver OurSimObject pre CondObserverImpl lt
//  LocalWords:  impl doConditionChange ConditionMode SimObject cMap
//  LocalWords:  boolean addCondition ConditionInfo
