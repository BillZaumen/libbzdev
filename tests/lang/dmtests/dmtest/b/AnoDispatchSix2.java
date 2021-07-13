package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperSix", localHelper="AnoHelperSix2")
public class AnoDispatchSix2 extends AnoDispatchSix1 {
    static {
	AnoHelperSix2.register();
    }

    public boolean countMode = false;
    long count2;
    public int fastDispatch(AnoMessage msg) {
	if (msg instanceof AnoMessage2) {
	    return doDispatch((AnoMessage2) msg);
	}
	return 0;
    }

    @DMethodImpl("dmtest.a.AnoHelperSix")
    public int doDispatch(AnoMessage2 msg) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2");
	return 2;
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchSix d1 = new AnoDispatchSix1();
	AnoDispatchSix d2 = new AnoDispatchSix2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	System.out.println ("returned " +d1.dispatch(msg1));
	System.out.println ("returned " +d2.dispatch(msg1));
	System.out.println ("returned "	+d2.dispatch(msg2));
	System.exit(0);
    }
}
