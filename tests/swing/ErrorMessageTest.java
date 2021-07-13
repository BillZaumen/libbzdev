import org.bzdev.swing.*;
import java.util.Locale;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.bzdev.util.ErrorMessage;

public class ErrorMessageTest {
    public static void main(String argv[]) throws Exception {

	ErrorMessage.setAppendable(System.out);

	ErrorMessage.display("hello");
	ErrorMessage.display("hello.txt", 10, "hello");
	ErrorMessage.display(new Exception("exception"));
	ErrorMessage.format("%s", "hello there");
	ErrorMessage.format((Locale)null, "%s", "hello there");
	ErrorMessage.addSeparatorIfNeeded();
	ErrorMessage.addSeparatorIfNeeded();

	System.out.println("... try using a frame");

	SimpleConsole tc = new SimpleConsole();
        int i;
        JFrame frame = new JFrame("Error Message Test");
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
	
	ErrorMessage.setAppendable(tc);
	SwingErrorMessage.setComponent(frame);
	ErrorMessage.display("hello.txt", 10, "hello");
	ErrorMessage.display(new Exception("exception"));
	ErrorMessage.format("%s", "hello there");
	ErrorMessage.format((Locale)null, "%s", "hello there");
	ErrorMessage.addSeparatorIfNeeded();
	ErrorMessage.addSeparatorIfNeeded();
	ErrorMessage.displayConsoleIfNeeded();
	ErrorMessage.displayFormat("Error", "%s", "hello there");
	ErrorMessage.displayFormat("Error", (Locale)null, "%s",
				       "hello there");
	ErrorMessage.display("Error", "hello");
	SwingErrorMessage.displayFormat(frame, "Error", "%s", "hello there");
	SwingErrorMessage.displayFormat(frame, "Error", (Locale)null, "%s",
				       "hello there");
	SwingErrorMessage.display(frame, "Error", "hello");
	System.exit(0);
    }
}

