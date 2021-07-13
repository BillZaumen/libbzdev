package org.bzdev.swing;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Menu Item for toggling stack traces for the SwingErrorMessage class.
 * If {@link SwingErrorMessage#setStackTrace(boolean)} is called, all
 * instance of StackTraceMenuItem will adjust their states.
 * <P>
 * If this class is to be permanently removed from a menu, the
 * method {@link StackTraceMenuItem#dispose()} should be called
 * to prevent unneeded computations and to allow garbage collection of
 * this object.
 * @see SwingErrorMessage
 */
public class StackTraceMenuItem extends JCheckBoxMenuItem {
    ActionListener al;
    ChangeListener cl;

    /**
     * Constructor.
     * @param label the label of the menu item
     */
    public StackTraceMenuItem(String label) {
	super(label, SwingErrorMessage.stackTraceEnabled());

	al = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    SwingErrorMessage.setStackTrace
			(StackTraceMenuItem.this.isSelected());
		}
	    };

	addActionListener(al);

	cl = new ChangeListener() {
		public void stateChanged(ChangeEvent event) {
		    StackTraceMenuItem.this.setSelected
			(SwingErrorMessage.stackTraceEnabled());
		}
	    };
	SwingErrorMessage.addChangeListener(cl);
    }

    /**
     * Clean up the internal state of this StackTraceMenuItem so that the
     * item can be garbage collected once all references to it are dropped.
     */
    public void dispose() {
	removeActionListener(al);
	al = null;
	SwingErrorMessage.removeChangeListener(cl);
	cl = null;
    }
}

//  LocalWords:  SwingErrorMessage setStackTrace boolean
//  LocalWords:  StackTraceMenuItem
