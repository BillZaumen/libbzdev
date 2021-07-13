import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.bzdev.protocols.*;

public class HtmlWithTocTest {
    public static void main(String argv[]) {
	try {
	    Handlers.enable();
	    java.io.FileInputStream is = 
		new java.io.FileInputStream(argv[0]);

	    HtmlWithTocPane tocPane = new HtmlWithTocPane(is, true, true);

	    JFrame frame = new JFrame("TreeTest");
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
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
