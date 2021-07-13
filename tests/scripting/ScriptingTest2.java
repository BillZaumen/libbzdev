import org.bzdev.scripting.*;
import org.bzdev.devqsim.Simulation;
import org.bzdev.drama.DramaSimulation;
import java.io.*;

public class ScriptingTest2 {
    public static void main(String argv[]) {
	try {
	    int ind = 0;
	    SecurityManager sm = null;
	    boolean dramaNotRemoved = true;
	    while (ind < argv.length && argv[ind].startsWith("-")) {
		System.out.println("options contain " + argv[ind]);
		if (argv[ind].equals("--sandbox")) {
		    sm = new SecurityManager();
		} else if (argv[ind].equals("--scriptSandbox")) {
		    sm = new ScriptingSecurityManager();
		}
		ind++;
	    }
	    System.out.println("[options processed]");
	    FileReader reader = new FileReader(argv[ind]);

	    if (sm != null) {
		System.out.println("setting security manager...");
		System.setSecurityManager(sm);
		System.out.println("... security manager set");
	    }
	    
	    int index = argv[ind].lastIndexOf('.');
	    String languageName =  null;
	    if (index > -1) {
		String ext = argv[ind].substring(index+1);
		languageName = Scripting.getLanguageNameByExtension(ext);
		System.out.println("language = " + languageName
				   + " for file-name extension " + ext);
	    }
	    ScriptingContext sc = new DefaultScriptingContext(languageName);

	    sc.putScriptObject("root", sc);
	    Simulation sim = new Simulation(sc);

	    try {
		DramaSimulation dsim = new DramaSimulation(sc);
		System.out.println("Drama constructor succeeded as expected");
	    } catch (SecurityException e) {
		System.out.println("dsim constructor failed unexpectedly");
		System.exit(1);
	    }
	    sim.evalScript(argv[ind], reader);
	    
	} catch (Exception e) {
	    Throwable t = e;
	    String indent = "";
	    while (t != null) {
		System.out.format("%s%s: %s\n", indent,
				  t.getClass(), t.getMessage());
		for (StackTraceElement ste: t.getStackTrace()) {
		    System.out.format("%s%s\n", indent, ste);
		}
		System.out.format("%s---------\n", indent);
		indent = indent + "  ";
		t = t.getCause();
	    }

	    System.exit(1);
	}
	System.exit(0);
    }
}
