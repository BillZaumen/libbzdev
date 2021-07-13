import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="Test1Helper",
		localHelper="Test1LocalHelper")
public class TestClass1 {

    static {
	Test1LocalHelper.register();
    }

    @DynamicMethod("Test1Helper")
    public void test(Object x, Object y) {
	try {
	    Test1Helper.getHelper().dispatch(this, x, y);
	} catch (MethodNotPresentException e) {
	    System.out.println("TestClass1: Object Object");
	}
    }
    
    @DMethodImpl("Test1Helper")
    public void doTest(String x, Object y) {
	System.out.println("TestClass1: String Object");
    }

    @DMethodImpl("Test1Helper")
    public void doTest(Object x, String y) {
	System.out.println("TestClass1: Object String");
    }
}
