import org.bzdev.scripting.*;
import org.bzdev.math.rv.*;
import java.util.*;
import java.io.*;
import java.awt.BasicStroke;
import org.bzdev.obnaming.misc.*;

public class Test {

    public static class TC<T> {
	T getnull() {return null;}
    }

    public static void main(String argv[]) throws Exception {
	String lang = System.getProperty("script.language", "ECMAScript");
	ATestNamer.setLanguage(lang);
	TestNamer namer = new TestNamer();
	TestNamer altNamer = new TestNamer();
	altNamer.addObjectNamer(namer);
	TestObject obj1 = new TestObject1(namer, "name1", true);
	TestObject alt1 = new TestObject4(altNamer, "name1", true);

	TestObject obj2 = new
	    TestObject2(namer, "name2", true);
	TestObject obj3 = new
	    TestObject3(namer, "name3", true);
	TestObject obj4 = new
	    TestObject4(namer, "name4", true);
	TestObject obj6 = new
	    TestObject6(namer, "name6", true);
	TestObject obj5 = new
	    TestObject5(namer, "name5", true);

	if (obj1 != namer.getObject("name1") ||
	    obj2 != namer.getObject("name2") ||
	    obj3 != namer.getObject("name3") ||
	    obj4 != namer.getObject("name4") ||
	    obj5 != namer.getObject("name5") ||
	    obj6 != namer.getObject("name6")) {
	    System.out.println("getObject returned unexpected results");
	    System.exit(1);
	}

	if (alt1 != altNamer.getObject("name1")
	    || alt1 != altNamer.getObject("name1", TestObject4.class)
	    || obj1 != altNamer.getObject("name1", TestObject1.class)) {
	    System.out.println("getObject for altNamer returned "
			       + "unexpected results");
	    System.exit(1);
	}

	System.out.println("all names:");
	for (String name: namer.getObjectNames()) {
	    System.out.println(name);
	}

	System.out.println("names for type TestObject2:");
	for(String name: namer.getObjectNames(TestObject2.class)) {
	    System.out.println(name);
	}
	System.out.println("names for type TestObject5:");
	for(String name: namer.getObjectNames(TestObject5.class)) {
	    System.out.println(name);
	}
	System.out.println("names for type TestObject4:");
	for(String name: namer.getObjectNames(TestObject4.class)) {
	    System.out.println(name);
	}

	System.out.println("all objects from collection:");
	for (TestObject to: namer.getObjects()) {
	    System.out.println(to.getName());
	}
	System.out.println("names for type TestObject2 from collection:");
	for (TestObject2 to: namer.getObjects(TestObject2.class)) {
	    System.out.println(to.getName());
	}
	System.out.println("names for type \"TestObject2\" from collection:");
	for (TestObject to: namer.getObjects("TestObject2")) {
	    System.out.println(to.getName());
	}

	System.out.println("names for type TestObject5 from collection:");
	for (TestObject5 to: namer.getObjects(TestObject5.class)) {
	    System.out.println(to.getName());
	}
	System.out.println("names for type TestObject4 from collection:");
	for (TestObject4 to: namer.getObjects(TestObject4.class)) {
	    System.out.println(to.getName());
	}

	System.out.println("nonexistent case:");
	for (Double to: namer.getObjects(Double.class)) {
	    System.out.println(to);
	}
	System.out.println("... nothing should be printed");

	TestObject5 tobj5 = namer.getObject("name5", TestObject5.class);
	System.out.println("tobj5 name = " + tobj5.getName());
	tobj5 = namer.getObject("name6", TestObject5.class);
	System.out.println("... next name = " + tobj5.getName());
	tobj5 = namer.getObject("name1", TestObject5.class);
	if (tobj5 != null) {
	    System.out.println("name1 fetched as TestObject5 instance");
	    System.exit(1);
	}

	System.out.println("before deletion ...");
	System.out.println("isDeleted: " + obj1.isDeleted()
			   + "; deletePending: " + obj1.deletePending()
			   + "; canDelete:  " + obj1.canDelete());

	obj1.delete(); obj2.delete(); obj3.delete();
	obj4.delete(); obj5.delete(); obj6.delete();

	System.out.println("after deletion ...");
	System.out.println("isDeleted: " + obj1.isDeleted()
			   + "; deletePending: " + obj1.deletePending()
			   + "; canDelete:  " + obj1.canDelete());

	int size = namer.getObjectNames().size();
	if (size != 0) {
	    System.out.println("a deletion failed - " + size
			       + " object(s) left");
	    System.exit(1);
	}

	System.out.println("number of objects after deletion = "
			   + namer.getObjectNames().size());

	System.out.println("-----------------");

	ATestNamer anamer = new ATestNamer();
	ATestNamer altAnamer = new ATestNamer();
	altAnamer.addObjectNamer(anamer);

	if (!(anamer instanceof ScriptingContext)) {
	    System.out.println("anamer is not a scripting context");
	    System.exit(1);
	}

	ATestObject aobj1 = new ATestObject1(anamer, "name1", true);
	ATestObject altaobj1 = new ATestObject4(altAnamer, "name1", true);

	ATestObject aobj2 = new
	    ATestObject2(anamer, "name2", true);
	ATestObject aobj3 = new
	    ATestObject3(anamer, "name3", true);
	ATestObject aobj4 = new
	    ATestObject4(anamer, "name4", true);
	ATestObject aobj6 = new
	    ATestObject6(anamer, "name6", true);
	ATestObject aobj5 = new
	    ATestObject5(anamer, "name5", true);

	if (aobj1 != anamer.getObject("name1") ||
	    aobj2 != anamer.getObject("name2") ||
	    aobj3 != anamer.getObject("name3") ||
	    aobj4 != anamer.getObject("name4") ||
	    aobj5 != anamer.getObject("name5") ||
	    aobj6 != anamer.getObject("name6")) {
	    System.out.println("getObject returned unexpected results");
	    System.exit(1);
	}

	if (altaobj1 != altAnamer.getObject("name1")
	    || altaobj1 != altAnamer.getObject("name1", ATestObject4.class)
	    || aobj1 != altAnamer.getObject("name1", ATestObject1.class)) {
	    System.out.println("getObject for altAnamer returned "
			       + "unexpected results");
	    System.exit(1);
	}

	System.out.println("all names (anamer):");
	for (String name: anamer.getObjectNames()) {
	    System.out.println(name);
	}
	System.out.println("names for type ATestObject2:");
	for(String name: anamer.getObjectNames(ATestObject2.class)) {
	    System.out.println(name);
	}
	System.out.println("names for type ATestObject5:");
	for(String name: anamer.getObjectNames(ATestObject5.class)) {
	    System.out.println(name);
	}
	System.out.println("names for type ATestObject4:");
	for(String name: anamer.getObjectNames(ATestObject4.class)) {
	    System.out.println(name);
	}

	System.out.println("all objects in anamer from collection:");
	for (ATestObject to: anamer.getObjects()) {
	    System.out.println(to.getName());
	}
	System.out.println("names for type TestObject2 from collection:");
	for (ATestObject2 to: anamer.getObjects(ATestObject2.class)) {
	    System.out.println(to.getName());
	}
	System.out.println("names for type TestObject5 from collection:");
	for (ATestObject5 to: anamer.getObjects(ATestObject5.class)) {
	    System.out.println(to.getName());
	}
	System.out.println("names for type TestObject4 from collection:");
	for (ATestObject4 to: anamer.getObjects(ATestObject4.class)) {
	    System.out.println(to.getName());
	}


	ATestObject5 atobj5 = anamer.getObject("name5", ATestObject5.class);
	System.out.println("atobj5 name = " + atobj5.getName());
	atobj5 = anamer.getObject("name6", ATestObject5.class);
	System.out.println("... next name = " + atobj5.getName());
	atobj5 = namer.getObject("name1", ATestObject5.class);
	if (atobj5 != null) {
	    System.out.println("name1 fetched as ATestObject5 instance");
	    System.exit(1);
	}

	System.out.println("before deletion ...");
	System.out.println("isDeleted: " + aobj1.isDeleted()
			   + "; deletePending: " + aobj1.deletePending()
			   + "; canDelete:  " + aobj1.canDelete());

	aobj1.delete(); aobj2.delete(); aobj3.delete();
	aobj4.delete(); aobj5.delete(); aobj6.delete();

	System.out.println("after deletion ...");
	System.out.println("isDeleted: " + aobj1.isDeleted()
			   + "; deletePending: " + aobj1.deletePending()
			   + "; canDelete:  " + aobj1.canDelete());
	int asize = anamer.getObjectNames().size();
	if (asize != 0) {
	    System.out.println("a deletion failed - "  + asize
			       +" object(s) left");
	    System.exit(1);
	}
	System.out.println("number of objects after deletion = " + asize);

	System.out.println("\n---------------------\n");
	System.out.println("Use Factory\n");

	ATestObject2Factory factory = new ATestObject2Factory(anamer);

	anamer.putScriptObject("namer", anamer);
	anamer.putScriptObject("factory", factory);

	System.out.println("For value1, tip = " + factory.getTip("value1"));
	System.out.println("For value1, label = " + factory.getLabel("value1"));
	System.out.println("For value1, doc = " + factory.getDoc("value1"));
	System.out.println("For keyed.Value1, tip = "
			   + factory.getTip("keyed.Value1"));
	System.out.println("For keyed.Value1, label = "
			   + factory.getLabel("keyed.Value1"));
	System.out.println("For keyed.Value1, doc = "
			   + factory.getDoc("keyed.Value1"));

	System.out.println("Parameter prefixes:");
	    for (String prefix: factory.parmPrefixes()) {
		System.out.println("    " + prefix);
	    }
	System.out.println("for parameter keyed.Value1, prefix is "
			   + factory.getParmPrefix("keyed.Value1"));

	try {
	    // make sure the tightening on value2 worked.
	    factory.testValue2();

	    factory.set("value1", "10");
	    if (factory.getValue1() != 10) {
		throw new Exception("bad value1");
	    }
	    factory.set("value1", 10L);
	    if (factory.getValue1() != 10) {
		throw new Exception("bad value1");
	    }
	    factory.set("value1", 10.0);
	    if (factory.getValue1() != 10) {
		throw new Exception("bad value1");
	    }
	    factory.set("value2", "10");
	    if (factory.getNextValue2() != 10) {
		throw new Exception("bad value2");
	    }
	    factory.set("value2", 10L);
	    if (factory.getNextValue2() != 10) {
		throw new Exception("bad value2");
	    }
	    factory.set("value2", 10.0);
	    if (factory.getNextValue2() != 10) {
		throw new Exception("bad value2");
	    }

	    factory.set("value2c", new FixedDoubleRV(11.0));
	    factory.print2c();
	    factory.clear("value2c");
	    factory.print2c();
	    
	    System.out.println("setting keyed.Value2c for test [1]");
	    factory.set("strkeyed.Value2c", "test",
			new FixedDoubleRV(12.5));
	    factory.printKeyed();
	    factory.unset("strkeyed.Value2c", "test");
	    factory.printKeyed();

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}


	factory.set("value1", 10);
	factory.set("value2", 20);
	factory.set("value3",
		    new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					   new UniformIntegerRV(10, 15)));

