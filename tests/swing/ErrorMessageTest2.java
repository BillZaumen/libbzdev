import org.bzdev.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.util.ErrorMessage;

public class ErrorMessageTest2 {

    static Runnable setup = new Runnable() {
	    public void run() {
		JFrame frame = new JFrame("menu test");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
		    });
		JMenuBar menubar = new JMenuBar();
		JMenu toolsMenu = new JMenu("Tools");
		SimpleConsole console = new SimpleConsole();
		SwingErrorMessage.setAppendable(console);
		toolsMenu.add(console.createMenuItem("Console", "Console",
						     800, 600));
		toolsMenu.add(new StackTraceMenuItem("StackTrace"));
		toolsMenu.add(new StackTraceMenuItem("StackTrace Duplicate"));
		menubar.add(toolsMenu);
		frame.setJMenuBar(menubar);
		frame.setSize(200, 200);
		frame.setVisible(true);
	    }
	};


    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(setup);
	for (int i = 0; i < 30; i++) {
	    try {
	    Thread.currentThread().sleep(5000);
	    ErrorMessage.display(new Exception("hello"));
	    } catch (Exception e) {}
	}
	Thread.sleep(30000L);
	System.exit(0);
    }
}
