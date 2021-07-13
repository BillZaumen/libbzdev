import org.bzdev.swing.AuthenticationPane;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class AuthPaneTest {
    static public void main(String argv[]) {
	JFrame frame = new JFrame("AuthenticationPane Test");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 
        fpane.setLayout(new FlowLayout());
	String url1 = argv.length > 0 ? argv[0]: null;
	String url2 = argv.length > 1? argv[1]: null;

	Authenticator.setDefault(AuthenticationPane.getAuthenticator(null));
	try {
	    if (url1 != null) {
		fpane.add(new JLabel(new ImageIcon(new URL(url1))));
	    }
	    if (url2 != null) {
		fpane.add(new JLabel(new ImageIcon(new URL(url2))));
	    }
	    if (url1 == null && url2 == null) {
		fpane.add(new JLabel("no images"));
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	frame.pack();
        frame.setVisible(true);
    }
}
