package pkg;
import java.io.*;
import org.bzdev.lang.MethodNotPresentException;
import org.bzdev.lang.annotations.*;

@DMethodContext(helper="DMHelper", localHelper="TestDMHelper")
public class Test extends DM {
    static {
	TestDMHelper.register();
    }

    @DMethodImpl("DMHelper")
    public void ourprint(String s) {
	System.out.println("Test: " + s);
    }
    
    public static void main(String argv[]) throws IOException {
	DM testPrinter = new Test();
	// dispatching of dynamic methods will result in
	// this method calling ourprint().
	testPrinter.print("hello");
    }
}
