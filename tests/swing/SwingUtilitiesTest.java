import java.awt.*;
import javax.swing.*;

public class SwingUtilitiesTest {

    public static void main(String argv[]) throws Exception {

	if (argv.length > 0 && argv[0].equals("--useToolkit")) {
	    Toolkit.getDefaultToolkit();
	}

	System.setSecurityManager(new SecurityManager());

	System.out.println("hello");
	SwingUtilities.invokeAndWait(() -> {
		System.out.println("goodbye");
	    });
    }
}
