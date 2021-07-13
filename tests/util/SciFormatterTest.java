import org.bzdev.util.*;
import org.bzdev.graphs.*;
import java.util.*;

public class SciFormatterTest {
    public static void main(String argv[]) {
	try {
	    SciFormatter formatter = new SciFormatter();
	    Object[] array = {Double.valueOf(10.0), Double.valueOf(20.0)};
	    formatter.format("%.2f %.2f", array);
	    System.out.println(formatter.toString());

	    double tests[] = {
		20000.0, .0000002, 1.0, 9.0, -20000.0, -0.0000002, 40.0, 10.0
	    };

	    formatter = new SciFormatter();
	    formatter.format("printing an int but passed a double: %d\n", 20.0);
	    try {
		formatter.format("printing an int but passed a double: %d\n",
				 20.2);
		System.out.println("exception not thrown");
		System.exit(1);
	    } catch (Exception e) {
		System.out.println("exception expected and caught");
	    }

	    System.out.println(formatter.toString());
	    formatter = new SciFormatter();
	    for (int i = 0; i < tests.length; i++) {
		formatter.format("%#5.3g\n", tests[i]);
	    }
	    System.out.println(formatter.toString());
	    System.out.println("again but with both formats and using '$':");
	    formatter = new SciFormatter();
	    for (int i = 0; i < tests.length; i++) {
		System.out.format("%1$5.3g %1$5.3e\n", tests[i]);
		formatter.format("%1$#5.3g %1$#5.3e\n", tests[i]);
	    }
	    System.out.println("results:");
	    System.out.println(formatter.toString());
	    System.out.println("again but with more formatting:");
	    formatter = new SciFormatter();
	    for (int i = 0; i < tests.length; i++) {
		System.out.format("case %d: x = %2$5.3e, x= %2$#5.3e\n",
				  i+1, tests[i]);
		formatter.format("case %d: x = %2$5.3e, x= %2$#5.3e\n", 
				 i+1, tests[i]);
	    }
	    System.out.println(formatter.toString());
	    System.out.println("now with '<':");
	    formatter = new SciFormatter();
	    for (int i = 0; i < tests.length; i++) {
		formatter.format("case %d: x = %5.3e, x= %<#5.3e\n", 
				 i+1, tests[i]);
		formatter.format("case %d: x = %#5.3e, x= %<5.3e\n", 
				 i+1, tests[i]);
	    }
	    System.out.println(formatter.toString());

	    formatter = new SciFormatter();
	    System.out.println("now try with some using '$' and some not:");
	    formatter.format("1 2 3 = %3$d %d %1$d", 3, 2, 1);
	    System.out.println(formatter.toString());

	    formatter = new SciFormatter();
	    System.out.println("locale tests, using France as the locale:");
	    formatter.format(Locale.FRANCE, "%.2f (SciFormatter)", 10.0);
	    System.out.println(formatter.toString());
	    Formatter uformatter = new Formatter();
	    System.out.println("locale tests, using France as the locale:");
	    uformatter.format(Locale.FRANCE, "%.2f (Formatter)", 10.0);
	    System.out.println(uformatter.toString());

	    System.out.println("Now try the %#E cases:");
	    double data[] = {
		-100.0,
		-10.0,
		-1.0,
		-0.5,
		0.0,
		0.5,
		1.0,
		10.0,
		100.0,
	    };
	    for (int i = 0; i < data.length; i++) {
		formatter = new SciFormatter();
		formatter.format("%#3.0E", data[i]);
		System.out.println(formatter.toString());
	    }
	    for (int i = 0; i < data.length; i++) {
		formatter = new SciFormatter();
		formatter.format("%#3.0E for %3.0g", data[i], data[i]);
		System.out.println(formatter.toString());
	    }
	    for (int i = 0; i < data.length; i++) {
		formatter = new SciFormatter();
		formatter.format("%#-3.0E for %-3.0g", data[i], data[i]);
		System.out.println(formatter.toString());
	    }

	    formatter = new SciFormatter();
	    formatter.format("%g", 0.0);
	    System.out.println(formatter.toString());

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
