import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;

import org.bzdev.swing.*;
import org.bzdev.swing.text.*;

public class VTextFieldTest {

    static VTextField utf;

    public static void init() {
	JFrame frame = new JFrame ("VTextFieldTest");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 

	CharDocFilter cdf = new CharDocFilter();
	cdf.setAllowedChars("09eeEE..,,++--");
	InputVerifier vtfiv = new InputVerifier() {
		public boolean verify(JComponent input) {
		    JTextField tf = (VTextField)input;
		    String string = tf.getText();
		    try {
			double value = new Double(string);
			if (value > 0.0) return true;
			else return false;
		    } catch (Exception e) {
			System.out.println("verify exception: returned false");
			return false;
		    }
		}
	    };

	utf = new VTextField(String.format("%10.3g", 1.0), 10) {
		@Override
		protected void onAccepted() {
		    try {
			System.out.println("accepted "
					   + Double.valueOf(getText()));
		    } catch (Exception e) {
			System.out.println("not accepted: text = \""
					   + getText() + "\"");
		    }
		}
		@Override
		protected boolean handleError() {
		    JOptionPane.showMessageDialog
			(this, "Must enter a positive number",
			 "Error", JOptionPane.ERROR_MESSAGE);
		    return false;
		}
	    };

	((AbstractDocument)utf.getDocument()).setDocumentFilter(cdf);
	utf.setInputVerifier(vtfiv);

	frame.setSize(200,100);
	fpane.setLayout(new BorderLayout());
	fpane.add("North", utf);
	frame.pack();
	frame.setVisible(true);
    }

    public static void main(String argv[]) throws Exception {

	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    init();
		}
	    });
	
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    System.out.println("setting text to 20.0");
		    utf.setText("20.0");
		}
	    });

	Thread.sleep(2000L);

	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    System.out.println("setting text to 1.0");
		    utf.setText("1.0");
		}
	    });

	Thread.sleep(15000L);
	System.exit(0);
    }
}
