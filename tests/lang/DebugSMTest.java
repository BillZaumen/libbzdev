import org.bzdev.lang.*;
import java.io.*;
import java.security.Permission;

public class DebugSMTest {

    public static void main(String argv[]) {
	SecurityManager sm = null;
	try {
	    if (argv.length == 0) {
		NullSecurityManager nsm = new NullSecurityManager();
		nsm.setOutput(System.out);
		sm = nsm;
	    } else if (argv[0].equals("sm")) {
		sm = new SecurityManager();
	    } else if (argv[0].equals("tsm")) {
		TracingSecurityManager tsm = new TracingSecurityManager();
		sm = tsm;
		tsm.setOutput(System.out);
		if (argv.length > 1) {
		    int limit = Integer.parseInt(argv[1]);
		    tsm.setNestingLimit(limit);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.out.println("Trying case " 
			   +((argv.length == 0)? "null security manager":
			     argv[0]));
	System.out.println("(sm = " + sm + ")");
	try {
	    System.setSecurityManager(sm);
	    File file = new File("DebugSMTest.java");
	    sm.checkPackageAccess("org.bzdev.io");
	    FileInputStream is = new FileInputStream(file);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(2);
	}
	System.exit(0);
    }
}