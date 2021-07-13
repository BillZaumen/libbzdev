package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatchSeven  {
    @DynamicMethod("AnoHelperSeven")
    public final Integer dispatch(AnoMessage msg1,
				      int index,
				      AnoMessage msg2,
				      AnoMessage... array) 
	throws Exception
    {
	return AnoHelperSeven.getHelper().dispatch(this,
						   msg1, index, msg2, array);
    }
}
