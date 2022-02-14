package org.bzdev.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;


/**
 * Menu item to display a URLTextAreaPane.
 * There are several protected methods that can be used to interact with
 * an application:
 * <UL>
 *    <LI>{@link URLTextAreaMenuItem#inputURLInUse()}.
 *    <LI>{@link URLTextAreaMenuItem#inputText()}.
 *    <LI>{@link URLTextAreaMenuItem#inputURL()}.
 *    <LI>{@link URLTextAreaMenuItem#outputURLInUse(boolean)}.
 *    <LI>{@link URLTextAreaMenuItem#outputText(String)}.
 *    <LI>{@link URLTextAreaMenuItem#outputURL(String)}.
 * </UL>
 * The methods whose names start with <code>input</code> are called when
 * the menu item is pushed and before a dialog box appears.  The methods whose
 * names start with <code>output</code> are called after the dialog box
 * is closed.  Whether  input or output methods are called at all depends
 * on a mode provided in a constructor.  The default when a constructor
 * does not provide a mode is to use both the input and output methods.
 */
public class URLTextAreaMenuItem extends JMenuItem
{
    /**
     * Called to indicate whether a URL was used to create the text.
     * Users should override it to perform any necessary
     * actions based on whether the URL is valid or not (e.g., to store a
     * flag  permanently).  This is called before outputText and
     * outputURL is called.
     * @param inUse true if it was; false otherwise.
     * @see org.bzdev.swing.URLTextAreaButton.Mode
     */
    protected void outputURLInUse(boolean inUse) {};
    /**
     * Called to write text from this component.
     * This is called when the dialog box is dismissed.
     * Users should override it to perform any necessary
     * actions based on that text.
     * @param value the new text.
     * @see org.bzdev.swing.URLTextAreaButton.Mode
     */
    protected void outputText(String value) {}
    /**
     * Called to write the URL used to load this component.
     * This is called when the dialog box is dismissed.
     * Users should override it to perform any necessary
     * actions based on that URL (e.g., to store it permanently).
     * @param url the URL
     * @see org.bzdev.swing.URLTextAreaButton.Mode
     */
    protected void outputURL(String url) {}

    /**
     * Obtain input text when a URL does not specify the location of the text.
     * @return the text of the component
     * @see org.bzdev.swing.URLTextAreaButton.Mode
     */
    protected String inputText() {return "";}
    /**
     * Obtain the URL when a URL specifies the location of the text.
     * @return the URL
     * @see org.bzdev.swing.URLTextAreaButton.Mode
     */
    protected String inputURL() {return "";}
    /**
     * Indicate if a URL will specify the location of the text.
     * This is called when the menu item is pushed before inputText or inputURL.
     * @return true if a URL will specify the location of the text; false
     *         otherwise
     * @see org.bzdev.swing.URLTextAreaButton.Mode
     */
    protected boolean inputURLInUse() {return false;}

    boolean inputMode = true;
    boolean outputMode = true;

    /**
     * Constructor with mode.
     * @param label the menu-item label
     * @param rows  the number of rows for text
     * @param cols the number of columns for text
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param errorTitle the title of dialog boxes used when handling errors
     * @param mode the enumeration values are USE_OUTPUT_NO_STATE when inputs
     *        and outputs are set using protected methods, USE_OUTPUT_WITH_STATE
     *        when outputs but not inputs are set by protected methods, and
     *        NO_OUTPUT_WITH_STATE when protected methods for setting inputs
     *        and outputs are not used at all.
     * @see URLTextAreaButton.Mode
     */
    public URLTextAreaMenuItem (String label, final int rows, final int cols,  
			   final Component frame,
			      final String title, String errorTitle,
				URLTextAreaButton.Mode mode)
    {
	this(label, rows, cols, frame, title, errorTitle);
	switch (mode) {
	case USE_OUTPUT_NO_STATE:
	    inputMode = true;
	    outputMode = true;
	    break;
	case USE_OUTPUT_WITH_STATE:
	    inputMode = false;
	    outputMode = true;
	    break;
	case NO_OUTPUT_WITH_STATE:
	    inputMode = false;
	    outputMode = false;
	    break;
	}
    }

