package org.bzdev.swing.table;
import java.awt.Color;
import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import org.bzdev.graphs.Colors;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.swing.table.lpack.CSSTableCellRenderer

/*
 * Modified from a tutorial example that containined the following
 * legalease:
 * ------------
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
 * ------------
 *
 * The example in the tutorial rendered colors.  The modification
 * assumes that what is stored in a table is a string giving the CSS
 * specification for a color.
 */

/**
 * Table cell renderer for colors represented by a CSS specification.
 * @see org.bzdev.graphs.Colors
 */

public class CSSTableCellRenderer extends JLabel
                           implements TableCellRenderer {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.swing.table.lpack.CSSTableCellRenderer");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }
    
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;

    /**
     * Constructor.
     * @param isBordered true if this cell renderer should use a border;
     *                   false otherwise
     */
    public CSSTableCellRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    @Override
    public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
	String spec = (String)color;
	if (spec == null) {
	    spec = "";
	} else {
	    spec = spec.trim();
	}
	boolean empty = spec.equals("");
        Color newColor = empty? null:  Colors.getColorByCSS(spec);
        setBackground(newColor);
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }
	if (empty) {
	    setToolTipText(errorMsg("noColor"));
	} else {
	    setToolTipText(spec);
	}
        return this;
    }
}
