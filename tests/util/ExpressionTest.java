import org.bzdev.anim2d.*;
import org.bzdev.lang.MathOps;
import org.bzdev.util.*;
import org.bzdev.graphs.Graph;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;

import static java.lang.Math.hypot;
import static java.lang.Math.cos;
import static java.lang.Math.acos;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import java.security.*;
import java.util.ArrayList;
import org.bzdev.lang.UnexpectedExceptionError;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ExpressionTest {

    public static interface Foo1 {
	int doit();
    }

    static class Foo2 implements Foo1 {
	public int doit() {return 1;}
    }

    static class Foo3 extends Foo2 {
    }

    // Use to test null arguments for methods and constructors.
    public  static class NullTest {
	int value;
	public NullTest(Number n) {value = 10;}
	public NullTest(String s) {value = 20;}
	public int getValue() {return value;}

	public void set(Number n) {value = 10;}
	public void set (String s) {value = 20;}

	public static Number getNullNumber() {return null;}
	public static String getNullString() {return null;}
    }


    private static int classDist(Class<?> start, Class<?> end) {
	if (start.equals(end)) return 0;
	if (!end.isAssignableFrom(start)) return Integer.MAX_VALUE;
	int dist = classDist(start.getSuperclass(), end);
	if (dist != Integer.MAX_VALUE) {
	    return (dist + 1);
	}
	Class<?>[] interfaces = start.getInterfaces();
	for (Class<?> c: interfaces) {
	    int d = classDist(c, end);
	    if (d < dist) {
		dist = d;
	    }
	}
	return (dist == Integer.MAX_VALUE)? dist: (dist + 1);
    }

    private static  Class<?> findBestClass( Class<?> target,
						Class<?>[]carray)
    {
	/*
	  if (classCache.containsKey(target)) {
	  return classCache.get(target);
	  }
	*/
	Class<?> result = null;
	try {
	    ArrayList<Class<?>> calist = new ArrayList<>(carray.length);
	    ArrayList<Class<?>> ialist = new ArrayList<>(carray.length);
	    for (Class<?> c: carray) {
		if (c.equals(target)) {
		    result = c;
		    return result;
		}
		if (c.isAssignableFrom(target)) {
		    if (c.isInterface()) {
			ialist.add(c);
		    } else {
			calist.add(c);
		    }
		}
	    }
	    int sz = calist.size() + ialist.size();
	    carray = new Class<?>[sz];
	    int i = 0;
	    for (Class<?> c: calist) {
		carray[i++] = c;
	    }
	    for (Class<?> c: ialist) {
		carray[i++] = c;
	    }
	    int dist = Integer.MAX_VALUE;
	    for (Class<?> c: carray) {
		int d = classDist(target, c);
		if (d < dist) {
		    dist = d;
		    result = c;
		}
	    }
	    return result;
	} finally {
	    // classCache.put(target, result);
	}
    }

    static String getLineTail(String s, int i) {
	// Get the tail up to an EOL.

	int len = s.length();
	if (i >= len) return "";
        char ch = s.charAt(i);
	if (ch == '\r' || ch == '\n') return "";
	int end = i + 1;
	while (end < len) {
	    ch = s.charAt(end);
	    if (ch == '\r' || ch == '\n') break;
	    end++;
	}
	return s.substring(i, end);
    }


    // test methods from ExpressionParser that were copied to
    // this file for stand-alone testing
    private static void testMethods() throws Exception {

	String ts = "sldkf lsjdf  sldf \n sldkjf";
	for  (int i = 0; i < ts.length(); i++) {
	    System.out.println("ts tail at " + i + " = "
			       + getLineTail(ts, i));
	}
	if (classDist(Double.class, Number.class) != 1)
	    throw new Exception();
	if (classDist(Double.class, Object.class) != 2)
	    throw new Exception();

	if (classDist(Double.class, Comparable.class) != 1)
	    throw new Exception();

	if (classDist(Double.class, java.io.Serializable.class) != 2)
	    throw new Exception();

	if (classDist(Double.class, Integer.class) != Integer.MAX_VALUE)
	    throw new Exception();

	Class<?> carray[] = {
	    Object.class, Comparable.class, Number.class
	};

	Class<?> clasz = findBestClass(Double.class, carray);

	if (!clasz.equals(Number.class)) {
	    System.out.println("class is " + clasz);
	    throw new Exception();
	}

	Foo3 foo3 = new Foo3();

	Method m1 = Foo1.class.getMethod("doit");
	Method m2 = Foo2.class.getMethod("doit");
	Method m3 = Foo3.class.getMethod("doit");
	int i1 = (Integer)m2.invoke(foo3);
	int i2 = (Integer)m1.invoke(foo3);
	int i3 = (Integer)m3.invoke(foo3);
	if (i1 != i2) {
	    throw new Exception();
	}
	if (i1 != i3) {
	    throw new Exception();
	}
	System.out.println(m1);
	System.out.println(m2);
	System.out.println(m3);
    }

    public static class Pair {
	char type;
	String s;
	double v;
	boolean vb;
	Class<?> vc;
	int vi;
	String vs;
	public Pair(String s, double v) {
	    type = 'd';
	    this.s = s;
	    this.v = v;
	}
	public Pair(String s, boolean b) {
	    type = 'b';
	    this.s = s;
	    this.vb = b;
	}
	public Pair(String s, int iv) {
	    type = 'i';
	    this.s = s;
	    this.vi = iv;
	}
	public Pair(String s, String sv) {
	    type = 's';
	    this.s = s;
	    this.vs = sv;
	}
	public Pair(String s, Class<?> cv) {
	    type = 'c';
	    this.s = s;
	    this.vc = cv;
	}
    }

    public static class Ops {

	public static double slen(String s) {
	    return (double)s.length();
	}

	public static double slen(String s, double x) {
	    return (double)s.length() + x;
	}

	static double total = 0.0;
	public static void add (Number n) {
	    total += n.doubleValue();
	}
    }

    public static class Ops2 {
	public static double slen(String s, double x) {
	    return (double)s.length() + x + 0.05;
	}
	public static String cat(String s1, String s2) {
	    return s1 + s2;
	}
	public static boolean and(boolean b1, boolean b2) {
	    return b1 && b2;
	}

	public static boolean or(boolean b1, boolean b2) {
	    return b1 || b2;
	}
    }

    public static enum Flag {
	TRUE,
	FALSE
    }

    public static class Ops3 {
	int value;

	public static final int FOO = 25;

	public int getValue() {return value;}

	public static int getOne() {return 1;}
	public static int getTwo() {return 2;}

	public Ops3(int value) {this.value = value;}

	public Ops3(int v1, int... values) {
	    if (values.length == 0) value = v1;
	    else value = values[values.length - 1];
	    System.out.println("v1 = " + v1);
	    System.out.println("value = " + value);
	}

	public Ops3 add(Ops3 arg) {
	    return new Ops3(this.value + arg.value);
	}
	public String toString() {
	    return "" + value;
	}

	public boolean test(Ops3 arg) {
	    return this.value < arg.value;
	}

	public Ops3 invert(boolean mode) {
	    return new Ops3(mode? -value: value);
	}

	public Ops3 invert(Flag mode) {
	    return new Ops3(mode == Flag.TRUE? -value: value);
	}
    }

    public static class BinaryOpTester {

	public BinaryOpTester(){}

	public double call(DoubleBinaryOperator op, double x, double y) {
	    return op.applyAsDouble(x, y);
	}
    }

    public static interface Interface {
	public Number op1(Number n1, Number n2);
	public Number op2(Number n1, Number n2);

    }

    public static class IFTest {
	public IFTest() {}
	public void testit(Interface ops) {
	    System.out.println(ops.op1(10, 20));
	    System.out.println(ops.op2(10, 20));
	}
    }

    static final char[] linesep = System.getProperty("line.separator")
	.toCharArray();
    static final char SEP_END = (linesep.length == 0)? '\n':
	linesep[linesep.length-1];

    static final String COMMENT_RE =
	"(//.*" + SEP_END + "|/[*][^*]*[*]/)";


    public static void main(String argv[]) throws Exception {

	testMethods();

	String tstr1 = "lang.Math./*cos*/cos";
	String tstr2 = tstr1.replaceAll(COMMENT_RE, "");
	if (!tstr2.equals("lang.Math.cos")) {
	    throw new Exception("COMMENT_RE failed");
	}
	String tstr1a = "lang.Math.\\cos" + SEP_END + "cos";
	String tstr2a = tstr1.replaceAll(COMMENT_RE, "");
	if (!tstr2a.equals("lang.Math.cos")) {
	    throw new Exception("COMMENT_RE failed");
	}


	ClassArraySorter sorter = new ClassArraySorter();
	sorter.addKey(new ClassArraySorter.Key(new Class<?>[0]));
	java.util.LinkedList<ClassArraySorter.Key> list = sorter.createList();
	System.out.println(list.size());

	String s;
	Object value;

	ExpressionParser parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.addClasses(java.awt.Color.class);

	try {
	    s = "blue.darker();";
	    value = parser.parse(s);
	    System.out.println("value = " + value);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    s = "Color . blue./*hi*/darker();";
	    value = parser.parse(s);
	    System.out.println("value = " + value);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    s = "Color . blue./*hi*/darker().darker();";
	    value = parser.parse(s);
	    System.out.println("value = " + value);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.addClasses(Math.class);

	try {
	    // s = "function f2d(x,y) {Math.cos(x)*Math.cos(y)}";
	    s = "function f2d(x,y) {-Math.sin(x)*Math.sin(y)}";
	    value = parser.parse(s);
	    s = "f2d(0.7, 0.7)";
	    value = parser.parse(s);
	    System.out.println("value = " + value);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.addClasses(NullTest.class);
	parser.setPrefixMode();
	try {
	    s = "var nt1 = new NullTest(Number::null)";
	    System.out.println(s);
	    parser.parse(s);
	    s = "var nt2 = new NullTest(String::null)";
	    System.out.println(s);
	    parser.parse(s);
	    s = "= nt1.getValue()";
	    System.out.println("nt1.getValue() = " + parser.parse(s));
	    s = "= nt2.getValue()";
	    System.out.println("nt2.getValue() = " + parser.parse(s));
	    s = "= nt1.set(String::null)";
	    parser.parse(s);
	    s = "= nt2.set(Number::null)";
	    parser.parse(s);
	    s = "= nt1.getValue()";
	    System.out.println("nt1.getValue() = " + parser.parse(s));
	    s = "= nt2.getValue()";
	    System.out.println("nt2.getValue() = " + parser.parse(s));
	    s = "var null1 = Number::null";
	    parser.parse(s);
	    s = "var null2 = String::null";
	    parser.parse(s);
	    s = "= nt1.set(null1)";
	    parser.parse(s);
	    s = "= nt2.set(null2)";
	    parser.parse(s);
	    s = "= nt1.getValue()";
	    System.out.println("nt1.getValue() = " + parser.parse(s));
	    s = "= nt2.getValue()";
	    System.out.println("nt2.getValue() = " + parser.parse(s));
	    s = "= nt1.set(NullTest.getNullString())";
	    parser.parse(s);
	    s = "= nt2.set(NullTest.getNullNumber())";
	    parser.parse(s);
	    s = "= nt1.getValue()";
	    System.out.println("nt1.getValue() = " + parser.parse(s));
	    s = "= nt2.getValue()";
	    System.out.println("nt2.getValue() = " + parser.parse(s));
	    s = "= Number::null == String::null";
	    if (parser.parse(s) != Boolean.TRUE) {
		throw new Exception("Number::null != String::null");
	    }
	    s = "= String::null instanceof String";
	    if (parser.parse(s) != Boolean.TRUE) {
		throw new Exception("String::null not instance of String?");
	    }
	    s = "= Number::null instanceof String";
	    if (parser.parse(s) != Boolean.FALSE) {
		throw new Exception("Number::null instance of String?");
	    }
	    s = "= null instanceof String";
	    if (parser.parse(s) != Boolean.FALSE) {
		throw new Exception("null instance of String?");
	    }
	    s = "= null1 == null2";
	    if (parser.parse(s) != Boolean.TRUE) {
		throw new Exception("null1 == null2 failed");
	    }
	    s = "= null1 != null2";
	    if (parser.parse(s) != Boolean.FALSE) {
		throw new Exception("null1 != null2 failed");
	    }
	    Object null1 = parser.get("null1");
	    Object null2 = parser.get("null2");
	    if (null1 != null || null2 != null) {
		throw new Exception("typed nulls not null");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setPrefixMode();
	parser.set("gout", System.out);
	parser.set("osg", "osg");
	s = "= (gout == null)? void: function() {"
	    + "osg = OutputStreamGraphics.newInstance(gout, 800, 600, \"png\");"
	    + "void}()";
	try {
	    parser.parse(s);
	    System.err.println("NO EXCEPTION");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println(e.getInput());
	    System.out.println("offset = " + e.getOffset());
	    System.out.println(e.getInput().substring(0, e.getOffset()) +"^");
	    System.out.println("... EXCEPTION EXPECTED");
	}

	parser = new ExpressionParser();
	parser.addClasses(MathOps.class);
	s = "var log2a = MathOps::log2";
	value = parser.parse(s);
	s = "= log2a.invoke(64, 1.0)";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (value instanceof Double) {
	    if (((double)(Double)value) != 6.0) {
		throw new Exception();
	    }
	}

	parser = new ExpressionParser(Math.class);
	parser.addClasses(Math.class);

	/*
	parser.testit("-(10.0 + 3) + 20.0 * (3*4 + 5) * 30 "
		      + "- f(10*29-5,30+40) - g(x+10, y)"
		      + "- 25.0*3 + 10");
	*/

	System.out.println(parser.parse("= 1+2 < 1 + 5"));



	s = "var ctst = null";
	parser.parse(s);
	s = "var ctst ??= 10";
	parser.parse(s);
	s = "= ctst";
	value = parser.parse(s);
	if (((Integer)value).intValue() != 10) {
	    throw new Exception();
	}

	s = "var ctst1 = 5";
	parser.parse(s);
	s = "var ctst1 ??= 10";
	parser.parse(s);
	s = "= ctst1";
	value = parser.parse(s);
	if (((Integer)value).intValue() != 5) {
	    throw new Exception();
	}

	s = "var ctst2 ??= 10";
	parser.parse(s);
	s = "= ctst2";
	value = parser.parse(s);
	if (((Integer)value).intValue() != 10) {
	    throw new Exception();
	}

	s = "= {\"prop\": 10}";
	parser.parse(s);


	System.out.println(parser.parse("= 30.0 * lang.Math./*cos*/cos(1.0)"));

        parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.addClasses(Math.class, MathOps.class);

	System.out.println("------------------------");
	ExpressionParser.ESPFunction qf = (ExpressionParser.ESPFunction)
	    parser.parse("function (t) {(t < 90)? 1.1: (t < 180)? 1.5: 2.2}");
	System.out.println(qf);
	System.out.println("------------------------");
	value = qf.invoke(10);
	System.out.println("value = " + value);
	System.out.println("------------------------");


	parser.set("vt1", 100);
	value = parser.parse ("var vt1 ?= 200");
	System.out.println("value = " + value);
	if ((Integer)value != 100) {
	    throw new Exception();
	}

	value = parser.parse ("var vt2 ?= 200");
	System.out.println("value = " + value);
	if ((Integer)value != 200) {
	    throw new Exception();
	}


	s = "function (namer, package, spec) {"
	    + " var f = function(entry) {"
	    + "namer.println(entry.getKey() + \": \" "
	    + " + package + \".\" + entry.getValue());"
	    + "void};"
	    + "spec.properties().stream().forEach(f);"
	    + "void }";
	parser.parse(s);



	parser.parse("var count = 10");
	try {
	    s = "function badf(x) {3count}";
	    System.out.println("trying s " + s);
	    value = parser.parse(s);
	    s = "badf(10)";
	    System.out.println("trying s " + s);
	    value = parser.parse(s);
	    System.out.println("expected an error, not value = " + value);
	    System.exit(1);
	} catch (Exception e) {
	    System.out.println("expected exception: " + e.getMessage());
	}

	parser.parse("function ctest() {count = asInt(count - 1);"
		     +"(count == 0)? true: ctest()}");
	value = parser.parse("ctest()");
	System.out.println(value);
	System.out.println(parser.get("count"));


	parser = new ExpressionParser(Math.class, MathOps.class);
	parser.addClasses(Math.class, Ops.class);
	parser.addClasses(BasicStats.class, BasicStats.Population.class);

	parser.set("vt1", 100);
	value = parser.parse ("var vt1 ?= 200");
	System.out.println("value = " + value);
	if ((Integer)value != 100) {
	    throw new Exception();
	}

	value = parser.parse ("var vt2 ?= 200");
	System.out.println("value = " + value);
	if ((Integer)value != 200) {
	    throw new Exception();
	}

	s = "= 30L";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (!(value instanceof Long)) {
	    throw new Exception();
	}
	if ((Long)value != 30L) {
	    throw new Exception();
	}


	s = "= (10 < 20)? 10: 20";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 10) {
	    throw new Exception();
	}

	s = "function qtest(t) {(t < 10)? 10.0: 20.0}";
	value = parser.parse(s);
	ExpressionParser.ESPFunction qtestf =
	    (ExpressionParser.ESPFunction) (parser.get("qtest"));
	value = qtestf.invoke(5.0);
	if ((Double)value != 10.0) {
	    throw new Exception();
	}

	s = "= qtest(5.0)";
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 10.0) {
	    throw new Exception();
	}

	s = "= false? true: false";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}


	s = "= throw \"hello\"";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "= false || throw \"hello\"";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "= true && throw \"hello\"";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "= true? throw \"hello\": false";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "= false? true: throw \"hello\"";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "= function(){ throw \"hello \" + \"there\"}()";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "var throwTest ?= throw \"throwTest not defined\"";
	try {
	    parser.parse(s);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	}

	s = "var vt1 ?= throw \"vt1 not defined\"";
	try {
	    parser.parse(s);
	} catch (ObjectParser.Exception e) {
	    System.out.println("at " +e.getOffset() + ": " + e.getMessage());
	    System.exit(1);
	}

	s = "= {x: 10,"
	    + "len10(s) {var ss = s; this.get(\"x\") + ss.length()}"
	    + "}";
	parser.parse(s);


	s = "var firstobj = {a: 10, testf() {this[\"a\"]}}";
	System.out.println("trying " + s);
	parser.parse(s);
	s = "= firstobj.testf()";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);

	s = "var secondobj = {a: 10, testf() {this.a}}";
	System.out.println("trying " + s);
	parser.parse(s);
	s = "= secondobj.testf()";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);

	s = "var firstlist = [10, -20, 30]";
	System.out.println("trying " + s);
	value = parser.parse(s);
	s = "= firstlist.stream().map(Math::abs)"
	    + ".reduce(0, function(x,y) {x + y})";
	System.out.println("trying s" + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (value instanceof Number) {
	    double val = (Double) value;
	    if (val != 60.0) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "var secondlist = [\"One\", \"Two\", \"Three\", \"Four\"]";
	System.out.println("trying " + s);
	value = parser.parse(s);
	s = "= secondlist.stream().reduce(\"\", String::concat)";
	System.out.println("trying s" + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (value instanceof String) {
	    String val = (String) value;
	    if (!val.equals("OneTwoThreeFour")) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "var sconcat = String::concat";
	value = parser.parse(s);
	s = "= secondlist.stream().reduce(\"\", sconcat)";
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (value instanceof String) {
	    String val = (String) value;
	    if (!val.equals("OneTwoThreeFour")) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}
	s = "= sconcat.invoke(\"Hello\",\"There\")";
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (value instanceof String) {
	    String val = (String) value;
	    if (!val.equals("HelloThere")) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "var newstr = String::new";
	parser.parse(s);
	s = "= newstr.invoke(\"hello\")";
	value = parser.parse(s);
	if (!value.equals("hello")) {
	    throw new Exception();
	}

	s = "var lstat = new BasicStats.Population()";
	value = parser.parse(s);
	s = "var lstatAdd = lstat::add";
	value = parser.parse(s);
	s = "= lstatAdd.invoke(2.0)";
	value = parser.parse(s);
	s = "= lstat.size()";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (value instanceof Integer) {
	    if (((int)(Integer)value) != 1) {
		throw new Exception();
	    }
	}
	s = "= MathOps.log2(64, 1.0)";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (value instanceof Double) {
	    if (((double)(Double)value) != 6.0) {
		throw new Exception();
	    }
	}



	s = "= 10 + \"abcde\".length() + function(x, y) {x*x + y*y}(100, 200)";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((double)(Double)value != 50015.0) {
	    throw new Exception();
	}

	s = "var temp1 = ({a: 10, b: 30}[\"a\"] + "
	    + "[10, 20, 30, 40][0]) / tan(20);";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);

	s = "= temp1 * temp1 / 20.0 + 30.0 * 40.0 + \"abcde\".length()"
	    + " + function(x, y) { x*x + y*y}(100, 200)";

	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);



	s = "= Math.class";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (!((Class<?>)value).equals(Math.class)) {
	    new Exception();
	}

	s = "= lang.Math.class";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (!((Class<?>)value).equals(Math.class)) {
	    new Exception();
	}

	s = "= java.lang.Math.class";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if (!((Class<?>)value).equals(Math.class)) {
	    new Exception();
	}


	int[] atst = {1, 2, 3};
	parser.set("atst", atst);
	s = "= atst[1]";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 2) {
	    throw new Exception();
	}

	s = "= atst.size()";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 3) {
	    throw new Exception();
	}

	s = "= atst.stream().reduce(0, function(x, y){(x+y)})";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 6) {
	    throw new Exception();
	}

	Ops.total = 0.0;
	s = "= atst.forEach(function(x) {Ops.add(x)})";
	parser.parse(s);
	if (Ops.total != 6.0) {
	    throw new Exception();
	}

	Ops.total = 0.0;
	s = "= atst.forEach(Ops::add)";
	parser.parse(s);
	if (Ops.total != 6.0) {
	    throw new Exception();
	}


	s = "= atst.get(1)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 2) {
	    throw new Exception();
	}

	s = "= atst.set(1, 4)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 4) {
	    throw new Exception();
	}

	s = "= atst.parallelStream().reduce(0, function(x, y){(x+y)})";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 8) {
	    throw new Exception();
	}

	s = "= [1, 2, 3][1]";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 2) {
	    throw new Exception();
	}

	s = "= {x: 10, y: 20}[\"x\"]";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Integer)value != 10) {
	    throw new Exception();
	}


	s = "= false && true || false";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}

	s = "= false && true || true";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= false && true && true";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}

	s = "= true || false || false";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= true && true || false || false";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	// System.exit(1);		// TO STOP SHORT FOR TESTING

	s = "= var.foo";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}

	s = "var foo = 10.0";
	value = parser.parse(s);
	s = "= var.foo";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= var.foo? foo > 5: false";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= var.foo? foo > 20: false";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}


	s = "= \"hello\" instanceof String";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= \"hello\" instanceof Double";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}

	s = "= 10 instanceof Number";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= 10 instanceof Integer";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != true) {
	    throw new Exception();
	}

	s = "= 10 instanceof Double";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Boolean)value != false) {
	    throw new Exception();
	}

	s = "var sobj = {x: 10,"
	    + "len10(s) {var ss = s; this.get(\"x\") + ss.length()}"
	    + "}";
	System.out.println("trying " + s);
	parser.parse(s);
	s = "= sobj.len10(\"hello\")";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);

	s = "var obj = {x: 10.0, y: 20.0,"
	    + "distTo(x1, y1) {"
	    + "var dx = x1 - this.get(\"x\");"
	    + "var dy = y1 - this.get(\"y\");"
	    + "(dx*dx + dy*dy) } }";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println(parser.get("obj").getClass());
	s = "= obj.distTo(20.0, 30.0)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);

	s = "function plus(a, b) {a + b}";
	value = parser.parse(s);
	s = "function plus3(a, b, c) {var x = plus(a, b); x + c}";
	value = parser.parse(s);
	s = "= plus3(10, 20, 30)";
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 60.0) {
	    throw new Exception();
	}

	s = "= function(a, b) {var c = a+b; var d = b; c*d }(10, 20)";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 600.0) {
	    throw new Exception();
	}

	s = "= function(a, b) {var c = a+b; c*b}(10, 20)";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 600.0) {
	    throw new Exception();
	}

	s = "function f0(a, b){var c = a+b; c*b}";
	System.out.println("trying " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	s = "= f0(10,20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println("value = " + value);
	if ((Double)value != 600.0) {
	    throw new Exception();
	}

	s = "= String.format(\"hello\")";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println(value);
	if (!(value instanceof String)) {
	    throw new Exception();
	}

	s = "= String.format(\"%d = %d\", 10, 10)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	System.out.println(value);
	if (!(value instanceof String)) {
	    throw new Exception();
	}

	s = "var list = [10, 20, 30]";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (!(value instanceof ExpressionParser.ESPArray)) {
	    throw new Exception();
	}

	s = "var list2 = []";
	parser.parse(s);
	s = "= list.forEach(function(x) {list2.add(x)})";
	parser.parse(s);
	for (int i = 0; i < 3; i++) {
	    if (!((JSArray)parser.get("list")).get(i)
		.equals(((JSArray)parser.get("list2")).get(i))) {
		throw new Exception();
	    }
	}

	Ops.total = 0.0;
	s = "= list.forEach(Ops::add)";
	parser.parse(s);
	if (Ops.total != 60.0) {
	    throw new Exception();
	}


	s = "= list.stream().reduce(0, function(x,y) {x + y})";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    double val = (Double) value;
	    System.out.println("value = " + val);
	    if (val != 60.0) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "= list.get(0)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    int val = (Integer) value;
	    System.out.println("value = " + val);
	    if (val != 10) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "= list.get(1)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    int val = (Integer) value;
	    System.out.println("value = " + val);
	    if (val != 20) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "= list.get(2)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    int val = (Integer) value;
	    System.out.println("value = " + val);
	    if (val != 30) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "= list.size()";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    int val = (Integer) value;
	    System.out.println("value = " + val);
	    if (val != 3) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "var mcallObject0 = {offset: 5, "
	    + "mcall(x,y) {this.get(\"offset\") + this.fcall(x, y)},"
	    + " fcall: function(x, y) {x + y},"
	    + " mcall2(x,y) {x+y+this.mcall(x,y) + this.fcall(x, y)}}";
	System.out.println("trying " + s);
	value = parser.parse(s);
	if (!(value instanceof ExpressionParser.ESPObject)) {
	    throw new Exception();
	}

	s = "= mcallObject0.mcall(10, 20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    double val = (Double) value;
	    System.out.println("value = " + val);
	    if (val != 35.0) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "= mcallObject0.fcall(10, 20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    double val = (Double)value;
	    System.out.println("value = " + val);
	    if (val != 30.0) throw new Exception();
	} else {
	    throw new Exception();
	}

	s = "= mcallObject0.mcall2(10, 20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    double val = (Double)value;
	    System.out.println("value = " + val);
	    if (val != 95.0) throw new Exception();
	} else {
	    throw new Exception();
	}


	s = "var mcallObject = {mcall: function(x,y){x+y}}";
	System.out.println("trying " + s);
	value = parser.parse(s);
	if (!(value instanceof ExpressionParser.ESPObject)) {
	    throw new Exception();
	}
	s = "= mcallObject.mcall(10, 20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if (value instanceof Number) {
	    System.out.println("value = " + value);
	} else {
	    throw new Exception();
	}

	s = "= {}";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    if (obj.size() > 0) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "= []";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPArray)) {
	    ExpressionParser.ESPArray obj = (ExpressionParser.ESPArray) value;
	    if (obj.size() > 0) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}


	s = "= [10, 20]";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPArray)) {
	    ExpressionParser.ESPArray obj = (ExpressionParser.ESPArray) value;
	    System.out.println("obj[0] = " + obj.get(0));
	    System.out.println("obj[1] = " + obj.get(1));
	} else {
	    throw new Exception();
	}


	s = "= {a: 10, b: 20}";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	} else {
	    throw new Exception();
	}

	s = "= {a: 10, b: {a: 20, b: 30}}";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    value = obj.get("b");
	    if (value instanceof ExpressionParser.ESPObject) {
		obj = (ExpressionParser.ESPObject) value;
		System.out.println("obj.b.a = " + obj.get("a"));
		System.out.println("obj.b.b = " + obj.get("b"));
	    } else {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "= function(a,b,c) {{a: a, b: {a: b, b: c}}}(10, 20, 30)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    value = obj.get("b");
	    if (value instanceof ExpressionParser.ESPObject) {
		obj = (ExpressionParser.ESPObject) value;
		System.out.println("obj.b.a = " + obj.get("a"));
		System.out.println("obj.b.b = " + obj.get("b"));
	    } else {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "= true? {a: 10, b: 20}: {a: 30, b: 40}";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	    int n1 = (Integer)obj.get("a");
	    int n2 = (Integer)obj.get("b");
	    if (n1 != 10 && n2 != 20) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "= function(test,a,b) {test?{a:a,b:b}:{a:a+1,b:b+1}}(true,10,20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	    int n1 = (Integer)obj.get("a");
	    int n2 = (Integer)obj.get("b");
	    if (n1 != 10 && n2 != 20) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "= function(test,a,b) {test?{a:a,b:b}:{a:a+1,b:b+1}}(false,10,20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	    double n1 = (Double)obj.get("a");
	    double n2 = (Double)obj.get("b");
	    if (n1 != 11.0 && n2 != 21.0) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "function objf (test,a,b) {test?{a:a,b:b}:{a:a+1,b:b+1}}";
	value = parser.parse(s);
	s = "= objf(true, 10, 20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);

	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	    int n1 = (Integer)obj.get("a");
	    int n2 = (Integer)obj.get("b");
	    if (n1 != 10 && n2 != 20) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}
	value = parser.parse(s);

	s = "= objf(false, 10, 20)";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	    double n1 = (Double)obj.get("a");
	    double n2 = (Double)obj.get("b");
	    if (n1 != 11.0 && n2 != 21.0) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}




	s = "= false? {a: 10, b: 20}: {a: 30, b: 40}";
	System.out.println("trying s " + s);
	value = parser.parse(s);
	if ((value instanceof ExpressionParser.ESPObject)) {
	    ExpressionParser.ESPObject obj = (ExpressionParser.ESPObject) value;
	    System.out.println("obj.a = " + obj.get("a"));
	    System.out.println("obj.b = " + obj.get("b"));
	    int n1 = (Integer)obj.get("a");
	    int n2 = (Integer)obj.get("b");
	    if (n1 != 30 && n2 != 40) {
		throw new Exception();
	    }
	} else {
	    throw new Exception();
	}

	s = "= function(a, b) {a + b}";
	value = parser.parse(s);
	if (value instanceof ExpressionParser.ESPFunction) {
	    ExpressionParser.ESPFunction f =
		 (ExpressionParser.ESPFunction) value;
	    value = f.invoke(1, 2);
	    if ((Double)value != 3.0) {
		System.out.println("value = " + value);
		throw new Exception ("f(1, 2) != 3.0");
	    }
	} else {
	    System.out.println("function = " + value);
	    throw new Exception ("function expected");
	}

	s = "= 1.e-14";
	System.out.println("s " + s);
	value = parser.parse(s);
	if ((Double)value != 1.e-14) {
	    System.out.println("value = " + value);
	    throw new Exception("not 1.e-14");
	}


	s = "function f1(a,b) {a + b}";
	System.out.println("s = " + s);
	value = parser.parse(s);
	if (value != null) {
	    throw new Exception("not null");
	}
	s = "function f2(x,y) {x + y}";
	System.out.println("s = " + s);
	value = parser.parse(s);
	if (value != null) {
	    throw new Exception("not null");
	}

	value = parser.get("f1");
	if (value instanceof ExpressionParser.ESPFunction) {
	    ExpressionParser.ESPFunction function =
		(ExpressionParser.ESPFunction) value;

	    value = function.invoke(1.0, 2.0);
	    System.out.println("function.invoke(1.0, 2.0) = " + value);
	    if (value == null || Math.abs((Double)value - 3.0) > 1.e-14) {
		throw new Exception("not 3");
	    }
	}

	s = "= f1(1.0, 2.0)";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (value != null && Math.abs((Double)value - 3.0) > 1.e-14) {
	    throw new Exception("not 3");
	}

	s = "= f2(1.0, 2.0)";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (value != null && Math.abs((Double)value - 3.0) > 1.e-14) {
	    throw new Exception("not 3");
	}

	s = "= Math.sqrt(Math.sqrt(4.0))";
	value = parser.parse(s);
	System.out.println(value + " " + s);

	System.out.println("=========");

	s = "= f1(1.0, f2(2.0,3.0))";
	System.out.println("s " + s);
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (value != null && Math.abs((Double)value - 6.0) > 1.e-14) {
	    throw new Exception("not 3");
	}

	s = "= Math.max(true? 10: false? 20: 30,true? 40: false? 50: 60)";
	System.out.println("s " + s);
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (((Number)value).intValue() != 40) {
	    throw new Exception("not 1");
	}

	s = " = true ? (true ? 1: 2): (true? 3: 4)";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (((Number)value).intValue() != 1) {
	    throw new Exception("not 1");
	}

	s = "= true || false ? 1 + 0: 1 + 1";
	value = parser.parse(s);
	System.out.println(value + " " + s);
	if (((Number)value).intValue() != 1) {
	    throw new Exception("not 1");
	}


	s = "= true? 1: 2";
	value = parser.parse(s);
	System.out.println(value + " "  + s);
	if (((Integer)value).intValue() != 1) {
	    throw new Exception("not 1");
	}
	s = "= false? 1: 2";
	value = parser.parse(s);
	System.out.println(value + " "  + s);
	if (((Integer)value).intValue() != 2) {
	    throw new Exception("not 2");
	}


	s = "= 1 < 2 == 3 < 4";
	value = parser.parse(s);
	System.out.println(value + " "  + s);
	if (((Boolean)value).booleanValue() != true) {
	    throw new Exception ("not true");
	}

	s  = "= 2*(3 + 2*3)*3";
	 value = parser.parse(s);
	System.out.println(value + " "  + s);
	if (((Number)value).doubleValue() != 54) {
	    throw new Exception("not 54");
	}


	s = "= 10.35";
	value = parser.parse(s);
	System.out.println(parser.parse(s) + " "  + s);

	s = "var x = 30";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= x";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= x + 4";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= 2*x + 3*2";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= 2*(3 + 2*3)*3";
	value = parser.parse(s);
	System.out.println(value + " "  + s + ", expecting 54");


	parser = new ExpressionParser(Math.class);

	// parser.testit("acos(0.5)");

	s = "= acos(0.5)";
	value = parser.parse(s);
	System.out.println(value + " "  + s + ", expecting 1.04...");

	s  = "= hypot(3.0, 4.0)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= hypot(3.0, 4.0)";
	value = parser.parse(s);
	System.out.println(value + " "  + s + " (again)");

	s  = "= hypot(3, 4)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= - hypot(3, 4)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= 2 - hypot(3, 4)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= hypot(1*2+1, 2*(1+1))";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= -3";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= hypot(1*2+1, -2*(1+1))";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= sin(1.0)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);
	
	s  = "= sin(-1.0)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= atan2(1,-1)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= atan2(1,1)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s  = "= atan2(0,-1)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	Class<?> argClasses[] = {
	    String.class
	};

	Class<?> functionClasses[] = {
	    Math.class,
	    Ops.class
	};

	parser = new ExpressionParser(null, argClasses, functionClasses,
				      null, null);


	s = "= slen(\"hello\")";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= slen(\"hello\", 0.5)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	parser = new ExpressionParser(null, null, new Class<?>[] {
			Math.class,
			Ops.class,
			Ops2.class
		    }, null, null);

	s = "= Ops.slen(\"hello\", 0.5)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= Ops2.slen(\"hello\", 0.5)";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= cat(\"hello\", \"1\")";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "var s1 = cat(\"hello\", \"1\")";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= cat(s1, \"2\")";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	try {
	    s = "= new String(\"hello there\")";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);
	} catch (Exception e) {
	    e.getCause().printStackTrace();
	    throw e;
	}

	s = "var s1a = new String(\"hello there\")";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	s = "= s1a";
	value = parser.parse(s);
	System.out.println(value + " "  + s);

	parser = new ExpressionParser(null, null, new Class<?>[] {
			Math.class,
			Ops.class,
			Ops2.class
		    }, new Class<?>[] {
			String.class
		    }, null);

	try {
	    s = "var s1 = new String(\"hello there\")";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= s1.indexOf(\"there\")";
	    System.out.println("Trying " + s  + " ...");
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);
	} catch (Exception e) {
	    e.getCause().printStackTrace();
	    throw e;
	}

	parser = new ExpressionParser(new Class<?>[] {
		       Ops3.class,
		       BinaryOpTester.class,
		       IFTest.class,
		       Interface.class,
		    }, new Class<?>[] {
		       IFTest.class,
		       Ops3.class,
		       DoubleBinaryOperator.class,
		       Interface.class,
		    }, new Class<?>[] {
		       IFTest.class,
		       Math.class,
		       Ops.class,
		       Ops2.class,
		       Ops3.class
		    }, new Class<?>[] {
		       String.class,
		       Ops3.class,
		       BinaryOpTester.class,
		       DoubleBinaryOperator.class,
		       Interface.class,
		       IFTest.class,
		    }, new Class<?>[] {
		       Ops3.class
		    });
	try {
	    s = "var xy = new Ops3(10, 20, 30)";
	    System.out.println(" trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "var x = new Ops3(10)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "var y = new Ops3(20)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "var z = x.add(y)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);
	    System.out.println("type of value: " + value.getClass());

	    s = "= z.toString()";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= x.add(y).toString()";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= x.test(y)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= y.test(x)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= true";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= false";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= x.invert(true)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= x.invert(false)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = " = Ops3.FOO";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = " = Flag.TRUE";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= x.invert(Flag.TRUE)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= x.invert(Flag.FALSE)";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 | 2";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= ~1";
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);


	    s = "= 1 & 3 & ~1";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 2 & 3 & ~1";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 2 & 3 & ~(1)";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= ~1 + 1 & 7";
	    System.out.println("trying " + s + " = 7");
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= ~(1) + 1 & 7";
	    System.out.println("trying " + s + " = 7");
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= ~2  & 7";
	    System.out.println("trying " + s + " = 5");
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 + 1 & 7 | 8";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 < 2";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 <= 2";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 <= 1";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 > 2";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 >= 2";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 >= 1";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 == 2";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 == 1";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 != 2";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "= 1 != 1";
	    System.out.println("trying " + s);
	    value = parser.parse(s);
	    System.out.println(value + " "  + s);

	    s = "var iftest = new IFTest()";
	    System.out.println("trying s = " + s);
	    value = parser.parse(s);

	    s = "= iftest.testit({op1: function(x,y){x+y}, "
		+ "op2: function(x,y){x*y}})";
	    System.out.println("trying s = " + s);
	    value = parser.parse(s);
	    System.out.println(value + " = "  + s);

	} catch (Exception e) {
	    if (e.getCause() != null) {
		e.getCause().printStackTrace();
	    }
	    throw e;
	}
	if (true) {
	    System.out.println("Constants:");
	    for (String descr: parser.getConstants()) {
		System.out.println("    " + descr);
	    }
	    System.out.println("Return types:");
	    for (Class<?> clasz: parser.getReturnClasses()) {
		System.out.println("    " + clasz.getName().replace('$','.'));
	    }
	    System.out.println("Argument types:");
	    for (Class<?> clasz: parser.getArgumentClasses()) {
		System.out.println("    " + clasz.getName().replace('$','.'));
	    }
	    System.out.println("Constructors:");
	    for (String descr: parser.getConstructors()) {
		System.out.println("    " + descr);
	    }
	    System.out.println("Functions:");
	    for (String descr: parser.getFunctions()) {
		System.out.println("    " + descr);
	    }
	    System.out.println("Methods:");
	    for (String descr: parser.getMethods()) {
		System.out.println("    " + descr);
	    }
	}

	System.out.println();
	// longer tests
	double x = 30.0;
	double y = 40.0;
	double z = 27.0;
	int ix = 1;
	int iy = 2;
	int iz = 4;
	int mask = 6;
	double lang = 30.0;

	parser.set("x", x);
	parser.set("y", y);
	parser.set("z", z);
	parser.set("ix", ix);
	parser.set("iy", iy);
	parser.set("iz", iz);
	parser.set("mask", mask);
	parser.set("lang", 30.0);
	parser.set("ops31", new Ops3(1));
	parser.set("ops32", new Ops3(2));
	parser.parse("function ourfact (n) {"
		     + "(abs(n) < 1.e-14)? 1: n * ourfact(n-1)}");

	parser.parse("var sum = function(a,b) { a + b }");
	parser.parse("var sum3 = function(a,b,c) { a + b + c}");

	parser.parse("var bopTester = new BinaryOpTester()");

	Pair[] pairs = {
	    new Pair("= String.class", String.class),
	    new Pair("= double.class", double.class),
	    new Pair("= long.class", long.class),
	    new Pair("= int.class", int.class),
	    new Pair("= boolean.class", boolean.class),
	    new Pair("= \"hello\"", "hello"),
	    new Pair("= \"he\\bllo\"", "he\bllo"),
	    new Pair("= \"he\\fllo\"", "he\fllo"),
	    new Pair("= \"he\\nllo\"", "he\nllo"),
	    new Pair("= \"he\\rllo\"", "he\rllo"),
	    new Pair("= \"he\\tllo\"", "he\tllo"),
	    new Pair("= \"he\\sllo\"", "hesllo"),
	    new Pair("= \"he\\u00FCllo\"", "he\u00FCllo"),
	    new Pair("= \"he\\u2EA5llo\"", "he\u2EA5llo"),
	    new Pair("= \"\\nhe\\f\\bllo\\r\\t\"", "\nhe\f\bllo\r\t"),
	    new Pair("= \"hel\\\\lo\"", "hel\\lo"),
	    new Pair("= \"hel/*l*/lo\"", "hel/*l*/lo"),

	    new Pair("= 3 % 5", 3),
	    new Pair("= 13 % 5", 3),
	    new Pair("= 13.0 % 5", 3),
	    new Pair("= (3 + 1000000000000) % 5", 3),
	    new Pair("= 3 % 1000000000000", 3),
	    new Pair("= (3 + 1000000000000) % 1000000000000", 3),
	    new Pair("= 13 % 5.0", 3),
	    new Pair("= 13.0 % 5.0", 3),
	    new Pair("= (3 + 1000000000000) % /* 10 */ 10.0", 3),
	    new Pair("= ~getOne() & getTwo()", 2),
	    new Pair("= ~ops31.getValue() & ops32.getValue()", 2),
	    new Pair("= getTwo() & ~getOne()", 2),
	    new Pair("= ops32.getValue() & ~ops31.getValue()", 2),
	    new Pair("= x*-y+x", (double)(x*-y+x)),
	    new Pair("= x*-hypot(-x,-hypot(x,z))",
		     (double)(x*-hypot(-x,-hypot(x,z)))),
	    new Pair("= x/-y", (double)(x/-y)),
	    new Pair("= x+-(z/y)", (double)(x+-(z/y))),
	    new Pair("= hypot(x,y)-hypot(x,z)",
		     (double)(hypot(x,y)-hypot(x,z))),
	    new Pair("= x*y/z", (double)(x*y/z)),
	    new Pair("= x/z*y+x", (double)(x/z*y+x)),
	    new Pair("= -x*\"hello\".length()", (double)(-x*"hello".length())),
	    new Pair("= ix|iy", (double)(ix|iy)),
	    new Pair("= mask & (ix|iy|iz)", (double)(mask & (ix|iy|iz))),
	    new Pair("= ix|iy|iz", (double)(ix|iy|iz)),
	    new Pair("= ~mask & (ix|iy|iz)", (double)(~mask & (ix|iy|iz))),
	    new Pair("= hypot(x*hypot(x,y), z)",
		     (double)(hypot(x*hypot(x,y), z))),
	    new Pair("= hypot(x,hypot(x,y))", (double)(hypot(x,hypot(x,y)))),
	    new Pair("= x*(x*y-z*(x+y))*z + x",
		     (double)(x*(x*y-z*(x+y))*z + x)),
	    new Pair("= -x*(-x*y - -z*(x+-y))*-z + x",
		     (double)(-x*(-x*y - -z*(x+-y))*-z + x)),
	    new Pair("= ((x+y))", (double)(((x+y)))),
	    new Pair("= x*((x+z))", (double)(x*((x+z)))),
	    new Pair("= x/((x+z))", (double)(x/((x+z)))),
	    new Pair("= x*(ix|iy)", (double)(x*(ix|iy))),
	    new Pair("= x*(mask & (ix|~iy))", (double)(x*(mask & (ix|~iy)))),
	    new Pair("= ~~ix", (double)(~~ix)),
	    new Pair("= --x", (double)(-(-x))),
	    new Pair("= Math . acos (0.5)", (double)(Math . acos (0.5))),
	    new Pair("= java . lang . Math. cos(1.0)",
		     (double)(java . lang . Math. cos(1.0))),
	    new Pair("= java . lang . Math. cos(1.0)",
		     (double)(java.lang . Math . cos (1.0))),
	    new Pair("= lang * lang.Math./*cos*/cos(1.0)",
		     (double)(lang * java.lang.Math.cos(1.0))),
	    new Pair("= x*y*z + x/y*z + z*y/x",
		     (double)(x*y*z + x/y*z + z*y/x)),
	    new Pair("= x*y*z - x/y*z - z*y/x",
		     (double)(x*y*z - x/y*z - z*y/x)),
	    new Pair("= x*y*z + x/z*z - z*z/y",
		     (double)(x*y*z + x/z*z - z*z/y)),
	    new Pair("= -x/z*y -x/z*-z + y/-z",
		     (double)(-x/z*y -x/z*-z + y/-z)),
	    new Pair("= x*cos(y)*z + cos(x)*y*z + x*y*cos(z)",
		     (double)(x*cos(y)*z + cos(x)*y*z + x*y*cos(z))),
	    new Pair("= x/cos(y)/z + cos(x)/y/z + x/y/cos(z)",
		     (double)(x/cos(y)/z + cos(x)/y/z + x/y/cos(z))),
	    new Pair("= x/cos(y*y)/z + cos(x*x)/y/z + x/y/cos(z*z)",
		     (double)(x/cos(y*y)/z + cos(x*x)/y/z + x/y/cos(z*z))),
	    new Pair("= x/cos(y/x)/z + cos(x/z)/y/z + x/y/cos(z*z)",
		     (double)(x/cos(y/x)/z + cos(x/z)/y/z + x/y/cos(z*z))),
	    new Pair("= -x/cos(y/x)/z - cos(x/z)/y/z - x/y/cos(z*z)",
		     (double)(-x/cos(y/x)/z - cos(x/z)/y/z - x/y/cos(z*z))),
	    new Pair("= x + -y", (double)(x + -y)),
	    new Pair("= x - -y", (double)(x - -y)),
	    new Pair("= 1 | 2", (int)(1 | 2)),
	    new Pair("= ~1", (int)(~1)),
	    new Pair("= 1 & 3 & ~1", (int)(1 & 3 & ~1)),
	    new Pair("= 2 & 3 & ~1", (int)(2 & 3 & ~1)),
	    new Pair("= 2 & 3 & ~(1)", (int)(2 & 3 & ~(1))),
	    new Pair("= ~1 + 1 & 7", (int)(~1 + 1 & 7)),
	    new Pair("= ~(1) + 1 & 7", (int)(~(1) + 1 & 7)),
	    new Pair("= ~2 & 7", (int)(~2 & 7)),
	    new Pair("= 1 + 1 & 7 | 8", (int)(1 + 1 & 7 | 8)),
	    new Pair("= 1 + 8 | 1 & 7 ", (int)(1 + 8 | 1 & 7)),
	    new Pair("= 1 < 2", true),
	    new Pair("= 1 <= 2", true),
	    new Pair("= 1 > 2", false),
	    new Pair("= 1 >= 2", false),
	    new Pair("= 1 >= 1", true),
	    new Pair("= 1 == 1", true),
	    new Pair("= 1 != 2", true),
	    new Pair("= 1 != 1", false),
	    new Pair("= 1 < 2 == 3 < 4", true),
	    new Pair("= 1 < 2 != 4 < 3", true),
	    new Pair("= 1 <= 2 == 3 <= 4", true),
	    new Pair("= 1 <= 2 != 4 <= 3", true),
	    new Pair("= true && true", true),
	    new Pair("= false && true", false),
	    new Pair("= true && false", false),
	    new Pair("= false && false", false),
	    new Pair("= !true", false),
	    new Pair("= !false", true),
	    new Pair("= !false && true", true),
	    new Pair("= true && !false", true),
	    new Pair("= true || true", true),
	    new Pair("= true || false", true),
	    new Pair("= false || true", true),
	    new Pair("= false || false", false),
	    new Pair("= true && false || true", true),
	    new Pair("= true && false || false", false),
	    new Pair("= true || false && true", true),
	    new Pair("= true || true && false", true),
	    new Pair("= (true || false) && (false || true)",
		     (true || false) && (false || true)),
	    new Pair("= and(true && true, false || true)", true),
	    new Pair("= and(false || true, true && true)", true),
	    new Pair("= and(true && false, false || true)", true),
	    new Pair("= or(false || true, true && false)", true),
	    new Pair("= true ? 1 + 0: 1 + 1", 1),
	    new Pair("= false ? 1 + 0: 1 + 1", 2),
	    new Pair("= (true || false) ? 1 + 0: 1 + 1", 1),
	    new Pair("= true || false ? 1 + 0: 1 + 1", 1),
	    new Pair("= false && true ? 1 + 0: 1 + 1", 2),
	    new Pair(" = true ? (true ? 1: 2): (true? 3: 4)", 1),
	    new Pair(" = true ? (true ? 1: 2): (false? 3: 4)", 1),
	    new Pair(" = true ? (false ? 1: 2): (true? 3: 4)", 2),
	    new Pair(" = true ? (false ? 1: 2): (false? 3: 4)", 2),
	    new Pair(" = false ? (true ? 1: 2): (true? 3: 4)", 3),
	    new Pair(" = false ? (true ? 1: 2): (false? 3: 4)", 4),
	    new Pair(" = false ? (false ? 1: 2): (true? 3: 4)", 3),
	    new Pair(" = false ? (false ? 1: 2): (false? 3: 4)", 4),
	    new Pair(" = Math.max(true?1:2, true?3:4)", 3),
	    new Pair(" = Math.max(true?1:2, false?3:4)", 4),
	    new Pair(" = Math.max(false?1:2, true?3:4)", 3),
	    new Pair(" = Math.max(false?1:2, false?3:4)", 4),
	    new Pair(" = Math.min(true?1:2, true?3:4)", 1),
	    new Pair(" = Math.min(true?1:2, false?3:4)", 1),
	    new Pair(" = Math.min(false?1:2, true?3:4)", 2),
	    new Pair(" = Math.min(false?1:2, false?3:4)", 2),
	    new Pair(" = true? Math.min(1,2): Math.min(3,4)", 1),
	    new Pair(" = false? Math.min(1,2): Math.min(3,4)", 3),
	    new Pair(" = false? 10: true? 20: 30",
		     false? 10: true? 20: 30),
	    new Pair("= false? 10: false? 20: false? 30: 40",
		     40),
	    new Pair("= false? 10: false? 20: true? 30: 40",
		     30),
	    new Pair("= false? 10: true? 20: true? 30: 40",
		     20),
	    new Pair("= true? 10: true? 20: true? 30: 40",
		     10),
	    new Pair("= false? true? 10: 20: 30", false? true? 10: 20: 30),
	    new Pair("= true? true? 10: 20: 30", true? true? 10: 20: 30),
	    new Pair("= true? false? 10: 20: 30", true? false? 10: 20: 30),
	    new Pair("= Math.max(false? 10: false? 20: 30,"
		     + "false? 40: false? 50: 60)", 60),
	    new Pair("= Math.min(false? 10: false? 20: 30, "
		     + "false? 30: false? 40: 50)", 30),
	    new Pair("= Math.max(true? 10: false? 20: 30,"
		     + "true? 40: false? 50: 60)", 40),
	    new Pair("= Math.min(true? 10: false? 20: 30, "
		     + "true? 30: false? 40: 50)", 10),
	    new Pair("= abs(0)", 0.0),
	    new Pair("= abs(0) < 1.e-14", true),
	    new Pair("= ourfact(0)", 1),
	    new Pair("= ourfact(1)", 1),
	    new Pair("= ourfact(2)", 2),
	    new Pair("= ourfact(3)", 6),
	    new Pair("= ourfact(4)", 24),
	    new Pair("= ourfact(5)", 120),
	    new Pair("= sum(1, 2)", 3.0),
	    new Pair("= (function (a, b) {a + b}).invoke(1,2)", 3.0),
	    new Pair("= (function (a, b, c) { "
		     + "a + (function(x, y) {x + y}).invoke(b, c)})"
		     + ".invoke(1, 2, 3)", 6),
	    new Pair("= function (a, b, c) { "
		     + "a + function(x, y) {x + y}.invoke(b, c)}"
		     + ".invoke(1, 2, 3)", 6),
	    new Pair("= sum3(1, function(x, y){x + y}.invoke(2, 3), 3)", 9),
	    new Pair("= sum3(1, function(x, y){x + y}(2, 3), 3)", 9),

	    new Pair("= bopTester.call(function (x,y) {x + y}, 1.0, 2.0)", 3.0),
	    new Pair("= bopTester.call(function (x,y) {x * y}, 2.0, 4.0)", 8.0),
	    new Pair("= bopTester.call(sum, 2.0, 4.0)", 6.0),
	    new Pair("= 1 << 16", 1 << 16),
	    new Pair ("= 1 << 16 >> 15", 1 << 16 >> 15),
	    new Pair ("= -1 >> 16", -1 >> 16),
	    new Pair ("= -1 >>> 16", -1 >>> 16),
	    new Pair ("= 1+2 << 2", 1+2 << 2),
	    new Pair ("= 1<<2 == 1<<2", 1<<2 == 1<<2)
	};

	for (Pair p: pairs) {
	    System.out.println("... testing " + p.s);
	    if (p.type == 'd') {
		Number v = (Number) parser.parse(p.s);
		double vv = v.doubleValue();
		if (Math.abs(vv - p.v) > 1.e-10) {
		    System.out.println(p.s + " failed , got " +v
				       +", expecting " + p.v);
		    throw new Exception();
		}
	    } else if (p.type == 'i') {
		Number n = (Number) parser.parse(p.s);
		double v = n.doubleValue();
		if (v != Math.rint(v)) {
		    System.out.println(p.s + " failed: value not an integer");
		    throw new Exception();
		}
		int i = (int)Math.round(v);
		if (i != p.vi) {
		    System.out.println(p.s + " failed , got " + i
				       +", expecting " + p.vi);
		    throw new Exception();
		}
	    } else if (p.type == 'b') {
		Boolean b = (Boolean) parser.parse(p.s);
		if (b != p.vb) {
		    System.out.println(p.s + " failed , got " +b
				       +", expecting " + p.vb);
		    throw new Exception();
		}
	    } else if (p.type == 's') {
		String str = (String) parser.parse(p.s);
		if (!str.equals(p.vs)) {
		    System.out.println(p.s + " failed , got \"" + str
				       +"\", expecting \"" + p.vs + "\"");
		    throw new Exception();
		}
	    } else if (p.type == 'c') {
		Class<?> c = (Class<?>) parser.parse(p.s);
		if (!c.equals(p.vc)) {
		    System.out.println(p.s + " failed , got \"" + c
				       +"\", expecting \"" + p.vc + "\"");
		    throw new Exception();
		}
	    }
	}

	String bad[] = { "= acos(0.3) acos(0.4)",
			 "= 10 + + 20",
			 "= 10 *+ 30",
			 "= 10 */ 30",
			 "= 10(20)",
			 "= 10 20",
			 "= x y",
			 "= (10) y"
	};

	for (String str: bad) {
	    try {
		parser.parse(str);
		System.out.println("error: accepted " + str);
		System.exit(1);
	    } catch (Exception e) {
	    }
	}

	parser = new ExpressionParser(new Class<?>[] {
			Animation2D.class,
			AnimationLayer2D.Type.class,
		    }, new Class<?>[] {
			Animation2D.class,
			AnimationLayer2D.Type.class
		    }, new Class<?>[] {
			Math.class,
		    }, new Class<?>[] {
			String.class,
		    }, new Class<?>[] {
		        Graph.class
		    });

	if (true) {
	    List<URL> links = new LinkedList<>();
	    Collections
		.addAll(links,
			new URL("file:/usr/share/doc/openjdk-11-doc/api/"),
			new URL("file:/usr/share/doc/libbzdev-doc/api/"));

	    parser.createAPIMap(links);

	    System.out.println(parser.findDocURL(String.class));
	    System.out.println(parser.findDocURL(Animation2D.class));

	    System.out.println(parser.findDocURL("java.lang.String"));
	    System.out
		.println(parser.findDocURL("org.bzdev.anim2d.Animation2D"));

	    System.out.println(parser.findDocURL(Graph.class, "DEFAULT_WIDTH"));

	    System.out.println(parser.findDocURL("org.bzdev.graphs.Graph",
						 "DEFAULT_WIDTH"));
	    System.out.println("----------------------");

	    var keymap = new TemplateProcessor.KeyMap();
	    keymap.put("const", parser.keylistForConstants());
	    keymap.put("rclasses", parser.keylistForReturnClasses());
	    keymap.put("aclasses", parser.keylistForArgumentClasses());
	    keymap.put("functs", parser.keylistForFunctions());
	    keymap.put("methods", parser.keylistForMethods());
	    keymap.print();
	}
    }
}
