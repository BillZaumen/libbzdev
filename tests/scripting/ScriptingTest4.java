import org.bzdev.scripting.*;
import org.bzdev.devqsim.Simulation;
import org.bzdev.drama.DramaSimulation;
import java.io.*;

public class ScriptingTest4 {
    
    public static interface Listener {
	void method1(String name, int status);
    }

    public static class Adapter extends ScriptListenerAdapter
	implements Listener
    {
	public Adapter() {
	    super(null, null);
	}

	public Adapter(ScriptingContext sc, Object scriptObject) {
	    super(sc, scriptObject);
	}

	public void method1(String name, int status) {
	    callScriptMethod("method1", status);
	}
    }

    // taken from a simulation where the behavior was eratic using Rhino.
    public static class Hub {
	static int i = 0;
	String name;
	public Hub() {
	    i++;
	    name = "hub-" + i;
	}
	public String getName() {return name;}
    }

    public static interface TripDataListener {
	void tripStarted(long tripID, double time, long ticks, Hub hub);
	void tripEnded(long tripID, double time, long ticks, Hub hub);
	void tripFailedAtStart(long tripID, double time, long ticks, Hub hub);
	void tripFailedMidstream(long tripID, double time, long ticks, Hub hub);
    }
    public static class TripDataAdapter extends ScriptListenerAdapter
	implements TripDataListener
    {
	public TripDataAdapter() {
	    super(null,null);
	}
	public TripDataAdapter(ScriptingContext sc, Object scriptObject) {
	    super(sc, scriptObject);
	}
	@Override
	public void tripStarted(long tripID, double time, long ticks, Hub hub)
	{
	    callScriptMethod("tripStarted", tripID, time, ticks, hub);
	}

	@Override
	public void tripEnded(long tripID, double time, long ticks, Hub hub)
	{
	    callScriptMethod("tripEnded", tripID, time, ticks, hub);
	}

	@Override
	public void tripFailedAtStart(long tripID, double time, long ticks,
				      Hub hub)
	{
	    callScriptMethod("tripFailedAtStart", tripID, time, ticks, hub);
	}

	@Override
	public void tripFailedMidstream(long tripID,
					double time, long ticks,
					Hub hub)
	{
	    callScriptMethod("tripFailedMidstream", tripID, time, ticks, hub);
	}
    }

    public static void main(String argv[]) {
	try {
	    int ind = 0;
	    SecurityManager sm = null;
	    boolean trusted = false;
	    while (ind < argv.length && argv[ind].startsWith("-")) {
		if (argv[ind].equals("--sandbox")) {
		    sm = new SecurityManager();
		} else if (argv[ind].equals("--scriptSandbox")) {
		    sm = new ScriptingSecurityManager();
		} else if (argv[ind].equals("--trusted")) {
		    sm = new ScriptingSecurityManager();
		    trusted = true;
		}
		ind++;
	    }

	    int index = argv[ind].lastIndexOf('.');
	    String languageName =  null;
	    if (index > -1) {
		String ext = argv[ind].substring(index+1);
		languageName = Scripting.getLanguageNameByExtension(ext);
		System.out.println("language = " + languageName
				   + " for file-name extension " + ext);
	    }

	    FileReader reader = new FileReader(argv[ind]);
	    if (sm != null) {
		try {
		    System.setSecurityManager(sm);
		} catch (UnsupportedOperationException eu) {
		    // OpenJDK 18 &  later don't support security
		    // managers
		    System.exit(0);
		}
	    }
	    
	    ScriptingContext sc = new DefaultScriptingContext(languageName,
							      trusted);
	    sc.putScriptObject("scripting", sc);
	    sc.evalScript(argv[ind], reader);

	    Object obj = sc.getScriptObject("listener");;
	    if (obj instanceof Listener) {
		Listener listener = (Listener) obj;
		listener.method1("test", 10);
	    } else {
		throw new Exception("listener is not an instance of Listener");
	    }
	    obj = sc.getScriptObject("tl");
	    if (obj == null) {
		throw new Exception("tl was null");
	    }
	    if (obj instanceof String) {
		String s = (String) obj;
		if (s.equals("test string")) {
		    System.out.println("test string found");
		} else {
		    throw new Exception("tl was not the string we expected");
		}
	    } else if (obj instanceof TripDataAdapter) {
		Hub hub = new Hub();
		TripDataAdapter tl = (TripDataAdapter) obj;
		tl.tripStarted(23L, 200.0, 200000L, hub);
	    } else {
		throw new Exception("tl not an instance of TripDataAdapter: "
				    +obj.getClass());
	    }

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.exit(1);
	}
	System.exit(0);
    }
}
