import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;

public class ObjTocPaneTest {
    public static void main(String argv[]) throws Exception {
	try {
	    final ObjTocPane tocPane = new ObjTocPane();

	    tocPane.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			ObjTocPane.Entry entry = (ObjTocPane.Entry)
			    e.getSource();
			System.out.println((String)(entry.getValue())
					   +", row count = "
					   +tocPane.getRowCount());
		    }
		});
	    if (argv.length > 0) {
		if (argv[0].equals("light")) {
		    tocPane.setBackground(new Color(224,224,255));
		} else if (argv[0].equals("dark")) {
		    tocPane.setBackground(new Color(0, 0, 64));
		    tocPane.setForeground(Color.WHITE);
		} else if (argv[0].equals("mix")) {
		    tocPane.setBackground(new Color(89, 128, 164));
		    tocPane.setForeground(Color.WHITE);
		} else if (argv[0].equals("fixed")) {
		    tocPane.setBackground(new Color(0, 0, 64));
		    tocPane.setForeground(Color.WHITE);
		    tocPane.setTCRBackgroundSelectionColor
			(new Color(32, 32, 164));
		    tocPane.setTCRTextSelectionColor(Color.YELLOW);
		}
	    }

	    JFrame frame = new JFrame("TreeTest 2");
	    Container hpane = frame.getContentPane();
	    tocPane.addEntry("Table of Contents", "http:foo.com/help.html");
	    tocPane.nextLevel();

	    tocPane.addEntry("Chapter 1", "http://foo.com/chapter1");
	    tocPane.addEntry("Chapter 2", "http://foo.com/chapter2");
	    tocPane.nextLevel();
	    tocPane.addEntry("Section 1", "http://foo.com/section1");
	    tocPane.addEntry("Section 2", "http://foo.com/section2");
	    tocPane.addEntry("Section 3", "http://foo.com/section3");
	    tocPane.prevLevel();
	    tocPane.addEntry("Chapter 3", "http://foo.com/chapter3");

	    tocPane.prevLevel();
	    tocPane.entriesCompleted();
	    // tocPane.setRootVisible(false);
	    // tocPane.setShowsRootHandles(true);

	    tocPane.setSelectionWithAction(0);


	    hpane.setLayout(new BorderLayout());
	    hpane.add(tocPane, "Center");
	
	    frame.setSize(200,400);
	    frame.addWindowListener(new WindowAdapter () {
		    public void windowClosing(WindowEvent e) {
			System.exit(0);
		    }
		});
	    frame.setVisible(true);
	    new Thread(new Runnable() {
		    public void run() {
			try {
			    Thread.sleep(20000);
			} catch (Exception e) {}
			System.out.println("try to change Toc");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    tocPane.clearToc();
				    tocPane.addEntry("Table of Contents", 
						     "http:foo.com/help.html");
				    tocPane.nextLevel();
			
				    tocPane.addEntry("Ch 1",
						     "http://foo.com/ch1");
				    tocPane.addEntry("Ch 2",
						     "http://foo.com/ch2");
				    tocPane.nextLevel();
				    tocPane.addEntry("Sec 1",
						     "http://foo.com/sec1");
				    tocPane.addEntry("Sec 2",
						     "http://foo.com/sec2");
				    tocPane.addEntry("Sec 3",
						     "http://foo.com/sec3");
				    tocPane.prevLevel();
				    tocPane.addEntry("Ch 3",
						     "http://foo.com/ch3");
				    tocPane.entriesCompleted();
				    tocPane.setSelectionWithAction(0);
				}
			    });
		    }
		}).start();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	Thread.sleep(30000L);
	System.exit(0);
    }
}
