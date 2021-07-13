import org.bzdev.devqsim.*;
import org.bzdev.lang.*;
import org.bzdev.scripting.*;

public class ScriptTest {

    public static class OurSimObject extends SimObject {

	OurSimObject(Simulation sim, String name, boolean intern) {
	    super(sim, name, intern);
	}

	public void runTask() {
	    scheduleTaskScript("x = 10;"
			       + "java.lang.System.out.println"
			       + "(\"task started (local x = \""
			       + " + x + \")\");"
			       + "sim.pauseTask(5);"
			       + "java.lang.System.out.println"
			       + "(\"task ended (local x = \""
			       + " + x + \")\");",
			       1);
	}
    }

    public static void main(String argv[]) {

	try {
	    ScriptingContext scripting =
		new DefaultScriptingContext("ECMAScript");

	    Simulation sim = new Simulation(scripting);
	    OurSimObject obj = new OurSimObject(sim, "object", true);
	    scripting.putScriptObject("sim", sim);
	    scripting.putScriptObject("obj", obj);

	    sim.scheduleScript("java.lang.System.out.println(\"hello\")", 9);
	    sim.scheduleScript("obj.runTask()", 10);
	    sim.scheduleScript("java.lang.System.out.println(\"task paused\")", 15);
	    sim.scheduleScript("java.lang.System.out.println(\"goodbye\")", 25);

	    String listenerDef = "listenerObj = {"
		+ "simulationStart: function(s) {java.lang.System.out.println(\"sim started\");}, "
		+ "simulationStop: function(s) {java.lang.System.out.println(\"sim stopped\");}"
		+ "};";

	    scripting.evalScript(listenerDef
				 + "adapter = sim.createAdapter(listenerObj);"
				 + "sim.addSimulationListener(adapter);");

	    String monitorDef = "monitor = sim.createMonitor({"
		+ "simulationPauses: function(xsim) {"
		+ "   if (xsim.moreEventsScheduled()) {"
		+ "        return false;"
                + "   } else {"
		+ "        return true;"
		+ "   }"
		+ "}});";

	    scripting.evalScript("x = 20;"
				 + "java.lang.System.out.println(\"starting (x = \" + x + \")\");"
				 + monitorDef
				 + "sim.run(monitor);"
				 + "java.lang.System.out.println(\"done (x = \" + x + \")\")");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
