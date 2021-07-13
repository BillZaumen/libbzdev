package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatch  {
    @DynamicMethod("AnoHelper")
    // @DMethodOptions(lockingMode=DMethodOptions.Locking.NONE)
    @DMethodOptions(limitFactor=2)
    public final void dispatch(AnoMessage msg) {
	AnoHelper.getHelper().dispatch(this, msg);
    }
}
