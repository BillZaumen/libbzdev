import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="Test2Helper", localHelper="Test2LHelper")
public class Test2 {
    
    static {
	DMethodParameters.setDefaultCacheLimit(2);
	Test2LHelper.register();
    }

    @DynamicMethod("Test2Helper") @DMethodOptions(traceMode=true)
    public void test(Object obj1, Object obj2) {
	try {
	    Test2Helper.getHelper().dispatch(this, obj1, obj2);
	} catch (MethodNotPresentException e) {
	    System.out.println("found Object, Object");
	}
    }

    @DMethodImpl("Test2Helper")
    public void doTest(Number number1, Number number2) {
	System.out.println("Found  " + number1.getClass().getName()
			   + ", "
			   + number2.getClass().getName());
    }

    public static void main(String argv[]) throws Exception {
	// very small cache so we can can fill it to capacity

	Object objects[] = {
	    Double.valueOf(10.0),
	    Integer.valueOf(20),
	    Float.valueOf(30.0F),
	    Long.valueOf(40)
	};
	Test2 test2 = new Test2();
	Double object2 = new Double(50.0);
	for (Object object: objects) {
	    test2.test(object, object2);
	}
	System.exit(0);
    }
}
