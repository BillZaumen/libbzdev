package org.bzdev.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.Document;
import org.bzdev.util.SafeFormatter;
import java.beans.*;

//@exbundle org.bzdev.swing.lpack.ClearableFileChooser

/**
 * File chooser that can distiguish between canceling the dialog and
 * indicating that no file is provided.
 * <P>
 * This class is used to implement {@link FileNameCellEditor}. For this
 * dialog,
 * <UL>
 *   <LI> The "Accept" button will set the call's value to  the selected file.
 *   <LI> The "Clear" button will remove the value from cell.
 *   <LI> The "Cancel" button will leave the value in the cell unchanged.
 * </UL>
 */
public class ClearableFileChooser {
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.swing.lpack.ClearableFileChooser");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
    
    JFileChooser fileChooser;
    // File selectedFile = null;

    File selectedFile = null;

    /**
     * Returns the selected file. This can be set either by the
     * programmer via <code>setSelectedFile</code> or by a user action, such as
     * either typing the filename into the UI or selecting the
     * file from a list in the UI.
     * @return the selected file
     * @see #setSelectedFile
     */
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Sets the selected file. This can be set either by the
     * programmer via <code>setSelectedFile</code> or by a user action, such as
     * either typing the filename into the UI or selecting the
     * file from a list in the UI.
     * @param f the file
     * @see #getSelectedFile
     */
    public void setSelectedFile(File f) {
	fileChooser.setSelectedFile(f);
	selectedFile = f;
    }

    /**
     * Return value if approve (yes, ok) is chosen.
     */
    public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;

    /**
     * Return value if cancel is chosen.
     */
    public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;

    /**
     * REturn value if clear is chosen.
     */
    public static final int CLEAR_OPTION = 2;

     /**
     * Return value if an error occurred.
     */
   public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

    private boolean allowNewFiles = true;

    /**
     * Constructor.
     * @param cdir the working directory; null for the current working
     *        directory
     * @param allowNewFiles true if new files (not yet existing) can be
     *        provided; false otherwise
     */
    public ClearableFileChooser(File cdir, boolean allowNewFiles) {
	this(cdir, null, allowNewFiles);
    }

    /**
     * Constructor providing a file system view.
     * @param cdir the working directory; null for the current working
     *        directory
     * @param fsv a file system view
     * @param allowNewFiles true if new files (not yet existing) can be
     *        provided; false otherwise
     */
    public ClearableFileChooser(File cdir, FileSystemView fsv,
				boolean allowNewFiles)
    {
	this.allowNewFiles = allowNewFiles;
	if (cdir == null) cdir = new File (System.getProperty("user.dir"));
	if (allowNewFiles) {
	    fileChooser = fsv == null? new JFileChooser(cdir):
		new JFileChooser(cdir, fsv);
	} else {
	    Boolean ro = UIManager.getBoolean("FileChooser.readOnly");
	    UIManager.put("FileChooser.readOnly",
			  allowNewFiles? Boolean.FALSE: Boolean.TRUE);
	    fileChooser = fsv == null? new JFileChooser(cdir):
		new JFileChooser(cdir, fsv);
	    UIManager.put("FileChooser.readOnly", ro);
	}
    }

    /**
     * Sets the current file filter.  The file filter is used by the
     * file chooser to filter out files from the user's view.
     * @param filter the file filter
     */
    public void setFileFilter(FileFilter filter) {
	fileChooser.setFileFilter(filter);
    }

    /**
     * Returns the currently selected file filter.
     * @return the current file filter.
     */
    public FileFilter getFileFilter() {
	return fileChooser.getFileFilter();
    }

    /**
     * Adds a filter to the list of user choosable file filters.
     *  For information on setting the file selection mode, see 
     *  {@link #setFileSelectionMode(int)}.
     */
    public void addChoosableFileFilter(FileFilter filter) {
	fileChooser.addChoosableFileFilter(filter);
    }

    /**
     * Removes a filter from the list of user choosable file filters.
     * @param filter the file filter to remove
     * @return true if the file filter was removed; false otherwise.
     */
    public boolean removeChoosableFileFilter(FileFilter filter) {
	return fileChooser.removeChoosableFileFilter(filter);
    }

    /**
     * Resets the choosable file filter list to its starting state.
     * Normally, this removes all added file filters while leaving
     * the AcceptAll file filter.
     */
    public void resetChoosableFileFilters() {
	fileChooser.resetChoosableFileFilters();
    }


