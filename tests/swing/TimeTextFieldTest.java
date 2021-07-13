import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.swing.TimeTextField;

public class TimeTextFieldTest {
    static public void main(String argv[]) {
	final TimeTextField tf = new 
	    TimeTextField(12, TimeTextField.Mode.QUERY_AND_INDEFINITE) {
		protected boolean handleError() {
		    JOptionPane.showMessageDialog
			(this,
			 "must be in time format",
			 "error",
			 JOptionPane.ERROR_MESSAGE);
		    return false;
		}
		
	    };
	JFrame frame = new JFrame("TimeTextField Test");
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
		    System.out.println(tf.getValue());
		}
	    });


        fpane.add("North", tf);
	fpane.add("South", okButton);

        // fpane.setVisible(true);
        frame.setVisible(true);
    }
}