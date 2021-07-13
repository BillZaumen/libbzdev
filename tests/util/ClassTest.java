import org.bzdev.util.*;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

public class ClassTest {
    
    public static void main(String argv[]) throws Exception {
	ClassSorter classSorter  = new ClassSorter();
	classSorter.addKey(Integer.class);
	classSorter.addKey(Long.class);
	classSorter.addKey(Double.class);
	classSorter.addKey(Float.class);
	classSorter.addKey(Number.class);

	for (Class<?> clasz: classSorter.createList()) {
	    System.out.println(clasz);
	}

	ClassArraySorter cas = new ClassArraySorter();
	cas.addKey(new ClassArraySorter.Key(new Class<?>[] {Long.class}));
		   
	LinkedList<ClassArraySorter.Key> list = cas.createList();
	ClassArraySorter.Key key = list.get(0);

	cas = new ClassArraySorter();
	cas.addKey(new ClassArraySorter.Key(new Class<?>[] {Long.class}));
	list = cas.createList(true);
	cas.addKey(new ClassArraySorter.Key(new Class<?>[] {Double.class}));
	System.out.println("check createList(true)");
	list = cas.createList(true);
	key = list.get(0);
	System.out.println(key);
	key = list.get(1);
	System.out.println(key);

	ClassArraySorter.Key testkey = new ClassArraySorter.Key(new Class<?>[] {
		Integer.class
	});
	if (key.isAssignableFrom(testkey)) throw new Exception();

	if (!key.isAssignableFrom(testkey, true)) throw new Exception();

	key = new ClassArraySorter.Key(new Class<?>[] {
		Runnable.class});

	ClassArraySorter.Key testkey1 =
	    new ClassArraySorter.Key(new Class<?>[] {
		    ExpressionParser.ESPFunction.class},
		true, new int[] {0});

	ClassArraySorter.Key testkey2 =
	    new ClassArraySorter.Key(new Class<?>[] {
		    ExpressionParser.ESPFunction.class},
		true, new int[] {ClassArraySorter.NO_ARGCOUNT_TEST});

	ClassArraySorter.ArgCountMap rmap = new ClassArraySorter.ArgCountMap();
	rmap.put("run", 0);

	ClassArraySorter.ArgCountMap maps[] = {rmap};

	ClassArraySorter.Key testkey3 =
	    new ClassArraySorter.Key(new Class<?>[] {
		    ExpressionParser.ESPObject.class},
		true, new int[] {ClassArraySorter.INTERFACE_TEST}, maps);


	if (!key.isAssignableFrom(testkey1)) {
	    throw new Exception();
	}

	if (key.isAssignableFrom(testkey2)) {
	    throw new Exception();
	}

	System.out.println("... checking testkey3 against a Runnable");
	if (!key.isAssignableFrom(testkey3)) {
	    throw new Exception();
	}

	key = new ClassArraySorter.Key(new Class<?>[] {
		Appendable.class});

	if (key.isAssignableFrom(testkey1)) {
	    throw new Exception();
	}

	if (key.isAssignableFrom(testkey2)) {
	    throw new Exception();
	}

	System.out.println("... checking testkey3 against an Appendable");
	if (key.isAssignableFrom(testkey3)) {
	    throw new Exception();
	}

	key = new ClassArraySorter.Key(new Class<?>[] {
		String.class});

	System.out.println("... checking testkey3 against a String");
	if (key.isAssignableFrom(testkey3)) {
	    throw new Exception();
	}

	cas = new ClassArraySorter();

	Class<?>[] array1 = {Number.class, Number.class, Number[].class};
	Class<?>[] array2 = {Number.class, Number.class, Double[].class};
	Class<?>[] array3 = {Integer.class, Long.class, Double[].class};
	key = new ClassArraySorter.Key(array1);
	key.varargsMode();
	cas.addKey(key);
	key = new ClassArraySorter.Key(array2);
	key.varargsMode();
	cas.addKey(key);
	key = new ClassArraySorter.Key(array3);
	key.varargsMode();
	cas.addKey(key);
	list = cas.createList();
	for (ClassArraySorter.Key k: list) {
	    System.out.print("key: ");
	    for (Class<?>clasz: k.toArray()) {
		System.out.print(" " + clasz.getName());
	    }
	    System.out.println();
	}

	testkey = new ClassArraySorter.Key(new Class<?>[] {
		Integer.class, Long.class
	});
	System.out.println();
	System.out.print("testkey: ");
	for (Class<?>clasz: testkey.toArray()) {
	    System.out.print(" " + clasz.getName());
	}
	System.out.println();
	for (ClassArraySorter.Key k: list) {
	    System.out.println(k.isAssignableFrom(testkey));
	}
	System.out.println();

	testkey = new ClassArraySorter.Key(new Class<?>[] {
		Double.class, Double.class
	});
	System.out.println();
	System.out.print("testkey: ");
	for (Class<?>clasz: testkey.toArray()) {
	    System.out.print(" " + clasz.getName());
	}
	System.out.println();
	for (ClassArraySorter.Key k: list) {
	    System.out.println(k.isAssignableFrom(testkey));
	}
	System.out.println();


	testkey = new ClassArraySorter.Key(new Class<?>[] {
		Integer.class, Long.class, Double.class, Double.class
	});
	System.out.println();
	System.out.print("testkey: ");
	for (Class<?>clasz: testkey.toArray()) {
	    System.out.print(" " + clasz.getName());
	}
	System.out.println();
	for (ClassArraySorter.Key k: list) {
	    System.out.println(k.isAssignableFrom(testkey));
	}
	System.out.println();

	testkey = new ClassArraySorter.Key(new Class<?>[] {
		Integer.class, Long.class, Double.class, Integer.class
	});
	System.out.print("testkey: ");
	for (Class<?>clasz: testkey.toArray()) {
	    System.out.print(" " + clasz.getName());
	}
	System.out.println();
	for (ClassArraySorter.Key k: list) {
	    System.out.println(k.isAssignableFrom(testkey));
	}
	System.out.println();

	testkey = new ClassArraySorter.Key(new Class<?>[] {
		Integer.class, Double.class, Double.class, Integer.class
	});
	System.out.print("testkey: ");
	for (Class<?>clasz: testkey.toArray()) {
	    System.out.print(" " + clasz.getName());
	}
	System.out.println();
	for (ClassArraySorter.Key k: list) {
	    System.out.println(k.isAssignableFrom(testkey));
	}
	System.out.println();

	testkey = new ClassArraySorter.Key(new Class<?>[] {
		Integer.class, Double.class, Double.class, Double.class
	});
	System.out.print("testkey: ");
	for (Class<?>clasz: testkey.toArray()) {
	    System.out.print(" " + clasz.getName());
	}
	System.out.println();
	for (ClassArraySorter.Key k: list) {
	    System.out.println(k.isAssignableFrom(testkey));
	}
    }
}
