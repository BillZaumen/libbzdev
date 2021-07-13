import org.bzdev.swing.proxyconf.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProxyDialogTest {
    static public void main(String argv[]) {
	JFrame frame = new JFrame();
	JFrame dframe = new JFrame();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
	Container fpane = frame.getContentPane();
	fpane.setLayout(new BorderLayout());
	fpane.setBackground(Color.gray);
	JButton obutton = new JButton("open");
	JButton pbutton = new JButton("Print Properties");
	// ProxyComponent c = new ProxyComponent(null);
	// final JDialog dialog = new JDialog(frame, "Proxy Config", true);
	// dialog.add(c);
	final ProxyDialog dialog = 
	    ProxyComponent.createDialog(dframe, "Proxy Dialog Test", true,
					(ProxyInfo)null);
	obutton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    dialog.setVisible(true);
		}
	    });
	pbutton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    System.getProperties().list(System.out);
		}
	    });
	// fpane.add(new ProxyComponent(null), "Center");
	fpane.add(obutton, "Center");
	fpane.add(pbutton, "South");
	frame.setResizable(false);
	frame.pack();
	frame.validate();
	frame.setVisible(true);
    }
}
