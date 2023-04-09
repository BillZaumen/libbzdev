package org.bzdev.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
import java.util.*;

/**
 * Authenticator GUI component.
 * <p>
 * Typical usage:
 * <code>Authenticator.setDefault(AuthenticationPane.getAuthenticator(component));</code>
 * where <code>component</code> is a component on which to center a dialog
 * that is created when interaction with the user is necessary.
 */
public class AuthenticationPane extends JComponent {

    static private final String resourceBundleName = 
	"org.bzdev.swing.lpack.AuthenticationPane";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    /**
     * Get an authenticator for network authentication requests.
     * @param comp the component on which a dialog box should be centered
     *        when an authentication request is given to the user
     * @return an authenticator
     */
    public static Authenticator getAuthenticator(final Component comp) {
	return new Authenticator() {
	    Component component = comp;
	    private PasswordAuthentication result = null;
	    protected PasswordAuthentication getPasswordAuthentication() {
		Runnable r = new Runnable() {
			public void run() {
			    AuthenticationPane apane = new AuthenticationPane();
			    String rtype = getRequestorType().toString();
			    String type = rtype;
			    try {
				type = localeString(type);
				if (type == null) type = rtype;
			    } catch (Exception e){
				type = rtype;
			    }

			    String name1 = getRequestingProtocol()
				+ " " + type + " " + getRequestingHost() +":"
				+ getRequestingPort();
			    String name2 = getRequestingPrompt();
			    apane.setRequestor(name1, name2, null, null);
			    if (JOptionPane.showConfirmDialog
				(component, apane,
				 localeString("title"),
				 JOptionPane.OK_CANCEL_OPTION,
				 JOptionPane.QUESTION_MESSAGE) == 0) {
				    result =
					new PasswordAuthentication
					(apane.getUser(), apane.getPassword());
			    } else {
				result = null;
			    }
			}
		    };
		if (SwingUtilities.isEventDispatchThread()) {
		    r.run();
		} else {
		    try {
			SwingUtilities.invokeAndWait(r);
		    } catch (InterruptedException e) {
			result = null;
		    } catch (java.lang.reflect.InvocationTargetException ite) {
			result = null;
		    }
		}
		return result;
	    }
	};
    }

    private JLabel name1Label = new JLabel("");
    private JLabel name2Label = new JLabel("");
    
    private JLabel usrl = new JLabel(localeString("userLabel") + ":");
    private JTextField utf = new JTextField(32);

    private JLabel pwl = new JLabel(localeString("passwordLabel") + ":");
    private JPasswordField pwf = new JPasswordField(32);
    private char echoChar;
    
    private JCheckBox pwcb = new 
	JCheckBox(localeString("passwordCheckBox"), false);

    void setRequestor(String name1, String name2, String user, char[] pw)
    {
	name1Label.setText(name1);
	name2Label.setText(name2);
	utf.setText((user == null)? "": user);
	pwf.setText((pw == null)?"": new String(pw));
    }

    String getUser() {
	return utf.getText();
    }

    char[] getPassword() {
	return pwf.getPassword();
    }

    AuthenticationPane() {
	super();
	echoChar = pwf.getEchoChar();
	setLayout(new GridLayout(7, 1));
	add(name1Label);
	add(name2Label);
	add(usrl);
	add(utf);
	add(pwl);
	add(pwf);
	add(pwcb);

	utf.setToolTipText(localeString("utfToolTip"));
	pwf.setToolTipText(localeString("pwfToolTip"));


	pwcb.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    if (pwcb.isSelected()) {
			pwf.setEchoChar((char)0);
		    } else {
			pwf.setEchoChar(echoChar);
		    }
		}
	    });
    }
}

//  LocalWords:  Authenticator authenticator userLabel passwordLabel
//  LocalWords:  passwordCheckBox utfToolTip pwfToolTip
