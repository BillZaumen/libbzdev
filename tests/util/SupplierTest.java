import org.bzdev.lang.MathOps;
import org.bzdev.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;



public class SupplierTest {

    public static class MTest {
	DoubleSupplier s;

	public void setSupplier(DoubleSupplier supplier) {
	    s = supplier;
	}
	
	public void test() {
	    Double value = s.getAsDouble();
	    System.out.println(value);
	}
    }

    public static class MRef {
	public double dm() {return 20.0;}
    }

    static void  jtest() {
	var mt = new SupplierTest.MTest();
	var mr = new SupplierTest.MRef();
	mt.setSupplier(mr::dm);
	mt.test();
    }

    static void printInfo(ExpressionParser parser) throws Exception {
	List<URL> links = new LinkedList<>();
	Collections
	    .addAll(links,
		    new URL("file:/usr/share/doc/openjdk-11-doc/api/"),
		    new URL("file:/usr/share/doc/libbzdev-doc/api/"));
	
	parser.createAPIMap(links);
	var keymap = new TemplateProcessor.KeyMap();
	keymap.put("const", parser.keylistForConstants());
	keymap.put("rclasses", parser.keylistForReturnClasses());
	keymap.put("aclasses", parser.keylistForArgumentClasses());
	keymap.put("functs", parser.keylistForFunctions());
	keymap.put("methods", parser.keylistForMethods());
	keymap.print(new PrintWriter("supplier.txt"));
    }


    public static void main(String argv[]) throws Exception {
	// jtest();
	ExpressionParser parser = new ExpressionParser();
	try {
	    parser.setScriptingMode();
	    parser.setImportMode();
	    parser.setScriptImportMode();
	    parser.setGlobalMode();
	    parser.addClasses(SupplierTest.class,
			      SupplierTest.MTest.class,
			      SupplierTest.MRef.class);

	    parser.parse("var mt = new SupplierTest.MTest();");
	    parser.parse("mt.setSupplier(function() {10.0})");
	    parser.parse("mt.test();");

	    parser.parse("var mr = new SupplierTest.MRef();");
	    parser.parse("mt.setSupplier(mr::dm);");
	    parser.parse("mt.test();");
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
	    printInfo(parser);
	}
    }
}
