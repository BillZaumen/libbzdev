import org.bzdev.lang.MathOps;
import org.bzdev.scripting.*;
import org.bzdev.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExprScriptTest17 {
    public static void main(String argv[]) throws Exception {

	DefaultScriptingContext dsc = new
	    DefaultScriptingContext();
	ExtendedScriptingContext sc = new ExtendedScriptingContext(dsc);
	PrintWriter out = new PrintWriter(System.out);
	sc.setWriter(out);
	sc.putScriptObject("scripting", sc);
	String s = Files.readString(new File("test17.esp").toPath());
	try {
	    // Uses the same function for the 1st and 2nd derivatives,
	    // which is mathematically wrong but we want as few objects
	    // defined as possible as we are testing how functional
	    // interfaces are handled.
	    Object o = sc.evalScript(s);
	    out.flush();
	    System.out.println("result = " + o);
	} catch (Exception e) {
	    String name = e.getClass().getName();
	    name = name.replace('$', '.');
	    System.out.println(name + ": "
			       + e.getMessage());
	    for (StackTraceElement ste: e.getStackTrace()) {
		String cn = ste.getClassName();
		cn.replace('$', '.');
		int ind = cn.lastIndexOf('.');
		if (ind >= 0) {
		    cn = cn.substring(ind+1);
		}
		System.out.format("... at %s.%s (line %d)\n",
				  cn,
				  ste.getMethodName(),
				  ste.getLineNumber());
	    }
	    Throwable ee = e;
	    while ( (ee = ee.getCause()) != null) {
		System.out.println("---------");
		name = ee.getClass().getName();
		name = name.replace('$', '.');
		System.out.println(name + ": "
				   + ee.getMessage());
		for (StackTraceElement ste: ee.getStackTrace()) {
		    String cn = ste.getClassName();
		    cn = cn.replace('$', '.');
		    int ind = cn.lastIndexOf('.');
		    if (ind >= 0) {
			cn = cn.substring(ind+1);
		    }
		    System.out.format("... at %s.%s (line %d)\n",
				      cn,
				      ste.getMethodName(),
				      ste.getLineNumber());
		}
	    }
	}
    }
}
