package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatchSix  {
    @DynamicMethod("AnoHelperSix")
    public final int dispatch(AnoMessage msg) {
	return AnoHelperSix.getHelper().dispatch(this, msg);
    }
}
