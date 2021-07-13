package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoOrderTest4Helper",
		localHelper = "AnoOrderTest4Helper")
public class AnoOrderTest4 extends dmtest.a.AnoOrderTest4 {
    static {
	AnoOrderTest4Helper.register();
    }
    public boolean countMode = false;
    long count1 = 0;

    @DMethodImpl("dmtest.a.AnoOrderTest4Helper")
    public void doDispatch1(AnoMessage1 msg1, AnoMessage msg2,
			    AnoMessage msg3, AnoMessage msg4) {
	if (countMode) count1++;
	else  System.out.println ("AnoMessage1 is first argument");
    }

    @DMethodImpl("dmtest.a.AnoOrderTest4Helper")
    public void doDispatch2(AnoMessage msg1, AnoMessage msg2,
			    AnoMessage msg3, AnoMessage1 msg4) {
	if (countMode) count1++;
	else  System.out.println ("AnoMessage1 is forth argument");
    }

    public static void main (String argv[]) throws Exception {
	AnoOrderTest4 d1 = new AnoOrderTest4();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	d1.dispatch(msg1, msg1, msg1, msg1);
	d1.dispatch(msg2, msg2, msg2, msg2);
	System.exit(0);
    }
}
