import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
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

    public TestActor(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    protected void defaultCall() {
	System.out.println("    defaultCall: " +getName());
    }

    // to make it public
    public void scheduleDefaultCall(long delay) {
	super.scheduleDefaultCall(delay);
    }

    protected void defaultTask() {
	for (int i = 0; i < 5; i++) {
	    System.out.println("defaultTask executing: "
			       + getName()
			       + " at time "
			       + getSimulation().currentTicks());
	    TaskThread.pause(10);
	}
	System.out.println("defaultTask completed: "
			   + getName() 
			   + " at time "
			   + getSimulation().currentTicks());
    }

    // to make it public
    public void scheduleDefaultTask(long delay)
	throws InterruptedException, IllegalStateException
    {

	super.scheduleDefaultTask(delay);
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
}
