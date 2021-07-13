import java.io.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.bzdev.devqsim.Simulation;
import org.bzdev.devqsim.SimulationLauncher;
import org.bzdev.devqsim.SimFunction;
import org.bzdev.devqsim.SimFunctionTwo;
import org.bzdev.devqsim.SimFunctionFactory;
import org.bzdev.devqsim.SimFunctionTwoFactory;
import org.bzdev.io.DetabReader;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.math.RealValuedFunctTwoOps;
import org.bzdev.obnaming.ObjectNamerLauncher;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.scripting.DefaultScriptingContext;
import org.bzdev.scripting.ExtendedScriptingContext;
import org.bzdev.util.JSObject;
import org.bzdev.util.JSOps;
import org.bzdev.util.JSUtilities;


public class SimFunctionTest {
    public static void main(String argv[]) throws Exception {

	// ScriptingContext scripting = new DefaultScriptingContext("ECMAScript");
	ScriptingContext scripting = new DefaultScriptingContext("ESP");
	scripting = new ExtendedScriptingContext(scripting);
	Simulation sim = new Simulation(scripting);

	RealValuedFunction f = new RealValuedFunction() {
		public double valueAt(double x) {return Math.sin(x);}
		public double derivAt(double x) {return Math.cos(x);}
	    };
	
	SimFunction sf = new SimFunction(sim, "sf", true, f);

	SimFunction sfa = new SimFunction(sim, "sfa", true,
					  x -> {return Math.sin(x);},
					  x -> {return Math.cos(x);},
					  null);


	System.out.format("sf.valueAt(0.0) = %g, sf.derivAt(0.0) = %g\n",
			  sf.valueAt(0.0), sf.derivAt(0.0));

	System.out.format("sfa.valueAt(0.0) = %g, sfa.derivAt(0.0) = %g\n",
			  sfa.valueAt(0.0), sfa.derivAt(0.0));

	SimFunctionFactory sff = new SimFunctionFactory(sim);
	sff.set("object", f);
	sf = sff.createObject("sff");
	System.out.format("sf.valueAt(0.0) = %g, sf.derivAt(0.0) = %g\n",
			  sf.valueAt(0.0), sf.derivAt(0.0));

	RealValuedFunctOps rvf = sf;

	scripting.putScriptObject("scripting", scripting);
	scripting.putScriptObject("sim", sim);
	
	RealValuedFunctionTwo f2 = new RealValuedFunctionTwo() {
		public double valueAt(double x, double y) {
		    return Math.sin(x)*Math.cos(y);
		}
		public double deriv1At(double x, double y) {
		    return Math.cos(x) * Math.cos(y);
		}
		public double deriv2At(double x, double y) {
		    return -Math.sin(x) * Math.sin(y);
		}
		public double deriv11At(double x, double y) {
		    return -Math.sin(x) * Math.cos(y);
		}
		public double deriv12At(double x, double y) {
		    return -Math.cos(x)*Math.sin(y);
		}
		public double deriv21At(double x, double y) {
		    return -Math.cos(x)*Math.sin(y);
		}
		public double deriv22At(double x, double y) {
		    return -Math.sin(x) * Math.cos(y);
		}
	    };

	SimFunctionTwo sf2 = new SimFunctionTwo(sim, "sf2", true, f2);

	SimFunctionTwo sfta = new SimFunctionTwo
	    (sim, "st2a", true,
	     (x,y)->{return Math.sin(x)*Math.cos(y);},
	     (x,y)->{return Math.cos(x)*Math.cos(y);},
	     (x,y)->{return -Math.sin(x)*Math.sin(y);},
	     (x,y)->{return -Math.sin(x)*Math.cos(y);},
	     (x,y)->{return -Math.cos(x)*Math.sin(y);},
	     (x,y)->{return -Math.cos(x)*Math.sin(y);},
	     (x,y)->{return -Math.sin(x)*Math.cos(y);});

	RealValuedFunctTwoOps rvf2 = sfta;


	double xx = Math.PI/4.0;
	double yy = xx;
	System.out.println("x = " + xx + ", y = " + yy);

	System.out.println("f2.valueAt(x,y) = " + f2.valueAt(xx, yy));
	System.out.println("f2.deriv1At(x,y) = " + f2.deriv1At(xx, yy));
	System.out.println("f2.deriv2At(x,y) = " + f2.deriv2At(xx, yy));
	System.out.println("f2.deriv11At(x,y) = " + f2.deriv11At(xx, yy));
	System.out.println("f2.deriv12At(x,y) = " + f2.deriv12At(xx, yy));
	System.out.println("f2.deriv21At(x,y) = " + f2.deriv21At(xx, yy));
	System.out.println("f2.deriv22At(x,y) = " + f2.deriv22At(xx, yy));


	System.out.println("sf2.valueAt(x,y) = " + sf2.valueAt(xx, yy));
	System.out.println("sf2.deriv1At(x,y) = " + sf2.deriv1At(xx, yy));
	System.out.println("sf2.deriv2At(x,y) = " + sf2.deriv2At(xx, yy));
	System.out.println("sf2.deriv11At(x,y) = " + sf2.deriv11At(xx, yy));
	System.out.println("sf2.deriv12At(x,y) = " + sf2.deriv12At(xx, yy));
	System.out.println("sf2.deriv21At(x,y) = " + sf2.deriv21At(xx, yy));
	System.out.println("sf2.deriv22At(x,y) = " + sf2.deriv22At(xx, yy));

	System.out.println("sfta.valueAt(x,y) = " + sfta.valueAt(xx, yy));
	System.out.println("sfta.deriv1At(x,y) = " + sfta.deriv1At(xx, yy));
	System.out.println("sfta.deriv2At(x,y) = " + sfta.deriv2At(xx, yy));
	System.out.println("sfta.deriv11At(x,y) = " + sfta.deriv11At(xx, yy));
	System.out.println("sfta.deriv12At(x,y) = " + sfta.deriv12At(xx, yy));
	System.out.println("sfta.deriv21At(x,y) = " + sfta.deriv21At(xx, yy));
	System.out.println("sfta.deriv22At(x,y) = " + sfta.deriv22At(xx, yy));

	System.out.println();
	System.out.println("Now use a scripting language:");
	System.out.println();

	InputStreamReader isr =
	    new InputStreamReader(new FileInputStream("ftest.esp"), "UTF-8");
	try {
	    scripting.evalScript(isr);
	} catch (Exception e) {
	    Throwable t = e;
	    while (t != null) {
		System.out.println(t.getMessage());
		for (StackTraceElement elem: t.getStackTrace()) {
		    System.out.format("    class %s, line %d: %s\n",
				      elem.getClassName(),
				      elem.getLineNumber(),
				      elem.getMethodName());
		}
		System.out.println("----");
		t = t.getCause();
	    }
	    System.exit(1);
	}

	sim = null;

	Reader r =
	    new InputStreamReader(new FileInputStream("config.yaml"), "UTF-8");
	r = new DetabReader(r, 8);
	JSObject config = (JSObject)JSUtilities.YAML.parse(r);

	ObjectNamerLauncher.newInstance("devqsim", "math");
	// new SimulationLauncher(config);

	for (String s: SimulationLauncher.getFunctions()) {
	    System.out.println(s);
	}

	r = new InputStreamReader(new FileInputStream("ftest.yaml"), "UTF-8");
	r = new DetabReader(r, 8);
	/*
	JSOps input = (JSOps)JSUtilities.YAML.parse(r);
	SimulationLauncher.process(input);
	*/
	SimulationLauncher.process(r, true);
	sim = (Simulation) SimulationLauncher.get("sim");
	SimFunction simF = (SimFunction)sim.getObject("simF");

	System.out.println("sin(0) = " + simF.valueAt(0.0));
	System.out.println("(d/dx)sin (0) = " + simF.derivAt(0.0));

	SimFunction simF2 = (SimFunction)sim.getObject("simF2");
	System.out.println("asin(0) = " + simF2.valueAt(0.0));
	System.out.println("(d/dx)asin (0) = " + simF2.derivAt(0.0));
	System.out.println("(d2/dx2)asin (0) = " + simF2.secondDerivAt(0.0));


	System.exit(0);
    }
}
