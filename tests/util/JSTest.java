import org.bzdev.util.*;

import java.lang.reflect.Method;
import org.bzdev.io.AppendableWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class JSTest {
    // test method to check compilation - to make sure
    // we can use varargs with primitive types and so we
    // can check the types returned.
    public double[] foo(double... values) {
	return values;
    }

    public double[][] bar(double[]... values) {
	return values;
    }

    public static void main(String argv[]) throws Exception {
	JSObject jsobject = new JSObject();
	JSObject jsobject2 = new JSObject();
	JSArray jsarray = new JSArray();
	jsobject.put("x1", "foo");
	jsobject.put("x2", 10);
	jsobject.put("x3", jsobject2);
	jsobject.put("x4", jsarray);
	jsobject.put("x5", 10.5);
	jsobject.put("x6", 20L);

	jsobject2.put("y1", "a string");
	jsobject2.put("y2", 30);

	jsarray.add(10);
	jsarray.add("string");

	System.out.println("x1 = " + jsobject.get("x1"));
	System.out.println("x2 = " + jsobject.get("x2"));
	System.out.println("x3 = " + jsobject.get("x3"));
	System.out.println("x4 = " + jsobject.get("x4"));
	System.out.println("x5 = " + jsobject.get("x5"));
	System.out.println("x6 = " + jsobject.get("x6"));

	System.out.println("jsarray[0] = " + jsarray.get(0));
	System.out.println("jsarray[1] = " + jsarray.get(1));

	jsarray.stream().forEachOrdered((x) -> {
		System.out.println(".... " + x);
	    });
	System.out.println(".... [again]");
	jsarray.stream().forEach((x) -> {
		System.out.println(".... " + x);
	    });


	String[] firstTests = {
	    "\"hello\"",
	    "\"\\\\hello\\\\\"",
	    "\"\\\"hello\\\"\"",
	    "\"u-umlaut = \\u00fc\"",
	    "20",
	    "35.4",
	    "37.6e5",
	    "25.4e-10",
	    "1.2e+2",
	    "5e+8",
	    "5e+17",
	    "true",
	    "false",
	    "null",
	    "[]",
	    "[30]",
	    "[ 10, 20, \"foo\", true, false, null]",
	    "{}",
	    "{\"x1\": 30}",
	    "{\"x1\": 10, \"x2\": 20, \"x3\": \"foo\", "
	    + "\"x4\": true, \"x5\":false, \"x6\": null}",
	};
	Writer w = new AppendableWriter(System.out);
	for (String json: firstTests) {
	    Object object = JSUtilities.JSON.parse(json);
	    System.out.println(object);
	    JSUtilities.JSON.writeTo(w, object);
	    w.flush();
	    System.out.println();
	}

	String nestedTest1 ="{\"object\": "
	    + "{\"x1\": 10, \"x2\": 20, \"x3\": \"foo\", "
	    + "\"x4\": true, \"x5\":false, \"x6\": null}"
	    + ", \"array\": "
	    + "[ 10, 20, \"foo\", true, false, null]"
	    + ", \"value\": 10}";

	System.out.println("nestedTest1: ");
	System.out.println(nestedTest1);
	JSObject result = (JSObject) JSUtilities.JSON.parse(nestedTest1);
	System.out.println(result);
	JSUtilities.JSON.writeTo(w, result);
	w.flush();
	System.out.println();

	String nestedTest2 ="["
	    + "{\"x1\": 10, \"x2\": 20, \"x3\": \"foo\", "
	    + "\"x4\": true, \"x5\":false, \"x6\": null}"
	    + ", [ 10, 20, \"foo\", true, false, null]"
	    + ", 10 ]";
	JSArray array = (JSArray)JSUtilities.JSON.parse(nestedTest2);
	System.out.println(array);
	JSUtilities.JSON.writeTo(w, array);
	w.flush();
	System.out.println();
	
	System.out.println("Test JSON String Formatting");

	String[] strings = {
	    "abcd",
	    "ab\r\n",
	    "ab\u0002ffff"
	};
	for (String s: strings) {
	    System.out.println(JSUtilities.JSON.quote(s));
	}

	JSArray sarray = new JSArray();
	sarray.add("hello");
	sarray.add("goodbye");
	sarray.add("ok");
	Object[] oarray = sarray.toArray();
	for (Object o: oarray) {
	    System.out.println(o);
	}

	String[] sArray = sarray.toArray(String.class);
	for (String s: sArray) {
	    System.out.println(s);
	}
	JSArray matrix = new JSArray();
	JSArray row1 = new JSArray();
	JSArray row2 = new JSArray();
	JSArray row3 = new JSArray();

	row1.add("a"); row1.add("b");
	row2.add("c"); row2.add("d");
	row3.add("e"); row3.add("f");

	matrix.add(row1);
	matrix.add(row2);
	matrix.add(row3);

	String[][] marray = matrix.toMatrix(String.class);
	for (String[] row: marray) {
	    for (String col: row) {
		System.out.print(col);
	    }
	    System.out.println();
	}

	JSArray da = new JSArray();
	da.add(10.0); da.add(20.0); da.add(30.0);
	double[] darray = da.toDoubleArray();
	for (double x: darray) {
	    System.out.println(x);
	}

	JSArray dm = new JSArray();
	JSArray dm1 = new JSArray();
	dm1.add(10.0); dm1.add(20.0);
	JSArray dm2 = new JSArray();
	dm2.add(30.0); dm2.add(40.0);
	JSArray dm3 = new JSArray();
	dm3.add(50.0); dm3.add(60.0);
	dm.add(dm1); dm.add(dm2); dm.add(dm3);

	double[][] dmatrix = dm.toDoubleMatrix();
	for (double[] row: dmatrix) {
	    for (double col: row) {
		System.out.print(col + " ");
	    }
	    System.out.println();
	}

	JSArray la = new JSArray();
	la.add(10L); la.add(20L); la.add(30L);
	long[] larray = la.toLongArray();
	for (long x: larray) {
	    System.out.println(x);
	}

	JSArray lm = new JSArray();
	JSArray lm1 = new JSArray();
	lm1.add(10L); lm1.add(20L);
	JSArray lm2 = new JSArray();
	lm2.add(30L); lm2.add(40L);
	JSArray lm3 = new JSArray();
	lm3.add(50L); lm3.add(60L);
	lm.add(lm1); lm.add(lm2); lm.add(lm3);

	long[][] lmatrix = lm.toLongMatrix();
	for (long[] row: lmatrix) {
	    for (long col: row) {
		System.out.print(col + " ");
	    }
	    System.out.println();
	}

	JSArray ia = new JSArray();
	ia.add(10); ia.add(20); ia.add(30);
	int[] iarray = ia.toIntArray();
	for (int x: iarray) {
	    System.out.println(x);
	}

	JSArray im = new JSArray();
	JSArray im1 = new JSArray();
	im1.add(10); im1.add(20);
	JSArray im2 = new JSArray();
	im2.add(30); im2.add(40);
	JSArray im3 = new JSArray();
	im3.add(50); im3.add(60);
	im.add(im1); im.add(im2); im.add(im3);

	int[][] imatrix = im.toIntMatrix();
	for (int[] row: imatrix) {
	    for (int col: row) {
		System.out.print(col + " ");
	    }
	    System.out.println();
	}
	System.out.println(double[].class);
	System.out.println(double[][].class);
	System.out.println(double[].class.getSuperclass());

	Method m = JSTest.class.getMethod("foo", double[].class);
	System.out.println(m.getName());

	m = JSTest.class.getMethod("bar", double[][].class);
	System.out.println(m.getName());
	System.out.println("double <- Double: "
			   + double.class.isAssignableFrom(Double.class));
	System.out.println("Object <- double: "
			   + Object.class.isAssignableFrom(double.class));
	System.out.println("Object <- Double: "
			   + Object.class.isAssignableFrom(Double.class));

    }
}
