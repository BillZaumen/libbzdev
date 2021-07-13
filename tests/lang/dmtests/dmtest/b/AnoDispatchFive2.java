package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperFive", localHelper="AnoHelperFive2")
public class AnoDispatchFive2 extends AnoDispatchFive1 {
    static {
	AnoHelperFive2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg) {
	if (msg instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelperFive")
    public void doDispatch(AnoMessage2 msg) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2");
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchFive d1 = new AnoDispatchFive1();
	AnoDispatchFive d2 = new AnoDispatchFive2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	try {
	    d1.dispatch(msg1);
	    d2.dispatch(msg1);
	    d2.dispatch(msg2);
	} catch (Exception e) {
	    System.out.println("exception caught");
	}
	System.exit(0);
    }
}
