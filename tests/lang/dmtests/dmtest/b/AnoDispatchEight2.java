package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperEight", localHelper="AnoHelperEight2")
public class AnoDispatchEight2 extends AnoDispatchEight1 {
    static {
	AnoHelperEight2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg1,
			     AnoMessage msg2) {
	if (msg1 instanceof AnoMessage2 && msg2 instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg1, (AnoMessage2) msg2);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelperEight")
    public void doDispatch(AnoMessage2 msg1,
			   AnoMessage2 msg2) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2");
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchEight d1 = new AnoDispatchEight1();
	AnoDispatchEight d2 = new AnoDispatchEight2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();

	if (argv.length == 0) {
	    d1.dispatch(msg1, msg1);
	    d2.dispatch(msg1, msg1);
	    d2.dispatch(msg2, msg2);
	} else {
	    int n = Integer.parseInt(argv[0]);
	    ((AnoDispatchEight2)d2).countMode = true;
	    int i;
	    for (i = 0; i < n; i++) {
		((AnoDispatchEight2)d2).fastDispatch(msg2, msg2);
	    }
	    for (i = 0; i < n; i++) {
		d2.dispatch(msg2, msg2);
	    }
	    // now time it
	    long timestamp = System.currentTimeMillis();
	    for (i = 0; i < n; i++) {
		((AnoDispatchEight2)d2).fastDispatch(msg2, msg2);
	    }
	    long interval1 = System.currentTimeMillis() - timestamp;

	    timestamp = System.currentTimeMillis();
	    for (i = 0; i < n; i++) {
		d2.dispatch(msg2, msg2);
	    }
	    long interval2 = System.currentTimeMillis() - timestamp;
	    System.out.println(((interval2 * 100) /interval1) / 100.0);
	}
	System.exit(0);
    }
}
