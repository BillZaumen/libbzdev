import org.bzdev.scripting.*;
import org.bzdev.lang.*;
import java.io.*;
import java.util.Properties;
import javax.script.*;

public class SecTest2 {
    public static class OurScriptingContext extends DefaultScriptingContext {
	Properties props = new Properties();

	private void init() {
	    props.setProperty("ECMAScript",
			      "({open: function(name) {"
			      + "return new java.io.FileInputStream(name)"
			      + "}})");
	}

	public OurScriptingContext() {
	    super();
	    init();
	}
	public OurScriptingContext(boolean trusted) {
	    super(trusted);
	    init();
	}
	public void doit(ScriptingContext.PFMode mode, String name)
	    throws Exception
	{
	    invokePrivateFunction(props, mode, "open", name);
	}
    }


    public static void main(String argv[]) throws Exception {

	OurScriptingContext sc = new OurScriptingContext();
	sc.putScriptObject("scripting", sc);

	OurScriptingContext tsc = new OurScriptingContext(true);
	tsc.putScriptObject("scripting", tsc);


	System.setSecurityManager(new ScriptingSecurityManager());

	ScriptingContext.PFMode[] modes = {
	    ScriptingContext.PFMode.NORMAL,
	    ScriptingContext.PFMode.PRIVILEGED,
	    ScriptingContext.PFMode.SANDBOXED,
	};

	

	for (ScriptingContext.PFMode mode:  modes) {
	    System.out.println("mode = " + mode + " ...");
	    System.out.print("sc: ");
	    try {
		sc.doit(mode, "test1.js");
		System.out.println("succeeded");
	    } catch (Exception e) {
		System.out.println("failed");
	    }
	    System.out.print("sc eval: ");
	    try {
		sc.putScriptObject("mode", mode);
		sc.evalScript("scripting.doit(mode,\"test1.js\")");
		System.out.println("succeeded");
	    } catch (Exception e) {
		System.out.println("failed");
	    }
	    System.out.print("tsc: ");
	    try {
		tsc.doit( mode, "test1.js");
		System.out.println("succeeded");
	    } catch (Exception e) {
		System.out.println("failed");
	    }
	    System.out.print("tsc eval: ");
	    try {
		tsc.putScriptObject("mode", mode);
		tsc.evalScript("scripting.doit(mode,\"test1.js\")");
		System.out.println("succeeded");
	    } catch (Exception e) {
		System.out.println("failed");
	    }

	}
	System.out.println("Expected:");
	System.out.println("... mode = NORMAL ...");
	System.out.println("... sc: succeeded");
	System.out.println("... sc eval: failed?*");
	System.out.println("... tsc: succeeded");
	System.out.println("... tsc eval: succeeded");
	System.out.println("... mode = PRIVILEGED ...");
	System.out.println("... sc: succeeded");
	System.out.println("... sc eval: succeeded");
	System.out.println("... tsc: succeeded");
	System.out.println("... tsc eval: succeeded");
	System.out.println("... mode = SANDBOXED ...");
	System.out.println("... sc: failed?*");
	System.out.println("... sc eval: failed?");
	System.out.println("... tsc: succeeded");
	System.out.println("... tsc eval: succeeded");
	System.out.println();
	System.out.println("* script engines might ignore permissions");
	System.out.println("  granted by a security policy - for example,");
	System.out.println("  a script could be dynamically compiled, in");
	System.out.println("  which case the codebase is less than obvious");
    }
}