	factory.set("value4", 40.0);
	factory.set("value5", new GaussianRV(50.0, 1.0));
	factory.set("value5a", new GaussianRV(50.0, 1.0));

	System.out.println("next value 5 = " + factory.getNextValue5());
	System.out.println("next value 5a = " + factory.getNextValue5a());


	ATestObject2 fobj1 = factory.createObject("factoryCreated1");
	ATestObject2 fobj2 = factory.createObject("factoryCreated2");

	factory.set("value6", "OPT2");
	factory.add("value7", "OPT2");
	factory.add("value7", ATestObject2Factory.Option.OPT3);
	factory.set("peer", "factoryCreated1");
	factory.set("label", "<my label>");
	factory.add("others", "factoryCreated1");
	factory.add("others", /*"factoryCreated2"*/ fobj2);

	ATestObject2 fobj3 = factory.createObject("factoryCreated3");

	factory.remove("others", "factoryCreated2");
	factory.set("value6", ATestObject2Factory.Option.OPT1);
	factory.set("peer", fobj2);
	ATestObject2 fobj4 = factory.createObject("factoryCreated4");
	factory.clear("others");
	ATestObject2 fobj5 = factory.createObject("factoryCreated5");

	factory.add("others", "factoryCreated1");
	factory.add("others", fobj2);
	factory.remove("others", "factoryCreated1");
	factory.remove("others", fobj2);
	factory.remove("value7", ATestObject2Factory.Option.OPT3);
	factory.remove("value7", "OPT2");
	ATestObject2 fobj5a = factory.createObject("factoryCreated5a");
	for (ATestObject2 other: fobj5a.others()) {
	    System.out.println("fobj5a others: " + other.getName());
	    System.out.println("others for fobj5a should be empty");
	    System.exit(1);
	}
	for (ATestObject2Factory.Option option: fobj5a.getOptions()) {
	    System.out.println("    " + option);
	    System.out.println("options for fobj5a should be empty");
	    System.exit(1);
	}
	factory.add("value8", 10);
	factory.add("value8", 20);
	factory.add("value8", "30");
	factory.add("value8", 40L);
	factory.add("value8", 50.0);
	factory.printValue8();
	factory.remove("value8", 20);
	factory.remove("value8", "30");
	factory.remove("value8", 40L);
	factory.remove("value8", 50.0);
	factory.printValue8();
	factory.add("value8", "20");
	factory.printValue8();

	factory.add("value9", "hello");
	factory.add("value9", "goodbye");
	factory.printValue9();
	factory.remove("value9", "goodbye");
	factory.printValue9();
	factory.add("value9", "goodbye");
	factory.printValue9();

	System.out.println("setting keyed.Value2c for test [2]");
	factory.set("strkeyed.Value2c", "test",
		    new FixedDoubleRV(12.5));
	factory.printKeyed();
	factory.unset("strkeyed.Value2c", "test");
	factory.printKeyed();
	System.out.println("setting keyed.Value2c for test [3]");
	factory.set("strkeyed.Value2c", "test",
		    new FixedDoubleRV(13.5));
	factory.printKeyed();
	System.out.println("clearing strkeyed");
	factory.clear("strkeyed");
	factory.printKeyed();
	System.out.println("print after clearing strkeyed completed");
	ATestObject2 fobj6 = factory.createObject("factoryCreated6");

	factory.set("keyed.Value1", "factoryCreated1", 10);
	factory.set("keyed.Value2", "factoryCreated1",
		    new UniformIntegerRV(11, 20));
	factory.add("keyed.Others", "factoryCreated1.factoryCreated2");
	factory.add("keyed.Others", "factoryCreated1.factoryCreated3");

