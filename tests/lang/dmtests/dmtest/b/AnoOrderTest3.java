package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoOrderTest3Helper",
		localHelper = "AnoOrderTest3Helper")
public class AnoOrderTest3 extends dmtest.a.AnoOrderTest3 {
    static {
	AnoOrderTest3Helper.register();
    }
    public boolean countMode = false;
    long count1 = 0;

    @DMethodImpl("dmtest.a.AnoOrderTest3Helper")
    void doDispatch1(Double number, AnoMessage msg, boolean flag) {
	if (countMode) count1++;
	else  System.out.println (number + " is first argument");
    }

    @DMethodImpl("dmtest.a.AnoOrderTest3Helper")
    void doDispatch1(Integer number, AnoMessage msg, boolean flag) {
	if (countMode) count1++;
	else  System.out.println (number + " is first argument");
    }

    public static void main (String argv[]) throws Exception {
	AnoOrderTest3 d3 = new AnoOrderTest3();
	Double dval = Double.valueOf(10.0);
	Integer ival = Integer.valueOf(20);
	Long lval = Long.valueOf(30);
	AnoMessage msg = new AnoMessage1();
	d3.dispatch(dval, msg, true);
	d3.dispatch(ival, msg, true);
	d3.dispatch(lval, msg, true);
	System.exit(0);
    }
}
