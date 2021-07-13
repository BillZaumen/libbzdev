package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoOrderTest4  {
    @DynamicMethod("AnoOrderTest4Helper")
    @DMethodOrder({4,3,2,1})
    public final void dispatch(AnoMessage msg1, AnoMessage msg2,
			       AnoMessage msg3, AnoMessage msg4) {
	try {
	    AnoOrderTest4Helper.getHelper().dispatch(this, msg1, msg2,
						    msg3, msg4);
	} catch (MethodNotPresentException e) {
	    System.out.println("[no matching method found]");
	}
    }
}