	ATestObject2 fobj7 = factory.createObject("factoryCreated7");
	System.out.println("fobj1 value1 = " + fobj1.getValue1());
	System.out.println("fobj1 value2 = " + fobj1.getValue2());
	System.out.println("fobj2 value1 = " + fobj2.getValue1());
	System.out.println("fobj2 value2 = " + fobj2.getValue2());
	System.out.println("fobj3 value6 = " + fobj3.getDefaultOption());
	System.out.println("fobj4 value6 = " + fobj4.getDefaultOption());
	System.out.println("options (value7) for fobj3:");
	for (ATestObject2Factory.Option option: fobj3.getOptions()) {
	    System.out.println("    " + option);
	}
	System.out.println("fobj3 peer = " + fobj3.getPeer().getName());
	System.out.println("fobj4 peer = " + fobj4.getPeer().getName());
	System.out.println("fobj3 label = " + fobj3.getLabel());

	System.out.println("others for fobj3: ");
	for (ATestObject2 other: fobj3.others()) {
	    System.out.println("    " + other.getName());
	}
	System.out.println("others for fobj4: ");
	for (ATestObject2 other: fobj4.others()) {
	    System.out.println("    " + other.getName());
	}
	System.out.println("others for fobj5: ");
	for (ATestObject2 other: fobj5.others()) {
	    System.out.println("    " + other.getName());
	}
	System.out.println("('others' list should be empty )");
	System.out.println();
	int min1 = 15; int min2 = 15;
	int max1 = 0; int max2 = 0;

	System.out.println("fobj6 should have default values:");
	System.out.println("fobj6 value1 = " + fobj6.getValue1());
	System.out.println("fobj6 value2 = " + fobj6.getValue2());
	if (fobj6.getPeer() != null) {
	    System.out.println("fobj6 peer not null");
	}
	if (fobj6.others().size() != 0) {
	    System.out.println("fobj6 has a non-empty 'others' set");
	}


	System.out.println("factoryCreated7, keyed.Value1:");
	for (Map.Entry<ATestObject2,Integer> entry:
		 fobj7.keyedValue1s().entrySet())
	    {
		System.out.println("    " + entry.getKey().getName() + ": "
				   + entry.getValue());
	    }
	System.out.println("factoryCreated7, keyed.Value2:");
	for (Map.Entry<ATestObject2,Integer> entry:
		 fobj7.keyedValue2s().entrySet())
	    {
		System.out.println("    " + entry.getKey().getName() + ": "
				   + entry.getValue());
	    }
	System.out.println("factoryCreated7, keyed.Others:");
	for (Map.Entry<ATestObject2,Set<ATestObject2>> entry:
		 fobj7.keyedValue3s().entrySet())
	    {
		System.out.println("    " + entry.getKey().getName());
		for (ATestObject2 value: entry.getValue()) {
		    System.out.println("        " + value.getName());
		}
	    }

