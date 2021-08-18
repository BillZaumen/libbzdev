import org.bzdev.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProxyMenuTest {
    static public void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(() -> {
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
		JButton pbutton = new JButton("Print Properties");
		// ProxyComponent c = new ProxyComponent(null);
		// final JDialog dialog = new JDialog(frame, "Proxy Config", true);
		// dialog.add(c);
		JMenuBar menubar = new JMenuBar();
		JMenu editMenu = new JMenu("edit");
		JMenu helpMenu = new JMenu("help");
		JMenuItem proxyMenuItem = new ProxyMenuItem("proxy config",
							    frame,
							    "Proxy Configuration");
		org.bzdev.protocols.Handlers.enable();
		JMenuItem helpMenuItem = null;

		try {
		    helpMenuItem = new
			HelpMenuItem("help",
				     "sresource:/org/bzdev/swing/proxyconf/"
				     + "ProxyComponent.html",
				     "Help", 700, 600);
		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(1);
		}
		editMenu.add(proxyMenuItem);
		helpMenu.add(helpMenuItem);
		menubar.add(editMenu);
		menubar.add(helpMenu);
		frame.setJMenuBar(menubar);

		pbutton.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
			    System.getProperties().list(System.out);
			}
		    });
		// fpane.add(new ProxyComponent(null), "Center");
		fpane.add(pbutton, "Center");
		frame.setResizable(false);
		frame.pack();
		frame.validate();
		frame.setVisible(true);
	    });
	Thread.sleep(30000L);
	System.exit(0);
    }
}
