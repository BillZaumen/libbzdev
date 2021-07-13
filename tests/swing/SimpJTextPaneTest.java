import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.Color;


import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import org.bzdev.swing.*;

public class SimpJTextPaneTest {
    static SimpleJTextPane tp = new SimpleJTextPane();
    public static void main(String argv[]) {
	int i;
	JFrame frame = new JFrame("TextPane Test");
	Container fpane = frame.getContentPane();
	frame.addWindowListener(new WindowAdapter () {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
 
	frame.setSize(700,400);
	JScrollPane scrollpane = new JScrollPane(tp);
	fpane.setLayout(new BorderLayout());

	fpane.add("Center", scrollpane);
	// fpane.setVisible(true);
	frame.setVisible(true);
	    
	try {
	    tp.setEditable(false);
	    tp.perform((pane) -> {
		    pane.setBold(true);
		    pane.setItalic(true);
		    pane.setTextForeground(Color.YELLOW);
		    pane.setTextBackground(Color.BLACK);
		    pane.appendString("START\n\n");
		});
	    Thread.currentThread().sleep(2000);
	    tp.setSwingSafe(true);
	    for (i = 0; i < 100; i++) {
		String str = "line" +i +"\n";
		if (i % 3 == 1) tp.setBold(true);
		if (i % 2 == 1) tp.setItalic(true);
		if (i % 5 == 1) tp.setTextForeground(Color.RED);
		tp.appendString(str);
		tp.setBold(false);
		tp.setItalic(false);
		tp.setTextForeground(Color.BLACK);
	    }
	    tp.setSwingSafe(false);
	    SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			try {
			    Rectangle rectE = 
				tp.modelToView(tp.getDocument().getLength());
			    tp.scrollRectToVisible(rectE);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		});
	    Thread.currentThread().sleep(4000);
	    SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			try {
			    Rectangle rectS = tp.modelToView(0);
			    tp.scrollRectToVisible(rectS);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		});
	    Thread.currentThread().sleep(60000);
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
