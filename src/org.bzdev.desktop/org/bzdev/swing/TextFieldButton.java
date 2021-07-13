package org.bzdev.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Button to pop up a dialog box containing a text field.
 * This class arranges that the text field has the keyboard focus
 * when it appears. By default, the state of the text field is
 * read and written via one of two protected methods that the
 * user should implement:
 * {@link TextFieldButton#outputValue(String)} and
 * {@link TextFieldButton#inputValue()}.
 * <P>
 * For example,
 * <blockquote><code>
 * public class Application {
 *    JFrame appFrame = new JFrame("Application");
 *    String value = "initial value";
 *    TextFieldButton tfb =
 *         new TextFieldButton("Set Value", 32, appFrame, "Value") {
 *              protected String inputValue() {
 *                return value;
 *              }
 *              protected void outputValue(String s) {
 *                value = s;
 *              }
 *         };
 *    ...
 * }
 * </code></blockquote>
 * If the text field should be validated or should have some restriction
 * on the characters that can be entered, use a constructor that
 * explicitly provides the text field.  For example, if the text field
 * should specify a TCP port, the following code could be used:
 * <blockquote><code>
 * public class Application {
 *    JFrame appFrame = new JFrame("Application");
 *    int port = 80;
 *    PortTextField portTF = new PortTextField(5);
 *    TextFieldButton tfb =
 *         new TextFieldButton(portTF,
 *                             "Set TCP Port",
 *                              5, appFrame,
 *                             "TCP Port")
 *         {
 *              protected String inputValue() {
 *                portTF.setValue(port);
 *                return portTF.getText();
 *              }
 *              protected void outputValue(String s) {
 *                port = portTF.getValue();
 *              }
 *         };
 *    ...
 * }
 * </code></blockquote>
 * This code uses the port text field directly.
 * <P>
 * For arbitrary restrictions on the contents of a text field, if the
 * text field's document is a subclass of
 * {@link javax.swing.text.AbstractDocument}, one may call the method
 * {@link javax.swing.text.AbstractDocument#setDocumentFilter(DocumentFilter)}.
 * Its argument (of type {@link javax.swing.text.DocumentFilter}) will
 * provide some control over modifications to the document.  One may
 * also use the text field's method
 * {@link JComponent#setInputVerifier(InputVerifier)}, which will
 * prevent the keyboard focus from changing if the input is not valid.

 * @see javax.swing.InputVerifier
 * @see javax.swing.JTextField
 * @see javax.swing.text.AbstractDocument
 * @see javax.swing.text.DocumentFilter
 * @see org.bzdev.swing.text.CharDocFilter
 * @see org.bzdev.swing.VTextField
 * @see org.bzdev.swing.WholeNumbTextField
 * @see org.bzdev.swing.PortTextField
 * @see org.bzdev.swing.TimeTextField
 */
public class TextFieldButton extends JButton
{
    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    /**
     * Set the output value.
     * The user of this class should override this method to store
     * text. Whether this method is called depends on the TextFieldButton's
     * mode.
     * @param value the text of the component
     * @see org.bzdev.swing.TextFieldButton.Mode
     */
    protected void outputValue(String value) {}


    /**
     * Read the component's text from an external source.  This method
     * is expected to return the same value until the dialog box for
     * this component is closed.  Whether this method is called
     * depends on the TextFieldButton's mode.
     * @return a string containing the contents for a text area at
     * when the text area becomes visible.
     */
    protected String inputValue() {return "";}

    /**
     * The mode of the component.
     */
    public static enum Mode {
	/**
	 * When the text area's dialog box appears, its state is
	 * created by calling {@link TextAreaButton#inputValue()}.
	 * When the dialog box is
	 * closed, its state is written by calling
	 * {@link TextAreaButton#outputValue(String)}.
	 */
	USE_OUTPUT_NO_STATE, 
	/**
	 * Text is stored in the component. When the dialog box is closed,
	 * its state is written by calling
	 * {@link TextAreaButton#outputValue(String)}.
	 */
	USE_OUTPUT_WITH_STATE,
	/**
	 * Text is stored in the component and neither
	 * {@link TextAreaButton#inputValue()} nor
	 * {@link TextAreaButton#outputValue(String)} is called to store
	 *  the state of the text area.
	 */
	NO_OUTPUT_WITH_STATE
    }

    boolean inputMode = true;
    boolean outputMode = true;

    /**
     * Constructor with a mode.
     * @param label the button label
     * @param nchars the number of characters displayed by this text field
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param mode the mode for the component
     * @see TextFieldButton.Mode
     */
    public TextFieldButton (String label, final int nchars,  
			   final Component frame,
			   final String title, Mode mode)
    {
	this(null, label, nchars, frame, title, mode);
    }
    /**
     * Constructor with a mode and document.
     * @param textField this button's text-field; null if a text field
     *        should be provided.
     * @param label the button label
     * @param nchars the number of characters displayed by this text field;
     *        negative implies that an existing textField's value
     *        should not be modified
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param mode the mode for the component
     * @see TextFieldButton.Mode
     */
    public TextFieldButton (JTextField textField, String label,
			    final int nchars,
			    final Component frame,
			    final String title, Mode mode)
    {
	this(textField, label, nchars, frame, title);
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
     * returned is the value at the time the button was pressed.
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
     * @param label the button label
     * @param nchars the number of characters displayed by this text field
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @see TextFieldButton.Mode
     */
    public TextFieldButton (String label, int nchars,
			   Component frame,
			   String title)
    {
	this(null, label, nchars, frame, title);
    }

    /**
     * Constructor with a default mode of USE_OUTPUT_NO_STATE and
     * providing a document.
     * @param textField this button's text-field; null if a text field
     *        should be provided.
     * @param label the button label
     * @param nchars the number of characters displayed by this text field;
     *        negative implies that an existing textField's value
     *        should not be modified
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @see TextFieldButton.Mode
     * @exception IllegalArgumentException an argument was out of range
     *            (e.g., nchars was negative when textField was null)
     */
    public TextFieldButton(JTextField textField, String label,
			   final int nchars,
			   final Component frame,
			   final String title)
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
	addActionListener(new ActionListener() {
		private void tryAgainAux() {
		    // the idea is to delay the call to
		    // requestFocusInWindow until showMessageDialog
		    // has finished scheduling its events.
		    (new Thread() {
			    public void run() {
				try {
				    SwingUtilities
					.invokeAndWait(new Runnable() {
						public void run() {
						    tf.requestFocusInWindow();
						}
					    });
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
					if (!tf.requestFocusInWindow()) {
					    tryAgain();
					}
								   
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
		    /*
		    JOptionPane.showMessageDialog
			(frame,
			 tf,
			 title,
			 JOptionPane.PLAIN_MESSAGE);
		    outputValue(tf.getText());
		    */
		    int choice = JOptionPane.showOptionDialog
			(frame, tf, title, JOptionPane.OK_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, null, null);
		    if (choice == 0) {
			if (outputMode) outputValue(tf.getText());
			 if (inputMode) tf.setText("");
		    } else {
			if (inputMode) {
			    // ta.setText(inputValue());
			    tf.setText("");
			} else {
			    tf.setText(old);
			}
		    }
		}
	    });
    }
}

//  LocalWords:  exbundle TextFieldButton outputValue inputValue tfb
//  LocalWords:  blockquote JFrame appFrame TCP PortTextField portTF
//  LocalWords:  setValue getText getValue setDocumentFilter nchars
//  LocalWords:  DocumentFilter JComponent setInputVerifier textField
//  LocalWords:  InputVerifier TextFieldButton's TextAreaButton tf
//  LocalWords:  textField's IllegalArgumentException ncharsNegative
//  LocalWords:  requestFocusInWindow showMessageDialog JOptionPane
//  LocalWords:  setText
