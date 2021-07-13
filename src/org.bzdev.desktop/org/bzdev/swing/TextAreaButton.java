package org.bzdev.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Button to pop up a dialog box containing a text area.
 * This class arranges that the text area has the keyboard focus when
 * the text area appears. By default, the state of the text area is
 * read and written via one of two protected methods that the
 * user should implement:
 * {@link TextAreaButton#outputValue(String)} and
 * {@link TextAreaButton#inputValue()}.
 * <P>
 * For example,
 * <blockquote><code>
 * public class Application {
 *    JFrame appFrame = new JFrame("Application");
 *    String value = "initial value";
 *    TextFieldButton tfb =
 *         new TextAreaButton("Set Value", 32, 10, appFrame, "Value") {
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
 * If the text area should be validated or should have some
 * restriction on the characters that can be entered, use a
 * constructor that explicitly provides the text area.  For arbitrary
 * restrictions on the contents of a text area, if the text area's
 * document is a subclass of {@link javax.swing.text.AbstractDocument},
 *  one may call the method
 * {@link javax.swing.text.AbstractDocument#setDocumentFilter(javax.swing.text.DocumentFilter)}.
 * Its argument (of type {@link javax.swing.text.DocumentFilter}) will
 * provide some control over modifications to the document.  One may
 * also use the text area's method
 * {@link JComponent#setInputVerifier(InputVerifier)}, which will
 * prevent the keyboard focus from changing if the input is not valid.
 */
public class TextAreaButton extends JButton
{
    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    /**
     * Set the output value.
     * The user of this class should override this method to store
     * text. Whether this method is called depends on the TextAreaButton's
     * mode.
     * @param value the text of the component
     * @see org.bzdev.swing.TextAreaButton.Mode
     */
    protected void outputValue(String value) {}

    /**
     * Read the component's text from an external source.  This method
     * is expected to return the same value until the dialog box for
     * this component is closed.  Whether this method is called
     * depends on the TextAreaButton's mode.
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
     * Constructor with mode.
     * @param label the button label
     * @param rows  the number of rows for text (must not be negative)
     * @param cols the number of columns for text (must not be negative)
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param mode the mode for the component
     * @see TextAreaButton.Mode
     */
    public TextAreaButton(String label, int rows, int cols,
			  Component frame, String title, Mode mode)
    {
	this(null, label, rows, cols, frame, title, mode);
    }
    /**
     * Constructor with mode and specifying a JTextArea to use.
     * @param textArea the text area this button will cause to be
     *        displayed
     * @param label the button label
     * @param rows  the number of rows for text (must not be negative)
     * @param cols the number of columns for text (must not be negative)
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     * @param mode the mode for the component
     * @see TextAreaButton.Mode
     */
    public TextAreaButton(JTextArea textArea, String label,
			  final int rows, final int cols,
			  final Component frame,
			  final String title, Mode mode)
    {
	this(label, rows, cols, frame, title);
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

    JTextArea ta;
    JScrollPane tsp;

    /**
     * Get the text of the text-area component.
     * When the mode is USE_OUTPUT_NO_STATE (the default), the value
     * returned is the value at the time the button was pressed.
     * Otherwise it is the component's current value.
     * @return the component's text
     */
    public String getTextAreaText() {
	if (inputMode) {
	    return inputValue();
	} else {
	    return ta.getText();
	}
    }

    /**
     * Constructor with a default mode of USE_OUTPUT_NO_STATE.
     * @param label the button label
     * @param rows  the number of rows for text
     * @param cols the number of columns for text
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     */
    public TextAreaButton (String label, final int rows, final int cols,  
			   final Component frame,
			   final String title)
    {
	this(null, label, rows, cols, frame, title);
    }
    /**
     * Constructor with a default mode of USE_OUTPUT_NO_STATE, specifying
     * a text area.
     * @param textArea the text area this button will cause to be
     *        displayed
     * @param label the button label
     * @param rows  the number of rows for text; a negative value if the
     *        value provided by textArea should not be changed
     * @param cols the number of columns for text; a negative value if the
     *        value provided by textArea should not be changed
     * @param frame the frame on which to center a dialog box; null if none
     * @param title the title of the dialog box
     */
    public TextAreaButton (JTextArea textArea, String label,
			   int rows, int cols,
			   final Component frame,
			   final String title)
    {
	super(label);
	if (textArea == null) {
	    if (rows < 0) {
		throw new IllegalArgumentException
		    (errorMsg("rowsNegative", rows));
	    }
	    if (cols < 0) {
		throw new IllegalArgumentException
		    (errorMsg("colsNegative", cols));
	    }
	    ta = new JTextArea(rows, cols);
	} else {
	    if (rows >= 0) textArea.setRows(rows);
	    if (cols >= 0) textArea.setColumns(cols);
	    ta = textArea;
	}
	tsp = new JScrollPane(ta,
			      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
						    ta.requestFocusInWindow();
						}
					    });
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
			ta.setText(inputValue());
			ta.addAncestorListener
			    (new AncestorListener() {
				    public void
					ancestorAdded (AncestorEvent e)
				    {
					if (!ta.requestFocusInWindow()) {
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
			ta.setText(inputValue());
		    } else {
			old = ta.getText();
		    }
		    int choice = JOptionPane.showOptionDialog
			(frame, tsp, title, JOptionPane.OK_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, null, null);
		    if (choice == 0) {
			if (outputMode) outputValue(ta.getText());
			 if (inputMode) ta.setText("");
		    } else {
			if (inputMode) {
			    ta.setText("");
			} else {
			    ta.setText(old);
			}
		    }
		}
	    });
    }
}

//  LocalWords:  exbundle TextAreaButton outputValue inputValue tfb
//  LocalWords:  blockquote JFrame appFrame TextFieldButton JTextArea
//  LocalWords:  setDocumentFilter JComponent setInputVerifier
//  LocalWords:  InputVerifier TextAreaButton's textArea rowsNegative
//  LocalWords:  colsNegative requestFocusInWindow showMessageDialog
