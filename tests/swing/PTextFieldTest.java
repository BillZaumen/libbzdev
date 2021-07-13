import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;

import org.bzdev.swing.*;
import org.bzdev.swing.text.*;

public class PTextFieldTest {

    static PortTextField utf;

    public static void init() {
	JFrame frame = new JFrame ("PTextFieldTest");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 
	utf = new PortTextField("", 10) {
		@Override
		protected void onAccepted() throws Exception {
		    super.onAccepted();
		    try {
			System.out.println("accepted \"" + getText() + "\", "
					   + "value = " + getValue());
		    } catch (Exception e) {
		    }
		}
		@Override
		protected boolean handleError() {
		    JOptionPane.showMessageDialog
			(this, "Must enter a positive port number",
			 "Error", JOptionPane.ERROR_MESSAGE);
		    return false;
		}
	    };
	utf.setAllowEmptyTextField(false);
	System.out.println("utf.getValue() = " + utf.getValue());

        frame.setSize(200,100);
        fpane.setLayout(new BorderLayout());
	fpane.add("North", utf);
	fpane.add("South", new JButton("ok"));
	frame.pack();
	frame.setVisible(true);
    }

    public static void main(String argv[]) throws Exception {

	SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    init();
		}
	    });
    }
}

