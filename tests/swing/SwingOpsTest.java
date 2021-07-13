import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import org.bzdev.swing.*;
import org.bzdev.swing.text.*;


class SwingOpsTest {

    // copied and stripped down from an EPTS method.
    static class TestPane extends JPanel {
	JComboBox<String> pcb;

	JComponent targetTF = null;
	public JComponent getTargetTF() {return targetTF;}

	private static String[] modes = {
	    ("closedPathMode"), // 0
	    ("CCWPathMode"),  // 1
	    ("CCWReversedPathMode"), // 2
	    ("CWPathMode"),  // 3
	    ("CWReversedPathMode"), // 4
	};

	JComboBox<String> modesCB = new JComboBox<>(modes);

	/*
	  private static String[] dirs = {
	  ("CCWSide"),
	  ("CWSide")
	  };

	  JComboBox<String> dirCB = new JComboBox<>(dirs);
	*/
	JComboBox<String> lunits1 = new JComboBox<>(modes);
	JComboBox<String> lunits2 = new JComboBox<>(modes);
	JComboBox<String> lunits3 = new JComboBox<>(modes);
	CharDocFilter cdf = new CharDocFilter();
	InputVerifier utfiv1 = new InputVerifier() {
		boolean firstTime = true;

		public boolean verify(JComponent input) {
		    JTextField tf = (VTextField)input;
		    String string = tf.getText();
		    if (string == null) string = "";
		    string = string.trim();
		    try {
			if (string.length() == 0) {
			    if (firstTime) {
				return true;
			    } else {
				return false;
			    }
			}
			double value = Double.parseDouble(string);
			if (value >= 0.0) {
			    firstTime = false;
			    return true;
			} else {
			    return false;
			}
		    } catch (Exception e) {
			return false;
		    }
		}
	    };

	InputVerifier utfiv2 = new InputVerifier() {
		boolean firstTime = true;

		public boolean verify(JComponent input) {
		    JTextField tf = (VTextField)input;
		    String string = tf.getText();
		    if (string == null) string = "";
		    string = string.trim();
		    try {
			if (string.length() == 0) {
			    if (firstTime) {
				return true;
			    } else {
				return false;
			    }
			}
			double value = Double.parseDouble(string);
			if (value >= 0.0) {
			    firstTime = false;
			    return true;
			} else {
			    return false;
			}
		    } catch (Exception e) {
			return false;
		    }
		}
	    };

	InputVerifier utfiv3 = new InputVerifier() {
		boolean firstTime = true;

		public boolean verify(JComponent input) {
		    JTextField tf = (VTextField)input;
		    String string = tf.getText();
		    if (string == null) string = "";
		    string = string.trim();
		    try {
			if (string.length() == 0) {
			    if (firstTime) {
				return true;
			    } else {
				return false;
			    }
			}
			double value = Double.parseDouble(string);
			if (value >= 0.0) {
			    firstTime = false;
			    return true;
			} else {
			    return false;
			}
		    } catch (Exception e) {
			return false;
		    }
		}
	    };

	VTextField d1tf;
	VTextField d2tf;
	VTextField d3tf;


	public TestPane() {
	    super();
	    cdf.setAllowedChars("09eeEE..,,++--");
	    JLabel pl = new JLabel(("pathLabel"));
	    JLabel d1l = new JLabel(("dist1"));
	    JLabel d2l = new JLabel(("dist2"));
	    JLabel d3l = new JLabel(("dist3"));

	    Vector<String> pathnames =
		new Vector<>(10);
	    pathnames.add(("PCBHDR"));

	    pcb = new JComboBox<String>(pathnames);

	    d1tf = new VTextField("", 10) {
		    @Override
		    protected void onAccepted() {
			String text = getText();
		    }
		    @Override
		    protected boolean handleError() {
			JOptionPane.showMessageDialog
			    (this, "Must enter a real number",
			     "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		    }
		};
	    ((AbstractDocument)d1tf.getDocument()).setDocumentFilter(cdf);
	    d1tf.setInputVerifier(utfiv1);

	    d2tf = new VTextField("", 10) {
		    @Override
		    protected void onAccepted() {
			String text = getText();
		    }
		    @Override
		    protected boolean handleError() {
			JOptionPane.showMessageDialog
			    (this, "Must enter a real number",
			     "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		    }
		};
	    ((AbstractDocument)d2tf.getDocument()).setDocumentFilter(cdf);
	    d2tf.setInputVerifier(utfiv2);

	    d3tf = new VTextField("", 10) {
		    @Override
		    protected void onAccepted() {
			String text = getText();
		    }
		    @Override
		    protected boolean handleError() {
			JOptionPane.showMessageDialog
			    (this, "Must enter a real number",
			     "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		    }
		};
	    ((AbstractDocument)d3tf.getDocument()).setDocumentFilter(cdf);
	    d3tf.setInputVerifier(utfiv3);

	    // initialize fields
	    pcb.setSelectedIndex(0);

	    modesCB.setSelectedIndex(0);
	    targetTF = d3tf;
	    lunits1.setSelectedIndex(0);
	    lunits2.setSelectedIndex(0);
	    lunits3.setSelectedIndex(0);

	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    setLayout(gridbag);
	    c.insets = new Insets(5, 5, 5, 5);
	    c.ipadx = 5;
	    c.ipady = 5;
	    c.anchor = GridBagConstraints.LINE_START;

	    c.gridwidth = 1;
	    gridbag.setConstraints(pl, c);
	    add(pl);
	    gridbag.setConstraints(pcb, c);
	    add(pcb);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    JLabel blank = new JLabel(" ");
	    gridbag.setConstraints(blank, c);
	    add(blank);

	    c.gridwidth = 1;
	    gridbag.setConstraints(d1l, c);
	    add(d1l);
	    gridbag.setConstraints(d1tf, c);
	    add(d1tf);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(lunits1, c);
	    add(lunits1);

	    c.gridwidth = 1;
	    gridbag.setConstraints(d2l, c);
	    add(d2l);
	    gridbag.setConstraints(d2tf, c);
	    add(d2tf);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(lunits2, c);
	    add(lunits2);

	    c.gridwidth = 1;
	    gridbag.setConstraints(d3l, c);
	    add(d3l);
	    gridbag.setConstraints(d3tf, c);
	    add(d3tf);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(lunits3, c);
	    add(lunits3);

	    c.gridwidth = 1;
	    blank = new JLabel(" ");
	    gridbag.setConstraints(blank, c);
	    add(blank);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(modesCB, c);
	    add(modesCB);

	    lunits1.setSelectedIndex(0);
	    lunits2.setSelectedIndex(0);
	    lunits3.setSelectedIndex(0);
	    d3l.setEnabled(true);
	    d3tf.setEnabled(true);
	    lunits3.setEnabled(true);
	}
    }

    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(() -> {
		final TestPane testpane = new TestPane();
		testpane.addAncestorListener(new AncestorListener() {
			    @Override
			    public void ancestorAdded(AncestorEvent e) {
				Component target = testpane.getTargetTF();
				SwingOps.tryRequestFocusInWindow(target, 0);
			    }
			    @Override
			    public void ancestorMoved(AncestorEvent e) {}
			    @Override
			    public void ancestorRemoved(AncestorEvent e) {}
		    });
		JOptionPane.showConfirmDialog(null, testpane, "Test",
					      JOptionPane.OK_CANCEL_OPTION,
					      JOptionPane.QUESTION_MESSAGE);
		System.out.println("lastLimitCount = "
				   + SwingOps.lastLimitCount());
		System.exit(0);
	    });
	Thread.sleep(10000);
	System.exit(0); 
    }
}
