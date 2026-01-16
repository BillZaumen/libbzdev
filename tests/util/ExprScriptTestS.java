import org.bzdev.anim2d.*;
import org.bzdev.util.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.MathOps;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import static java.lang.Math.hypot;
import static java.lang.Math.cos;
import static java.lang.Math.acos;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.DoubleBinaryOperator;


public class ExprScriptTestS {

    static String errormsg(String name, Object...args) {
	return name;
    }

    public static void main(String argv[]) throws Exception {

	ExpressionParser parser = new ExpressionParser();
	parser.setScriptingMode();

	Map<String,Object> startBindings = parser.getBindings();
	parser.parse("var sum = 0");
	parser.parse("function add(incr) {sum = sum + incr; sum}");
	System.out.println(parser.parse("add(1)"));
	System.out.println(parser.parse("add(1)"));
	System.out.println(parser.parse("add(1)"));
	Map<String,Object>bindings = new HashMap<>();
	System.out.println("sum = " + parser.get("sum"));
	parser.parse("var sum = 0", bindings);
	parser.parse("function add(incr) {sum = sum + incr; sum}",
		     bindings);
	bindings.put("add1", parser.get("add"));
	System.out.println(parser.parse("add(1)", bindings));
	System.out.println(parser.parse("add(1)", bindings));
	System.out.println(parser.parse("add(1)", bindings));
	System.out.println(parser.parse("add(1)", bindings));
	System.out.println(parser.parse("add(1)", bindings));
	System.out.println(parser.parse("add(1)", bindings));
	System.out.println("sum = " + parser.get("sum"));
	System.out.println("sum [using bindings] = " + bindings.get("sum"));
	System.out.println(parser.parse("add1(1)", bindings));
	System.out.println(parser.parse("add1(1)", bindings));
	System.out.println("sum = " + parser.get("sum"));

	Map<String,Object> origBindings = parser.getBindings();
	System.out.println("startBindings/origBindings/bindings = "
			   + startBindings.hashCode() + "/"
			   + origBindings.hashCode() + "/"
			   + bindings.hashCode());
	parser.setBindings(bindings);
	System.out.println("sum (with bindings set) = " + parser.get("sum"));
	System.out.println(parser.parse("add(1)"));
	System.out.println(parser.parse("add(1)"));
	System.out.println("sum (with bindings set) = " + parser.get("sum")
			   + " [from bindings: " + bindings.get("sum")
			   + "]");
	parser.setBindings(origBindings);
	System.out.println("sum (original bindings) = " + parser.get("sum")
			   + " [from orig bindings: " + origBindings.get("sum")
			   + "]");
	System.exit(0);
    }
}
