package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperFour", localHelper="AnoHelperFour2")
public class AnoDispatchFour2 extends AnoDispatchFour1 {
    static {
	AnoHelperFour2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg1,
			     int index,
			     AnoMessage msg2,
			     AnoMessage... array) {
	if (msg1 instanceof AnoMessage2 && msg2 instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg1, index, (AnoMessage2) msg2, array);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelperFour")
    public void doDispatch(AnoMessage2 msg1,
			   int index,
			   AnoMessage2 msg2,
			   AnoMessage... array) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2 (index = " +index +")");
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchFour d1 = new AnoDispatchFour1();
	AnoDispatchFour d2 = new AnoDispatchFour2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();

	if (argv.length == 0) {
	    d1.dispatch(msg1, 1, msg1, msg1);
	    d2.dispatch(msg1, 2, msg1, msg1);
	    d2.dispatch(msg2, 3, msg2);
	} else {
	    int n = Integer.parseInt(argv[0]);
	    ((AnoDispatchFour2)d2).countMode = true;
	    int i;
	    for (i = 0; i < n; i++) {
		((AnoDispatchFour2)d2). fastDispatch(msg2, i, msg2);
	    }
	    for (i = 0; i < n; i++) {
		d2.dispatch(msg2, i, msg2);
	    }
	    // now time it
	    long timestamp = System.currentTimeMillis();
	    for (i = 0; i < n; i++) {
		((AnoDispatchFour2)d2). fastDispatch(msg2, i, msg2);
	    }
	    long interval1 = System.currentTimeMillis() - timestamp;

	    timestamp = System.currentTimeMillis();
	    for (i = 0; i < n; i++) {
		d2.dispatch(msg2, i, msg2);
	    }
	    long interval2 = System.currentTimeMillis() - timestamp;
	    System.out.println(((interval2 * 100) /interval1) / 100.0);
	}
	System.exit(0);
    }
}