	for (int i = 0; i < 10000; i++) {
	    int v1 = fobj1.getValue3();
	    int v2 = fobj2.getValue3();
	    if (v1 < min1) min1 = v1;
	    if (v2 < min2) min2 = v2;
	    if (v1 > max1) max1 = v1;
	    if (v2 > max2) max2 = v2;
	}
	System.out.println("fobj1 v3: [" +min1 + ", " + max1 + "]");
	System.out.println("fobj2 v3: [" +min2 + ", " + max2 + "]");
	System.out.println();
	factory.set("value2", new UniformIntegerRV(1,10));
	double mean = 0.0;
	int min = 10;
	int max = 0;
	factory.setInterned(false);
	for (int i = 0; i < 10000; i++) {
	    ATestObject2 obj = factory.createObject();
	    int v = obj.getValue2();
	    mean += v;
	    if (v < min) min = v;
	    if (v > max) max = v;
	}
	mean /= 10000.0;
	System.out.println("value2 mean for 10000 uninterned objects"
			   + " = " + mean
			   + ", in [" + min + ", " + max + "]");
	System.out.println();
	System.out.print("Object names:");
	int cnt = 0;
	for (String name: anamer.getObjectNames()) {
	    if (((cnt++) % 10) == 0) System.out.println();
	    System.out.print(" " + name);
	}
	System.out.println();
	System.out.println("now try configuring a factory via a script:");
	factory.clear();
	factory.setInterned(true);
	try {
	    anamer.quickTest();
	    String extension = "js";
	    if (lang.equals("ESP")) extension = "esp" ;
	    InputStream is = new FileInputStream(new File("testScript."
							  + extension));
	    Reader rd = new InputStreamReader(is, "UTF-8");
	    anamer.evalScript(rd);
	    Object obj = anamer.getScriptObject("sobj1");
	    if (obj instanceof ATestObject2) {
		ATestObject2 sobj1 = (ATestObject2) obj;
		for (ATestObject2Factory.Option option: sobj1.getOptions()) {
		    System.out.println(" [sobj1 option includes " + option
				       + "]");
		}
	    }
	    obj = anamer.getScriptObject("sobj4");
	    System.out.println("sobj4 keyed.Value1:");
	    if (obj instanceof ATestObject2) {
		ATestObject2 sobj4 = (ATestObject2) obj;
		for (Map.Entry<ATestObject2,Integer> entry:
			 sobj4.keyedValue1s().entrySet())
		{
		    System.out.println("    " + entry.getKey().getName() + ": "
				       + entry.getValue());
		}
	    }
	    System.out.println("sobj4 keyed.Value2:");
	    if (obj instanceof ATestObject2) {
		ATestObject2 sobj4 = (ATestObject2) obj;
		for (Map.Entry<ATestObject2,Integer> entry:
			 sobj4.keyedValue2s().entrySet())
		{
		    System.out.println("    " + entry.getKey().getName() + ": "
				       + entry.getValue());
		}
	    }
	    System.out.println("sobj4 keyed.Others:");
	    if (obj instanceof ATestObject2) {
		ATestObject2 sobj4 = (ATestObject2) obj;
		for (Map.Entry<ATestObject2,Set<ATestObject2>> entry:
		 sobj4.keyedValue3s().entrySet())
		{
		    System.out.println("    " + entry.getKey().getName());
		    for (ATestObject2 value: entry.getValue()) {
			System.out.println("        " + value.getName());
		    }
		}
	    }

	    factory.clear();

	    Object[] compound = new Object[2];
	    factory.set("keyed.Value1", fobj1, 10);
	    factory.set("keyed.Value2", fobj1, 20);
	    factory.set("keyed.Value3", fobj1, 30);
	    factory.set("keyed.Value4", fobj1, 40);
	    factory.set("keyed.Value5", fobj1, 50);
	    factory.set("keyed.Value5a", fobj1, new GaussianRV(50.0, 1.0));
	    compound[0] = fobj1;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("keyed.Value6", fobj1, ATestObject2Factory.Option.OPT1);
	    factory.add("keyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("keyed.Value7", compound);
	    factory.set("keyed.Peer", fobj1, fobj2);
	    factory.set("keyed.Label", fobj1, "hello");
	    compound[1] = fobj2;
	    factory.add("keyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("keyed.Others", compound);

	    factory.set("optkeyed.Value1", ATestObject2Factory.Option.OPT1, 10);
	    factory.set("optkeyed.Value2", ATestObject2Factory.Option.OPT1, 20);
	    factory.set("optkeyed.Value3", ATestObject2Factory.Option.OPT1, 30);
	    factory.set("optkeyed.Value4", ATestObject2Factory.Option.OPT1, 40);
	    factory.set("optkeyed.Value5", ATestObject2Factory.Option.OPT1, 50);
	    compound[0] = ATestObject2Factory.Option.OPT1;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("optkeyed.Value6", ATestObject2Factory.Option.OPT1,
			ATestObject2Factory.Option.OPT1);
	    factory.add("optkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("optkeyed.Value7", compound);
	    factory.set("optkeyed.Peer", ATestObject2Factory.Option.OPT1,
			fobj2);
	    factory.set("optkeyed.Label", ATestObject2Factory.Option.OPT1,
			"hello");
	    compound[1] = fobj2;
	    factory.add("optkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("optkeyed.Others", compound);

	    factory.set("strkeyed.Value1", "hello", 10);
	    factory.set("strkeyed.Value2", "hello", 20);
	    factory.set("strkeyed.Value3", "hello", 30);
	    factory.set("strkeyed.Value4", "hello", 40);
	    factory.set("strkeyed.Value5", "hello", 50);
	    compound[0] = "hello";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("strkeyed.Value6", "hello",
			ATestObject2Factory.Option.OPT1);
	    factory.add("strkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("strkeyed.Value7", compound);
	    factory.set("strkeyed.Peer", "hello", fobj2);
	    factory.set("strkeyed.Label", "hello", "hello");
	    compound[1] = fobj2;
	    factory.add("strkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("strkeyed.Others", compound);

	    factory.set("intkeyed.Value1", 5, 10);
	    factory.set("intkeyed.Value2", 5, 20);
	    factory.set("intkeyed.Value3", 5, 30);
	    factory.set("intkeyed.Value4", 5, 40);
	    factory.set("intkeyed.Value5", 5, 50);
	    compound[0] = 5;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5, fobj2);
	    factory.set("intkeyed.Label", 5, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed();
	    System.out.println(" ... long numbers:");
	    factory.set("keyed.Value1", fobj1, 10L);
	    factory.set("keyed.Value2", fobj1, 20L);
	    factory.set("keyed.Value3", fobj1, 30L);
	    factory.set("keyed.Value4", fobj1, 40L);
	    factory.set("keyed.Value5", fobj1, 50L);
	    factory.set("optkeyed.Value1", ATestObject2Factory.Option.OPT1,
			10L);
	    factory.set("optkeyed.Value2", ATestObject2Factory.Option.OPT1,
			20L);
	    factory.set("optkeyed.Value3", ATestObject2Factory.Option.OPT1,
			30L);
	    factory.set("optkeyed.Value4", ATestObject2Factory.Option.OPT1,
			40L);
	    factory.set("optkeyed.Value5", ATestObject2Factory.Option.OPT1,
			50L);
	    factory.set("strkeyed.Value1", "hello", 10L);
	    factory.set("strkeyed.Value2", "hello", 20L);
	    factory.set("strkeyed.Value3", "hello", 30L);
	    factory.set("strkeyed.Value4", "hello", 40L);
	    factory.set("strkeyed.Value5", "hello", 50L);

	    factory.set("intkeyed.Value1", 5, 10L);
	    factory.set("intkeyed.Value2", 5, 20L);
	    factory.set("intkeyed.Value3", 5, 30L);
	    factory.set("intkeyed.Value4", 5, 40L);
	    factory.set("intkeyed.Value5", 5, 50L);

	    factory.printKeyed();
	    System.out.println(" ... double numbers:");
	    factory.set("keyed.Value1", fobj1, 10.0);
	    factory.set("keyed.Value2", fobj1, 20.0);
	    factory.set("keyed.Value3", fobj1, 30.0);
	    factory.set("keyed.Value4", fobj1, 40.0);
	    factory.set("keyed.Value5", fobj1, 50.0);
	    factory.set("optkeyed.Value1", ATestObject2Factory.Option.OPT1,
			10.0);
	    factory.set("optkeyed.Value2", ATestObject2Factory.Option.OPT1,
			20.0);
	    factory.set("optkeyed.Value3", ATestObject2Factory.Option.OPT1,
			30.0);
	    factory.set("optkeyed.Value4", ATestObject2Factory.Option.OPT1,
			40.0);
	    factory.set("optkeyed.Value5", ATestObject2Factory.Option.OPT1,
			50.0);
	    factory.set("strkeyed.Value1", "hello", 10.0);
	    factory.set("strkeyed.Value2", "hello", 20.0);
	    factory.set("strkeyed.Value3", "hello", 30.0);
	    factory.set("strkeyed.Value4", "hello", 40.0);
	    factory.set("strkeyed.Value5", "hello", 50.0);

	    factory.set("intkeyed.Value1", 5, 10.0);
	    factory.set("intkeyed.Value2", 5, 20.0);
	    factory.set("intkeyed.Value3", 5, 30.0);
	    factory.set("intkeyed.Value4", 5, 40.0);
	    factory.set("intkeyed.Value5", 5, 50.0);

	    factory.printKeyed(); factory.clear();
	    System.out.println(" ... string values");

	    factory.set("keyed.Value1", fobj1, "10");
	    factory.set("keyed.Value2", fobj1, "20");
	    factory.set("keyed.Value3", fobj1, "30");
	    factory.set("keyed.Value4", fobj1, "40");
	    factory.set("keyed.Value5", fobj1, "50");
	    compound[0] = fobj1;
	    compound[1] = "OPT1";
	    factory.set("keyed.Value6", fobj1, "OPT1");
	    factory.add("keyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("keyed.Value7", compound);
	    factory.set("keyed.Peer", fobj1, "factoryCreated2");
	    factory.set("keyed.Label", fobj1, "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("keyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("keyed.Others", compound);

	    factory.set("optkeyed.Value1", ATestObject2Factory.Option.OPT1,
			"10");
	    factory.set("optkeyed.Value2", ATestObject2Factory.Option.OPT1,
			"20");
	    factory.set("optkeyed.Value3", ATestObject2Factory.Option.OPT1,
			"30");
	    factory.set("optkeyed.Value4", ATestObject2Factory.Option.OPT1,
			"40");
	    factory.set("optkeyed.Value5", ATestObject2Factory.Option.OPT1,
			"50");
	    compound[0] = ATestObject2Factory.Option.OPT1;
	    compound[1] = "OPT1";
	    factory.set("optkeyed.Value6", ATestObject2Factory.Option.OPT1,
			"OPT1");
	    factory.add("optkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("optkeyed.Value7", compound);
	    factory.set("optkeyed.Peer", ATestObject2Factory.Option.OPT1,
			"factoryCreated2");
	    factory.set("optkeyed.Label", ATestObject2Factory.Option.OPT1,
			"hello");
	    compound[1] = "factoryCreated2";
	    factory.add("optkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("optkeyed.Others", compound);

	    factory.set("strkeyed.Value1", "hello", "10");
	    factory.set("strkeyed.Value2", "hello", "20");
	    factory.set("strkeyed.Value3", "hello", "30");
	    factory.set("strkeyed.Value4", "hello", "40");
	    factory.set("strkeyed.Value5", "hello", "50");
	    compound[0] = "hello";
	    compound[1] = "OPT1";
	    factory.set("strkeyed.Value6", "hello",
			"OPT1");
	    factory.add("strkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("strkeyed.Value7", compound);
	    factory.set("strkeyed.Peer", "hello", "factoryCreated2");
	    factory.set("strkeyed.Label", "hello", "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("strkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("strkeyed.Others", compound);

	    factory.set("intkeyed.Value1", 5, "10");
	    factory.set("intkeyed.Value2", 5, "20");
	    factory.set("intkeyed.Value3", 5, "30");
	    factory.set("intkeyed.Value4", 5, "40");
	    factory.set("intkeyed.Value5", 5, "50");
	    compound[0] = 5;
	    compound[1] = "OPT1";
	    factory.set("intkeyed.Value6", 5, "OPT1");
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5, "factoryCreated2");
	    factory.set("intkeyed.Label", 5, "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("intkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("intkeyed.Others", compound);

	    factory.set("unkeyed.Value1", "10");
	    factory.set("unkeyed.Value2", "20");
	    factory.set("unkeyed.Value3", "30");
	    factory.set("unkeyed.Value4", "40");
	    factory.set("unkeyed.Value5", "50");
	    factory.set("unkeyed.Value5a", new GaussianRV(50.0, 1.0));
	    factory.set("unkeyed.Value6", "OPT1");
	    factory.add("unkeyed.Value7", "OPT1");
	    factory.set("unkeyed.Peer", "factoryCreated2");
	    factory.set("unkeyed.Label", "hello");
	    factory.add("unkeyed.Others", "factoryCreated2");
	    factory.add("unkeyed.Others", "factoryCreated3");
	    factory.printKeyed();
	    factory.printUnkeyed();
	    System.out.println("----- clear unkeyed parameters ---------");
	    factory.clear("unkeyed");
	    System.out.println("(expecting default values)");
	    factory.printUnkeyed();

	    factory.clear();
	    System.out.println(" --- make keys a string ----");

	    factory.set("keyed.Value1", "factoryCreated1", 10);
	    factory.set("keyed.Value2", "factoryCreated1", 20);
	    factory.set("keyed.Value3", "factoryCreated1", 30);
	    factory.set("keyed.Value4", "factoryCreated1", 40);
	    factory.set("keyed.Value5", "factoryCreated1", 50);
	    factory.set("keyed.Value5a", "factoryCreated1",
			new GaussianRV(50.0, 1.0));
	    compound[0] = "factoryCreated1";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("keyed.Value6", "factoryCreated1",
			ATestObject2Factory.Option.OPT1);
	    factory.add("keyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("keyed.Value7", compound);
	    factory.set("keyed.Peer", "factoryCreated1", fobj2);
	    factory.set("keyed.Label", "factoryCreated1", "hello");
	    compound[1] = fobj2;
	    factory.add("keyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("keyed.Others", compound);

	    factory.set("optkeyed.Value1", "OPT1", 10);
	    factory.set("optkeyed.Value2", "OPT1", 20);
	    factory.set("optkeyed.Value3", "OPT1", 30);
	    factory.set("optkeyed.Value4", "OPT1", 40);
	    factory.set("optkeyed.Value5", "OPT1", 50);
	    compound[0] = "OPT1";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("optkeyed.Value6", "OPT1",
			ATestObject2Factory.Option.OPT1);
	    factory.add("optkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("optkeyed.Value7", compound);
	    factory.set("optkeyed.Peer", "OPT1",
			fobj2);
	    factory.set("optkeyed.Label", "OPT1",
			"hello");
	    compound[1] = fobj2;
	    factory.add("optkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("optkeyed.Others", compound);

	    factory.set("strkeyed.Value1", "hello", 10);
	    factory.set("strkeyed.Value2", "hello", 20);
	    factory.set("strkeyed.Value3", "hello", 30);
	    factory.set("strkeyed.Value4", "hello", 40);
	    factory.set("strkeyed.Value5", "hello", 50);
	    compound[0] = "hello";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("strkeyed.Value6", "hello",
			ATestObject2Factory.Option.OPT1);
	    factory.add("strkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("strkeyed.Value7", compound);
	    factory.set("strkeyed.Peer", "hello", fobj2);
	    factory.set("strkeyed.Label", "hello", "hello");
	    compound[1] = fobj2;
	    factory.add("strkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("strkeyed.Others", compound);

	    factory.set("intkeyed.Value1", 5, 10);
	    factory.set("intkeyed.Value2", 5, 20);
	    factory.set("intkeyed.Value3", 5, 30);
	    factory.set("intkeyed.Value4", 5, 40);
	    factory.set("intkeyed.Value5", 5, 50);
	    compound[0] = "5";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5, fobj2);
	    factory.set("intkeyed.Label", 5, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed();
	    System.out.println(" ... long numbers:");
	    factory.set("keyed.Value1", "factoryCreated1", 10L);
	    factory.set("keyed.Value2", "factoryCreated1", 20L);
	    factory.set("keyed.Value3", "factoryCreated1", 30L);
	    factory.set("keyed.Value4", "factoryCreated1", 40L);
	    factory.set("keyed.Value5", "factoryCreated1", 50L);
	    factory.set("optkeyed.Value1", "OPT1",
			10L);
	    factory.set("optkeyed.Value2", "OPT1",
			20L);
	    factory.set("optkeyed.Value3", "OPT1",
			30L);
	    factory.set("optkeyed.Value4", "OPT1",
			40L);
	    factory.set("optkeyed.Value5", "OPT1",
			50L);
	    factory.set("strkeyed.Value1", "hello", 10L);
	    factory.set("strkeyed.Value2", "hello", 20L);
	    factory.set("strkeyed.Value3", "hello", 30L);
	    factory.set("strkeyed.Value4", "hello", 40L);
	    factory.set("strkeyed.Value5", "hello", 50L);

	    factory.set("intkeyed.Value1", 5, 10L);
	    factory.set("intkeyed.Value2", 5, 20L);
	    factory.set("intkeyed.Value3", 5, 30L);
	    factory.set("intkeyed.Value4", 5, 40L);
	    factory.set("intkeyed.Value5", 5, 50L);

	    factory.printKeyed();
	    System.out.println(" ... double numbers:");
	    factory.set("keyed.Value1", "factoryCreated1", 10.0);
	    factory.set("keyed.Value2", "factoryCreated1", 20.0);
	    factory.set("keyed.Value3", "factoryCreated1", 30.0);
	    factory.set("keyed.Value4", "factoryCreated1", 40.0);
	    factory.set("keyed.Value5", "factoryCreated1", 50.0);
	    factory.set("optkeyed.Value1", "OPT1",
			10.0);
	    factory.set("optkeyed.Value2", "OPT1",
			20.0);
	    factory.set("optkeyed.Value3", "OPT1",
			30.0);
	    factory.set("optkeyed.Value4", "OPT1",
			40.0);
	    factory.set("optkeyed.Value5", "OPT1",
			50.0);
	    factory.set("strkeyed.Value1", "hello", 10.0);
	    factory.set("strkeyed.Value2", "hello", 20.0);
	    factory.set("strkeyed.Value3", "hello", 30.0);
	    factory.set("strkeyed.Value4", "hello", 40.0);
	    factory.set("strkeyed.Value5", "hello", 50.0);

	    factory.set("intkeyed.Value1", 5, 10.0);
	    factory.set("intkeyed.Value2", 5, 20.0);
	    factory.set("intkeyed.Value3", 5, 30.0);
	    factory.set("intkeyed.Value4", 5, 40.0);
	    factory.set("intkeyed.Value5", 5, 50.0);

	    factory.printKeyed(); factory.clear();


	    System.out.println(" ... string values");

	    factory.set("keyed.Value1", "factoryCreated1", "10");
	    factory.set("keyed.Value2", "factoryCreated1", "20");
	    factory.set("keyed.Value3", "factoryCreated1", "30");
	    factory.set("keyed.Value4", "factoryCreated1", "40");
	    factory.set("keyed.Value5", "factoryCreated1", "50");
	    compound[0] = "factoryCreated1";
	    compound[1] = "OPT1";
	    factory.set("keyed.Value6", "factoryCreated1", "OPT1");
	    factory.add("keyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("keyed.Value7", compound);
	    factory.set("keyed.Peer", "factoryCreated1", "factoryCreated2");
	    factory.set("keyed.Label", "factoryCreated1", "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("keyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("keyed.Others", compound);

	    factory.set("optkeyed.Value1", "OPT1", "10");
	    factory.set("optkeyed.Value2", "OPT1", "20");
	    factory.set("optkeyed.Value3", "OPT1", "30");
	    factory.set("optkeyed.Value4", "OPT1", "40");
	    factory.set("optkeyed.Value5", "OPT1", "50");
	    compound[0] = "OPT1";
	    compound[1] = "OPT1";
	    factory.set("optkeyed.Value6", "OPT1",
			"OPT1");
	    factory.add("optkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("optkeyed.Value7", compound);
	    factory.set("optkeyed.Peer", "OPT1", "factoryCreated2");
	    factory.set("optkeyed.Label", "OPT1", "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("optkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("optkeyed.Others", compound);

	    factory.set("strkeyed.Value1", "hello", "10");
	    factory.set("strkeyed.Value2", "hello", "20");
	    factory.set("strkeyed.Value3", "hello", "30");
	    factory.set("strkeyed.Value4", "hello", "40");
	    factory.set("strkeyed.Value5", "hello", "50");
	    compound[0] = "hello";
	    compound[1] = "OPT1";
	    factory.set("strkeyed.Value6", "hello", "OPT1");
	    factory.add("strkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("strkeyed.Value7", compound);
	    factory.set("strkeyed.Peer", "hello", "factoryCreated2");
	    factory.set("strkeyed.Label", "hello", "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("strkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("strkeyed.Others", compound);

	    factory.set("intkeyed.Value1", "5", "10");
	    factory.set("intkeyed.Value2", "5", "20");
	    factory.set("intkeyed.Value3", "5", "30");
	    factory.set("intkeyed.Value4", "5", "40");
	    factory.set("intkeyed.Value5", "5", "50");
	    compound[0] = "5";
	    compound[1] = "OPT1";
	    factory.set("intkeyed.Value6", "5", "OPT1");
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", "5", "factoryCreated2");
	    factory.set("intkeyed.Label", "5", "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("intkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("intkeyed.Others", compound);

	    factory.printKeyed(); factory.clear();


	    System.out.println("----- int case only ----");

	    /*
	    System.out.println(" ... long keys, int values");
	    factory.set("intkeyed.Value1", 5L, 10);
	    factory.set("intkeyed.Value2", 5L, 20);
	    factory.set("intkeyed.Value3", 5L, 30);
	    factory.set("intkeyed.Value4", 5L, 40);
	    factory.set("intkeyed.Value5", 5L, 50);
	    compound[0] = 5L;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5L, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5L, fobj2);
	    factory.set("intkeyed.Label", 5L, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println(" ... long keys, long value numbers:");

	    factory.set("intkeyed.Value1", 5L, 10L);
	    factory.set("intkeyed.Value2", 5L, 20L);
	    factory.set("intkeyed.Value3", 5L, 30L);
	    factory.set("intkeyed.Value4", 5L, 40L);
	    factory.set("intkeyed.Value5", 5L, 50L);
	    compound[0] = 5L;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5L, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5L, fobj2);
	    factory.set("intkeyed.Label", 5L, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();


	    System.out.println(" ... long keys, double value numbers:");

	    factory.set("intkeyed.Value1", 5L, 10.0);
	    factory.set("intkeyed.Value2", 5L, 20.0);
	    factory.set("intkeyed.Value3", 5L, 30.0);
	    factory.set("intkeyed.Value4", 5L, 40.0);
	    factory.set("intkeyed.Value5", 5L, 50.0);
	    compound[0] = 5L;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5L, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5L, fobj2);
	    factory.set("intkeyed.Label", 5L, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println(" ... long keys, string value numbers:");

	    factory.set("intkeyed.Value1", 5L, "10");
	    factory.set("intkeyed.Value2", 5L, "20");
	    factory.set("intkeyed.Value3", 5L, "30");
	    factory.set("intkeyed.Value4", 5L, "40");
	    factory.set("intkeyed.Value5", 5L, "50");
	    compound[0] = 5L;
	    compound[1] = "OPT1";
	    factory.set("intkeyed.Value6", 5L, "OPT1");
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5L, "factoryCreated2");
	    factory.set("intkeyed.Label", 5L, "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("intkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();
	    */
	    /*
	    System.out.println(" ... double keys, int values");
	    factory.set("intkeyed.Value1", 5.0, 10);
	    factory.set("intkeyed.Value2", 5.0, 20);
	    factory.set("intkeyed.Value3", 5.0, 30);
	    factory.set("intkeyed.Value4", 5.0, 40);
	    factory.set("intkeyed.Value5", 5.0, 50);
	    compound[0] = 5.0;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5.0, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5.0, fobj2);
	    factory.set("intkeyed.Label", 5.0, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println(" ... double keys, long value numbers:");

	    factory.set("intkeyed.Value1", 5.0, 10L);
	    factory.set("intkeyed.Value2", 5.0, 20L);
	    factory.set("intkeyed.Value3", 5.0, 30L);
	    factory.set("intkeyed.Value4", 5.0, 40L);
	    factory.set("intkeyed.Value5", 5.0, 50L);
	    compound[0] = 5.0;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5.0, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5.0, fobj2);
	    factory.set("intkeyed.Label", 5.0, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();


	    System.out.println(" ... double keys, double value numbers:");

	    factory.set("intkeyed.Value1", 5.0, 10.0);
	    factory.set("intkeyed.Value2", 5.0, 20.0);
	    factory.set("intkeyed.Value3", 5.0, 30.0);
	    factory.set("intkeyed.Value4", 5.0, 40.0);
	    factory.set("intkeyed.Value5", 5.0, 50.0);
	    compound[0] = 5.0;
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", 5.0, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5.0, fobj2);
	    factory.set("intkeyed.Label", 5.0, "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println(" ... double keys, string value numbers:");

	    factory.set("intkeyed.Value1", 5.0, "10");
	    factory.set("intkeyed.Value2", 5.0, "20");
	    factory.set("intkeyed.Value3", 5.0, "30");
	    factory.set("intkeyed.Value4", 5.0, "40");
	    factory.set("intkeyed.Value5", 5.0, "50");
	    compound[0] = 5.0;
	    compound[1] = "OPT1";
	    factory.set("intkeyed.Value6", 5.0, "OPT1");
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", 5.0, "factoryCreated2");
	    factory.set("intkeyed.Label", 5.0, "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("intkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();
	    */


	    System.out.println(" ... string keys, int values");
	    factory.set("intkeyed.Value1", "5", 10);
	    factory.set("intkeyed.Value2", "5", 20);
	    factory.set("intkeyed.Value3", "5", 30);
	    factory.set("intkeyed.Value4", "5", 40);
	    factory.set("intkeyed.Value5", "5", 50);
	    compound[0] = "5";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", "5", ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", "5", fobj2);
	    factory.set("intkeyed.Label", "5", "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println(" ... string keys, long value numbers:");

	    factory.set("intkeyed.Value1", "5", 10L);
	    factory.set("intkeyed.Value2", "5", 20L);
	    factory.set("intkeyed.Value3", "5", 30L);
	    factory.set("intkeyed.Value4", "5", 40L);
	    factory.set("intkeyed.Value5", "5", 50L);
	    compound[0] = "5";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", "5",
			ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", "5", fobj2);
	    factory.set("intkeyed.Label", "5", "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();


	    System.out.println(" ... string keys, double value numbers:");

	    factory.set("intkeyed.Value1", "5", 10.0);
	    factory.set("intkeyed.Value2", "5", 20.0);
	    factory.set("intkeyed.Value3", "5", 30.0);
	    factory.set("intkeyed.Value4", "5", 40.0);
	    factory.set("intkeyed.Value5", "5", 50.0);
	    compound[0] = "5";
	    compound[1] = ATestObject2Factory.Option.OPT1;
	    factory.set("intkeyed.Value6", "5",
			ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = ATestObject2Factory.Option.OPT2;
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", "5", fobj2);
	    factory.set("intkeyed.Label", "5", "hello");
	    compound[1] = fobj2;
	    factory.add("intkeyed.Others", compound);
	    compound[1] = fobj3;
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println(" ... string keys, string value numbers:");

	    factory.set("intkeyed.Value1", "5", "10");
	    factory.set("intkeyed.Value2", "5", "20");
	    factory.set("intkeyed.Value3", "5", "30");
	    factory.set("intkeyed.Value4", "5", "40");
	    factory.set("intkeyed.Value5", "5", "50");
	    compound[0] = "5";
	    compound[1] = "OPT1";
	    factory.set("intkeyed.Value6", "5", "OPT1");
	    factory.add("intkeyed.Value7", compound);
	    compound[1] = "OPT2";
	    factory.add("intkeyed.Value7", compound);
	    factory.set("intkeyed.Peer", "5", "factoryCreated2");
	    factory.set("intkeyed.Label", "5", "hello");
	    compound[1] = "factoryCreated2";
	    factory.add("intkeyed.Others", compound);
	    compound[1] = "factoryCreated3";
	    factory.add("intkeyed.Others", compound);
	    factory.printKeyed(); factory.clear();

	    System.out.println();
	    System.out.println("--- test three-argument 'add' method ---");

	    factory.clear();
	    factory.add("intkeyed.Value7", 5, "OPT1");
	    factory.add("intkeyed.Value7", "6", "OPT2");
	    factory.add("intkeyed.Value7" , 7L, "OPT3");
	    factory.add("intkeyed.Value7", 8.0, "OPT1");
	    factory.add("intkeyed.Value7", 15, ATestObject2Factory.Option.OPT1);
	    factory.add("intkeyed.Value7", "16",
			ATestObject2Factory.Option.OPT2);
	    factory.add("intkeyed.Value7", 17L,
			ATestObject2Factory.Option.OPT3);
	    factory.add("intkeyed.Value7", 18.0,
			ATestObject2Factory.Option.OPT1 );
	    factory.printKeyed();
	    System.out.println(" ... now remove each entry that we added:");
	    System.out.println("     (affects only intkeyed.Value7 entries)");
	    factory.remove("intkeyed.Value7", 5, "OPT1");
	    factory.remove("intkeyed.Value7", "6", "OPT2");
	    factory.remove("intkeyed.Value7" , 7L, "OPT3");
	    factory.remove("intkeyed.Value7", 8.0, "OPT1");
	    factory.remove("intkeyed.Value7", 15,
			   ATestObject2Factory.Option.OPT1);
	    factory.remove("intkeyed.Value7", "16",
			ATestObject2Factory.Option.OPT2);
	    factory.remove("intkeyed.Value7", 17L,
			ATestObject2Factory.Option.OPT3);
	    factory.remove("intkeyed.Value7", 18.0,
			ATestObject2Factory.Option.OPT1);
	    factory.printKeyed();
	    System.out.println(" ... now remove all that match the main key");
	    factory.remove("intkeyed", 5);
	    factory.remove("intkeyed", "6");
	    factory.remove("intkeyed" , 7L);
	    factory.remove("intkeyed", 8.0);
	    factory.remove("intkeyed", 15);
	    factory.remove("intkeyed", "16");
	    factory.remove("intkeyed", 17L);
	    factory.remove("intkeyed", 18.0);

	    factory.printKeyed(); factory.clear();

	    System.out.println("Now try the unkeyed Parmset field");
	    System.out.println("... string valued values");
	    factory.set("unkeyed.Value1", "10");
	    factory.set("unkeyed.Value2", "20");
	    factory.set("unkeyed.Value3", "30");
	    factory.set("unkeyed.Value4", "40");
	    factory.set("unkeyed.Value5", "50");
	    factory.set("unkeyed.Value6", "OPT1");
	    factory.add("unkeyed.Value7", "OPT1");
	    factory.set("unkeyed.Peer", "factoryCreated2");
	    factory.set("unkeyed.Label", "hello");
	    factory.add("unkeyed.Others", "factoryCreated2");
	    factory.add("unkeyed.Others", "factoryCreated3");
	    factory.printUnkeyed(); factory.clear();
	    System.out.println("...  int values");
	    factory.set("unkeyed.Value1", 10);
	    factory.set("unkeyed.Value2", 20);
	    factory.set("unkeyed.Value3", 30);
	    factory.set("unkeyed.Value4", 40);
	    factory.set("unkeyed.Value5", 50);
	    factory.set("unkeyed.Value6", ATestObject2Factory.Option.OPT1);
	    factory.add("unkeyed.Value7", ATestObject2Factory.Option.OPT1);
	    factory.set("unkeyed.Peer", fobj2);
	    factory.set("unkeyed.Label", "hello");
	    factory.add("unkeyed.Others", fobj2);
	    factory.add("unkeyed.Others", fobj3);
	    factory.printUnkeyed(); factory.clear();

	    System.out.println("...  double values");
	    factory.set("unkeyed.Value1", 10.0);
	    factory.set("unkeyed.Value2", 20.0);
	    factory.set("unkeyed.Value3", 30.0);
	    factory.set("unkeyed.Value4", 40.0);
	    factory.set("unkeyed.Value5", 50.0);
	    factory.set("unkeyed.Value5a", new GaussianRV(50.0, 1.0));
	    factory.set("unkeyed.Value6", ATestObject2Factory.Option.OPT1);
	    factory.add("unkeyed.Value7", ATestObject2Factory.Option.OPT1);
	    factory.set("unkeyed.Peer", fobj2);
	    factory.set("unkeyed.Label", "hello");
	    factory.add("unkeyed.Others", fobj2);
	    factory.add("unkeyed.Others", fobj3);
	    factory.printUnkeyed(); factory.clear();

	    System.out.println("Now try keyed primitive case");
	    factory.set("kprim1",fobj1, 10);
	    factory.set("kprim2", fobj1, new UniformIntegerRV(1,10));
	    factory.set("kprim3", fobj1,
			new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					       new UniformIntegerRV(10, 15)));
	    factory.set("kprim4", fobj1, 20.0);
	    factory.set("kprim5", fobj1, new GaussianRV(30.0, 1.0));
	    factory.set("kprim5a", fobj1, new GaussianRV(30.0, 1.0));
	    factory.set("kprim6", fobj1, "OPT1");
	    factory.set("kprim7", fobj1, fobj1);
	    factory.set("kprim8", fobj1, "hello");
	    factory.add("kprim9", fobj1, fobj2);
	    factory.add("kprim9", fobj1, fobj3);

	    factory.remove("kprim9", fobj1, fobj2);
	    Map<ATestObject2,HashSet<ATestObject2>> kprimMap9 =
		factory.getKprimMap9();
	    if (kprimMap9.get(fobj1).contains(fobj2)) {
		System.out.println(fobj2.getName()
				   + " still in kprimMap9's set for "
				   + fobj1.getName());
		System.exit(1);
	    }
	    factory.remove("kprim9", fobj1);
	    if (kprimMap9.containsKey(fobj1)) {
		if (kprimMap9.get(fobj1).contains(fobj3)) {
		    System.out.println(fobj3.getName()
				       + " still in kprimMap9's set for "
				       + fobj1.getName());
		}
		System.out.println(fobj1.getName() + " still in kprimMap9");
		System.exit(1);
	    }
	    factory.add("kprim9", fobj1, fobj2);
	    factory.add("kprim9", fobj1, fobj3);
	    if (!(kprimMap9.get(fobj1).contains(fobj2)
		  && kprimMap9.get(fobj1).contains(fobj3))) {
		System.out.println("reinsert failed");
		System.exit(1);
	    }

	    factory.set("kprimInt1",25, 10);
	    factory.set("kprimInt2", 25, new UniformIntegerRV(1,10));
	    factory.set("kprimInt3", 25,
			new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					       new UniformIntegerRV(10, 15)));
	    factory.set("kprimInt4", 25, 20.0);
	    factory.set("kprimInt5", 25, new GaussianRV(30.0, 1.0));
	    factory.set("kprimInt5a", 25, new GaussianRV(30.0, 1.0));
	    factory.set("kprimInt6", 25, "OPT1");
	    factory.set("kprimInt7", 25, fobj1);
	    factory.set("kprimInt8", 25, "hello");
	    factory.add("kprimInt9", 25, fobj2);
	    factory.add("kprimInt9", 25, fobj3);

	    factory.set("kprimStr1","strkey", 10);
	    factory.set("kprimStr2", "strkey", new UniformIntegerRV(1,10));
	    factory.set("kprimStr3", "strkey",
			new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					       new UniformIntegerRV(10, 15)));
	    factory.set("kprimStr4", "strkey", 20.0);
	    factory.set("kprimStr5", "strkey", new GaussianRV(30.0, 1.0));
	    factory.set("kprimStr5a", "strkey", new GaussianRV(30.0, 1.0));
	    factory.set("kprimStr6", "strkey", "OPT1");
	    factory.set("kprimStr7", "strkey", fobj1);
	    factory.set("kprimStr8", "strkey", "hello");
	    factory.add("kprimStr9", "strkey", fobj2);
	    factory.add("kprimStr9", "strkey", fobj3);


	    factory.set("kprimOpt1",ATestObject2Factory.Option.OPT1, 10);
	    factory.set("kprimOpt2", ATestObject2Factory.Option.OPT1,
			new UniformIntegerRV(1,10));
	    factory.set("kprimOpt3", ATestObject2Factory.Option.OPT1,
			new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					       new UniformIntegerRV(10, 15)));
	    factory.set("kprimOpt4", ATestObject2Factory.Option.OPT1, 20.0);
	    factory.set("kprimOpt5", ATestObject2Factory.Option.OPT1,
			new GaussianRV(30.0, 1.0));
	    factory.set("kprimOpt5a", ATestObject2Factory.Option.OPT1,
			new GaussianRV(30.0, 1.0));
	    factory.set("kprimOpt6", ATestObject2Factory.Option.OPT1, "OPT1");
	    factory.set("kprimOpt7", ATestObject2Factory.Option.OPT1, fobj1);
	    factory.set("kprimOpt8", ATestObject2Factory.Option.OPT1, "hello");
	    factory.add("kprimOpt9", ATestObject2Factory.Option.OPT1, fobj2);
	    factory.add("kprimOpt9", ATestObject2Factory.Option.OPT1, fobj3);
	    factory.add("kprimOptSet", ATestObject2Factory.Option.OPT1,
			ATestObject2Factory.Option.OPT2);
	    factory.add("kprimOptSet", ATestObject2Factory.Option.OPT1,
			ATestObject2Factory.Option.OPT3);

	    factory.printKprims(); factory.clear();

	    factory.set("kprim1","factoryCreated1", 10);
	    factory.set("kprim2", "factoryCreated1",
			new UniformIntegerRV(1,10));
	    factory.set("kprim3", "factoryCreated1",
			new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					       new UniformIntegerRV(10, 15)));
	    factory.set("kprim4", "factoryCreated1", 20.0);
	    factory.set("kprim5", "factoryCreated1", new GaussianRV(30.0, 1.0));
	    factory.set("kprim5a", "factoryCreated1",new GaussianRV(30.0, 1.0));
	    factory.set("kprim6", "factoryCreated1", "OPT1");
	    factory.set("kprim7", "factoryCreated1", fobj1);
	    factory.set("kprim8", "factoryCreated1", "hello");
	    factory.add("kprim9", "factoryCreated1", fobj2);
	    factory.add("kprim9", "factoryCreated1", fobj3);
	    factory.printKprims(); factory.clear();

	    factory.set("kprimOpt1","OPT1", 10);
	    factory.set("kprimOpt2", "OPT1",
			new UniformIntegerRV(1,10));
	    factory.set("kprimOpt3", "OPT1",
			new UniformIntegerRVRV(new UniformIntegerRV(0, 10),
					       new UniformIntegerRV(10, 15)));
	    factory.set("kprimOpt4", "OPT1", 20.0);
	    factory.set("kprimOpt5", "OPT1",
			new GaussianRV(30.0, 1.0));
	    factory.set("kprimOpt5a", "OPT1",
			new GaussianRV(30.0, 1.0));
	    factory.set("kprimOpt6", "OPT1", "OPT1");
	    factory.set("kprimOpt7", "OPT1", fobj1);
	    factory.set("kprimOpt8", "OPT1", "hello");
	    factory.add("kprimOpt9", "OPT1", fobj2);
	    factory.add("kprimOpt9", "OPT1", fobj3);
	    factory.add("kprimOptSet", ATestObject2Factory.Option.OPT1,
			ATestObject2Factory.Option.OPT1);
	    factory.add("kprimOptSet", ATestObject2Factory.Option.OPT1,
			ATestObject2Factory.Option.OPT3);

	    factory.printKprims(); factory.clear();

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	if (factory.canAdd3("intkeyed.Value7") == false) {
	    System.out.println("canAdd3 returned false");
	    System.exit(1);
	}
	System.out.println();
	System.out.println("---------------------");
	System.out.println();
	System.out.println("Now try gfactory (uses generics)");

	ATestObject2GF<Integer,HashSet<Integer>> gfactory
	    = new ATestObject2GF<>(anamer);

	gfactory.set("value1", 30);
	gfactory.set("value2", 40);
	gfactory.set("value3", new UniformIntegerRV(0, true, 10, true));

	ATestObject2 gobj = gfactory.createObject("gobject");
	System.out.println(gobj.getName() + ": value1 = " +gobj.getValue1());
	System.out.println();
	System.out.print("Object names:");
	cnt = 0;
	for (String name: anamer.getObjectNames()) {
	    if (((cnt++) % 10) == 0) System.out.println();
	    System.out.print(" " + name);
	}
	System.out.println();

	System.out.println();
	System.out.println("---------------------");
	System.out.println();
	System.out.println("now try TestFactory, for extending tables");
	System.out.println("The test object is not actually initialized - "
			   + "instead the factory\n fields are printed");
	System.out.println();

	TestFactory tf = new TestFactory(anamer);

	tf.set("object.x", 1, 10.0);
	tf.set("object.y", 1, 20.0);
	tf.set("object.u", 1, 30.0);
	tf.set("object.u", 1, 40.0);
	tf.set("object.x", 2, 100.0);
	tf.set("object.y", 2, 200.0);
	tf.set("object.u", 2, 300.0);
	tf.set("object.u", 2, 400.0);
	System.out.println("tf1 ...");
	tf.createObject("tf1");
	tf.remove("object", 1);
	System.out.println("tf2 ...");
	tf.createObject("tf2");
	tf.add("object", 1);
	System.out.println("tf3 ...");
	tf.createObject("tf3");
	tf.clear();
	System.out.println("tf cleared ... now add a single entry");
	tf.set("object.x", 3, 1000.0);
	System.out.println("tf4 ...");
	tf.createObject("tf4");
	System.out.println("now clear and create a new object tf5 ...");
	tf.clear("object");
	tf.createObject("tf5");

	BasicStroke stroke = new BasicStrokeBuilder()
	    .setWidth(1.5)
	    .setCap(BasicStrokeParm.Cap.ROUND)
	    .setJoin(BasicStrokeParm.Join.BEVEL)
	    .setMiterLimit(10.0)
	    .setDashIncr(10.0)
	    .setDashPattern("-")
	    .createStroke();

	System.exit(0);
    }
}
