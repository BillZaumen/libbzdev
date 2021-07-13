package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelperEight",
		localHelper = "AnoHelperEight1")
public class AnoDispatchEight1 extends AnoDispatchEight {
    static {
	AnoHelperEight1.register();
    }
    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelperEight")
	public void doDispatch(AnoMessage1 msg1, 
			       AnoMessage1 msg2) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
    }
}
