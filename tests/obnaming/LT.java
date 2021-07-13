import org.bzdev.obnaming.*;
import org.bzdev.util.TemplateProcessor;

public class LT {
    public static void main(String argv[]) throws Exception  {

	if (argv.length == 0) {
	    System.out.println("launchers:");
	    for (String name: ObjectNamerLauncher.getLauncherNames()) {
		System.out.println("    " + name);
	    }

	    System.out.println("launcher-data:");
	    for (String name: ObjectNamerLauncher.getLauncherDataNames()) {
		System.out.println("    " + name);
	    }

	    TemplateProcessor.KeyMap keymap =
		ObjectNamerLauncher.getProviderKeyMap();

	    keymap.print();
	    System.exit(0);
	}

	final String lname = argv[0];
	final String dname = argv[1];
	try {
	    ObjectNamerLauncher launcher =
		ObjectNamerLauncher.newInstance(lname, dname);
	    System.out.println("created launcher for " + lname + "," +  dname);
	} catch (Exception e) {
	    System.out.println("could not create a "
			       + "launcher for "
			       + lname + "," + dname + ":");
	    System.out.println("    ... " + e.getClass() + ": "
			       +e.getMessage());
	    e.printStackTrace();
	}
    }
}
