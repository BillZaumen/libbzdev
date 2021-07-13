import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="Test1Helper", localHelper="Test1LHelper")
public class Test1 {
    
    static {
	DMethodParameters.setDefaultCacheLimit(2);
	Test1LHelper.register();
    }

    @DynamicMethod("Test1Helper") @DMethodOptions(traceMode=true)
    public void test(Object obj) {
	try {
	    Test1Helper.getHelper().dispatch(this, obj);
	} catch (MethodNotPresentException e) {
	    System.out.println("found Object");
	}
    }

    @DMethodImpl("Test1Helper")
    public void doTest(Number number) {
	System.out.println("Found a " + number.getClass().getName());
    }

    public static void main(String argv[]) throws Exception {
	// very small cache so we can can fill it to capacity

	Object objects[] = {
	    Double.valueOf(10.0),
	    Integer.valueOf(20),
	    Float.valueOf(30.0F),
	    Long.valueOf(40)
	};
	Test1 test1 = new Test1();
	for (Object object: objects) {
	    test1.test(object);
	}
	System.exit(0);
    }
}
