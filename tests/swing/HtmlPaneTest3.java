import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import org.bzdev.protocols.*;

public class HtmlPaneTest3 {
    public static void main(String argv[]) throws Exception {
	Handlers.enable();
	try {
	    URL url = new URL(argv[0]);
	    System.out.println("url = " + url);

	    SwingUtilities.invokeLater(() -> {
		    try {
			HtmlPane htmlPane = new HtmlPane();
			htmlPane.setEditable(true);
			htmlPane.setCaretColor(Color.WHITE);
			htmlPane.setPage(url);
			htmlPane.setBackground(Color.BLUE
					       .darker().darker().darker());
			htmlPane.setButtonBackground(Color.BLUE
						     .darker().darker(),
						     true);
			htmlPane.setSize(800, 600);
			JFrame frame = new JFrame("HtmlPane Test");
			Container hpane = frame.getContentPane();
			hpane.setLayout(new BorderLayout());
			frame.addWindowListener(new WindowAdapter () {
				public void windowClosing(WindowEvent e) {
				    System.exit(0);
				}
			    });
			hpane.add(htmlPane, "Center");
			frame.setSize(800,600);
			frame.setVisible(true);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		});
	} catch (Exception ee) {
	    ee.printStackTrace();
	}
    }
}
