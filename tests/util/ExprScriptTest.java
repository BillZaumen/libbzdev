import org.bzdev.anim2d.*;
import org.bzdev.util.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.MathOps;

import java.io.File;
import static java.lang.Math.hypot;
import static java.lang.Math.cos;
import static java.lang.Math.acos;

import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.DoubleBinaryOperator;


public class ExprScriptTest {

    static String errormsg(String name, Object...args) {
	return name;
    }

    static final char[] linesep = System.getProperty("line.separator")
	.toCharArray();
    static final char SEP_END = (linesep.length == 0)? '\n':
	linesep[linesep.length-1];

    static final String COMMENT_RE = "//.*" + SEP_END;

    // copied from ExpressionParser for testing
    private static int skipOverEOL(String s, int i) {
	int len = s.length();
	if (i == len) return i;
	char ch = s.charAt(i);
	while (ch != SEP_END) {
	    i++;
	    if (i == len) return i;
	    ch = s.charAt(i);
	}
	i++;
	return i;
    }

    // copied from ExpressionParser for testing
    private static int skipWhitespace(String s, int i, boolean noterm) {
	int len = s.length();
	char ch = s.charAt(i);
	while (Character.isWhitespace(ch) || ch == '/') {
	    if (ch == '/') {
		i++;
		if (i < len && s.charAt(i) == '/') {
		    i++;
		    i = skipOverEOL(s, i);
		} else {
		    i--;
		    return i;
		}
	    } else {
		i++;
	    }
	    if (i == len) {
		if (noterm) {
		    //input terminated unexpectedly
		    String msg = errormsg("unterminatedVar");
		    throw new ObjectParser.Exception(msg, s, i);
		} else {
		    return i;
		}
	    }
	    ch = s.charAt(i);
	}
	return i;
    }

    public static void main(String argv[]) throws Exception {

	String s = "abcdexyz\nABCDEXY\n";
	System.out.print(s.substring(skipOverEOL(s, 0)));
	System.out.print(s.substring(skipOverEOL(s, 5)));
	System.out.print(s.substring(skipOverEOL(s, 8)));
	System.out.println(s.substring(skipOverEOL(s, 9)));

	s = "    hello";
	System.out.println(s.substring(skipWhitespace(s, 0, true)));
	s = " // tests\nhello";
	System.out.println(s.substring(skipWhitespace(s, 0, true)));
	s = "// tests\nhello";
	System.out.println(s.substring(skipWhitespace(s, 0, true)));
	s = "/hello";
	System.out.println(s.substring(skipWhitespace(s, 0, true)));
	s = " /hello";
	System.out.println(s.substring(skipWhitespace(s, 0, true)));

	String[] testStrings = {
	    "hello // lskdfj \n.foo",
	    "hello . // lsdk\nfoo",
	    "hello . // lsdk\n foo",
	    "hello // lsdk\n. foo",
	    "hello//lsdk\n .foo",
	    "hello//lsdk\n . foo",
	    "hello . foo // lsdkjf\n"
	};

	for (String str: testStrings) {
	    System.out.println(str.replaceAll(COMMENT_RE, "")
			       .replaceAll("\\s", ""));
	}

	System.out.println("-----------------------------------");

	ExpressionParser parser = new ExpressionParser();
	s = "var temp1 = ({a: 10, b: 30}[\"a\"] + "
	    + "[10, 20, 30, 40][0]) / tan(20);";
	parser.addClasses(Math.class);
	parser.parse(s);
	System.out.println("first parse OK");

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.addClasses(Math.class);

	parser.parse(s);
	System.out.println("second parse OK");
	parser.set("temp1", 0);
	s = "temp1 = ({a: 10, b: 30}[\"a\"] + "
	    + "[10, 20, 30, 40][0]) / tan(20);";
	parser.parse(s);
	System.out.println("third parse OK");


	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();

	Map<String,Object> gmap = new HashMap<String,Object>();
	gmap.put("org.bzdev.test", true);
	parser.setGlobalBindings(gmap);

	// parser.addClasses(Math.class);
	// parser.addClasses(MathOps.class);
	// parser.addClasses(StringBuilder.class);

	int[] iarray = {1, 2, 0};
	parser.set("iarray", iarray);

	ExpressionParser parser2 = new ExpressionParser();
	parser2.setScriptingMode();

	s = Files.readString(new File("test.esp").toPath());


	if (argv.length > 0 && argv[0].equals("-s")) {
	    SecurityManager sm = new SecurityManager();
	    System.out.println("security manager set");
	    System.setSecurityManager(sm);
	}

	try {
	    parser.parse(s);
	} catch (ObjectParser.Exception e) {
	    System.out.println ("exception at offset " + e.getOffset());
	    throw e;
	} catch (java.lang.Exception e) {
	    throw e;
	}

	String[] resultNames = {
	    "result1",
	    "result2",
	    "result3",
	    "result4",
	    "result5",
	    "result6",
	    "result7",
	    "result8",
	    "result8a",
	    "result8b",
	    "result9",
	    "result10",
	    "result11",
	    "result13",
	    "result14",
	    "result15",
	    "result15a",
	    "result15b",
	    "result15c",
	    "result16",
	    "result16a",
	    "result16b",
	    "result16c",
	    "result16d",
	    "result17",
	    "result17a",
	    "result17b",
	    "result17c",
	    "result17d",
	    "result17e",
	    "result17f",
	    "result17g",
	    "result18",
	    "result19",
	    "result20",
	    "result21",
	    "result22",
	    "result23",
	};

	for (int i = 0; i < resultNames.length; i++) {
	    System.out.format("%s = %s\n", resultNames[i],
			      parser.get(resultNames[i]));
	}

	System.out.println("---------------------");
	parser = parser2;
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
	parser.setBindings(bindings);
	System.out.println("sum (with bindings set) = " + parser.get("sum"));
	System.out.println(parser.parse("add(1)"));
	System.out.println("sum (with bindings set) = " + parser.get("sum"));
	parser.setBindings(origBindings);
	System.out.println("sum (original bindings) = " + parser.get("sum"));

	parser = new ExpressionParser();

	String estr = "var x = 10 * + 20";
	try {
	    parser.parse(estr);
	} catch (ObjectParser.Exception ep) {
	    System.out.println(ep.getMessage() + " was expected: " + estr);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	parser = new ExpressionParser(null, null, null, null, null);
	parser = new ExpressionParser(new Class<?>[0], new Class<?>[0],
				      new Class<?>[0], new Class<?>[0],
				      new Class<?>[0]);
	Class<?> carray[] = {Math.class};
	if (System.getSecurityManager() == null) System.exit(0);
	try {
	    parser = new ExpressionParser(carray, null, null, null, null);
	    System.err.println("security exception should have been thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	try {
	    parser = new ExpressionParser(null, carray, null, null, null);
	    System.err.println("security exception should have been thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	try {
	    parser = new ExpressionParser(null, null, carray, null, null);
	    System.err.println("security exception should have been thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	try {
	    parser = new ExpressionParser(null, null, null, carray, null);
	    System.err.println("security exception should have been thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	try {
	    parser = new ExpressionParser(null, null, null, null, carray);
	    System.err.println("security exception should have been thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	System.exit(0);
    }
}
