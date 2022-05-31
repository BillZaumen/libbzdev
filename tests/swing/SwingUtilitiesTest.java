import java.awt.*;
import javax.swing.*;

public class SwingUtilitiesTest {

    public static void main(String argv[]) throws Exception {

	if (argv.length > 0 && argv[0].equals("--useToolkit")) {
	    Toolkit.getDefaultToolkit();
	}

	try {
	    System.setSecurityManager(new SecurityManager());
	} catch (UnsupportedOperationException eu) {}

	System.out.println("hello");
	SwingUtilities.invokeAndWait(() -> {
		System.out.println("goodbye");
	    });
	Thread.sleep(300000L);
	System.exit(0);
    }
}
