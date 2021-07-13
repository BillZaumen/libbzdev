package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoOrderTest3  {
    @DynamicMethod("AnoOrderTest3Helper")
    @DMethodOrder({1, 0, 0})
    public final void dispatch(Number number, AnoMessage msg, boolean flag) {
	try {
	    AnoOrderTest3Helper.getHelper().dispatch(this, number, msg, flag);
	} catch (MethodNotPresentException e) {
	    System.out.println("[no matching method found]");
	}
    }
}