    URLTextAreaPane ta;
    JScrollPane tsp;

    /**
     * Get Text.
     * @return the text associated with the component
     */
    public String getTextAreaText() {
	return ta.getText();
    }

    /**
     * Get the URL.
     * @return the URL for the component's text; a null string if there is
     *         none
     */
    public String getURL() {
	return ta.getURL();
    }

    /**
     * Determine URL status.
     * @return true if the text was loaded from a resource given by a URL;
     *         false otherwise
     */
    public boolean getURLInUse() {
	return ta.urlInUse();
    }


    /**
     * Constructor with default mode.
     * @param label the menu-item label
     * @param rows  the number of rows for text
     * @param cols the number of columns for text
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param titleError the title of dialog boxes used when handling errors
     */
    public URLTextAreaMenuItem (String label, final int rows, final int cols,  
			      final Component frame,
			      final String title, String titleError)
    {
	super(label);
	ta = new URLTextAreaPane(rows, cols, titleError);
	tsp = new JScrollPane(ta,
			      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	addActionListener(new ActionListener() {
		private void tryAgainAux() {
		    // the idea is to delay the call to
		    // requestFocusInWindow until showMessageDialog
		    // has finished scheduling its events.
		    (new Thread() {
			    boolean done = false;
			    void ourRequestFocus() {
				done = ta.requestFocusInWindow();
			    }
			    public void run() {
				try {
				    for (int i = 0; i < 16; i++) {
					SwingUtilities
					    .invokeAndWait(new Runnable() {
						    public void run() {
							ourRequestFocus();
						    }
						});
					if (done) break;
					try {
					    Thread.sleep(100);
					} catch (Exception e) {}
				    }
				} catch (Exception e){}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					    ta.requestFocusInWindow();
					}
				    });
			    }
			}).start();
		}
		private void tryAgain() {
		    SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				tryAgainAux();
			    }
			});

		}
		boolean addNeeded = true;
		public void actionPerformed(ActionEvent e) {
		    if (addNeeded) {
			boolean inUse = inputURLInUse();
			ta.init((inUse? inputURL(): inputText()), inUse);
			ta.addAncestorListener
			    (new AncestorListener() {
				    public void
					ancestorAdded (AncestorEvent e)
				    {
					SwingUtilities.invokeLater(() -> {
						tryAgain();
					    });
				    }
				    public void ancestorMoved (AncestorEvent e)
				    {
				    }
				    public void ancestorRemoved(AncestorEvent e)
				    {
				    }
				});
			addNeeded = false;
		    }
		    String old = null;
		    String oldURL = null;
		    boolean oldInUse = false;
		    if (inputMode) {
			boolean inUse = inputURLInUse();
			// System.out.println("initializing ta");
			ta.init((inUse? inputURL(): inputText()), inUse);
			// ta.setText(inputValue());
		    } else {
			old = ta.getText();
			oldURL = ta.getURL();
			oldInUse = ta.urlInUse();
		    }
		    int choice = JOptionPane.showOptionDialog
			(frame, tsp, title, JOptionPane.OK_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, null, null);
		    if (choice == 0) {
			if (outputMode) {
			    outputURLInUse(ta.urlInUse());
			    outputText(ta.getText());
			    outputURL(ta.getURL());
			}
			if (inputMode) {
			    ta.init("", false, "");
			}
		    } else {
			if (inputMode) {
			    // ta.setText(inputValue());
			    ta.init("", false, "");
			} else {
			    ta.init((oldInUse?oldURL:old), oldInUse);
			}
		    }
		}
	    });
    }
}

//  LocalWords:  URLTextAreaPane URLTextAreaMenuItem inputURLInUse url
//  LocalWords:  inputText inputURL outputURLInUse boolean outputText
//  LocalWords:  outputURL inUse errorTitle titleError setText
//  LocalWords:  requestFocusInWindow showMessageDialog inputValue
