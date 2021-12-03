import org.bzdev.lang.MathOps;
import org.bzdev.scripting.*;
import org.bzdev.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExprScriptTest6 {
    public static void main(String argv[]) throws Exception {

	DefaultScriptingContext dsc = new
	    DefaultScriptingContext();
	ExtendedScriptingContext sc = new ExtendedScriptingContext(dsc);
	sc.setWriter(new PrintWriter(System.out));
	sc.putScriptObject("scripting", sc);
	String s = Files.readString(new File("test6.esp").toPath());
	try {
	    Object value = sc.evalScript(s);
	    System.out.println(value);
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
