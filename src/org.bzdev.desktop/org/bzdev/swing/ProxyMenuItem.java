package org.bzdev.swing;

import java.util.*;
import javax.swing.*;
import org.bzdev.swing.proxyconf.ProxyDialog;
import java.awt.*;
import java.awt.event.*;

/**
 * Menu Item for opening a proxy-configuration  dialog box.
 * The menu item should be created before an application needs
 * to use a proxy when this menu item is going to be used, unless
 * org.bzdev.swing.proxyconf.ProxyComponent.setProxies is called.
 * Otherwise, the preference database will not be read before a
 * network connection is attempted.
 */
public class ProxyMenuItem extends JMenuItem {
    ProxyDialog proxyDialog = null;

    String bundleName = null;
    String labelKey = null;
    String titleKey = null;

    /**
     * Set the locale.
     * @param locale the locale
     */
    public void setLocale(Locale locale) {
	super.setLocale(locale);
	ResourceBundle bundle = (bundleName != null)?
	    ResourceBundle.getBundle(bundleName, locale): null;
	String newtitle = null;
	String newlabel = null;
	String newurl = null;
	if (bundle != null) {
	    newtitle = bundle.getString(titleKey);
	    newlabel = bundle.getString(labelKey);
	}

	if (newtitle != null) {
	    proxyDialog.setTitle(newtitle);
	}
	if (newlabel != null) {
	    setText(newlabel);
	}
    }

    /**
     * Constructor.
     * @param label the label of the menu item
     * @param frame the frame that owns the proxy dialog for this menu item
     * @param title the dialog's title
     */
    public ProxyMenuItem(String label, Frame frame, String title)
    {
	super(label);
	proxyDialog = new ProxyDialog(frame, title, false);
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    proxyDialog.setVisible(true);
		}
	    });
    }

    private static String getLocalizedString(Locale locale,
					     String bundleName,
					     String key)
    {
	return ResourceBundle.getBundle(bundleName, locale).getString(key);
    }

    /**
     * Constructor specifying a resource bundle.
     * For the current locale, the keys labelKey and titleKey will
     * be used to look up the label and title respectively, using the
     * resource bundle.
     * @param bundleName the name of a resource bundle
     * @param labelKey the key for the label of the menu item
     * @param frame the frame that owns the proxy dialog for this menu item
     * @param titleKey the key for the dialog's title
     */
    public ProxyMenuItem(String bundleName, String labelKey,
			 Frame frame, String titleKey) {
	this(JComponent.getDefaultLocale(), bundleName, labelKey,
	     frame, titleKey);
    }

    /**
     * Constructor specifying a resource bundle and locale.
     * For the specified locale, the keys labelKey and titleKey will
     * be used to look up the label and title respectively, using the
     * resource bundle.
     * @param locale the locale
     * @param bundleName the name of a resource bundle
     * @param labelKey the key for the label of the menu item
     * @param frame the frame that owns the proxy dialog for this menu item
     * @param titleKey the key for the dialog's title
     */
    public ProxyMenuItem(Locale locale, String bundleName,
			 String labelKey, Frame frame, String titleKey) {
	this(getLocalizedString(locale, bundleName, labelKey),
	     frame,
	     getLocalizedString(locale, bundleName, titleKey));
	this.bundleName = bundleName;
	this.labelKey = labelKey;
	this.titleKey = titleKey;
    }
}

//  LocalWords:  dialog's labelKey titleKey bundleName
