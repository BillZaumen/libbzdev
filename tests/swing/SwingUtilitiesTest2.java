import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class SwingUtilitiesTest2 {
    static JTextPane tc;
    static AttributeSet aset = StyleContext.getDefaultStyleContext().
	getStyle(StyleContext.DEFAULT_STYLE);
    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeAndWait(() -> {
		tc = new JTextPane();
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
		// fpane.setVisible(true);
		frame.setVisible(true);
	    });


	System.setSecurityManager(new SecurityManager());
	System.out.println("security manager installed");
	SwingUtilities.invokeAndWait(() -> {
		Document doc = tc.getDocument();
		try {
		    doc.insertString(doc.getLength(), "start\n", aset);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    });
    }
}
