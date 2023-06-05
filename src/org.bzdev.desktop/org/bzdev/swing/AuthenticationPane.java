package org.bzdev.swing;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.awt.*;
import java.awt.event.*;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.swing.lpack.AuthenticationPane

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
    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(bundle.getString(key), args)
	    .toString();
    }

    private static SecureBasicUtilities dops = new SecureBasicUtilities();;
    private static SecureBasicUtilities ops = null;
    private static File pemFile = null;
    private static final Charset utf8 = Charset.forName("UTF-8");

    /**
     * Set the private key for secure basic authentication.
     * The file's extension should be ".pem" or ".pem.gpg" when
     * the file is GPG encrypted.
     * @param pemfile a file, possibly encrypted, in PEM format containing
     *        the private key
     */
    public static void setPrivateKey(File pemfile) {
	pemFile = pemfile;
    }

    private static char[] password = null;

    /**
     * Request a GPG passphrase.
     * This method will open a dialog box to request a GPG pasphrase
     * for decryption.
     * @param owner a component over which a dialog box should be displayed
     */
    public static void requestPassphrase(Component owner) {
	if (password == null) {
	    if (!SwingUtilities.isEventDispatchThread()) {
		try {
		    SwingUtilities.invokeAndWait(() -> {
			    requestPassphrase(owner);
			});
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
		}
		return;
	    }
	    JPasswordField pwf = new JPasswordField(16);
	    pwf.addFocusListener(new FocusAdapter() {
		    boolean retry = true;
		    public void focusLost(FocusEvent e) {
			Component other = e.getOppositeComponent();
			Window w1 = SwingUtilities.getWindowAncestor
			    (pwf);
			Window w2 = (other == null)? null:
			    SwingUtilities.getWindowAncestor(other);
			if (retry && e.getCause()
			    == FocusEvent.Cause.UNKNOWN
			    && w1 == w2) {
			    SwingUtilities.invokeLater(() -> {
				    pwf.requestFocusInWindow();
				});
			} else {
			    retry = false;
			}
		    }
		});
	    pwf.addAncestorListener(new AncestorListener() {
		    public void ancestorAdded(AncestorEvent ev) {
			SwingUtilities.invokeLater(() -> {
				pwf.requestFocusInWindow();
			    });
		    }
		    public void ancestorRemoved(AncestorEvent ev) {
		    }
		    public void ancestorMoved(AncestorEvent ev) {
		    }
		});

	    System.out.println("got here 2");
	    int status = JOptionPane
		.showConfirmDialog(owner, pwf, localeString("enterPW"),
				   JOptionPane.OK_CANCEL_OPTION);
	    System.out.println("JOptionPane status = " + status);
	    if (status == JOptionPane.OK_OPTION) {
		password = pwf.getPassword();
	    }
	}
    }

    /**
     * Remove the current GPG passphrase.
     * As a general rule, this method should be called as soon as
     * a passphrase is no longer needed, or will not be needed for
     * some time.
     */
    public static void clearPassphrase() {
	if (password != null) {
	    for (int i = 0; i < password.length; i++) {
		password[i] = (char)0;
	    }
	}
	password = null;
    }

    private static synchronized SecureBasicUtilities getOps()
	throws IOException, GeneralSecurityException
    {
	if (pemFile == null) return null;
	if (ops == null) {
	    String name = pemFile.getName();
	    if (name.endsWith(".gpg")) {
		ProcessBuilder pb = new
		    ProcessBuilder("gpg",
				   "--pinentry-mode", "loopback",
				   "--passphrase-fd", "0",
				   "--batch", "-d",
				   pemFile.getCanonicalPath());
		// pb.redirectError(ProcessBuilder.Redirect.DISCARD);
		try {
		    Process p = pb.start();
		    System.out.println("got here");
		    requestPassphrase(null);
		    if (password == null) return null;
		    OutputStream os = p.getOutputStream();
		    OutputStreamWriter w = new OutputStreamWriter(os, utf8);
		    w.write(password, 0, password.length);
		    w.flush();
		    w.close();
		    os.close();
		    InputStream is = p.getInputStream();
		    ops = new SecureBasicUtilities(is);
		    is.close();
		} catch (Exception e) {
		    System.err.println(errorMsg("decryption", name));
		    return null;
		}
	    } else {
		ops = new SecureBasicUtilities(new FileInputStream(pemFile));
	    }
	}
	return ops;
    }

    private static class URLData {
	URL context;
	URL origin;
	String userName;
	char[] password;
	String realm;
	Certificate cert;
	SecureBasicUtilities ops;
    }

    private static class URLTimestamp {
	URL url;
	long time;
	URLTimestamp(URL url) {
	    this.url = url;
	    time = System.currentTimeMillis();
	}
    }

    private static ConcurrentHashMap<Authenticator,
	ConcurrentHashMap<URL,URLData>> map
	= new ConcurrentHashMap<>();

    /*
    public static String getBasicAuthorization(Authenticator authenticator,
					       URL url,
					       String realm)
    {
	ConcurrentHashMap<URL, URLData> urlmap = map.get(authenticator);
	if (urlmap == null) return null;
	URLData urldata =  getUData(urlmap, url, realm);
	if (urldata == null) return null;
	SecureBasicUtilities.Mode mode =
	  SecureBasicUtilities.getMode(urldata.realm);
	char[] password;
	if (mode == SecureBasicUtilities.Mode.PASSWORD) {
	    password = urldata.password;
	} else {
	    try {
		password = urldata.ops.createPassword(urldata.cert,
						      urldata.password);
	    } catch (Exception e) {
		return null;
	    }
	}
	byte[] uname = urldata.userName.getBytes(utf8);
	byte[] pwtext = new byte[password.length + uname.length + 1];
	System.arraycopy(uname, 0, pwtext, 0, uname.length);
	pwtext[uname.length] = (byte)':';
	int offset = uname.length+1;
	for (int i = 0; i < password.length; i++) {
	    pwtext[i+offset] = (byte) password[i];
	}
	byte[] encoded = Base64.getEncoder().encode(pwtext);
	return "Basic " + new String(encoded, utf8);
    }
    */

    // This version returns null if nothing is found.
    private static URLData getUData(ConcurrentHashMap<URL, URLData> urlmap,
			     URL url, String realm)
    {
		    URL context = url;
		    String path = url.getPath();
		    if (path == null
			|| path.length() == 0
			|| !(path.charAt(0) == '/')) {
			return null;
		    } else if (path.length() != 1 && !path.endsWith("/")) {
			path = path.substring(0, path.lastIndexOf('/')+1);
			try {
			    context = new URL(url, path);
			} catch (MalformedURLException e) {}
		    }
		    URL ctxt = context;
		    while (!(urlmap.containsKey(ctxt)
			     && urlmap.get(ctxt).realm.equals(realm))) {
			String p = ctxt.getPath();
			int len = p.length();
			if (len == 1) {
			    return null;
			}
			p = p.substring(p.length()-1);
			p = p.substring(0, p.lastIndexOf('/')+1);
			try {
			    ctxt = new URL(ctxt, p);
			} catch (MalformedURLException e) {}
		    }
		    return urlmap.get(ctxt);
		}


    /**
     * Set the time limit used to determine if multiple authentication
     * failures for cached entries are occuring close to each other in
     * time to indicate that the user should be queried.
     * The default value is 60 seconds.
     * <P>
     * Ideally this would not be necessary, but the Java API for an
     * {@link Authenticator} does not allow us to determine whether
     * a failed authentication request was generated by this authenticator
     * or by a cached value stored elsewhere.
     * @param limit the limit in milliseconds
     */
    public static void setTimeLimit(long limit) {
	tdLimit = limit;
    }

    /**
     * Get an authenticator for network authentication requests.
     * This is equivalent to
     * {@link #getAuthenticator(Component,boolean,boolean) getAuthenticator(comp,true,false)}.
     * <P>
     * @param comp the component on which a dialog box should be centered
     *        when an authentication request is given to the user
     * @return an authenticator
     * @see Authenticator
     * @see SecureBasicUtilities
     */
    public static Authenticator getAuthenticator(final Component comp) {
	return getAuthenticator(comp, true, false);
    }

    static long tdLimit = 60000L;

    /**
     * Get an authenticator for network authentication requests,
     * optionally placing the authenticator in a map.
     * <P>
     * When the argument withMap is true, the user names and passwords
     * entered in a dialog box will be saved for future use so that
     * new secure-basic passwords can be generated as needed. When
     * the argument originMode is true, an exception is made for a
     * URL that caused password information to be saved when none
     * previously existed or could be used.
     * <P>
     * Whan a map is used, the authenticator first removes everything
     * from the path after the last '/' and uses that as a key to look
     * up password data. If there is none, the final '/' is removed and
     * the process is repeated entry for that key is found. If none is
     * found, a new entry is added and that new entry's key is the
     * starting path truncated immediately after  its last '/".
     * @param comp the component on which a dialog box should be centered
     *        when an authentication request is given to the user
     * @param withMap true if the authenticator maps URLs to results;
     *        false otherwise
     * @param originMode true if cached passwords should not be used when the
     *        URL matches the one that created a map entry; false otherwise
     * @return an authenticator
     * @see Authenticator
     * @see SecureBasicUtilities
     */
    public static Authenticator getAuthenticator(final Component comp,
						 final boolean withMap,
						 final boolean originMode)
    {
	final ConcurrentHashMap<URL,URLData> urlmap =
	    withMap? new ConcurrentHashMap<URL,URLData>(): null;
	Authenticator authenticator = new Authenticator() {
		Component component = comp;


		java.util.List<URLTimestamp> ulist = Collections
		    .synchronizedList(new LinkedList<URLTimestamp>());


		private Certificate getCertificate() {
		    // It would be better to get the certificate
		    // from the SSL connection that is making this
		    // authentication request
		    String host = getRequestingHost();
		    InetAddress addr = getRequestingSite();
		    int port = getRequestingPort();
		    SSLSocketFactory factory = (SSLSocketFactory)
			SSLSocketFactory.getDefault();
		    try (SSLSocket socket = (SSLSocket)
			 (host == null? factory.createSocket(addr, port):
			  factory.createSocket(host, port))) {
			SSLSession session = socket.getSession();
			Certificate[] chain = session.getPeerCertificates();
			return (chain == null || chain.length == 0)? null:
			    chain[0];
		    } catch (Exception e) {
			return null;
		    }
		}

		class ResultHolder {
		    PasswordAuthentication result = null;
		    ResultHolder() {
		    }
		}

		private URLData getUData(URL url, String realm) {
		    URL context = url;
		    String path = url.getPath();
		    if (path == null
			|| path.length() == 0
			|| !(path.charAt(0) == '/')) {
			return null;
		    } else if (path.length() != 1 && !path.endsWith("/")) {
			path = path.substring(0, path.lastIndexOf('/')+1);
			try {
			    context = new URL(url, path);
			} catch (MalformedURLException e) {}
		    }
		    URL ctxt = context;
		    while (!(urlmap.containsKey(ctxt)
			     && urlmap.get(ctxt).realm.equals(realm))) {
			String p = ctxt.getPath();
			int len = p.length();
			if (len == 1) {
			    URLData udata = new URLData();
			    udata.context = context;
			    udata.origin = url;
			    urlmap.put(context, udata);
			    /*
			    System.out.println("putting new udata for "
					       + context
				       + ", url = " + url);
			    */
			    return udata;
			}
			p = p.substring(0, p.length()-1);
			p = p.substring(0, p.lastIndexOf('/')+1);
			try {
			    ctxt = new URL(ctxt, p);
			} catch (MalformedURLException e) {}
		    }
		    /*
		    System.out.println("getting udata for " + ctxt
				       + ", url = " + url);
		    */
		    return urlmap.get(ctxt);
		}

		// private PasswordAuthentication result = null;
		protected PasswordAuthentication getPasswordAuthentication() {
		    final ResultHolder resultHolder = new ResultHolder();
		    final URL reqURL = getRequestingURL();
		    final String realm = getRequestingPrompt();
		    URLData udata = withMap? getUData(reqURL, realm): null;
		    if (withMap && ((!originMode && udata.userName != null)
				    || !udata.origin.equals(reqURL))) {
			// We can reuse the password, etc., previously provided
			Iterator<URLTimestamp> it = ulist.iterator();
			boolean authorizing = true;
			while (it.hasNext()) {
			    URLTimestamp ts = it.next();
			    if (ts.url.equals(reqURL)) {
				long now = System.currentTimeMillis();
				if ((now - ts.time) < tdLimit) {
				    authorizing = false;
				} else {
				    it.remove();
				}
			    }
			}
			if (authorizing) {
			    if (udata.ops == null) {
				ulist.add(new URLTimestamp(reqURL));
				return new
				    PasswordAuthentication(udata.userName,
							   udata.password);
			    } else {
				try {
				    char[] pw = udata.ops
					.createPassword(udata.cert,
							udata.password);
				    ulist.add(new URLTimestamp(reqURL));
				    return new
					PasswordAuthentication(udata.userName,
							       pw);
				} catch (GeneralSecurityException e) {
				    // System.out.println("fail 1");
				    return null;
				} catch (UnsupportedEncodingException e) {
				    // System.out.println("fail 2");
				    return null;
				}
			    }
			} else {
			    ulist.clear();
			}
		    }
		    Runnable r = new Runnable() {
			    public void run() {
				AuthenticationPane apane =
				    new AuthenticationPane();
				String rtype = getRequestorType().toString();
				String type = rtype;
				try {
				    type = localeString(type);
				    if (type == null) type = rtype;
				} catch (Exception e){
				    type = rtype;
				}
				SecureBasicUtilities.Mode mode =
				    SecureBasicUtilities.getMode(realm);

				InetAddress addr = getRequestingSite();
				URL url = getRequestingURL();
				String scheme = url.getProtocol();
				if (mode ==
				    SecureBasicUtilities.Mode
				    .SIGNATURE_WITH_CERT
				    && !scheme.equalsIgnoreCase("https")) {
				    mode = SecureBasicUtilities.Mode
					.SIGNATURE_WITHOUT_CERT;
				}

				int port = getRequestingPort();

				String name1 = getRequestingProtocol()
				    + " " + type
				    + " " + getRequestingHost() +":"
				    + port;
				String name2 = "("
				    + SecureBasicUtilities.iconedRealm(realm)
				    + ")";
				apane.setRequestor(name1, name2, null, null);
				if (JOptionPane.showConfirmDialog
				    (component, apane,
				     localeString("title"),
				     JOptionPane.OK_CANCEL_OPTION,
				     JOptionPane.QUESTION_MESSAGE) == 0) {
				    char[] pw = apane.getPassword();
				    String uname = apane.getUser();
				    try {
					Certificate cert = null;
					switch (mode) {
					case DIGEST:
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = cert;
						udata.realm = realm;
						udata.ops = dops;
					    }
					    pw = dops.createPassword(null, pw);
					    break;
					case SIGNATURE_WITHOUT_CERT:
					    ops = getOps();
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = cert;
						udata.realm = realm;
						udata.ops = ops;
					    }
					    pw = ops.createPassword(null, pw);
					    break;
					case SIGNATURE_WITH_CERT:
					    ops = getOps();
					    cert = getCertificate();
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = cert;
						udata.realm = realm;
						udata.ops = ops;
					    }
					    pw = ops.createPassword(cert, pw);
					    break;
					case PASSWORD:
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = null;
						udata.realm = realm;
						udata.ops = null;
					    }
					    break;
					}
				    } catch (Exception e) {
					// System.err.println(e.getMessage());
					resultHolder.result = null;
					return;
				    }
				    resultHolder.result = new
					PasswordAuthentication(apane
							       .getUser(),
							       pw);
				} else {
				    resultHolder.result = null;
				}
			    }
			};
		    if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		    } else {
			try {
			    SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
			    resultHolder.result = null;
			} catch (java.lang.reflect.InvocationTargetException
				 ite) {
			    resultHolder.result = null;
			}
		    }
		    if (udata != null) {
			if (resultHolder.result == null) {
			    urlmap.remove(udata.context);
			    // safe because the list will be rebuilt
			    // as needed.
			    ulist.clear();
			}
		    }
		    return resultHolder.result;
		}
	    };
	if (withMap) map.put(authenticator, urlmap);
	return authenticator;
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

	// The user text field will not get the keyboard focus
	// unless we are proactive: the OK button gets it instead.
	utf.addFocusListener(new FocusAdapter() {
		boolean retry = true;
		public void focusLost(FocusEvent e) {
		    Component other = e.getOppositeComponent();
		    Window w1 = SwingUtilities.getWindowAncestor
			(AuthenticationPane.this);
		    Window w2 = (other == null)? null:
			SwingUtilities.getWindowAncestor(other);
		    if (retry && e.getCause() == FocusEvent.Cause.UNKNOWN
			&& w1 == w2
			&& !SwingUtilities.isDescendingFrom
			(other, AuthenticationPane.this)) {
			SwingUtilities.invokeLater(() -> {
				utf.requestFocusInWindow();
			    });
		    } else {
			retry = false;
		    }
		}
	    });
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
	// Start asking for the keyboard focus.
	SwingUtilities.invokeLater(() -> {utf.requestFocusInWindow();});
    }
}

//  LocalWords:  Authenticator authenticator userLabel passwordLabel
//  LocalWords:  passwordCheckBox utfToolTip pwfToolTip exbundle UTF
//  LocalWords:  pem gpg pemfile getBasicAuthorization url URLData
//  LocalWords:  ConcurrentHashMap urlmap urldata getUData uname utf
//  LocalWords:  SecureBasicUtilities pwtext arraycopy boolean SSL
//  LocalWords:  getAuthenticator withMap udata ctxt reqURL userName
//  LocalWords:  PasswordAuthentication https getMessage resultHolder
//  LocalWords:  auth getPassword
