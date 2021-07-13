package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatchFive  {
    @DynamicMethod("AnoHelperFive")
    public final void dispatch(AnoMessage msg) 
	throws Exception
    {
	AnoHelperFive.getHelper().dispatch(this, msg);
    }
}
