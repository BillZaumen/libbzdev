package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="dmtest.a.AnoHelperSeven", localHelper="AnoHelperSeven2")
public class AnoDispatchSeven2 extends AnoDispatchSeven1 {
    static {
	AnoHelperSeven2.register();
    }

    public boolean countMode = false;
    long count2;
    public void fastDispatch(AnoMessage msg1,
			     int index,
			     AnoMessage msg2,
			     AnoMessage... array) 
	throws Exception
    {
	if (msg1 instanceof AnoMessage2 && msg2 instanceof AnoMessage2) {
	    doDispatch((AnoMessage2) msg1, index, (AnoMessage2) msg2, array);
	}
    }

    @DMethodImpl("dmtest.a.AnoHelperSeven")
    public Integer doDispatch(AnoMessage2 msg1,
			   int index,
			   AnoMessage2 msg2,
			   AnoMessage... array) 
	throws Exception
    {
	if (countMode) count2++;
	else System.out.println ("handled AnoMessage2 (index = " +index +")");

	if (index == 4) 
	    throw new Exception("exception thrown because second argument = 4");
	
	return Integer.valueOf(2);
    }

    public static void main (String argv[]) throws Exception {
	AnoDispatchSeven d1 = new AnoDispatchSeven1();
	AnoDispatchSeven d2 = new AnoDispatchSeven2();
	AnoMessage msg1 = new AnoMessage1();
	AnoMessage msg2 = new AnoMessage2();
	try {
	    System.out.println("result = " +d1.dispatch(msg1, 1, msg1, msg1));
	    System.out.println("result = " +d2.dispatch(msg1, 2, msg1, msg1));
	    System.out.println("result = " +d2.dispatch(msg2, 3, msg2));
	    System.out.println("result = " +d2.dispatch(msg2, 4, msg2, msg2));
	} catch (Exception e) {
	    System.out.println(e.getMessage() 
			       + " (expected when 2nd arg = 4)");
	}
	System.exit(0);
    }
}
