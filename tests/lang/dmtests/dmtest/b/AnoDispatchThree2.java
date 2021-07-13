package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperThree", localHelper="AnoHelperThree2")
public class AnoDispatchThree2 extends AnoDispatchThree1 {
    static {
	AnoHelperThree2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg1,
			     int index,
			     AnoMessage[] array,
			     AnoMessage msg2) {
	if (msg1 instanceof AnoMessage2 && msg2 instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg1, index, array, (AnoMessage2) msg2);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelperThree")
    public void doDispatch(AnoMessage2 msg1,
			   int index,
			   AnoMessage[] array,
			   AnoMessage2 msg2) {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2 (index = " +index +")");
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchThree d1 = new AnoDispatchThree1();
	AnoDispatchThree d2 = new AnoDispatchThree2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	d1.dispatch(msg1, 1, null, msg1);
	d2.dispatch(msg1, 2, null, msg1);
	d2.dispatch(msg2, 3, null,  msg2);
	System.exit(0);
    }
}
