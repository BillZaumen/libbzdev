package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoOrderTest  {
    @DynamicMethod("AnoOrderTestHelper")
    @DMethodOrder({2,1})
    public final void dispatch(AnoMessage msg1, AnoMessage msg2 ) {
	try {
	    AnoOrderTestHelper.getHelper().dispatch(this, msg1, msg2);
	} catch (MethodNotPresentException e) {
	    System.out.println("msg1 instanceof " + msg1.getClass());
	    System.out.println("msg2 instanceof " + msg2.getClass());
	    System.out.println("[no matching method found]");
	}
    }
}
