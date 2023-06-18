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

    private static SecureBasicUtilities dops = new SecureBasicUtilities();
    private static SecureBasicUtilities ops = null;
    private static File pemFile = null;
    private static final Charset utf8 = Charset.forName("UTF-8");

    /**
     * Set the default private key for secure basic authentication.
     * The file's extension should be ".pem" or ".pem.gpg" when
     * the file is GPG encrypted.
     * @param pemfile a file, possibly encrypted, in PEM format containing
     *        the private key
     */
    public static void setPrivateKey(File pemfile) {
	pemFile = pemfile;
    }

    static ConcurrentHashMap<Authenticator,File> pemMap =
	new ConcurrentHashMap<>();

    /**
     * Set the private key for secure basic authentication for a
     * specific authenticator.
     * This method should be used when there are multiple authenticators,
     * some with an authenticator-specific private key.
     * @param authenticator the authenticator
     * @param pemfile a file, possibly encrypted, in PEM format containing
     *        the private key; null to remove the authenticator-specific key
     */
    public static void setPrivateKey(Authenticator authenticator,
				     File pemfile)
    {
	if (pemfile == null) {
	    pemMap.remove(authenticator);
	} else {
	    pemMap.put(authenticator, pemfile);
	}
    }

    private static char[] password = null;

    /**
     * Request a GPG passphrase.
     * This method will open a dialog box to request a GPG passphrase
     * for decryption.  GPG encryption can be optionally used to protect
     * a private key when Secure Basic Authentication is used.
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

	    for (;;) {
		int status = JOptionPane
		    .showConfirmDialog(owner, pwf, localeString("enterPW"),
				       JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
		    char[] pw = pwf.getPassword();
		    if (pw == null) continue;
		    boolean ok = true;
		    for (int i = 0; i < pw.length; i++) {
			if (pw[i] == '\n') {
			    ok = false;
			    break;
			}
			if (pw[i] == '\r') {
			    ok = false;
			    break;
			}
		    }
		    if (!ok) {
			pwf.setText("");
			continue;
		    }
		    password = pw;
		    break;
		} else {
		    break;
		}
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

    private static synchronized SecureBasicUtilities getOps(Component comp,
							    File pemFile)
	throws IOException, GeneralSecurityException
    {
	if (pemFile == null) return null;
	String name = pemFile.getName();
	SecureBasicUtilities ops = null;
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
		requestPassphrase(comp);
		if (password == null) return null;
		OutputStream os = p.getOutputStream();
		OutputStreamWriter w = new OutputStreamWriter(os, utf8);
		w.write(password, 0, password.length);
		w.write(System.getProperty("line.separator"));
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
	return ops;
    }

    private static class URLData {
	URL context;
	String userName;
	char[] password;
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
	ConcurrentHashMap</*URL*/UDataKey,URLData>> map
	= new ConcurrentHashMap<>();

    /**
     * Set the time limit used to determine if multiple authentication
     * failures for cached entries are occurring close to each other in
     * time to indicate that the user should be queried.
     * The default value is 60 seconds.
     * <P>
     * Ideally this would not be necessary, but the Java API for an
     * {@link Authenticator} does not allow us to determine whether
     * a failed authentication request was generated by this authenticator
     * or by a cached value stored elsewhere.
     * <P>
     * The value should be large enough to account for a server that is
     * heavily overloaded.  When a server is on a local-area network and
     * is not going to be overloaded, a much lower value than the default
     * is appropriate.
     * @param limit the limit in milliseconds
     */
    public static void setTimeLimit(long limit) {
	tdLimit = limit;
    }

    /**
     * Get an authenticator for network authentication requests.
     * This is equivalent to
     * {@link #getAuthenticator(Component,boolean) getAuthenticator(comp,true)}.
     * <P>
     * @param comp the component on which a dialog box should be centered
     *        when an authentication request is given to the user
     * @return an authenticator
     * @see Authenticator
     * @see SecureBasicUtilities
     */
    public static Authenticator getAuthenticator(final Component comp) {
	return getAuthenticator(comp, true);
    }

    static long tdLimit = 60000L;

    private static class AuthHolder {
	Authenticator authenticator = null;
    }

    private static class UDataKey {
	private String scheme;
	private String host;
	private int port;
	private String realm;
	int hashcode;
	public UDataKey(String scheme, String host, int port, String realm) {
	    this.scheme = scheme;
	    this.host = host;
	    this.port = port;
	    this.realm = realm;
	   int hashcode = 1;
	   hashcode = 127*hashcode +
	       ((scheme == null)? 0: scheme.hashCode());
	   hashcode = 127*hashcode +
	       ((host == null)? 0: host.hashCode());
	   hashcode = 127*hashcode
	       + port;
	   hashcode = 127*hashcode
	       + ((realm == null)? 0: realm.hashCode());
	}

	@Override
	public boolean equals(Object o) {
	    if (o instanceof UDataKey) {
		UDataKey other = (UDataKey) o;
		return ((scheme == null)? other.scheme == null:
			 scheme.equals(other.scheme))
		    && ((host == null)? other.host == null:
			host.equals(other.host))
		    && port == other.port
		    && ((realm == null)? other.realm == null:
			realm.equals(other.realm));
	    } else {
		return false;
	    }
	}

       @Override
       public int hashCode() {
	   return hashcode;
       }
    }

    /**
     * Get an authenticator for network authentication requests,
     * optionally placing the authenticator in a map.
     * <P>
     * When the argument withMap is true, the user names and passwords
     * entered in a dialog box will be saved for future use so that
     * new secure-basic passwords can be generated as needed without
     * querying the user.
     * <P>
     * @param comp the component on which a dialog box should be centered
     *        when an authentication request is given to the user
     * @param withMap true if the authenticator maps URLs to results;
     *        false otherwise
     * @return an authenticator
     * @see Authenticator
     * @see SecureBasicUtilities
     */
    public static Authenticator getAuthenticator(final Component comp,
						 final boolean withMap)
    {
	final ConcurrentHashMap</*URL*/UDataKey,URLData> urlmap =
	    withMap? new ConcurrentHashMap</*URL*/UDataKey,URLData>(): null;
	final AuthHolder authHolder = new AuthHolder();
	Authenticator authenticator = new Authenticator() {
		Component component = comp;

		SecureBasicUtilities ops = null;
		File pemF = null;

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

		private URLData getUData(String scheme, String host, int port,
					 String realm)
		{
		    UDataKey key = new UDataKey(scheme, host, port, realm);
		    URLData udata = urlmap.get(key);
		    if (udata == null) {
			udata = new URLData();
			urlmap.put(key, udata);
		    }
		    return udata;
		}

		// private PasswordAuthentication result = null;
		protected PasswordAuthentication getPasswordAuthentication() {
		    final ResultHolder resultHolder = new ResultHolder();
		    final URL reqURL = getRequestingURL();
		    final String realm = getRequestingPrompt();
		    final int port = getRequestingPort();
		    String h = getRequestingHost();
		    InetAddress site = (h == null)? getRequestingSite(): null;
		    if (site != null) {
			h = site.getCanonicalHostName();
		    }
		    final String scheme = getRequestingScheme();
		    final String host = h;
		    // URLData udata = withMap? getUData(reqURL, realm): null;
		    URLData udata = withMap?
			getUData(scheme, host, port, realm):
			null;
		    if (withMap && udata.userName != null) {
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
				// System.out.println("... authenticated");
				return new
				    PasswordAuthentication(udata.userName,
							   udata.password);
			    } else {
				try {
				    char[] pw = udata.ops
					.createPassword(udata.cert,
							udata.password);
				    ulist.add(new URLTimestamp(reqURL));
				    // System.out.println("... authenticated");
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
			    // System.out.println("... not authorizing");
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
				    File pf = pemMap
					.get(authHolder.authenticator);
				    if (pf == null) pf = pemFile;
				    try {
					Certificate cert = null;
					switch (mode) {
					case DIGEST:
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = cert;
						udata.ops = dops;
					    }
					    pw = dops.createPassword(null, pw);
					    break;
					case SIGNATURE_WITHOUT_CERT:
					    if (pf == null) {
						throw new
						    IllegalStateException
						    (errorMsg("noPrivateKey"));
					    }
					    if (ops == null
						|| !pf.equals(pemF)) {
						ops = getOps(component, pf);
					    }
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = cert;
						udata.ops = ops;
					    }
					    pw = ops.createPassword(null, pw);
					    break;
					case SIGNATURE_WITH_CERT:
					    if (pf == null) {
						throw new
						    IllegalStateException
						    (errorMsg("noPrivateKey"));
					    }
					    if (ops == null
						|| !pf.equals(pemF)) {
						ops = getOps(component, pf);
						pemF = pf;
					    }
					    cert = getCertificate();
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = cert;
						udata.ops = ops;
					    }
					    pw = ops.createPassword(cert, pw);
					    break;
					case PASSWORD:
					    if (udata != null) {
						udata.userName = uname;
						udata.password = pw;
						udata.cert = null;
						udata.ops = null;
					    }
					    break;
					}
				    } catch (Exception e) {
					// System.err.println(e.getMessage());
					// System.out.println("... no new password");
					resultHolder.result = null;
					return;
				    }
				    // System.out.println("... new password");
				    resultHolder.result = new
					PasswordAuthentication(apane
							       .getUser(),
							       pw);
				} else {
				    // System.out.println("... no new password");
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
			    UDataKey key = new UDataKey(scheme, host, port,
							realm);
			    urlmap.remove(key);
			    // safe because the list will be rebuilt
			    // as needed.
			    ulist.clear();
			}
		    }
		    return resultHolder.result;
		}
	    };
	authHolder.authenticator = authenticator;
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
//  LocalWords:  pem gpg pemfile getBasicAuthorization url URLData fd
//  LocalWords:  ConcurrentHashMap urlmap urldata getUData uname utf
//  LocalWords:  SecureBasicUtilities pwtext arraycopy boolean SSL pb
//  LocalWords:  getAuthenticator withMap udata ctxt reqURL userName
//  LocalWords:  PasswordAuthentication https getMessage resultHolder
//  LocalWords:  auth getPassword authenticators enterPW pinentry len
//  LocalWords:  loopback redirectError getPath charAt endsWith
//  LocalWords:  substring MalformedURLException containsKey
