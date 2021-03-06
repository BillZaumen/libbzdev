import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.bzdev.protocols.*;

public class HtmlWithTocTest {
    public static void main(String argv[]) throws Exception {
	boolean notimeout = argv.length > 1 && argv[1].equals("--no-timeout");
	boolean systemUI = argv.length > 2 && argv[2].equals("--systemUI");
	try {
	    if (systemUI) {
		Runnable r = () -> {
		    System.out.println("trying setSystemPLAF()");
		    DarkmodeMonitor.setSystemPLAF();
		    DarkmodeMonitor.init();
		};
		// SwingUtilities.invokeLater(r);
		r.run();
	    }
	    Handlers.enable();
	    java.io.FileInputStream is = 
		new java.io.FileInputStream(argv[0]);

	    if (systemUI) {
		System.out.println("darkmode initially " +
				   DarkmodeMonitor.getDarkmode());
		DarkmodeMonitor.addPropertyChangeListener(evnt -> {
			System.out.println(evnt.getPropertyName()
					   + " = " + evnt.getNewValue());
		    });
	    }

	    SwingUtilities.invokeLater(() -> {
		    try {
			/*
			HtmlWithTocPane tocPane =
			    new HtmlWithTocPane(is, true, true);
			*/
			HtmlWithTocPane tocPane = new HtmlWithTocPane();

			if (!systemUI) {
			    tocPane.setBackground(Color.BLUE.
						  darker().darker().darker());
			    tocPane.setSplitterBackground(Color.BLUE
							  .brighter()
							  .brighter());
			    tocPane.setTocBackground(Color.BLUE
						     .darker()
						     .darker()
						     .darker());

			    tocPane.setTocScrollbarBackground(Color.BLUE);

			    tocPane.setTocForeground(Color.WHITE);
			    tocPane.setHtmlButtonBackground(Color.BLUE.darker()
							    .darker(), true);
			    tocPane.setHtmlButtonBackground(Color.BLUE.darker()
							    .darker(), true);

			    tocPane.setHtmlPaneBackground(Color.BLUE.darker()
							  .darker()
							  .darker());
			} else {
			    tocPane.setHtmlPaneBorder
				(BorderFactory.createMatteBorder
				 (0, 10, 0, 10, Color.BLUE));
			    tocPane.setContentPaneBorder
				(BorderFactory.createMatteBorder
				 (0, 10, 0, 10, Color.BLUE.darker()
				  .darker().darker()));
			}
			// tocPane.setSelectionWithAction(0);

			JFrame frame = new JFrame("TreeTest 1");
			Container hpane = frame.getContentPane();
			hpane.setLayout(new BorderLayout());
			hpane.add(tocPane, "Center");
	
			frame.setSize(600,400);
			frame.addWindowListener(new WindowAdapter () {
				public void windowClosing(WindowEvent e) {
				    System.exit(0);
				}
			    });
			tocPane.setToc(is, true, false);
			frame.setVisible(true);
		    } catch (Exception ee) {
			ee.printStackTrace();
			System.exit(1);
		    }
		});
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	if (!notimeout) {
	    Thread.sleep(30000L);
	    System.exit(0);
	}
    }
}