    /**
     * Returns whether the AcceptAll FileFilter is used.
     * @return true if the AcceptAll FileFilter is used; false otherwise
     */
    public boolean isAcceptAllFileFilterUsed() {
	return fileChooser.isAcceptAllFileFilterUsed();
    }


    /**
     * Determines whether the AcceptAll FileFilter is used as an
     * available choice in the choosable filter list.
     * If the argument is false, the AcceptAll file filter is removed
     * from the list of available file filters. If true, the AcceptAll
     * file filter will become the actively used file filter.
     * @param b true if the AcceptAll FileFilter will be used; false
     *          otherwise
     */
    public void setAcceptAllFileFilterUsed(boolean b) {
	fileChooser.setAcceptAllFileFilterUsed(b);
    }

    /**
     * Sets the JFileChooser to allow the user to just select files,
     * just select directories, or select both files and directories.
     * The default is JFilesChooser.FILES_ONLY.
     * @param mode the type of files to be displayed: {@link
     *        JFileChooser#FILES_ONLY}, {@link JFileChooser#DIRECTORIES_ONLY},
     *        or {@link JFileChooser#FILES_AND_DIRECTORIES}
     * @exception IllegalArgumentException &emdash; if mode is an illegal
     *            file-selection mode
     */
    public void setFileSelectionMode(int mode) {
	fileChooser.setFileSelectionMode(mode);
    }

    /**
     * Returns the current file-selection mode.
     * The default is JFilesChooser.FILES_ONLY.
     * @return the type of files to be displayed:
     *         {@link JFileChooser#FILES_ONLY},
     *         {@link JFileChooser#DIRECTORIES_ONLY},
     *         or {@link JFileChooser#FILES_AND_DIRECTORIES}
     */
    public int getFileSelectionMode() {
	return fileChooser.getFileSelectionMode();
    }

    /**
     * Gets the string that goes in the dialog's title bar.
     * @see #setDialogTitle
     */
    public String getDialogTitle() {
	return fileChooser.getDialogTitle();
    }


     /**
     * Sets the string that goes in the dialog's title bar.
     *
     * @param dialogTitle the new <code>String</code> for the title bar
     *
     * @see #getDialogTitle
     *
     */
   public void setDialogTitle(String dialogTitle) {
       fileChooser.setDialogTitle(dialogTitle);
    }

    private static JTextField findTextField(JComponent c) {
	try {
	    for (int i = 0; i < c.getComponentCount(); i++) {
		Component ci = c.getComponent(i);
		JTextField tmp = null;
		if (ci instanceof JPanel) {
		    tmp = findTextField((JComponent)ci);
		}
		if (tmp != null) {
		    return (JTextField) tmp;
		} else if (ci instanceof JTextField) {
		    return (JTextField)ci;
		}
	    }
	} catch (Exception e){}
	return null;
    }
    
    JComponent bc = null;
    JButton b1 = null;
    JButton b2 = null;
    int i1 = -1; int i2 = -1;
    private  void removeStdButtons(JComponent c, String text)  {
	try {
	    JButton b1 = null;
	    JButton b2 = null;
	    int i1 = -1; int i2 = -1;
	    for (int i = 0; i < c.getComponentCount(); i++) {
		Component ci = c.getComponent(i);
		if (ci instanceof JPanel) {
		    removeStdButtons((JComponent)ci, text);
		} else if (ci instanceof JButton) {
		    JButton b = (JButton) ci;
		    String txt = b.getText();
		    if (txt != null && txt.length() > 0) {
			if (b1 == null) {
			    b1 = b;
			    i1 = i;
			} else if (b2 == null) {
			    b2 = b;
			    i2 = i;
			}
		    }
		}
	    }
	    if (b1 != null && b2 != null) {
		if (b1.getText().equals(text)
		    || b2.getText().equals(text)) {
		    bc = c;
		    this.b1 = b1;
		    this.b2 = b2;
		    this.i1 = i1;
		    this.i2 = i2;
		    c.remove(i2);
		    c.remove(i1);
		}
	    }
	} catch (Exception e){}
    }
    private void restoreStdButtons() {
	if (bc != null && b1 != null && b2 != null) {
	    bc.add(b1, i1);
	    bc.add(b2, i2);
	    bc = null;
	    b1 = null;
	    b2 = null;
	    i1 = -1;
	    i2 = -1;
	}
    }
    private static final String APPROVE_BUTTON_TEXT = errorMsg("SELECT");
    private static final String CLEAR_BUTTON_TEXT = errorMsg("CLEAR");
    private static final String CANCEL_BUTTON_TEXT  = errorMsg("CANCEL");

