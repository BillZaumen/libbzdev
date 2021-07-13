import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;
import org.bzdev.drama.common.ConditionMode;

@DMethodContexts({
	@DMethodContext(helper="org.bzdev.drama.DoReceive",
			localHelper = "TestActorDoReceive"),
	@DMethodContext(helper = "org.bzdev.drama.OnConditionChange",
			localHelper = "TestActorOnConditionChange")
})
public class TestActor extends Actor {

    static {
	TestActorDoReceive.register();
	TestActorOnConditionChange.register();
    }

    public TestActor(DramaSimulation sim, String name) {
	super(sim, name, true);
    }
    public TestActor(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }
    protected void defaultCall() {
	System.out.println("    defaultCall: " +getName());
	DramaSimulation sim = getSimulation();
	if (sim.getStackTraceMode()) {
	    for (StackTraceElement ste: sim.getEventStackTrace()) {
		     System.out.println(ste);
	    }
	}
    }

    public void scheduleDefaultCall(long delay) {
	super.scheduleDefaultCall(delay);
    }

    public void cancelDefaultCall() {
	super.cancelDefaultCall();
    }

    public void sendNewMsg(Actor dest, long delay) {
	Object msg = new TestMessage("hello");
	send(msg, dest, delay);
    }

    public void sendNewMsg(Group dest, long delay) {
	Object msg = new TestMessage("hello");
	send(msg, dest, delay);
    }

    public boolean sendNewMsg(Actor dest) {
	Object msg = new TestMessage("hello");
	return send(msg, dest);
    }



    public boolean sendNewMsg(Group dest) {
	Object msg = new TestMessage("hello");
	return send(msg, dest);
    }


    @DMethodImpl("org.bzdev.drama.DoReceive")
    protected void doReceiveImpl(TestMessage msg, Actor source,
				 boolean wereQueued) {
	System.out.println("    " + getName() + " received msg \""
			   + msg.getString()
			   + "\" from " + source.getName()
			   + " at time " + getSimulation().currentTicks());

	DramaSimulation sim = getSimulation();
	if (sim.getStackTraceMode()) {
	    for (StackTraceElement ste: sim.getEventStackTrace()) {
		     System.out.println(ste);
	    }
	}
    }

    @DMethodImpl("org.bzdev.drama.OnConditionChange")
    protected void doOnConditionChange(DoubleCondition c, ConditionMode mode,
				       SimObject source)
    {
	System.out.println("For actor " + getName()
			   + ": condition = " +c.getName()
			   + ": value = " + c.getValue()
			   + ", mode = " + mode
			   + ", source = " + source.getName());
    }

    @DMethodImpl("org.bzdev.drama.OnConditionChange")
    protected void doOnConditionChange(BooleanCondition c, ConditionMode mode,
				       SimObject source)
    {
	System.out.println("For actor " + getName()
			   + ": condition = " +c.getName()
			   + ", value = " + c.getValue()
			   + ", mode = " + mode
			   + ", source = " + source.getName());
    }


    public void testConditionHandling(Condition c) {
	onConditionChange(c, ConditionMode.OBSERVER_NOTIFIED,
			  c);
    }

    public void makeCall() {
	scheduleCall(new Callable() {
		public void call() {
		    System.out.println("called at " 
				       +getSimulation().currentTicks());
		}
	    }, 10, "makeCall");
    }

    public void makeRuns() {
	scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < 5; i++) {
			System.out.println("task is at simulation time "
					   +getSimulation().currentTicks()
					   +" [i=" +i +"]");
			TaskThread.pause(10);
		    }
		    System.out.println("task is at simulation time "
				       +getSimulation().currentTicks());
		}
	    },
	    10, "task test 1");
	
	scheduleTask(new Runnable() {
		public void run() {
		    System.out.println("next task is at simulation time "
				       +getSimulation().currentTicks());
		}
	    },
	    100, "task test 2");
    }
}
