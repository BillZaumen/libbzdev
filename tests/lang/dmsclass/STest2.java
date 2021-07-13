import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="STest1Helper", localHelper="STest2LHelper")
public class STest2 extends STest1 {
    static {
	STest2LHelper.register();
    }

    @DMethodImpl("STest1Helper")
    protected void doTest(Double x) {
	super.doTest(x);
	System.out.println("Number is a Double");
    }
}
