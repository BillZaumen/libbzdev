import org.bzdev.util.*;
import java.util.*;

public class VarFormatterTest {
    public static void main(String argv[]) throws Exception {
	VarArgsFormatter formatter = new VarArgsFormatter(System.out);
	formatter.setStrictMode(false);

	formatter.format("%d\n", 20.0);
	formatter.format("%d\n", 3);
	formatter.format("%g\n", 4.5);
	formatter.format("%s\n", "hello");

	formatter.format("%d %d\n", 20.0, 3);
	formatter.format("%d %d\n", 2000000000.00, 3);
	formatter.format("%g %<d\n", 20.0);
	formatter.flush();

	try {
	    formatter.format("%d\n", 2.5);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	formatter.setStrictMode(true);
	try {
	    formatter.format("%d\n", 2.5);
	    System.out.println("expected exception not thrown");
	    System.exit(1);
	} catch (Exception e) {
	}
	System.exit(0);
    }
}