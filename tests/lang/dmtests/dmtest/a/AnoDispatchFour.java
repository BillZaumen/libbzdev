package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatchFour  {
    @DynamicMethod("AnoHelperFour")
    // @DMethodOptions(lockingMode=DMethodOptions.Locking.NONE)
    public final void dispatch(AnoMessage msg1,
			       int index,
			       AnoMessage msg2,
			       AnoMessage... array) {
	AnoHelperFour.getHelper().dispatch(this, msg1, index, msg2, array);
    }
}
