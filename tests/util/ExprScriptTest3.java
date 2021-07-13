import org.bzdev.lang.MathOps;
import org.bzdev.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExprScriptTest3 {
    public static void main(String argv[]) throws Exception {

	ExpressionParser parser = new ExpressionParser(MathOps.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	parser.set("out", new PrintWriter(System.out));

	parser.set("bar1", null);
	parser.set("bar2", null);
	parser.set("bar4", 40.0);

	String s = Files.readString(new File("test3.esp").toPath());
	
	Object value = parser.parse(s);

	System.out.println("out = " + parser.get("out"));
	System.out.println("out1 = " + parser.get("out1"));
	System.out.println("out2 = " + parser.get("out2"));

	System.out.println("value = " + value);
	System.out.println("count = " + parser.get("count"));
	if (value instanceof Number) {
	    double val = ((Number)value).doubleValue();
	    if (val != 30.0) {
		throw new Exception();
	    }
	}  else {
	    throw new Exception("a Double was expected");
	}

	Map<String,Object> altBindings = Collections
	    .synchronizedMap(new HashMap<String,Object>());
	
	altBindings.put("f", parser.get("f"));

	System.out.println("before setBindings: "
			   + parser.exists("count"));
	Map<String,Object> origBindings = parser.getBindings();
	parser.setBindings(altBindings);
	System.out.println("after setBindings: "
			   + parser.exists("count"));
	System.out.println(parser.parse("f(20)"));

	System.out.println("count = " + origBindings.get("count"));
	
	Map<String,Object> bindings = Collections
	    .synchronizedMap(new HashMap<String,Object>());

	parser.setBindings(bindings);
	s = Files.readString(new File("test3a.esp").toPath());
	parser.parse(s);
	System.out.println("f(100) = " + parser.parse("f(100)"));
	System.out.println("g(100) = " + parser.parse("g(100)"));
	
	try {
	    bindings = Collections
		.synchronizedMap(new HashMap<String,Object>());
	    s = Files.readString(new File("test3b.esp").toPath());
	    parser.parse(s);
	    parser.parse("f(10)");
	    System.out.println("there should have been an exception");
	    System.exit(1);
	} catch(Exception e) {
	    System.out.println("saw exception as expected: "
			       + e.getMessage());
	}

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out, true));

	parser.set("vtest", 10);
	parser.set("vtest1", 20);
	parser.set("vtest2", 30);

	try {
	    s = Files.readString(new File("test3c.esp").toPath());
	    value =  parser.parse(s);
	} catch (Exception e) {
	    System.out.println(e);
	    System.out.println(e.getMessage());
	    for (StackTraceElement ste: e.getStackTrace()) {
		System.out.println(ste);
	    }
	    Throwable t = e;
	    while ((t = t.getCause()) != null) {
		System.out.println("-----");
		System.out.println(t);
		System.out.println(t.getMessage());
		for (StackTraceElement ste: t.getStackTrace()) {
		    System.out.println(ste);
		}
	    }
	    System.exit(1);
	}
	System.out.println("value = " + value);

	try {
	    s = Files.readString(new File("test3d.esp").toPath());
	    value =  parser.parse(s);
	    System.out.println("should have thrown an exception "
			       + "loading test3d.esp");
	    System.exit(1);
	} catch (Exception e) {
	    System.out.println("exception expected: " + e.getMessage());
	}

	s = Files.readString(new File("test3e.esp").toPath());
	value =  parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 20) {
	    throw new Exception("value not 20");
	}

	System.exit(0);

    }
}
