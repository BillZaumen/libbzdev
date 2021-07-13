package org.bzdev.swing.proxyconf;

import javax.swing.*;
import java.awt.*;

/**
 * Proxy Dialog class.
 * This class is little more than a wrapper for JDialog.
 * The caller is responsible for any cleanup actions needed when
 * the proxy dialog is no longer needed, which is not necessary if
 * org.bzdev.swing.ProxyMenuItem is used instead.
 *
 * @see org.bzdev.swing.ProxyMenuItem
 */
public class ProxyDialog extends JDialog {

    /**
     * Constructor when the owner is a Frame.
     * If the owner is null a shared hidden frame will be used (the
     * same one as for JDialog).
     * @param frame the Frame that owns the dialog
     * @param title the String to display in the dialog's title bar
     * @param modal specifies with the dialog blocks user input to 
     *       other top-level windows when shown. If true, the modality
     *       type property is set to JDialog.DEFAULT_MODALITY_TYPE;
     *       otherwise the dialog is modeless
     */
    public ProxyDialog(Frame frame, String title, boolean modal) {
	super(frame, title, modal);
	addProxyComponent(new ProxyComponent(null, this));
	pack();
    }
    /**
     * Constructor when the owner is a Dialog.
     * @param dialog the owner Dialog from which the dialog is displayed or null
     *        if this dialog has no owner
     * @param title the String to display in the dialog's title bar
     * @param modal specifies with the dialog blocks user input to other 
     *        top-level windows when shown. If true, the modality type 
     *        property is set to JDialog.DEFAULT_MODALITY_TYPE; otherwise
     *        the dialog is modeless
     *
     */
    public ProxyDialog(Dialog dialog, String title, boolean modal) {
	super(dialog, title, modal);
	addProxyComponent(new ProxyComponent(null, this));
	pack();
    }
    
    ProxyComponent c = null;
    void addProxyComponent(ProxyComponent c) {
	add(c);
	this.c = c;
    }
    /**
     *  Get the dialog's proxy component
     *  @return the proxy component for the dialog
     */
    ProxyComponent getProxyComponent() {return c;}
}

//  LocalWords:  JDialog dialog's modeless
