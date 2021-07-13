import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="Test1Helper",
		localHelper="Test1LocalHelper")
public class TestClass1 {

    static {
	Test1LocalHelper.register();
    }

    @DynamicMethod("Test1Helper") @DMethodOptions(traceMode=true)
    public void test(Object x, Object y) {
	try {
	    System.out.println("... calling dispatch");
	    Test1Helper.getHelper().dispatch(this, x, y);
	} catch (MethodNotPresentException e) {
	    System.out.println("TestClass1 (default): found Object Object");
	}
    }
    
    @DMethodImpl("Test1Helper")
    public void doTest1(String x, Object y) {
	System.out.println("TestClass1: String Object");
    }

    @DMethodImpl("Test1Helper")
    public void doTest2(Object x, String y) {
	System.out.println("TestClass1: Object String");
    }
}
