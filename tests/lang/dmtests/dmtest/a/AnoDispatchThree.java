package dmtest.a;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public class AnoDispatchThree  {
    @DynamicMethod("AnoHelperThree")
	public final void dispatch(AnoMessage msg1,
				   int index,
				   AnoMessage[] array,
				   AnoMessage msg2 ) {
	AnoHelperThree.getHelper().dispatch(this, msg1, index, array, msg2);
    }
}
