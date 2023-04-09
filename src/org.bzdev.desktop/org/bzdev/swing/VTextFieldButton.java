package org.bzdev.swing;
import org.bzdev.lang.Callable;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Button that displays a dialog box containing a {@link VTextField}
 * with an optional message.
 * <P>
 * The {@link VTextField} is expected to implement the method
 * {@link VTextField#handleError()} if the input can contain any errors.
 * The implementation of this method should either correct the error
 * or provide the user with enough information that the user will not
 * assume that the value was changed. Some classes such as
 * {@link PortTextField} have implemented this method in this way. The text
 * field will respond to the ESCAPE character by aborting a change and
 * keeping the previous value.  The character RETURN (or ENTER) will
 * accept the input as long as it is valid. An OK or CANCEL button can
 * be used as alternatives.  If the dialog box has a CLOSE button,
 * clicking on that button will result in a dialog asking if the user
 * intended to cancel the change.  If the user pushes this dialog's
 * "no" button, the user can continue editing the text field.  When
 * the dialog box becomes visible, the text field will have the
 * keyboard focus.
 * <P>
 * The following code provides an example of how this class can be used:
 * <BLOCKQUOTE><PRE><CODE>
 * JFrame frame = ...;
 * JMenubar menubar = ...;
 * ...
 * JMenu fileMenu = new JMenu("File");
 * ...
 * int port = 0;
 *
 * PortTextField ptf = new PortTextField(5) {
 *     	public void onAccepted() throws Exception {
 *         super.onAccepted();
 *         port = getValue();
 *      }
 * };
 * ptf.setAllowEmptyTextField(true);
 * pf.setDefaultValue(0);
 *
 * JButton portMenuItem = new
 *    VTextFieldMenuItem(ptf, "HTTP Port", frame, "HTTP TCP Port",
 *                       "Please enter the TCP port to use:",
 *                       true);
 *
 * fileMenu.add(portMenuItem);
 * ...
 * menubar.add(fileMenu);
 * </CODE></PRE></BLOCkQUOTE>
 */
public class VTextFieldButton extends JButton {
    JDialog dialog;
    String old = null;

    static final String abortMessage = SwingErrorMsg.errorMsg("abortMessage");
    static final String abortTitle = SwingErrorMsg.errorMsg("abortTitle");

    private void finishInit(Window w, VTextField tf, String msg)
    {
	if (msg != null) {
	    dialog.add(new JLabel(msg), BorderLayout.NORTH);
	}
	dialog.add(tf, BorderLayout.CENTER);
	JPanel panel = new JPanel();
	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");
	okButton.addActionListener(e1 -> {
		dialog.setVisible(false);
	    });
	cancelButton.addActionListener(e1 -> {
		tf.setText(old);
		dialog.setVisible(false);
	    });
	panel.add(okButton);
	panel.add(cancelButton);
	dialog.add(panel, BorderLayout.SOUTH);
	dialog.pack();
	tf.addActionListener(envt -> {
		// tf.getValidatedText();
		dialog.setVisible(false);
	    });
	addActionListener(evt -> {
		old = tf.getText();
		dialog.setLocationRelativeTo(w);
		dialog.setVisible(true);
		SwingUtilities.invokeLater(() -> {
			tf.requestFocusInWindow();
		    });
	    });

	tf.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
		    int modifiers = e.getModifiersEx();
		    if (modifiers == 0) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			    tf.setText(old);
			    dialog.setVisible(false);
			}
		    }
		}
	    });
	dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	dialog.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    if (JOptionPane.showConfirmDialog
			(dialog, abortMessage, abortTitle,
			 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			tf.setText(old);
			dialog.setVisible(false);
		    }
		}
	    });
    }
 
    /**
     * Constructor given a {@link Dialog} owner.
     * @param owner the window on which a dialog box should be centered
     * @param tf the text field
     * @param label the menu label
     * @param title the dialog-box title
     * @param msg the message that will appear in the dialog box
     *        above the text field
     * @param modal true for a modal dialog; false otherwise
     */
    public VTextFieldButton(VTextField tf, String label,
			    Dialog owner, String title, String msg,
			    boolean modal)
    {
	super(label);
	dialog = new JDialog(owner, title, modal);
	finishInit(owner, tf, msg);
    }

    /**
     * Constructor given a {@link Frame} owner.
     * @param tf the text field
     * @param label the menu label
     * @param owner the window on which a dialog box should be centered
     * @param title the dialog-box title
     * @param msg the message that will appear in the dialog box
     *        above the text field
     * @param modal true for a modal dialog; false otherwise
     */
    public VTextFieldButton(VTextField tf, String label,
			      Frame owner, String title, String msg,
			      boolean modal)
    {
	super(label);
	dialog = new JDialog(owner, title, modal);
	finishInit(owner, tf, msg);
    }

    /**
     * Constructor given a {@link Window} owner.
     * @param tf the text field
     * @param label the menu label
     * @param owner the window on which a dialog box should be centered
     * @param title the dialog-box title
     * @param msg the message that will appear in the dialog box
     *        above the text field
     * @param modal true for a modal dialog; false otherwise
     */
    public VTextFieldButton(VTextField tf, String label,
			      Window owner,String title, String msg,
			      boolean modal)
    {
	super(label);
	dialog = new JDialog(owner, title, modal?
			     JDialog.DEFAULT_MODALITY_TYPE:
			     Dialog.ModalityType.MODELESS);
	finishInit(owner, tf, msg);
    }

    /**
     * Free resources associated with this button.
     * After this method is called, the button must not be used.
     */
    public void dispose() {
	dialog.dispose();
    }
}
