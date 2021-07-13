import org.bzdev.drama.*;
import org.bzdev.drama.common.MessageFilter;


public class TestMsgForwardingInfo extends MsgForwardingInfo {
    public TestMsgForwardingInfo(DramaSimulation sim, String name,
				 boolean intern)
    {
	super(sim, name, intern);
    }

    boolean delayTraceMode = false;
    boolean msgFilterTraceMode = false;

    public void setTraceModes(boolean dvalue, boolean mvalue) {
	delayTraceMode = dvalue;
	msgFilterTraceMode = mvalue; 
    }

    protected long localDelay(Domain domain,
			      Actor src, Object msg, Actor dest)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + dest.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Actor src, Object msg, Group dest)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + dest.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Actor src, Object msg, Domain next)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + next.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Group src, Object msg, Actor dest)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + dest.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Group src, Object msg, Group dest)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + dest.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Group src, Object msg, Domain next)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + next.getName());
	return 1;
    }


    protected long localDelay(Domain domain,
			      Domain src, Object msg, Actor dest)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + dest.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Domain src, Object msg, Group dest)
    {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + dest.getName());
	return 1;
    }

    protected long localDelay(Domain domain,
			      Domain src, Object msg, Domain next) {
	if (delayTraceMode)
	    System.out.println("localDelay for " + domain.getName()
			       + ": " + src.getName()
			       + " -> " + next.getName());
	return 1;
    }

    private MessageFilter messageFilter = new MessageFilter() {
	    public Object filterMessage(Object msg) {
		if (msg instanceof TestMessage) {
		    return new
			TestMessage("*" + ((TestMessage)msg).getString());
		} else {
		    return msg;
		}
	    }
	};

    protected MessageFilter localMessageFilter(Domain domain, Actor src,
					       Object msg, Actor dest)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + dest.getName());
	return messageFilter;
    }

    protected MessageFilter localMessageFilter(Domain domain, Actor src,
					       Object msg, Group dest)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + dest.getName());
	return messageFilter;
    }

    protected MessageFilter localMessageFilter(Domain domain, Actor src,
					       Object msg, Domain next)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + next.getName());
	return messageFilter;
    }

    protected MessageFilter localMessageFilter(Domain domain, Group src,
					       Object msg, Actor dest)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + dest.getName());
	return messageFilter;
    }

    protected MessageFilter localMessageFilter(Domain domain, Group src,
					       Object msg, Group dest)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + dest.getName());
	return messageFilter;
    }


    protected MessageFilter localMessageFilter(Domain domain, Group src,
					       Object msg, Domain next)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + next.getName());
	return messageFilter;
    }


    protected MessageFilter localMessageFilter(Domain domain, Domain src,
					       Object msg, Actor dest)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + dest.getName());
	return messageFilter;
    }

    protected MessageFilter localMessageFilter(Domain domain, Domain src,
					       Object msg, Group dest)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + dest.getName());
	return messageFilter;
    }

    protected MessageFilter localMessageFilter(Domain domain, Domain src,
					       Object msg, Domain next)
    {
	if (msgFilterTraceMode) System.out.println("localMessageFilter for "
						   + domain.getName() + ": "
						   + src.getName()
						   + " -> " + next.getName());
	return messageFilter;
    }
}
