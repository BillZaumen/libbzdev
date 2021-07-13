import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="Test1Helper",
		localHelper="Test3LocalHelper")
public class TestClass3 extends TestClass1 {

    static {
	Test3LocalHelper.register();
    }

    @DMethodImpl("Test1Helper")
    public void doTest4(Object x, Object y) {
	System.out.println("TestClass3: Object Object");
    }
}
