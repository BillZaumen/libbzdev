import org.bzdev.scripting.*;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.ScriptEngine;
import java.util.Properties;
import java.io.FileNotFoundException;

public class ThreadTest {
    static class OurScriptingContext extends DefaultScriptingContext {
	Properties properties = null;
	ScriptEngine engine;
	public OurScriptingContext() {
	    super("ESP");
	    engine = doGetScriptEngine();
	}

	public OurScriptingContext(Properties properties) {
	    super("ESP");
	    engine = doGetScriptEngine();
	    this.properties = properties;
	}
	ScriptingContext.BindingSwapper swapper = null;

	public void setup(Bindings bindings) {
	    swapper = createBindingSwapper(bindings);
	}
	public void toggle() {
	    swapper.swap();
	}
	public Object testP(Object x) throws ScriptException {
	    return invokePrivateFunction(properties,
					 // ScriptingContext.PFMode.PRIVILEGED,
					 "test",
					 x);
	}

    }

    static Bindings bindings;
    static OurScriptingContext scripting = new OurScriptingContext();

    public static void setup() {
	scripting.setup(bindings);
    }

    public static void toggle() {
	scripting.toggle();
    }

    volatile static Object monitor1 = new Object();
    volatile static boolean monitor1Done = false;

    public static void wait1() {
	try {
	    synchronized(monitor1) {
		while (!monitor1Done) monitor1.wait();
	    }
	} catch (InterruptedException ie) {
	    System.out.println(ie.getMessage());
	} catch (IllegalMonitorStateException ime) {
	    System.out.println(ime.getMessage());
	}
    }
    public static void notify1() {
	synchronized(monitor1) {
	    monitor1Done = true;
	    monitor1.notify();
	}
    }

    static Object monitor2 = new Object();
    static boolean monitor2Done = false;

    public static void wait2() {
	try {
	    synchronized(monitor2) {
		while (!monitor2Done) monitor2.wait();
	    }
	} catch (InterruptedException ie) {
	    System.out.println(ie.getMessage());
	} catch (IllegalMonitorStateException ime) {
	    System.out.println(ime.getMessage());
	}
    }
    public static void notify2() {
	synchronized(monitor2) {
	    monitor2Done = true;
	    monitor2.notify();
	}
    }

    static Object monitor3 = new Object();
    static boolean monitor3Done = false;

    public static void wait3() {
	try {
	    synchronized(monitor3) {
		while (!monitor3Done) monitor3.wait();
	    }
	} catch (InterruptedException ie) {
	    System.out.println(ie.getMessage());
	} catch (IllegalMonitorStateException ime) {
	    System.out.println(ime.getMessage());
	}
    }
    public static void notify3() {
	synchronized(monitor3) {
	    monitor3Done = true;
	    monitor3.notify();
	}
    }

    public static void main(String argv[]) {
	scripting.putScriptObject("scripting", scripting);

	try  {
	    scripting.evalScript("import(\"\", [ThreadTest])");
	    scripting.evalScript("import(java.lang.Thread)");
	    System.out.println("... should print hello, a blank line,  and "
			       + "then goodbye");
	    scripting.evalScript
		("global.getWriter().println(\"hello\");" +
		 "scripting.evalScript(\"global.getWriter().println(\\\"\\\");\");" +
		 "global.getWriter().println(\"goodbye\");");

	    scripting.putScriptObject("x", Double.valueOf(10));

	    bindings = scripting.createBindings();
	    scripting.putScriptObject("bindings", bindings);
	    bindings.put("x", Double.valueOf(20));
	    // need to call createBindingSwapper (via setup()) in
	    // a script so that the inherited thread local variable
	    // will be set to the correct value.
	    scripting.evalScript
		("global.getWriter().println(x); Packages.ThreadTest.setup();");
	    // need to call toggle 3 times due to the test environment.
	    // The first call sets up the engine with the default bindings.
	    // The second call sets up the engine with 'bindings'.
	    // The third calll restores the default bindings.
	    System.out.println("execting to see 10, 20, 10, one per line");
	    toggle();
	    scripting.engine.eval("global.getWriter().println(x)");
	    toggle();
	    scripting.engine.eval("global.getWriter().println(x)");
	    toggle();
	    scripting.engine.eval("global.getWriter().println(x)");

	    bindings.put("plstring",
			 "global.getWriter().println(\"evaluated plstring\")");
	    /*
	      ESP does not have 'eval' as a built-in function
	    scripting.evalScript("eval(plstring)", bindings);
	    */

	    if (scripting.getScriptObject("plstring") != null) {
		System.out.println("wrong bindings?");
	    }
	    bindings.put("scripting", scripting);
	    scripting.setup(bindings);
	    scripting.evalScript("scripting.evalScript(plstring)",
				 bindings);

	    Thread thread = new Thread(new Runnable() {
		    public void run() {
			try {
			    scripting.evalScript
				("global.getWriter().println(\"thread started\");" +
				 "Packages.ThreadTest.notify1();" +
				 "Packages.ThreadTest.wait2();" +
				 "global.getWriter().println(\"thread completing\");" +
				 "Packages.ThreadTest.notify3();");
			} catch (ScriptException se) {
			    System.out.println("thread: " + se.getMessage());
			    System.exit(1);
			}
		    }
		});
	    scripting.putScriptObject("thread", thread);

	    scripting.evalScript
		("global.getWriter().println(\"starting thread test\");" +
		 "thread.start();" +
		 "Packages.ThreadTest.wait1();" +
		 "global.getWriter().println(\"thread pausing\");" +
		 "Packages.ThreadTest.notify2();" +
		 "Packages.ThreadTest.wait3();" +
		 "global.getWriter().println(\"ending thread test\")");


	    // Warning a scripting context using ESP just be created
	    // before a security manager is installed.
	    Properties props = new Properties();
	    props.setProperty("ECMAScript",
			      "({test: function(fname)"
			      + "{return new java.io.FileInputStream(fname)}"
			      + "})");
	    props.setProperty("ESP",
			      "import(java.io, FileInputStream);"
			      + "({test: function(fname)"
			      + "{new java.io.FileInputStream(fname)}"
			      + "})");

	    scripting = new OurScriptingContext(props);

	    /*
	    try {
		System.setSecurityManager(new SecurityManager());
	    } catch (UnsupportedOperationException eu) {System.exit(0);}
	    */

	    /*
	    try {
		scripting.testP("jtest2");
		System.out.println
		    ("invokePrivateFunction unexpectedly succeeeded");
		System.exit(1);
	    } catch (Exception e) {
		System.out.println
		    ("invokePrivateFunction threw an exception as expected");
	    }
	    */
	    try {
		scripting.testP("jtest2");
	    } catch (Exception e) {
		if (e.getCause().getCause() instanceof FileNotFoundException) {
		    System.out.println("File \"jtest2\" does not exist"
				       + "as expected");
		} else {
		    throw e;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.exit(1);
	}
	System.exit(0);
    }
}
