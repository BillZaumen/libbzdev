package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.Set;

class MessageSimulationEvent<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends SimulationEvent 
{
    Object message;

    A source;
    G intermediateHop;
    D domain;
    Set<CommDomainType> commDomainTypes;
    GenericMsgRecipient<S,A,C,D,DM,F,G>  destination;
    MessageSimulationEvent(Object message,
			   A  source, 
			   Set<CommDomainType> domainTypes,
			   G intermediateHop,
			   D  domain,
			   GenericMsgRecipient<S,A,C,D,DM,F,G> dest) {
	this.message = message;
	this.commDomainTypes = domainTypes;
	this.source = source;
	this.intermediateHop = intermediateHop;
	this.domain = domain;
	destination = dest;
    }

    protected void processEvent() {
	if (destination.isDeleted()) return;
	// domain is not null if the message was in transit through
	// the domain up to this point, so if the domain was deleted
	// before we process this event, the message should not be
	// delivered.
	if (domain != null && domain.isDeleted()) return;
	destination.fireMessageReceiveStart(source, message);
	destination.receive(this);
	destination.fireMessageReceiveEnd(source, message);
    }

    // Need Access to two protected methods within this package

    protected StackTraceElement[] getStackTraceArray() {
	return super.getStackTraceArray();
    }

    protected void setStackTraceArray(StackTraceElement[] array) {
	super.setStackTraceArray(array);
    }
}
