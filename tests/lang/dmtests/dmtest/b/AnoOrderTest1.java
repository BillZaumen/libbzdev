package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoOrderTestHelper",
		localHelper = "AnoOrderTest1Helper")
public class AnoOrderTest1 extends AnoOrderTest {
    static {
	AnoOrderTest1Helper.register();
    }
    public boolean countMode = false;
    long count1 = 0;

    @DMethodImpl("dmtest.a.AnoOrderTestHelper")
    public void doDispatch1(AnoMessage1 msg1, AnoMessage msg2) {
	if (countMode) count1++;
	else  System.out.println ("AnoMessage1 is first argument");
    }

    @DMethodImpl("dmtest.a.AnoOrderTestHelper")
    public void doDispatch2(AnoMessage msg1, AnoMessage1 msg2) {
	if (countMode) count1++;
	else  System.out.println ("AnoMessage1 is second argument");
    }

    public static void main (String argv[]) throws Exception {
	AnoOrderTest1 d1 = new AnoOrderTest1();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	d1.dispatch(msg1, msg1);
	d1.dispatch(msg2, msg2);
	System.exit(0);
    }
}
