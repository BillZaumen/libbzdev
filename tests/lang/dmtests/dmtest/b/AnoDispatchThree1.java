package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelperThree",
		localHelper = "AnoHelperThree1")
public class AnoDispatchThree1 extends AnoDispatchThree {
    static {
	AnoHelperThree1.register();
    }
    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelperThree")
	public void doDispatch(AnoMessage1 msg1, int index,
			       AnoMessage[] array, AnoMessage1 msg2) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
    }
}
