import org.bzdev.devqsim.*;
import org.bzdev.lang.*;
import org.bzdev.scripting.*;

public class ScriptTestESP {

    public static class OurSimObject extends SimObject {

	OurSimObject(Simulation sim, String name, boolean intern) {
	    super(sim, name, intern);
	}

	public void runTask() {
	    scheduleTaskScript("var x = 10;"
			       + "var out = global.getWriter();"
			       + "out.println"
			       + "(\"task started (local x = \""
			       + " + x + \")\");"
			       + "sim.pauseTask(5);"
			       + "out.println"
			       + "(\"task ended (local x = \""
			       + " + x + \")\");",
			       1);
	}
    }

    public static void main(String argv[]) {

	try {
	    ExtendedScriptingContext scripting =
		new ExtendedScriptingContext(new
					     DefaultScriptingContext("ESP"));

	    scripting.importClass("org.bzdev.devqsim.Simulation");
	    scripting.importClass("org.bzdev.devqsim.Simulation");
	    scripting.importClass("org.bzdev.scripting.ScriptListenerAdapter");
	    scripting.importClass("", "ScriptTestESP.OurSimObject");
	    scripting.finishImport();

	    Simulation sim = new Simulation(scripting);
	    OurSimObject obj = new OurSimObject(sim, "object", true);
	    scripting.putScriptObject("out", scripting.getWriter());
	    scripting.putScriptObject("sim", sim);
	    scripting.putScriptObject("obj", obj);

	    sim.scheduleScript("out.println(\"hello\")", 9);
	    sim.scheduleScript("obj.runTask()", 10);
	    sim.scheduleScript("out.println(\"task paused\")", 15);
	    sim.scheduleScript("out.println(\"goodbye\")", 25);

	    String listenerDef = "var listenerObj = {"
		+ "simulationStart: function(s) {out.println(\"sim started\");}, "
		+ "simulationStop: function(s) {out.println(\"sim stopped\");}"
		+ "};";

	    scripting.evalScript(listenerDef
				 + "var adapter = "
				 + "sim.createAdapter(listenerObj);"
				 + "sim.addSimulationListener(adapter);");

	    String monitorDef = "var monitor = sim.createMonitor({"
		+ "simulationPauses: function(xsim) {"
		+ "   xsim.moreEventsScheduled()? false: true}});";

	    scripting.evalScript("var x = 20;"
				 + "out.println(\"starting "
				 + "(x = \" + x + \")\");"
				 + monitorDef
				 + "sim.run(monitor);"
				 + "out.println(\"done (x = \" + x + \")\")");
	} catch (Exception e) {
	    Throwable t = (Throwable) e;
	    while (t != null) {
		System.out.format("%s: %s\n", t.getClass().getName(),
				  t.getMessage());
		StackTraceElement[] trace = t.getStackTrace();
		for (StackTraceElement ste: trace) {
		    System.out.format("  %s [%s] line %s\n",
				      ste.getClassName(),
				      ste.getMethodName(),
				      ste.getLineNumber());
		}
		t = t.getCause();
		if (t != null) {
		    System.out.println("----------");
		}
	    }
	    System.exit(1);
	}
	System.exit(0);
    }
}