    private static final String APPROVE_BUTTON_TIP = errorMsg("SELECT_TOOLTIP");
    private static final String CLEAR_BUTTON_TIP = errorMsg("CLEAR_TOOLTIP");
    private static final String CANCEL_BUTTON_TIP  = errorMsg("CANCEL_TOOLTIP");


    private String approveButtonText = APPROVE_BUTTON_TEXT;
    private String clearButtonText = CLEAR_BUTTON_TEXT;
    private String cancelButtonText = CANCEL_BUTTON_TEXT;

    private String approveButtonTooltip = APPROVE_BUTTON_TIP;
    private String clearButtonTooltip = CLEAR_BUTTON_TIP;
    private String cancelButtonTooltip = CANCEL_BUTTON_TIP;


    /**
     * Get the text for the Approve button.
     * @return the text
     */
    public String getApproveButtonText() {
	return approveButtonText;
    }

    /**
     * Get the text for the Clear button.
     * @return the text
     */
    public String getClearButtonText() {
	return clearButtonText;
    }

    /**
     * Get the text for the Cancel button.
     * @return the text
     */
    public String getCancelButtonText() {
	return cancelButtonText;
    }

    /**
     * Set the text for the Approve button.
     * @param text the text; null for a default
     */
    public void setApproveButtonText(String text) {
	approveButtonText = text == null? APPROVE_BUTTON_TEXT: text;
    }

    /**
     * Set the text for the Clear button.
     * @param text the text; null for a default
     */
    public void setClearButtonText(String text) {
	clearButtonText = text == null? CLEAR_BUTTON_TEXT: text;;
    }

    /**
     * Set the text for the Cancel button.
     * @param text the text; null for a default
     */
    public void setCancelButtonText(String text) {
	cancelButtonText = text == null? CANCEL_BUTTON_TEXT: text;;
    }


    /**
     * Get the tooltip for the Approve button.
     * @return the text
     */
    public String getApproveButtonTooltip() {
	return approveButtonTooltip;
    }

    /**
     * Get the tooltip for the Clear button.
     * @return the text
     */
    public String getClearButtonTooltip() {
	return clearButtonTooltip;
    }

    /**
     * Get the tooltip for the Cancel button.
     * @return the text
     */
    public String getCancelButtonTooltip() {
	return cancelButtonTooltip;
    }

    /**
     * Set the tooltip for the Approve button.
     * @param text the text; null for a default
     */
    public void setApproveButtonTooltip(String text) {
	approveButtonTooltip = text == null? APPROVE_BUTTON_TIP: text;
    }

    /**
     * Set the tooltip for the Clear button.
     * @param text the text; null for a default
     */
    public void setClearButtonTooltip(String text) {
	clearButtonTooltip = text == null? CLEAR_BUTTON_TIP: text;
    }

    /**
     * Set the tooltip for the Cancel button.
     * @param text the text; null for a default
     */
    public void setCancelButtonTooltip(String text) {
	cancelButtonTooltip = text == null? CANCEL_BUTTON_TIP: text;
    }


    private int status = -1;

    private boolean tfHasFocus = false;

