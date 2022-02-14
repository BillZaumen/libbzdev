package org.bzdev.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Menu item to pop up a dialog box containing a text field.
 * This class arranges that the text field has the keyboard focus
 * when it appears. By default, the state of the text field is
 * read and written via one of two protected methods that the
 * user should implement:
 * {@link TextFieldMenuItem#outputValue(String)} and
 * {@link TextFieldMenuItem#inputValue()}.
 * <P>
 * For example,
 * <blockquote><code><pre>
 * public class Application {
 *    JFrame appFrame;
 *    String value = "initial value";
 *    void init() {
 *      appFrame = new JFrame("Application");
 *      TextFieldMenuItem tfb =
 *         new TextFieldMenuItem("Set Value", 32, appFrame, "Value") {
 *              protected String inputValue() {
 *                return value;
 *              }
 *              protected void outputValue(String s) {
 *                value = s;
 *              }
 *         };
 *      ...
 *    }
 * }
 * </pre></code></blockquote>
 * If access to the text field is needed, one can use a constructor
 *  that explicitly provides the text field.  For example:
 * <blockquote><code><pre>
 * public class Application {
 *    JFrame appFrame;
 *    String value = "";
 *    void init() {
 *      JTextField tf = new JTextField(5);
 *      tf.setBackground(Color.GRAY);
 *      appFrame = new JFrame("Application");
 *      TextFieldMenuItem tfmi =
 *         new TextFieldMenuItem("String", tf, -1, appFrame, "String Value")
 *         {
 *              protected String inputValue() {
 *                return value;
 *              }
 *              protected void outputValue(String s) {
 *                value = s;
 *              }
 *         };
 *      ...
 *    }
 * }
 * </pre></code></blockquote>
 * <P>
 * IF the text field is a subclass of {@link VTextField}, the class
 * {@link VTextFieldMenuItem} should be used instead as methods such as
 * {@link TextFieldMenuItem#outputValue(String)} are not needed.

 * @see javax.swing.JTextField
 */
public class TextFieldMenuItem extends JMenuItem
{
    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    /**
     * Set the output value.
     * The user of this class should override this method to store
     * text. Whether this method is called depends on the TextFieldMenuItem's
     * mode.
     * @param value the text of the component
     * @see org.bzdev.swing.TextFieldButton.Mode
     */
    protected void outputValue(String value) {}


    /**
     * Read the component's text from an external source.  This method
     * is expected to return the same value until the dialog box for
     * this component is closed.  Whether this method is called
     * depends on the TextFieldMenuItem's mode.
     * @return a string containing the contents for a text area at
     * when the text area becomes visible.
     */
    protected String inputValue() {return "";}

    boolean inputMode = true;
    boolean outputMode = true;

    /**
     * Constructor with a mode.
     * @param label the menu-item label
     * @param nchars the number of characters displayed by this text field;
     *        negative implies that an existing textField's value
     *        should not be modified
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param mode the mode for the component
     * @see TextFieldButton.Mode
     */
    public TextFieldMenuItem (String label, int nchars,  
			      Component frame,
			      String title, TextFieldButton.Mode mode)
    {
	this(label, null, nchars, frame, title, mode);
    }
    /**
     * Constructor with a mode and text field.
     * @param label the menu-item label
     * @param textField this menu item's text-field; null if a text field
     *        should be provided
     * @param nchars the number of characters displayed by this text field
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param mode the mode for the component
     * @see TextFieldButton.Mode
     */
    public TextFieldMenuItem (String label, JTextField textField, int nchars,  
			      Component frame,
			      String title, TextFieldButton.Mode mode)
    {
	this(label, textField, nchars, frame, title);
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
    JTextField tf;

    /**
     * Get the text of the text-field component.
     * When the mode is USE_OUTPUT_NO_STATE (the default), the value
     * returned is the value at the time the menu item was pressed.
     * Otherwise it is the component's current value.
     * @return the component's text
     */
    public String getTextFieldText() {
	if (inputMode) {
	    return inputValue();
	} else {
	    return tf.getText();
	}
    }

    /**
     * Constructor with a default mode of USE_OUTPUT_NO_STATE.
     * @param label the menu item's label
     * @param nchars the number of characters displayed by this text field
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @see TextFieldButton.Mode
     */
    public TextFieldMenuItem(String label, int nchars,
			    Component frame, String title)
    {
	this(label, null, nchars, frame, title);
    }


    /**
     * Constructor with a default mode of
     * {@link TextFieldButton.Mode#USE_OUTPUT_NO_STATE}.
     * @param label the menu-item label
     * @param textField this menu item's text-field; null if a text field
     *        should be provided
     * @param nchars the number of characters displayed by this text field;
     *        negative implies that an existing textField's value
     *        should not be modified
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @see TextFieldButton.Mode
     * @exception IllegalArgumentException an argument was out of range
     *            (e.g., nchars was negative when textField was null)
     */
    public TextFieldMenuItem(String label, JTextField textField,
			     final int nchars,
			     final Component frame, final String title)
    {
	super(label);
	if (textField == null) {
	    if (nchars < 0) throw new
				IllegalArgumentException
				(errorMsg("ncharsNegative", nchars));
	    tf = new JTextField(nchars);
	} else {
	    tf = textField;
	    if (nchars >= 0) {
		tf.setColumns(nchars);
	    }
	}
	tf = new JTextField(nchars);
	addActionListener(new ActionListener() {
		private void tryAgainAux() {
		    // the idea is to delay the call to
		    // requestFocusInWindow until showMessageDialog
		    // has finished scheduling its events.
		    (new Thread() {
			    boolean done = false;
			    void ourRequestFocus() {
				done = tf.requestFocusInWindow();
			    }
			    public void run() {
				try {
				    for (int i = 0;  i < 16; i++) {
					SwingUtilities.invokeAndWait
					    (new Runnable() {
						    public void run() {
							ourRequestFocus();
						    }
						});
					if (done) break;
					try {
					    Thread.sleep(100);
					} catch (Exception e) {}
				    }
				    if (!done) System.out.println("not done");
				} catch (Exception e){}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					    tf.requestFocusInWindow();
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
			tf.setText(inputValue());
			tf.addAncestorListener
			    (new AncestorListener() {
				    public void
					ancestorAdded (AncestorEvent e)
				    {
					SwingUtilities.invokeLater(() -> {
						tryAgain();
					    });
					/*
					if (!tf.requestFocusInWindow()) {
					    tryAgain();
					}
					*/
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
		    if (inputMode) {
			tf.setText(inputValue());
		    } else {
			old = tf.getText();
		    }
		    int choice = JOptionPane.showOptionDialog
			(frame, tf, title, JOptionPane.OK_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, null, null);
		    if (choice == 0) {
			if (outputMode) outputValue(tf.getText());
			 if (inputMode) tf.setText("");
		    } else {
			if (inputMode) {
			    tf.setText("");
			} else {
			    tf.setText(old);
			}
		    }
		}
	    });
    }
}

//  LocalWords:  exbundle TextFieldMenuItem outputValue inputValue tfb
//  LocalWords:  blockquote JFrame appFrame TCP PortTextField portTF
//  LocalWords:  setValue getText getValue setDocumentFilter nchars
//  LocalWords:  DocumentFilter JComponent setInputVerifier textField
//  LocalWords:  InputVerifier TextFieldMenuItem's TextAreaButton tf
//  LocalWords:  textField's IllegalArgumentException ncharsNegative
//  LocalWords:  requestFocusInWindow showMessageDialog JOptionPane
//  LocalWords:  setText
