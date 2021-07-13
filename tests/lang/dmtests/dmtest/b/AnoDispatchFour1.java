package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelperFour",
		localHelper = "AnoHelperFour1")
public class AnoDispatchFour1 extends AnoDispatchFour {
    static {
	AnoHelperFour1.register();
    }
    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelperFour")
	public void doDispatch(AnoMessage1 msg1, 
			       int index,
			       AnoMessage1 msg2,
			       AnoMessage... array) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
    }
}
