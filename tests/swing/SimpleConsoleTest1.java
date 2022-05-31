import org.bzdev.swing.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleConsoleTest1 {
    static SimpleJTextPane tc;
    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeAndWait(new Runnable () {
		public void run() {
		    tc = new SimpleJTextPane();
		    tc.setSwingSafe(true);
		    JFrame frame = new JFrame("TextPane Test");
		    Container fpane = frame.getContentPane();
		    frame.addWindowListener(new WindowAdapter () {
			    public void windowClosing(WindowEvent e) {
				System.exit(0);
			    }
			});
		    frame.setSize(700,400);
		    JScrollPane scrollpane = new JScrollPane(tc);
		    fpane.setLayout(new BorderLayout());
		    fpane.add("Center", scrollpane);
		    frame.setVisible(true);
		}
	    });
	
	if (argv.length > 0 && argv[0].equals("--flush")) {
	    SwingUtilities.invokeAndWait(() -> {});
	}

	try {
	    System.setSecurityManager(new SecurityManager());
	} catch (UnsupportedOperationException eu) {}

	int len = tc.getLength();
	System.out.println("tc.getLength() = " + len);

	Thread.currentThread().sleep(4000);
	System.exit(0);
    }
}
