package dmtest.b;
import dmtest.a.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper = "dmtest.a.AnoHelper", localHelper = "AnoHelper1")
public class AnoDispatch1 extends AnoDispatch {

    // was commented out, but added because we cannot use
    // DMClassLoader with Java 11
    static {
	AnoHelper1.register();
    }

    public boolean countMode = false;
    long count1 = 0;
    @DMethodImpl("dmtest.a.AnoHelper")
    public void doDispatch(AnoMessage1 msg) {
	if (countMode) count1++;
	else  System.out.println ("handled AnoMessage1");
    }
}

