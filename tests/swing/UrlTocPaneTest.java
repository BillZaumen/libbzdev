import org.bzdev.swing.*;
import java.awt.*;
import javax.swing.*;
import org.bzdev.protocols.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class UrlTocPaneTest {
    public static void main(String argv[]) throws Exception {
	try {
	    Handlers.enable();
	    java.io.FileInputStream is = 
		new java.io.FileInputStream("toc.xml");

	    SwingUtilities.invokeLater(()-> {
		    UrlTocPane tocPane = new UrlTocPane();
		    try {
			tocPane.setToc(is, true, true);
		    } catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		    }

		    tocPane.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				ObjTocPane.Entry entry = (ObjTocPane.Entry)
				    e.getSource();
				System.out.println(entry.getValue().toString());
			    }
			});

		    JFrame frame = new JFrame("TreeTest 3");
		    Container hpane = frame.getContentPane();
		    hpane.setLayout(new BorderLayout());
		    hpane.add(tocPane, "Center");
	
		    frame.setSize(200,400);
		    frame.addWindowListener(new WindowAdapter () {
			    public void windowClosing(WindowEvent e) {
				System.exit(0);
			    }
			});
		    frame.setVisible(true);
		});
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	Thread.sleep(30000L);
	System.exit(0);
    }
}
