import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="Test2Helper",
		localHelper="Test2LocalHelper")
public class TestClass2 {

    static {
	Test2LocalHelper.register();
    }

    @DynamicMethod("Test2Helper")
    @DMethodOrder({2,1})
    public void test(Object x, Object y) {
	try {
	    Test2Helper.getHelper().dispatch(this, x, y);
	} catch (MethodNotPresentException e) {
	    System.out.println("TestClass2: Object Object");
	}
    }
    
    @DMethodImpl("Test2Helper")
    public void doTest(String x, Object y) {
	System.out.println("TestClass2: String Object");
    }

    @DMethodImpl("Test2Helper")
    public void doTest(Object x, String y) {
	System.out.println("TestClass2: Object String");
    }
}
