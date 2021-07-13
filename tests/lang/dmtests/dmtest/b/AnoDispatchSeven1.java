package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelperSeven",
		localHelper = "AnoHelperSeven1")
public class AnoDispatchSeven1 extends AnoDispatchSeven {
    static {
	AnoHelperSeven1.register();
    }
    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelperSeven")
	public Integer doDispatch(AnoMessage1 msg1, 
				  int index,
				  AnoMessage1 msg2,
				  AnoMessage... array) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
	return Integer.valueOf(1);
    }
}
