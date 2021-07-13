import org.bzdev.obnaming.*;
import org.bzdev.io.AppendableWriter;
import org.bzdev.util.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class JSTest {

    private static void factoryTest() throws Exception {
	ATestNamer anamer = new ATestNamer();
	ATestObject2 peer1 = new ATestObject2(anamer, "peer1", true);
	ATestObject2 peer2 = new ATestObject2(anamer, "peer2", true);
	ATestObject2 peer3 = new ATestObject2(anamer, "peer3", true);
	ATestObject2Factory factory = new ATestObject2Factory(anamer);
	
	Object spec = NJSUtilities.JSON.parse
	    (new FileInputStream("jstest.json"), "UTF-8");
	factory.configure(spec);
	System.out.println("value1: " + factory.getValue1());
	factory.printValue7();
	System.out.println("peer: " + factory.getPeerName());
	factory.printOthers();
	System.out.print("peer1: ");
	factory.printKeyedValue7(peer1);
	System.out.print("peer2: ");
	factory.printKeyedValue7(peer2);
	System.out.print("peer3: ");
	factory.printKeyedValue7(peer3);

	System.out.println("........ Syntax error testing");
	try {
	    System.out.print("jstest2: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest2.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest3: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest3.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest4: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest4.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest5: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest5.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest6: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest6.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest7: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest7.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest8: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest8.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

	try {
	    System.out.print("jstest9: ");
	    spec = NJSUtilities.JSON.parse(new FileInputStream("jstest9.json"),
					   "UTF-8");
	} catch (IOException eio) {
	    System.out.println(eio.getMessage());
	}

    }


    public static void main(String argv[]) throws Exception {

	OurObjectNamer namer = new OurObjectNamer();
	OurObjectFactory1 factory = new OurObjectFactory1(namer);

	OurNamedObject1 object1 = factory.createObject("ourObject");

	NJSObject jsobject = new NJSObject();
	NJSObject jsobject2 = new NJSObject();
	NJSArray jsarray = new NJSArray();
	jsobject.put("x1", "foo");
	jsobject.put("x2", 10);
	jsobject.put("x3", jsobject2);
	jsobject.put("x4", jsarray);
	jsobject.put("x5", 10.5);
	jsobject.put("x6", 20L);
	jsobject.put("x7", object1);

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
	System.out.println("x7 = " + jsobject.get("x7", OurNamedObject1.class)
			   .getName());

	jsarray.add(object1);
	System.out.println("last array element = "
			   + jsarray.get(2, OurNamedObject1.class).getName());

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
	    Object object = NJSUtilities.JSON.parse(json);
	    System.out.println(object);
	    NJSUtilities.JSON.writeTo(w, object);
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
	NJSUtilities.JSON.writeTo(w, result);
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

	System.out.println("-------- test factory ----------");
	factoryTest();
    }
}
