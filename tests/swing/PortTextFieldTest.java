import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.swing.PortTextField;

public class PortTextFieldTest {
    static String port;
    static public void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(() -> {
		final PortTextField tf = new
		    PortTextField(40) {
			protected boolean handleError() {
			    JOptionPane.showMessageDialog
				(this,
				 "must be a valid port number",
				 "error",
				 JOptionPane.ERROR_MESSAGE);
			    return false;
			}
			protected void onAccepted() {
			    port = getText();
			}
		    };
		JFrame frame = new JFrame("PortTextField Test");
		Container fpane = frame.getContentPane();
		frame.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
		    });
 
		frame.setSize(200,100);
		fpane.setLayout(new BorderLayout());


		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    System.out.println(port);
			}
		    });


		fpane.add("North", tf);
		fpane.add("South", okButton);

		// fpane.setVisible(true);
		frame.setVisible(true);
	    });
	Thread.sleep(300000L);
	System.exit(0);
    }
}
