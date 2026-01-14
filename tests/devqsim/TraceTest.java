import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

class TraceSimObject extends DefaultSimObject {
    public TraceSimObject(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    int count = 0;

    public void call() {
	trace(0, " initially count = %d", count);
	count++;
	trace(5, " intermediate count = %d", count);
	count++;
	trace(0, " finally count = %d", count);
    }
}

class TraceSimObjectFactory extends DefaultSimObjectFactory<TraceSimObject> {
    public TraceSimObjectFactory(Simulation sim) {
	super(sim);
    }

    protected TraceSimObject newObject(String name) {
	return new TraceSimObject(getSimulation(), name, willIntern());
    }
}

public class TraceTest {
    public static void main(String argv[]) throws Exception {
	Simulation sim = new Simulation();
	sim.setTraceOutput(System.out);

	final TraceSimObject object = new TraceSimObject(sim, "object", true);

	TraceSet traceSet1 = new TraceSet(sim, "traceSet1", true);

	traceSet1.setLevel(0);

	TraceSetFactory tsf = new TraceSetFactory(sim);
	tsf.set("level", 5);
	tsf.set("stackTraceMode", false);
	tsf.set("stackTraceLimit", 0);

	TraceSet traceSet2 = tsf.createObject("traceSet2");
	traceSet2.setOutput(System.out);


	object.addTraceSet(traceSet1);
	object.call();
	object.removeTraceSet(traceSet1);
	object.addTraceSet(traceSet2);
	object.call();
	object.clearTraceSets();
	object.addTraceSet(traceSet1);
	traceSet1.setStackTraceMode(true);
	object.call();
	traceSet1.setStackTraceLimit(1);
	object.call();
	System.out.println("disable tracing");
	sim.enableTracing(false);
	object.call();
	System.out.println("enable tracing");
	sim.enableTracing(true);
	object.call();
	System.out.println("--------------");
	System.out.println("now test trace set factory");
	object.clearTraceSets();
	tsf = new TraceSetFactory(sim);
	tsf.set("level", 1);
	tsf.set("stackTraceMode", true);
	tsf.set("stackTraceLimit", 10);

	tsf.set("timeline.time", 1, 2.0);
	tsf.set("timeline.stackTraceLimit", 1, 1);

        tsf.set("timeline.time", 2, 4.0);
	tsf.set("timeline.stackTraceMode", 2, false);

        tsf.set("timeline.time", 3, 6.0);
	tsf.set("timeline.level", 3, 5);

	TraceSet traceSet3 = tsf.createObject("traceSet3");
	traceSet3.setOutput(System.out);

	object.addTraceSet(traceSet3);

	for (int i = 1; i <= 7; i += 2) {
	    long ticks = Math.round(sim.getTicksPerUnitTime() * i);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			object.call();
		    }
		}, ticks);
	}
	sim.run();

	System.out.println("----------------------");
	System.out.println("Object timeline test");

	final Simulation tsim = new Simulation();
	tsim.setTraceOutput(System.out);

	traceSet1 = new TraceSet(tsim, "traceSet1", true);
	traceSet1.setLevel(0);

	traceSet2 = new TraceSet(tsim, "traceSet2", true);
	traceSet2.setLevel(5);

	TraceSimObjectFactory tsof =
	    new TraceSimObjectFactory(tsim);

	tsof.set("timeline.time", 1, 2.0);
	tsof.set("timeline.traceSetMode", 1, "KEEP");
	tsof.add("timeline.traceSets", 1, traceSet1);
	tsof.add("timeline.traceSets", 1, traceSet2);

	tsof.set("timeline.time", 2, 4.0);
	tsof.set("timeline.traceSetMode", 2, "REPLACE");
	tsof.add("timeline.traceSets", 2, traceSet1);

	tsof.set("timeline.time", 3, 6.0);
	tsof.set("timeline.traceSetMode", 3, "REMOVE");
	tsof.add("timeline.traceSets", 3, traceSet1);

	tsof.set("timeline.time", 4, 8.0);
	tsof.set("timeline.traceSetMode", 4, "REPLACE");
	tsof.add("timeline.traceSets", 4, traceSet1);
	tsof.add("timeline.traceSets", 4, traceSet2);

	tsof.set("timeline.time", 5, 10.0);
	tsof.set("timeline.traceSetMode", 5, "REPLACE");

	final TraceSimObject tso = tsof.createObject("tso");

	for (int i = 1; i <= 11; i += 2) {
	    long ticks = Math.round(sim.getTicksPerUnitTime() * i);
	    tsim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("calling at time "
					   + tsim.currentTime());
			tso.call();
		    }
		}, ticks);
	}
	tsim.run();

	
	TraceSet traceSet4 = new TraceSet(tsim, "traceSet4", true);

	tsim.allowSetStackTraceMode(true, true);

	try {
	    // System.setSecurityManager(new SecurityManager());

	    // verify that we do not throw a security exception
	    tsim.setStackTraceMode(true);
	    traceSet4.setStackTraceMode(true);
	} catch (UnsupportedOperationException eu) {}

	System.exit(0);
    }
}
