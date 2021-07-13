import org.bzdev.swing.*;
import java.awt.*;
import javax.swing.*;
import org.bzdev.protocols.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class UrlTocPaneTest {
    public static void main(String argv[]) {
	try {
	    Handlers.enable();
	    java.io.FileInputStream is = 
		new java.io.FileInputStream("toc.xml");
	    
	    UrlTocPane tocPane = new UrlTocPane();
	    tocPane.setToc(is, true, true);

	    tocPane.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			ObjTocPane.Entry entry = (ObjTocPane.Entry)
			    e.getSource();
			System.out.println(entry.getValue().toString());
		    }
		});

	    // UrlTocPane tocPane = new UrlTocPane("title", 
	    //					  "http:foo.com/help.html");
	    JFrame frame = new JFrame("TreeTest");
	    Container hpane = frame.getContentPane();
	    // tocPane.addEntry("Chapter 1", "http://foo.com/chapter1");
	    // tocPane.addEntry("Chapter 2", "http://foo.com/chapter2");
	    // tocPane.nextLevel();
	    // tocPane.addEntry("Section 1", "http://foo.com/section1");
	    // tocPane.addEntry("Section 2", "http://foo.com/section2");
	    // tocPane.addEntry("Section 3", "http://foo.com/section3");
	    // tocPane.prevLevel();
	    //tocPane.addEntry("Chapter 3", "http://foo.com/chapter3");

	    // tocPane.entriesCompleted();

	    hpane.setLayout(new BorderLayout());
	    hpane.add(tocPane, "Center");
	
	    frame.setSize(200,400);
	    frame.addWindowListener(new WindowAdapter () {
		    public void windowClosing(WindowEvent e) {
			System.exit(0);
		    }
		});
	    frame.setVisible(true);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
