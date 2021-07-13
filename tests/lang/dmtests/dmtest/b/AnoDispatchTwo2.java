package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperTwo", localHelper="AnoHelperTwo2")
public class AnoDispatchTwo2 extends AnoDispatchTwo1 {
    static {
	AnoHelperTwo2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg1, AnoMessage msg2) {
	if (msg1 instanceof AnoMessage2 && msg2 instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg1, (AnoMessage2) msg2);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelperTwo")
    public void doDispatch(AnoMessage2 msg1, AnoMessage2 msg2) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2");
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchTwo d1 = new AnoDispatchTwo1();
	AnoDispatchTwo d2 = new AnoDispatchTwo2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	d1.dispatch(msg1, msg1);
	d2.dispatch(msg1, msg1);
	d2.dispatch(msg2, msg2);
	System.exit(0);
    }
}
