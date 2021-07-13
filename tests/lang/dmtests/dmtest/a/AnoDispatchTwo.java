package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatchTwo  {
    @DynamicMethod("AnoHelperTwo")
    @DMethodOptions(limitFactor=2)
    public final void dispatch(AnoMessage msg1, AnoMessage msg2 ) {
	AnoHelperTwo.getHelper().dispatch(this, msg1, msg2);
    }
}
