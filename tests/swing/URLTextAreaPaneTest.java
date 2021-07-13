import org.bzdev.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.net.*;
import java.text.MessageFormat;


public class URLTextAreaPaneTest {
   static public void main(String argv[]) {
	try {
	    URLTextAreaPane pane = new URLTextAreaPane(10, 40, "Error");
	    final JFrame frame = new JFrame("URLTextAReaPane Test");
	    Container fpane = frame.getContentPane();
	    /*
	    frame.addWindowListener(pane.getOnClosingWindowListener());
	    pane.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			// System.out.println("main action listener");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    frame.dispose();
				}
			    });
			}
		});
	    */
	    frame.addWindowListener(new WindowAdapter () {
		    public void windowClosing(WindowEvent e) {
			// System.out.println("closing");
			// need to delay this so that the EditImagesPane's
			// listener will have a chance to do something.
			// once frame.displose is called, an event
			// closing the window will be posted, so we want
			// to be safe about ordering the events.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    frame.dispose();
				}
			    });
			}
		    public void windowClosed(WindowEvent e) {
			// System.out.println("closed");
			System.exit(0);
		    }
		});

 
	    // frame.setSize(500,500);
	    fpane.setLayout(new FlowLayout());
	    fpane.add(pane);
	    
	    // fpane.setVisible(true);
	    frame.pack();
	    // frame.setSize(750, 400);
	    System.out.println("frame constructed");
	    frame.setVisible(true);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}