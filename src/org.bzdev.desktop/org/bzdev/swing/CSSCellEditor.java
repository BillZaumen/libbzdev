package org.bzdev.swing;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.table.*;
import javax.swing.tree.TreeCellEditor;
import org.bzdev.graphs.Colors;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.swing.lpack.CSSCellEditor

/*
 * This class is based on an example in the java tutorials, which
 * contains the following notice:
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
 * The modifications allow the color as stored in a JTable to be
 * represented by a CSS specification (i.e., as a string).
 */


/**
 * Table cell editor for CSS color specifications.
 * The color is stored in a table as text, but will be edited using a
 * color chooser.  The color chooser can use CSS named colors if desired.
 * An example of when this class might be used is when an instance of
 * {@link ConfigPropertyEditor} includes entries providing colors:
 * while a color should be displayed, it has to be stored as text for
 * the property editor to save its configuration.
 *  <P>
 * The code is based on an example in the Java swing tutorial.
 * @see ConfigPropertyEditor
 */
public class CSSCellEditor extends AbstractCellEditor
    implements TableCellEditor, TreeCellEditor, ActionListener
{
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.swing.lpack.CSSCellEditor");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    Component parent;
    Component dialogParent;
    Color currentColor;
    JButton button;
    JColorChooser colorChooser;
    JDialog dialog;
    private static final String EDIT = "edit";

    /**
     * Constructor.
     */
    public CSSCellEditor() {
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setBorderPainted(false);

        //Set up the dialog that the button brings up.
        colorChooser = new JColorChooser();
	// Add additional panels as needed
	String swatchName =
	    UIManager.getString("ColorChooser.swatchesNameText",
				java.util.Locale.getDefault());
	AbstractColorChooserPanel swatchPanel = null;
	for (AbstractColorChooserPanel ccp:
		 ColorChooserComponentFactory.getDefaultChooserPanels()) {
	    if(ccp.getDisplayName().equals(swatchName)) {
		swatchPanel = ccp;
		break;
	    }
	}
	boolean hasSwatch = false;
	for (AbstractColorChooserPanel ccp: colorChooser.getChooserPanels()) {
	    if(ccp.getDisplayName().equals(swatchName)) {
		hasSwatch = true;
		break;
	    }
	}
	if (hasSwatch == false && swatchPanel != null) {
	    colorChooser.addChooserPanel(swatchPanel);
	}
	colorChooser.addChooserPanel(new CSSColorChooserPanel());

	parent = button;
	dialogParent = button;
	String msg = errorMsg("pick");
        dialog = JColorChooser.createDialog(dialogParent,
					    msg,
					    true,  //modal
					    colorChooser,
					    this,  //OK button handler
					    null); //no CANCEL button handler
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
            button.setBackground(currentColor);
            colorChooser.setColor(currentColor);
	    dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            fireEditingStopped(); //Make the renderer reappear.

        } else { //User pressed dialog's "OK" button.
            currentColor = colorChooser.getColor();
        }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    @Override
    public Object getCellEditorValue() {
        return currentColor == null? null: Colors.getCSS(currentColor);
    }

    //Implement the one method defined by TableCellEditor.
    @Override
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
	if (value == null || ((String)value).trim().equals("")) {
	    currentColor = null;
	} else {
	    currentColor = Colors.getColorByCSS((String)value);
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
	if (value == null || ((String)value).trim().equals("")) {
	    currentColor = null;
	} else {
	    currentColor = Colors.getColorByCSS((String)value);
	}
	parent = tree;
        return button;
    }
}
