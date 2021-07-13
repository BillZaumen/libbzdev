import org.bzdev.util.*;
import java.util.jar.*;
import java.util.*;

public class ClassSorterTest {

    public static void main (String argv[]) {
	try {
	    JarFile jarFile = new JarFile(argv[0]);
	    Enumeration<JarEntry> enumeration = jarFile.entries();

	    ClassSorter cs = new ClassSorter();

	    System.out.println("... adding classes");
	    while (enumeration.hasMoreElements()) {
		JarEntry entry = enumeration.nextElement();
		String name = entry.getName();
		if (!name.startsWith("java/")) continue;
		if (name.contains("$")) continue;
		String className = name.replaceAll("/", ".");
		int ind = className.lastIndexOf(".class");
		if (ind >= 0) {
		    className = className.substring(0, ind);
		}
		cs.addKey(Class.forName(className));
	    }
	    System.out.println("... generating list");
	    LinkedList<Class<?>> list = cs.createList();
	    
	    System.out.println("... testing list");

	    int i = 0;
	    for(Class<?> c1: list) {
		int j = 0;
		for (Class<?> c2: list) {
		    if (j >= i) break;
		    if (c2.isAssignableFrom(c1)) {
			System.out.println(c2.getName() + " is assignable from "
					   + c1.getName()
					   + ", j = " + j
					   + ", i = " +  i);
		    }
		    j++;
		}
		i++;
	    }

	    System.out.println("--------- short test ---------");
	    
	    Comparator<Class<?>> comparator = cs.getComparator();

	    cs = new ClassSorter();
	    cs.addKey(Class.forName("java.lang.StringBuilder"));
	    cs.addKey(Class.forName("java.lang.AbstractStringBuilder"));
	    cs.addKey(Class.forName("java.lang.Number"));
	    cs.addKey(Class.forName("java.lang.Double"));
	    list = cs.createList();
	    for (Class<?> cl: list) {
		System.out.println(cl.getName());
	    }
	    
	    System.out.println("--------- ClassArraySorter test ---------");

	    Class<?>[][] classArrays = {
		{StringBuilder.class, StringBuilder.class},
		{StringBuilder.class,
		 Class.forName("java.lang.AbstractStringBuilder")},
		{StringBuilder.class, Double.class},
		{StringBuilder.class, Number.class},
		{Class.forName("java.lang.AbstractStringBuilder"),
		 StringBuilder.class},
		{Class.forName("java.lang.AbstractStringBuilder"),
		 Class.forName("java.lang.AbstractStringBuilder")},
		{Class.forName("java.lang.AbstractStringBuilder"),
		 Double.class},
		{Class.forName("java.lang.AbstractStringBuilder"),
		 Number.class},
		{Double.class, StringBuilder.class},
		{Double.class,
		 Class.forName("java.lang.AbstractStringBuilder")},
		{Double.class, Double.class},
		{Double.class, Number.class},
		{Number.class, StringBuilder.class},
		{Number.class,
		 Class.forName("java.lang.AbstractStringBuilder")},
		{Number.class, Double.class},
		{Number.class, Number.class}
	    };
	    
	    ClassArraySorter cas = new ClassArraySorter();

	    for (Class<?>[] ca: classArrays) {
		cas.addKey(new ClassArraySorter.Key(ca));
	    }
	    LinkedList<ClassArraySorter.Key> calist = cas.createList();

	    i = 0;
	    for (ClassArraySorter.Key key: calist) {
		System.out.println(key);
		int j = 0;
		for (ClassArraySorter.Key key2: calist) {
		    if (j < i) {
			if (key2.isAssignableFrom(key)) {
			    System.out.println("... " + key2
					       + " not assignable from key");
			}
		    }
		    j++;
		}
		i++;
	    }
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
   }
}
