import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="STest1Helper", localHelper="STest1LHelper")
public class STest1 {

    static {
	STest1LHelper.register();
    }

    @DynamicMethod("STest1Helper")
    public void test(Object obj) {
	try {
	    STest1Helper.getHelper().dispatch(this, obj);
	} catch(MethodNotPresentException e) {
	    System.out.println("found Object");
	}
    }

    @DMethodImpl("STest1Helper")
    protected void doTest(Number number) {
	System.out.println("found a Number");
    }
}
