package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelperFive", 
		localHelper = "AnoHelperFive1")
public class AnoDispatchFive1 extends AnoDispatchFive {
    static {
	AnoHelperFive1.register();
    }
    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelperFive")
    public void doDispatch(AnoMessage1 msg) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
    }
}

