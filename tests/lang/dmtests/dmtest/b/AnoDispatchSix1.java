package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelperSix", localHelper = "AnoHelperSix1")
public class AnoDispatchSix1 extends AnoDispatchSix {
    static {
	AnoHelperSix1.register();
    }
    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelperSix")
    public int doDispatch(AnoMessage1 msg) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
	return 1;
    }
}

