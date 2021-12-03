import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.bzdev.protocols.*;

public class HtmlWithTocTest {
    public static void main(String argv[]) throws Exception {
	try {
	    Handlers.enable();
	    java.io.FileInputStream is = 
		new java.io.FileInputStream(argv[0]);

	    SwingUtilities.invokeLater(() -> {
		    try {
			HtmlWithTocPane tocPane =
			    new HtmlWithTocPane(is, true, true);

			tocPane.setTocBackground(Color.BLUE.
						 darker().darker().darker());
			tocPane.setTocForeground(Color.WHITE);


			JFrame frame = new JFrame("TreeTest 1");
			Container hpane = frame.getContentPane();
			hpane.setLayout(new BorderLayout());
			hpane.add(tocPane, "Center");
	
			frame.setSize(600,400);
			frame.addWindowListener(new WindowAdapter () {
				public void windowClosing(WindowEvent e) {
				    System.exit(0);
				}
			    });
			frame.setVisible(true);
		    } catch (Exception ee) {
			ee.printStackTrace();
			System.exit(1);
		    }
		});
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	Thread.sleep(30000L);
	System.exit(0);
    }
}
