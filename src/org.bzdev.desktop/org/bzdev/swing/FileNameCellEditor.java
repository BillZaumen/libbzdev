package org.bzdev.swing;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.TreeCellEditor;
import org.bzdev.util.SafeFormatter;


/*
 * This class is based on an example in the java tutorials for a
 * color editor, which contains the following notice:
 * ------------------
* Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ------------------
 *
 * The modifications first created a cell editor for colors, which was
 * in turn modified to create a cell editor for file names.
 *
 */


/**
 * Table cell editor for file names.
 * The file name is stored in a table as text, but will be edited using a
 * {@link JFileChooser}. 
 * An example of when this class might be used is when an instance of
 * {@link ConfigPropertyEditor} includes entries providing file names
 * and when one wants to use a file-chooser to pick appropriate files.
 * <P>
 * Relative file names are resolved relative to the current working
 * directory in effect when the constructor of this class is called.
 * <P>
 * The code is based on an example in the Java swing tutorial.
 * @see ConfigPropertyEditor
 */
public class FileNameCellEditor extends AbstractCellEditor
    implements TableCellEditor, TreeCellEditor, ActionListener
{
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.swing.lpack.FileNameCellEditor");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    Component parent;
    File  currentFile;
    JButton button;
    ClearableFileChooser fileChooser;
    File cwd = new File(System.getProperty("user.dir"));
    JDialog dialog;
    boolean existing = false;

    private static final String EDIT = "edit";

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
     * @param filter the file filter to add
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
     * 
     */
    public int getFileSelectionMode() {
	return fileChooser.getFileSelectionMode();
    }

    /**
     * Gets the string that goes in the dialog's title bar.
     * @see #setDialogTitle
     * @return the title
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


    /**
     * Constructor specifying a title.
     * @param title the title of dialog boxes that this class creates.
     * @param allowNewFiles true if new files (not yet existing) can be
     *        provided; false otherwise
     */
    public FileNameCellEditor(String title, boolean allowNewFiles) {
	this(allowNewFiles);
	fileChooser.setDialogTitle(title);
    }

    /**
     * Constructor.
     * @param allowNewFiles true if new files (not yet existing) can be
     *        provided; false otherwise
     */
    public FileNameCellEditor(boolean allowNewFiles) {
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setBorderPainted(false);

        //Set up the dialog that the button brings up.
        fileChooser = new ClearableFileChooser(cwd, allowNewFiles);

	parent = button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
	    String name = (currentFile == null)? "":
		currentFile.getName();
            button.setText(name);
	    
            fileChooser.setSelectedFile(currentFile);
	    switch (fileChooser.showDialog(parent)) {
	    case ClearableFileChooser.APPROVE_OPTION:
		currentFile = fileChooser.getSelectedFile();
		break;
	    case ClearableFileChooser.CLEAR_OPTION:
		currentFile = null;
		break;
	    case ClearableFileChooser.ERROR_OPTION:
	    case ClearableFileChooser.CANCEL_OPTION:
		break;
	    }

            fireEditingStopped(); //Make the renderer reappear.

        } else { //User pressed dialog's "OK" button.
            currentFile = fileChooser.getSelectedFile();
        }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    @Override
    public Object getCellEditorValue() {
	try {
	    return currentFile == null? null: currentFile.getCanonicalPath();
	} catch (IOException e) {
	    return null;
	}
    }

    //Implement the one method defined by TableCellEditor.
    @Override
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
	String fn = (value == null)? "": ((String)value).trim();

	if (fn.equals("")) {
	    currentFile = null;
	} else {
	    File f = new File(fn);
	    currentFile = f.isAbsolute()? f: new File(cwd, fn);
	}
	parent = table;
        return button;
    }

    //Implement the one method defined by TreeCellEditor.
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
						boolean isSelected,
						boolean expanded,
						boolean leaf,
						int row)
    {
	String fn = (value == null)? "": ((String)value).trim();

	if (fn.equals("")) {
	    currentFile = null;
	} else {
	    File f = new File(fn);
	    currentFile = f.isAbsolute()? f: new File(cwd, fn);
	}
	parent = tree;
        return button;
    }
}