    /**
     * Show a file-chooser dialog.
     * @param parent the component on which the dialog may be centered
     */
    public int showDialog(Component parent)
    {
	File originalFile = selectedFile;
	Component top = SwingUtilities.getRoot(parent);
	Frame fOwner = null;
	Dialog dOwner = null;
	Window wOwner = null;
	if (top instanceof Frame) {
	    fOwner = (Frame) top;
	} else if (top instanceof Dialog) {
	    dOwner = (Dialog) top;
	} else if (top instanceof Window) {
	    wOwner = (Window) top;
	}

	fileChooser.setMultiSelectionEnabled(false);
	fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
	// fileChooser.setSelectedFile(selectedFile);
	JTextField tf = findTextField(fileChooser);
	tf.setEditable(allowNewFiles);
	fileChooser.setAcceptAllFileFilterUsed(true);
	    
	JPanel panel = new JPanel(new BorderLayout());
	// panel.add(this, BorderLayout.CENTER);
	panel.add(fileChooser, BorderLayout.CENTER);
	// setControlButtonsAreShown(false);
	fileChooser.setApproveButtonText(approveButtonText);
	removeStdButtons(fileChooser, fileChooser.getApproveButtonText());
	JPanel ctlpane = new JPanel(new FlowLayout(FlowLayout.TRAILING));

	JButton approveButton = new JButton (approveButtonText);
	JButton clearButton = new JButton (clearButtonText);
	JButton cancelButton = new JButton(cancelButtonText);
	approveButton.setToolTipText(approveButtonTooltip);
	clearButton.setToolTipText(clearButtonTooltip);
	cancelButton.setToolTipText(cancelButtonTooltip);
	ctlpane.add(approveButton);
	ctlpane.add(clearButton);
	ctlpane.add(cancelButton);
	panel.add(ctlpane, BorderLayout.SOUTH);
	String title = fileChooser.getDialogTitle();
	JDialog dialog = (fOwner != null)? new
	    JDialog(fOwner, title, true):
	    ((dOwner != null)? new JDialog(dOwner, title, true):
	     ((wOwner != null)? new
	      JDialog(wOwner, title, Dialog.ModalityType.APPLICATION_MODAL):
	      new JDialog((Frame)null, title,  true)));
	if (top != null) {
	    dialog.setLocationRelativeTo(top);   
	}
	DocumentListener dl = null;
	PropertyChangeListener pcl = null;
	FocusListener fl = null;

	if (allowNewFiles) {
	    
	    fl = new FocusListener() {
		    public void focusGained(FocusEvent fe) {
			tfHasFocus = true;
		    }
		    public void focusLost(FocusEvent fe) {
			tfHasFocus = false;
		    }
		};

	    dl = new DocumentListener() {
		    public void changedUpdate(DocumentEvent event){}
		    public void insertUpdate(DocumentEvent event) {
			File sf = fileChooser.getSelectedFile();
			if (tfHasFocus) {
			    String text = tf.getText();
			    if (text.length() > 0) {
				File f = new File
				    (fileChooser.getCurrentDirectory(),
				     tf.getText());
				selectedFile = f;
				approveButton.setEnabled(true);
			    } else {
				selectedFile = null;
				approveButton.setEnabled(false);
			    }
			}
		    }
		    public void removeUpdate(DocumentEvent event) {
			File sf = fileChooser.getSelectedFile();
			String text = tf.getText();
			if (tfHasFocus) {
			    if (text.length() > 0) {
				File f = new File
				    (fileChooser.getCurrentDirectory(),
				     tf.getText());
				selectedFile = f;
				approveButton.setEnabled(true);
			    } else {
				selectedFile = null;
				approveButton.setEnabled(false);
			    }
			}
		    }
		};
	    tf.getDocument().addDocumentListener(dl);
	    tf.addFocusListener(fl);
	}

	PropertyChangeListener plc = (pce) -> {
	    String pname = pce.getPropertyName();
	    if (pname.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
		tf.setText("");
		selectedFile = null;
		approveButton.setEnabled(false);
	    } else if (pname.equals(JFileChooser
				    .SELECTED_FILE_CHANGED_PROPERTY)) {
		boolean hasSelection =
		    (fileChooser.getSelectedFile() != null);
		    
		selectedFile = fileChooser.getSelectedFile();
		approveButton.setEnabled(hasSelection);
		clearButton.setEnabled(hasSelection);
	    }
	};
	fileChooser.addPropertyChangeListener(plc);
	status = -1;
	ActionListener actionListener = (ae) -> {
	    Object src = ae.getSource();
	    if (src == approveButton) {
		status =  ClearableFileChooser.APPROVE_OPTION;
	    } else if (src == clearButton) {
		fileChooser.setSelectedFile(null);
		selectedFile = null;
		status = ClearableFileChooser.CLEAR_OPTION;
	    } else if (src == cancelButton) {
		status = ClearableFileChooser.CANCEL_OPTION;
		selectedFile = originalFile;
	    }
	    if (status > -1) {
		dialog.setVisible(false);
	    }
	};
	approveButton.addActionListener(actionListener);
	clearButton.addActionListener(actionListener);
	cancelButton.addActionListener(actionListener);
	approveButton.setEnabled(fileChooser.getSelectedFile() != null);

	dialog.add(panel);
	dialog.pack();
	dialog.setVisible(true);
	restoreStdButtons();
	dialog.getContentPane().removeAll();
	dialog.dispose();
	fileChooser.removePropertyChangeListener(plc);
	tf.getDocument().removeDocumentListener(dl);
	tf.removeFocusListener(fl);
	if (status == -1) status = ERROR_OPTION;
	return status;
    }
}

