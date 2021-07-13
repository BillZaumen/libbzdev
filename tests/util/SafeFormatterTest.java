import org.bzdev.util.*;
import java.util.*;

public class SafeFormatterTest {
    public static void main(String argv[]) {
	try {
	    SafeFormatter formatter = new SafeFormatter();

	    double tests[] = {
		20000.0, .0000002, 1.0, 9.0, -20000.0, -0.0000002, 40.0, 10.0
	    };

	    String[] stests = new String[tests.length];
	    for (int i = 0; i < tests.length; i++) {
		stests[i] = "" + tests[i];
	    }

	    for (int i = 0; i < tests.length; i++) {
		formatter.format("%#5.3g\n", tests[i]);
		formatter.format("%#5.3g\n", stests[i]);
	    }
	    System.out.println(formatter.toString());
	    System.out.println("again but with both formats and using '$':");
	    formatter = new SafeFormatter();
	    for (int i = 0; i < tests.length; i++) {
		formatter.format("%1$5.3g %1$5.3e\n", tests[i]);
		formatter.format("%1$5.3g %1$5.3e\n", stests[i]);
	    }
	    System.out.println(formatter.toString());
	    System.out.println("again but with more formatting:");
	    formatter = new SafeFormatter();
	    String format = "case %d: x = %2$5.3e, x= %2$5.3e\n";
	    for (int i = 0; i < tests.length; i++) {
		System.out.println("\"" + format + "\"");
		formatter.format(format, i+1, tests[i]);
		System.out.println("\"" + SafeFormatter.modify(format) + "\"");
		formatter.format(format, i+1, stests[i]);
	    }
	    System.out.println(formatter.toString());
	    System.out.println("now with '<':");
	    formatter = new SafeFormatter();
	    format = "case %d: x = %5.3e, x= %<5.3e\n";
	    for (int i = 0; i < tests.length; i++) {
		System.out.println("\"" + format + "\"");
		formatter.format(format, i+1, tests[i]);
		System.out.println("\"" + SafeFormatter.modify(format) + "\"");
		formatter.format(format, i+1, stests[i]);
	    }
	    System.out.println(formatter.toString());
	    System.out.println("last format contains "
			       + SafeFormatter.getDirectiveCount(format)
			       + " directives");
	    System.out.println("format counts for "
			       + "\"%%%n%<s%<,8.3g%4$,3.8g\""
			       + " = " 
			       + SafeFormatter.getDirectiveCount
			       ("%%%n%<s%<,8.3g%4$,3.8g"));
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
