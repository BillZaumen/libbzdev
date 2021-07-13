import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.swing.SimpleConsole;
import org.bzdev.swing.DebugTransferHandler;

public class DebugTransferTest {

    static void startGUI() {
	SimpleConsole tc = new SimpleConsole();
        JFrame frame = new JFrame("TextPane Test");
        Container fpane = frame.getContentPane();
	JLabel target = new
	    JLabel("<HTML><BODY><H1>drop target</H1></BODY></HTML>");
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        frame.setSize(700,400);
        JScrollPane scrollpane = new JScrollPane(tc);
        fpane.setLayout(new BorderLayout());
	
	fpane.add("North", target);
	target.setTransferHandler(new DebugTransferHandler(tc));
        fpane.add("Center", scrollpane);
        // fpane.setVisible(true);
        frame.setVisible(true);
    }


    public static void main(String argv[]) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    DebugTransferTest.startGUI();
		}
	    });
    }
}