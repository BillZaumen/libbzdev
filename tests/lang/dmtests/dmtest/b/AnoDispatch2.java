package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelper", localHelper="AnoHelper2")
public class AnoDispatch2 extends AnoDispatch1 {
    // was commented out, but added because we cannot use
    // DMClassLoader with Java 11
    static {
	AnoHelper2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg) {
	if (msg instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelper")
    public void doDispatch(AnoMessage2 msg) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2");
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatch d1 = new AnoDispatch1();
	AnoDispatch d2 = new AnoDispatch2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	if (argv.length == 0) {
	    d1.dispatch(msg1);
	    d2.dispatch(msg1);
	    d2.dispatch(msg2);
	} else {
	    int n = Integer.parseInt(argv[0]);
	    ((AnoDispatch2)d2).countMode = true;
	    int i;
	    // repeat to give hotspot compiler a change to optimize
	    // code.
	    for (i = 0; i < n; i++) {
		((AnoDispatch2)d2). fastDispatch(msg2);
	    }
	    for (i = 0; i < n; i++) {
		d2.dispatch(msg2);
	    }
	    // now time it
	    long timestamp = System.currentTimeMillis();
	    for (i = 0; i < n; i++) {
		((AnoDispatch2)d2). fastDispatch(msg2);
	    }
	    long interval1 = System.currentTimeMillis() - timestamp;

	    timestamp = System.currentTimeMillis();
	    for (i = 0; i < n; i++) {
		d2.dispatch(msg2);
	    }
	    long interval2 = System.currentTimeMillis() - timestamp;
	    System.out.println(((interval2 * 100) /interval1) / 100.0);
	}
	System.exit(0);
    }
}
