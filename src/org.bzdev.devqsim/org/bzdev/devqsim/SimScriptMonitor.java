package org.bzdev.devqsim;
import javax.script.ScriptException;

//@exbundle org.bzdev.devqsim.lpack.Simulation

class SimScriptMonitor
    extends SimulationMonitor<Simulation>
{
    static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    Object scriptObject;
    Simulation sim;

    @Override
    public final boolean simulationPauses() {
	try {
	    Object result = sim.callScriptMethod(scriptObject,
						 "simulationPauses", sim);
	    if (result == null) {
		return false;
	    } else if (result instanceof Boolean) {
		return ((Boolean)result).booleanValue();
	    } else if (result instanceof Number) {
		if (((Number)result).intValue() == 0) return false;
	    }
	    return true;
	} catch (ScriptException e) {
	    String msg = errorMsg("simScriptPauses");
	    throw new RuntimeException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("simScriptPauses");
	    throw new RuntimeException(msg, e);
	}
    }
    
    SimScriptMonitor(Simulation simulation, Object scriptObject) {
	super(simulation);
	sim = simulation;
	this.scriptObject = scriptObject;
    }
}

//  LocalWords:  exbundle simulationPauses simScriptPauses
