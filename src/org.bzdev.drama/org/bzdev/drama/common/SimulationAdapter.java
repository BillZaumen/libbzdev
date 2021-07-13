package org.bzdev.drama.common;
import org.bzdev.devqsim.*;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.util.ExpressionParser;
import org.bzdev.util.ExpressionParser.ESPObject;

//@exbundle org.bzdev.drama.common.lpack.ExceptionString

/**
 * This class provides the additional methods needed for drama-based
 * simulation flavors for an adapter for simulation-state events.
 * Most of the methods are provided by the superclass.
 */
public class SimulationAdapter extends DefaultSimAdapter {

    private String errorMsg(String key, Object... args) {
	return ExceptionString.errorMsg(key, args);
    }

    /**
     * Constructor.
     * This creates an adapter with no scripting support.
     */
    public SimulationAdapter() {
	super();
    }

    /**
     * Constructor with only a script object.
     * This creates an adapter for use with the ESP scripting language.
     * <P>
     * Note: This is equivalent to using the constructor
     * {@link #SimulationAdapter(ScriptingContext,Object)} with
     * a null first argument.
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    public SimulationAdapter(ESPObject scriptObject) {
	this(null, scriptObject);
    }

    /**
     * Constructor given a scripting context and script object.
     * This constructor implements the adapter using a scripting language
     * provided its arguments are not null. If a method is added to the
     * script object after this constructor is called, that method will
     * be ignored, so all of the methods the adapter implements must be
     * defined by the script object when this constructor is called.
     * <P>
     * If ESP is the scripting language, the context may be null provided
     * that scriptObject is an ESP object.  This special case is provided
     * for use with {@link org.bzdev.obnaming.ObjectNamerLauncher} and the
     * yrunner program.
     * @param context the scripting context for this adapter
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     * @exception IllegalArgumentException the script object was ill formed
     */
    public SimulationAdapter(ScriptingContext context, Object scriptObject)
	throws IllegalArgumentException
    {
	super(context, scriptObject);
    }

    /**
     * Process a simulation-state-change event.
     * The implementation dispatches the processing to the superclass and if
     * the superclass does not respond to an event with the current event's
     * type, the event-types handled by this class are tried.
     * then other methods in this class, based on the event type.
     * @param e the event
     */
    public final void stateChanged(SimulationStateEvent e) {
	Simulation sim = e.getSimulation();
	//System.out.println("found " +e.getType(DramaSimStateEventType.class));
	DramaSimStateEventType et = e.getType(DramaSimStateEventType.class);
	if (et == null) {
	    super.stateChanged(e);
	    return;
	}
	switch (et) {
	case RECEIVE_START:
	    {
		MessageRecipient from =  e.getOrigin(MessageRecipient.class);
		MessageRecipient to =  e.getSource(MessageRecipient.class);
		Object msg = e.getParameter();
		messageReceiveStart(sim, from, to, msg);
	    }
	    break;
	case RECEIVE_END:
	    {
		MessageRecipient from =  e.getOrigin(MessageRecipient.class);
		MessageRecipient to =  e.getSource(MessageRecipient.class);
		Object msg = e.getParameter();
		messageReceiveEnd(sim, from, to, msg);
	    }
	    break;
	default:
	    throw new  RuntimeException(errorMsg("missingCase"));
	}
    }


    /**
     * Indicate that an agent or group is receiving a message.
     * @param sim the simulation
     * @param from the message sender
     * @param to the message recipient
     * @param msg the message itself
     */
    public void messageReceiveStart(Simulation sim,
				    MessageRecipient from,
				    MessageRecipient to,
				    Object msg) 
    {
	callScriptMethod("messageReceiveStart", sim, from, to, msg);
    }

    /**
     * Indicate that an agent or group has completed receiving a message.
     * @param sim the simulation
     * @param from the message sender
     * @param to the message recipient
     * @param msg the message itself
     */
    public void messageReceiveEnd(Simulation sim,
				  MessageRecipient from,
				  MessageRecipient to,
				  Object msg) 
    {
	callScriptMethod("messageReceiveEnd", sim, from, to, msg);
    }
}

//  LocalWords:  exbundle superclass missingCase sim msg
