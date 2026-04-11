package org.bzdev.ejws;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.Properties;

import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.util.ConfigProperties;
import org.bzdev.util.ConfigPropUtilities;

//@exbundle org.bzdev.ejws.lpack.SecureBasicAuth

// If a keyID is KEYID (long string of hex digits), to
// import the key, use
//   gpg -homedir HOMEDIR -import -
// and to set the trust level, use
// echo KEYIUD:6: | gpg --homedir HOMEDIR --import-ownertrust

// cat FILE.asc | gpg --with-colons --import-options show-only --import
// will show what would be imported without actually importing it: useful
// to make sure there are no duplicate email addresses.


/**
 * Base class for EJWS authenticators.
 */
public abstract class EjwsAuthenticator extends BasicAuthenticator {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    static String errorMsg(String key, Object... args) {
	return EjwsSecureBasicAuth.errorMsg(key, args);
    }

    EmbeddedWebServer ews = null;
    String prefix = null;

    File gpghome = null;

    /**
     * Get the GPG home directory.
     * @return the GPG home directory; null if one has not been set
     */
    protected File gpghome() {
	return gpghome;
    }

    /**
     * Set the GPG home directory.
     * <P>
     * The methods
     * {@link #createUser(String,String,String[],Set)},
     * {@link #createUser(String,String,Set)},
     * {@link #storeGPGKey(String)},
     * and {@link #trustGPGKey(String,boolean)} will throw a
     * {@link NullPointerException} if this method is not called
     * with a non-null argument.
     * @param gpghome the home directory that GPG will use
     */
    public void setGPGHome(File gpghome) {
	this.gpghome = gpghome;
    }


    File sbldir = null;
    private String date = LocalDate.now()
	.format(DateTimeFormatter.ISO_LOCAL_DATE);

    private static long sblIndex = 0;

    public void setSBLDir(File sbldir) throws IllegalArgumentException {
	if (sbldir != null) {
	    if (!sbldir.isDirectory()) {
		throw new IllegalArgumentException
		    (errorMsg("sbldir", sbldir.toString()));
	    }
	}
	this.sbldir = sbldir;
    }

    protected String storeSBLData(InputStream is)
	throws IOException
    {
	Reader r = new InputStreamReader(is, UTF8);
	StringWriter sw = new StringWriter();
	r.transferTo(sw);
	String result = sw.toString();
	if (sbldir != null) {
	    File ofile;
	    synchronized(this) {
		ofile = new  File(sbldir, date + "--" + (++sblIndex));
	    }
	    FileOutputStream os = new FileOutputStream(ofile);
	    Writer w = new OutputStreamWriter(os, UTF8);
	    w.write(result);
	    w.flush();
	    w.close();
	}
	return result;
    }


    boolean defaultActive = true;


    /**
     * Get the default value for whether or not a user account is
     * active or not. An account is active if the user is allowed
     * to log in.
     * @return true if the account is active; false otherwise
     */
    public boolean isActiveDefault() {
	return defaultActive;
    }

    /**
     * Set the default for whether new users are active or not.
     * The value is used by the createUser methods.
     * @param value true if new users are active by default; false if
     *        not active by default
     */
    public void setDefaultActive(boolean value) {
	defaultActive = true;
    }

    private boolean canAddAccount = false;

    /**
     * Determine if this authenticator can add a new user
     * account.
     * @return true if an account can be added; false otherwise.
     */
    public boolean getCanAddAccount() {
	return canAddAccount;
    }

    /**
     * Set whether or not this authenticator can add a user account.
     * @param value true if an account can be added; false otherwise.
     */
    public void setCanAddAccount(boolean value) {
	canAddAccount = value;
    }


    private String truststore = null;

    public void setTruststore(String truststore) {
	this.truststore = truststore;
    }


    private char[] truststorePW = null;
    private static char[] defaultTrustStorePW = "changeit".toCharArray();

    public void setTruststorePW(char[] pw) {
	this.truststorePW = pw;
    }

    private boolean selfSigned = false;

    public void setSelfSigned(boolean selfSigned) {
	this.selfSigned = selfSigned;
    }

    private boolean allowLoopback = false;

    public void setAllowLoopback(boolean allowLoopback) {
	this.allowLoopback = allowLoopback;
    }

    // Called by EmbeddedWebServer when a prefix is added.
    void setEWSPrefix(EmbeddedWebServer ews, String prefix)
	throws IllegalStateException
    {
	if (ews != this.ews) {
	    throw new IllegalStateException(errorMsg("authTwoServers"));
	}
	if (this.prefix != null) {
	    String msg = errorMsg("authPrefix", prefix, this.prefix);
	    throw new IllegalStateException(msg);
	}
	this.prefix = prefix;
	for (UserInfo ui: setBaseList) {
	    ui.setBase(prefix);
	}
	setBaseList = null;
    }

    private void configureUserInfo(UserInfo ui) {
    }

    ArrayList<UserInfo> setBaseList = new ArrayList<>();

    /**
     * Create an instance of {@link EjwsAuthenticator.UserInfo}.
     * For example,
     * <BLOCKQUOTE><PRE><CODE>
     * EmbeddedWebServer ews = ...;
     * File gpghome = ...;
     * EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, "test-realm");
     * auth.setGPGHome(gpghome);
     * String recipients[] = {
     *   "user@example.com"
     * };
     * URI logoutURI = ...;
     * auth.add(auth.createUser("user@example.com", "Example",
     *                          recipients, roles)
     *          .setURI("/docs/login.html")
     *          .addUser(true));
     * ews.add("/", DirWebMap.class, dir, auth, true, true, true)
     *    .setWelcome("/index.html")
     *    .setLoginAlias("login.html", "", true)
     *    .setLogoutAlias("logout.html", logoutURI);
     * </CODE></PRE></BLOCKQUOTE>
     * In this example, the order of the calls to <CODE>auth.add</CODE>
     * and <CODE>ews.add</CODE> can be swapped.
     * <P>
     * Each recipient must be a string that can be used with the
     * gpg <STRONG>-r</STRONG> option.
     * @param userName the user name
     * @param title A title for an SBL file
     * @param recipients the GPG recipients
     * @param roles the user's roles
     * @throws NullPointerException if the GPG home directory had not been set
     * @throws IllegalStateException if the recipient does not have
     *         a known GPG public key or if there was a certificate error
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     * @see #setGPGHome(File)
     */
    public UserInfo createUser(String userName, String title,
			       String[] recipients, Set<String>roles)
	throws IllegalStateException, IOException, NullPointerException
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	UserInfo ui = new UserInfo(ews, this, "user", title,
				   gpghome.getCanonicalPath(), recipients);
	ui.setUser(userName);
	if (prefix != null) {
	    ui.setBase(prefix);
	} else {
	    setBaseList.add(ui);
	}
	ui.setDescription(errorMsg("description", userName));
	ui.setRoles(roles);
	ui.setActive(defaultActive);
	// if we need to see these - e.g., for testing locally - all
	// users for this authenticator are likely to need the same
	// configuration.
	if (truststore != null) {
	    ui.setTruststore(truststore);
	}
	if (truststorePW != null && truststorePW != defaultTrustStorePW) {
	    ui.setTruststorePW(truststorePW);
	}
	if (selfSigned) {
	    ui.setSelfSigned(selfSigned);
	}
	if (allowLoopback) {
	    ui.setAllowLoopback(allowLoopback);
	}
	return ui;
    }

    /**
     * Create an instance of {@link EjwsAuthenticator.UserInfo}.
     * For example,
     * <BLOCKQUOTE><PRE><CODE>
     * EmbeddedWebServer ews = ...;
     * File gpghome = ...;
     * EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, "test-realm");
     * URI logoutURI = ...;
     * auth.add(auth.createUser("user@example.com", "Example", roles)
     *          .setURI("/docs/login.html")
     *          .addUser(true));
     * ews.add("/", DirWebMap.class, dir, auth, true, true, true)
     *    .setWelcome("/index.html")
     *    .setLoginAlias("login.html", "", true)
     *    .setLogoutAlias("logout.html", logoutURI);
     * </CODE></PRE></BLOCKQUOTE>
     * In this example, the order of the calls to <CODE>auth.add</CODE>
     * and <CODE>ews.add</CODE> can be swapped.
     * <P>
     * Each recipient must be a string that can be used with the
     * gpg <STRONG>-r</STRONG> option.
     * @param email the user's email address
     * @param title A title for an SBL file
     * @param roles the user's roles
     * @throws NullPointerException if the GPG home directory had not been set
     * @throws IllegalStateException if the recipient does not have
     *         a known GPG public key or if there was a certificate error
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     * @see #setGPGHome(File)
     */
    public UserInfo createUser(String email, String title, Set<String> roles)
	throws IllegalStateException, IOException, NullPointerException
    {
	String recipients[] = {email};
	return createUser(email, title, recipients, roles);
    }

     /**
     * Create an instance of {@link EjwsAuthenticator.UserInfo} based
     * on a public key provided by a remote user.
     * For example,
     * <BLOCKQUOTE><PRE><CODE>
     * EmbeddedWebServer ews = ...;
     * String user = ...;
     * String password = ...;
     * String publickeyPEM = ...;
     * EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, "test-realm");
     * String recipients[] = {
     * URI logoutURI = ...;
     * auth.add(auth.createUser(ews, user, password, publicKeyPEM, null)
     * ews.add("/", DirWebMap.class, dir, auth, true, true, true)
     *    .setWelcome("/index.html")
     *    .setLoginAlias("login.html", "", true)
     *    .setLogoutAlias("logout.html", logoutURI);
     * </CODE></PRE></BLOCKQUOTE>
     * In this example, the order of the calls to <CODE>auth.add</CODE>
     * and <CODE>ews.add</CODE> can be swapped.
     * @param userName the user name
     * @param password the user's password
     * @param publicKeyPEM the user's publicKey in PEM format
     * @param roles the user's roles; null if there are none
     *
     */
   public UserInfo createUser(String userName,
			      String password,
			      String publicKeyPEM,
			      Set<String> roles)
    {
	UserInfo ui = new UserInfo(ews, this, userName, password, publicKeyPEM);
	if (roles != null && roles.size() > 0) {
	    ui.setRoles(roles);
	}
	ui.setActive(defaultActive);

	if (prefix != null) {
	    ui.setBase(prefix);
	} else {
	    setBaseList.add(ui);
	}
	ui.addUser();
	return ui;
    }

    /**
     * Create an instance of {@link EjwsAuthenticator.UserInfo} based
     * on a {@link org.bzdev.util.ConfigProperties} object  provided
     * by a remote user.
     * @param props
     * @param roles a set of roles; null if there are none
     * @throws IllegalStateException if the recipient does not have
     *         a known GPG public key or if there was a certificate error
     * @throws IllegalArgumentException if the property file was ill formed
     */
    public UserInfo createUser(ConfigProperties props,
			       Set<String> roles)
	throws IllegalStateException, IllegalArgumentException
    {
	String key = null;
	for (String k: props.getKeys()) {
	    if (k.endsWith(".description")) {
		int ind = k.lastIndexOf('.');
		key = k.substring(0, ind);
		// Just in case someone decides to base-64 encode
		// the description.
		if (key.startsWith("base64.")) {
		    key = key.substring(7);
		}
		break;
	    }
	}
	if (key != null) {
	    String userName = props.getProperty(key + ".user");
	    String password = props.getProperty("base64." + key + ".password");
	    
	    String publicKeyPem = props.getProperty("base64.keypair.publicKey");
	    SecureBasicUtilities.Mode ourmode = getMode();
	    if (userName == null || password == null
		|| (ourmode != SecureBasicUtilities.Mode.PASSWORD
		    && publicKeyPem == null)) {
		throw new IllegalArgumentException(errorMsg("badPropsFile"));
	    }
	    if (ourmode == SecureBasicUtilities.Mode.PASSWORD) {
		UserInfo ui =  new
		    UserInfo(ews, this, userName, password, null);
		ui.setActive(defaultActive);
		if (prefix != null) {
		    ui.setBase(prefix);
		} else {
		    setBaseList.add(ui);
		}
		ui.setRoles(roles);
		ui.addUser();
		return ui;
	    } else {
		return createUser(userName, password, publicKeyPem, roles);
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("badPropsFile"));
	}
    }

    /**
     * Create an instance of {@link EjwsAuthenticator.UserInfo} based
     * on a string representing a {@link org.bzdev.util.ConfigProperties}
     * object provided by a remote user.
     * The first argument is a string that was in effect created by
     * the following steps:
     * <OL>
     *    <LI> store a {@link java.util.Properties} object by using
     *         the method {@link java.util.Properties#store(Writer,String)}
     *         with the {@link java.io.Writer} argument set to a
     *         {@link java.io.Writer} that uses the UTF-8 character set
     *         with CRLF line separators. The first line in this file
     *         will be "#(M.T application/vnd.bzdev.sblauncher)", which is
     *         used to determine the File's media type.
     *    <LI> Compress the byte stream produced in the first step
     *         using GZIP.
     *    <LI> Finally Base-64 encode the compressed byte stream
     * </OL>
     * <P>
     * The easiest way to create this string is to use the program SBL
     * to create an SBL file, select a site (listed by keys), and then
     * select the "Copy Server SBL to Clipboard" menu item under the
     * File menu.
     * @param propsString a string representing an
     *        {@link org.bzdev.util.ConfigProperties} object
     * @param roles a set of roles; null if there are none
     * @throws IOException if the media type does not match that of the
     *         Base-64 encoded representation
     * @throws IllegalArgumentException if the property file was ill formed
     */
    public UserInfo createUser(String propsString, Set<String> roles)
	throws IOException, IllegalArgumentException
    {
	ConfigProperties props = new
	    ConfigProperties(propsString, "application/vnd.bzdev.sblauncher");
	return createUser(props, roles);
    }

    protected String getUserNameFromSBL(String propsString) throws Exception {
	ConfigProperties props = new
	    ConfigProperties(propsString, "application/vnd.bzdev.sblauncher");
	String key = null;
	for (String k: props.getKeys()) {
	    if (k.endsWith(".description")) {
		int ind = k.lastIndexOf('.');
		key = k.substring(0, ind);
		// Just in case someone decides to base-64 encode
		// the description.
		if (key.startsWith("base64.")) {
		    key = key.substring(7);
		}
		break;
	    }
	}
	if (key != null) {
	     String uname = props.getProperty(key + ".user");
	     if (uname == null) {
		 throw new Exception(errorMsg("missingUserName"));
	     }
	     return uname;
	} else {
	    throw new Exception(errorMsg("missingUserName"));
	}
    }

    /**
     * Generate a URI for a login request that will provide
     * an SBL file for a user.
     * The host name in the URI will be preferentially taken from
     * the server's certificate when SSL is used.
     * @param username the user name; null for just the login URL
     * @return the URL
     */
    protected String generateRequestURI(String username) {
	String scheme = ews.usesHTTPS()? "https": "http";
	InetSocketAddress saddr = ews.getAddress();
	Certificate[][] certs = ews.getCertificates();
	String host;
	if (certs != null && certs.length > 0) {
	    Certificate cert = certs[0][0];
	    if (cert instanceof X509Certificate) {
		X509Certificate xcert = (X509Certificate)cert;
		host = xcert.getSubjectX500Principal()
		    .getName("canonical").substring(3);
	    } else {
		// not documented because this shouldn't ever happen.
		throw new IllegalStateException(errorMsg("certError"));
	    }
	} else {
	    try {
		host = InetAddress.getLocalHost().getHostName();
	    } catch (UnknownHostException e) {
		host = InetAddress.getLoopbackAddress().getHostName();
	    }
	}
	int port = saddr.getPort();
	String uriSchemeAuthority = scheme + "://" + host + ":" + port;
	FileHandler handler = ews.getFileHandler(prefix);
	String loginAlias = handler.getLoginAlias();
	String ourprefix = prefix.endsWith("/")? prefix: prefix + "/";
	return uriSchemeAuthority + ourprefix + loginAlias
	    + ((username == null)? "":
	       "?user=" + URLEncoder.encode(username, UTF8));
    }


    /**
     * Generate a sequence of bytes containing an SBL file that
     * instructs the SBL program as to how to download data needed
     * to create an account. The format is a UTF-8 encoded string,
     * where the string is produced by
     * {@link ConfigPropUtilities#store(Properities,String)} (which
     * describes the string format in detail).
     * @param username the user name
     * @param type "pgpkey" when a PGP/GPG public key should be
     *        downloaded; "sbl" if an SBL file should be downloaded
     */
    protected byte[] requestFromUser(String username, String type) {
	String scheme = ews.usesHTTPS()? "https": "http";
	InetSocketAddress saddr = ews.getAddress();
	Certificate[][] certs = ews.getCertificates();
	String host;
	if (certs != null && certs.length > 0) {
	    Certificate cert = certs[0][0];
	    if (cert instanceof X509Certificate) {
		X509Certificate xcert = (X509Certificate)cert;
		host = xcert.getSubjectX500Principal()
		    .getName("canonical").substring(3);
	    } else {
		// not documented because this shouldn't ever happen.
		throw new IllegalStateException(errorMsg("certError"));
	    }
	} else {
	    try {
		host = InetAddress.getLocalHost().getHostName();
	    } catch (UnknownHostException e) {
		host = InetAddress.getLoopbackAddress().getHostName();
	    }
	}
	int port = saddr.getPort();
	String uriSchemeAuthority = scheme + "://" + host + ":" + port;
	FileHandler handler = ews.getFileHandler(prefix);
	Properties props = new Properties();
	props.setProperty("sbl.downloaded", "true");
	props.setProperty("user", username);
	String[] recipients = null;
	String gpgdir = null;
	if (gpghome != null) {
	    try {
		gpgdir = gpghome.getCanonicalPath();
	    } catch (Exception e) {
		gpgdir = null;
	    }
	}
	if (gpgdir != null) {
	    String rs[] = {username};
	    recipients = rs;
	    try {
		UserInfo.checkRecipients(gpgdir, recipients);
		props.setProperty("recipients",
				  ConfigPropUtilities
				  .encodeRecipients(recipients));
	    } catch (IllegalStateException eis){
		// checkRecipients throws an exception if it can't
		// find the key.
		recipients = null;
	    }
	}

	String ourprefix = prefix.endsWith("/")? prefix: prefix + "/";
	props.setProperty("base", uriSchemeAuthority + ourprefix);
	props.setProperty("loginAlias", handler.getLoginAlias());
	props.setProperty("mode", "" + getMode().ordinal());
	props.setProperty("need", type);
	if (truststore != null) {
	    props.setProperty("trustStore.file", truststore);
	    if (truststorePW != null
		 && truststorePW != defaultTrustStorePW
		&& recipients != null) {
		ConfigPropUtilities.setProperty(props,
						"ebase64.trustStore.password",
						truststorePW,
						gpgdir,
						recipients);
	    }
	}
	if (allowLoopback) {
	    props.setProperty("trust.allow.loopback", "" + allowLoopback);
	}
	if (selfSigned) {
	    props.setProperty("trust.selfsigned", "" + selfSigned);
	}
	return ConfigPropUtilities
	    .storeBytes(props, "application/vnd.bzdev.sblauncher", false);
    }


    /**
     * Class to generate user info.
     * Normally instances of this class are created by calling
     * {@link EjwsAuthenticator#createUser(String,String,String[],Set)},
     * which will configure an authenticator to call
     * {@link EjwsAuthenticator.UserInfo#setBase(String)}
     * when the argument to setBase (equal to a prefix) is available.
     * Example:
     * <BLOCKQUOTE><PRE><CODE>
     * EjwsSecureBasicAuth auth = new
     *       EjwsSecureBasicAuth("test-realm",
     *                           SecureBasicUtilities.Mode
     *                           .SIGNATURE_WITH_CERT);
     * String recipients[] = {
     *   "user@example.com"
     * };
     * var user = new EjwsSecureBasicAuth
     *            .UserInfo(auth, "usrkey", "SBL Example",
     *                      null, recipients)
     *            .setDescription("Example using localhost")
     *            .setUser("user@example.com")
     *            .setBase("https://example.com/")
     *            .setURI("https://example.com/login.html")
     *            .addUser(true);
     * </CODE></PRE></BLOCKQUOTE>
     */
    public static class UserInfo {

	static void checkRecipients(String gpghome, String[] recipients)
	    throws IllegalStateException
	{
	    for (String recipient: recipients) {
		ProcessBuilder pb = (gpghome == null)?
		    new ProcessBuilder("gpg", "--with-colons", "-k",
				       recipient):
		    new ProcessBuilder("gpg",
				       "--homedir", gpghome,
				       "--trust-model", "tofu",
				       "--tofu-default-policy", "good",
				       "--with-colons", "-k", recipient);
		pb.redirectError(ProcessBuilder.Redirect.DISCARD);
		try {
		    Process p = pb.start();
		    LineNumberReader r = new LineNumberReader
			(new InputStreamReader(p.getInputStream()));
		    int cnt = 0;
		    String line;
		    // int status = -1;
		    ArrayList<String>keyids = new ArrayList<>();
		    ArrayList<String>fprs = new ArrayList<>();

		    while ((line = r.readLine()) != null) {
			if (line.startsWith("pub:")) {
			    // status = 0;
			    cnt++;
			}
		    }
		    if (cnt > 1) {
			String msg =
			    errorMsg("duplicateRecipient", recipient);
			throw new IllegalStateException(msg);
		    }
		} catch (IOException e) {
		    String msg = errorMsg("badRecipientFetch", e.getMessage());
		    throw new IllegalStateException(msg);
		}
	    }
	}

	boolean frozen = false;
	boolean baseNeeded = true;

	boolean active = true;

	/**
	 * Determine whether or not this entry is active.
	 * Authentication should fail if an entry is not active.
	 * @return  true if the entry is active; false otherwise
	 */
	public boolean isActive() {
	    return active;
	}

	private static SecureRandom random = new SecureRandom();

	static String genpw() {
	    char[] pw = new char[16];
	    for (int i = 0; i < 16; i++) {
		char ch = (char)(random.nextInt(127 - 33) + 33);
		pw[i] = ch;
	    }
	    return new String(pw);
	}
	String gpghome;
	String[] recipients;

	String key;

	Set<String>roles;

	/**
	 * Get a user's roles.
	 * @return a set of roles
	 */
	public Set<String> getRoles() {
	    return roles;
	}

	/*
	 * Get the name for the user's entry.
	public String getKey() {
	    return key;
	}
	*/
	
	String publicKeyPEM = null;

	/**
	 * Get the public key.
	 * @return the public key
	 */
	public String getPublicKey() {
	    return publicKeyPEM;
	}
	String username = null;

	/**
	 * Get a user's user name
	 * @return the user's user name.
	 */
	public String getUserName() {
	    return username;
	}

	String password = null;

	/**
	 * Get the user's password.
	 * @return the user's password
	 */
	public String getPassword() {
	    return password;
	}

	String base = null;
	/**
	 * Get the user's base URI.
	 * This is the common portion of a URI to which
	 * an authenticator applies.
	 * @return the URI
	 */
	public String getBase() {
	    return base;
	}

	String uriS;

	/**
	 * Get a user's URI.
	 * This is the URI a user is expected to use initially.
	 * @return the URI
	 */
	public String uri() {
	    return uriS;
	}

	private final String mediaType = "application/vnd.bzdev.sblauncher";
	ConfigProperties cpe = null;

	boolean sblcompressed = false;
	byte[]  sbldata;
	/**
	 * Get the SBL file.
	 * @return the SBL file as a byte array
	 */
	public byte[] getSBL() {
	    return sbldata;
	}

	/**
	 * Determine if SBL data is compressed.
	 * @return true if {#getSBL()} returns GZIP-compressed data;
	 *         false otherwise
	 */
	public boolean isSBLCompressed() {return sblcompressed;}


	SecureBasicUtilities.Mode mode;

	/**
	 * Get a user's authentication mode.
	 * @return the mode
	 */
	public SecureBasicUtilities.Mode getMode() {
	    return mode;
	}

	String uriSchemeAuthority;

	EjwsAuthenticator auth;

	/**
	 * Constructor.
	 * The key is an identifier acceptable for use as a Java
	 * variable name.
	 * @param ews the web server
	 * @param auth the {@link EjwsAuthenticator} for this
	 *             {@link EjwsAuthenticator.UserInfo}
	 * @param key the user's key
	 * @param title a title for the SBL file
	 * @param gpghome the GPG home directory; null for the default
	 * @param recipients the recipients' names,  key IDs or other
	 *        strings acceptable for the gpg -r option
	 * @throws IllegalStateException if the recipient does not have
	 *         a known GPG public key or if there was a certificate error
	 */
	protected UserInfo(EmbeddedWebServer ews,
			   EjwsAuthenticator auth, String key, String title,
			   String gpghome, String[] recipients)
	    throws IllegalStateException
	{
	    if (gpghome == null) {
		throw new NullPointerException(errorMsg("noGPGHome"));
	    }
	    cpe = new ConfigProperties(mediaType);
	    cpe.setProperty("sbl.downloaded", "true");
	    this.auth = auth;
	    this.key = key;
	    this.recipients = recipients;
	    checkRecipients(gpghome, recipients);
	    this.gpghome = gpghome;
	    String scheme = ews.usesHTTPS()? "https": "http";
	    InetSocketAddress saddr = ews.getAddress();
	    int port = saddr.getPort();
	    Certificate[][] certs = ews.getCertificates();
	    String host;
	    if (certs != null && certs.length > 0) {
		Certificate cert = certs[0][0];
		if (cert instanceof X509Certificate) {
		    X509Certificate xcert = (X509Certificate)cert;
		    host = xcert.getSubjectX500Principal()
			.getName("canonical").substring(3);
		} else {
		    // not documented because this shouldn't ever happen.
		    throw new IllegalStateException(errorMsg("certError"));
		}
	    } else {
		try {
		    host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		    host = InetAddress.getLoopbackAddress().getHostName();
		}
	    }
	    uriSchemeAuthority = scheme + "://" + host + ":" + port;

	    String[] keypair;
	    try {
		keypair = SecureBasicUtilities.createPEMPair(null,null);
	    } catch (Exception e) {
		throw new UnexpectedExceptionError(e);
	    }
	    this.publicKeyPEM = keypair[1];
	    cpe.setProperty("title", title);
	    cpe.setProperty("ebase64.keypair.privateKey", keypair[0],
			    gpghome, recipients);
	    cpe.setProperty(key +".mode", "2");
	    password = genpw();
	    cpe.setProperty("ebase64." + key +".password", password,
			    gpghome, recipients);
	    mode = auth.getMode();
	    int i = 0;
	    while (i < modes.length) {
		if (mode.equals(modes[i])) break;
		i++;
	    }
	    cpe.setProperty(key + ".mode", ""+i);
	}
	
	/**
	 * Constructor given a public key and password.
	 * The key is an identifier acceptable for use as a Java
	 * variable name.
	 * @param ews the web server
	 * @param auth the {@link EjwsAuthenticator} for this
	 *             {@link EjwsAuthenticator.UserInfo}
	 * @param user the user name
	 * @param password a basic-authentication password; null for a
	 *        default
	 * @param publicKeyPEM a PEM encoded elliptic-curve public key
	 */
	protected UserInfo(EmbeddedWebServer ews,
			   EjwsAuthenticator auth,
			   String user,
			   String password,
			   String publicKeyPEM)
	{
	    this.auth = auth;
	    String scheme = ews.usesHTTPS()? "https": "http";
	    InetSocketAddress saddr = ews.getAddress();
	    int port = saddr.getPort();
	    Certificate[][] certs = ews.getCertificates();
	    String host;
	    if (certs != null && certs.length > 0) {
		Certificate cert = certs[0][0];
		if (cert instanceof X509Certificate) {
		    X509Certificate xcert = (X509Certificate)cert;
		    host = xcert.getSubjectX500Principal()
			.getName("canonical").substring(3);
		} else {
		    // not documented because this shouldn't ever happen.
		    throw new IllegalStateException(errorMsg("certError"));
		}
	    } else {
		try {
		    host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		    host = InetAddress.getLoopbackAddress().getHostName();
		}
	    }
	    uriSchemeAuthority = scheme + ":" + host + ":" + port;
	    this.username = user;
	    this.password = (password == null)? genpw(): password;
	    this.publicKeyPEM = publicKeyPEM;
	}


	/**
	 * Set whether or not this entry is active.
	 * Authentication should fail if an entry is not active.
	 * If this method was not called or overridden,
	 * {@link #isActive()} will return true.
	 * @param active true if the entry is active; false otherwise
	 */
	public UserInfo setActive(boolean active) {
	    this.active = active;
	    return this;
	}


	/*
	 * Set a user's roles
	 * @param roles the roles
	 */
	public UserInfo setRoles(Set<String>roles) {
	    this.roles = roles;
	    return this;
	}

	/**
	 * Set a user's trust store
	 * @param trustStore the file name of the user's trust store
	 * @return this object
	 */
	public UserInfo setTruststore(String trustStore)
	    throws IllegalStateException
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    if (frozen) throw new IllegalStateException(errorMsg("frozen"));
	    cpe.setProperty("trustStore.file", trustStore);
	    return this;
	}
	
	/**
	 * Set a user's password for a trust store.
	 * @param pw the password
	 * @return this object
	 */
	public UserInfo setTruststorePW(char[] pw)
	    throws IllegalStateException
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    if (frozen) throw new IllegalStateException(errorMsg("frozen"));
	    cpe.setProperty("ebase64.trustStore.password", pw,
			    gpghome, recipients);
	    return this;
	}

	/**
	 * Set whether or not a user accepts self-signed certificates.
	 * @param value true to accept self-signed certificates; false
	 *        otherwise
	 * @return this object
	 */
	public UserInfo setSelfSigned(boolean value)
	    throws IllegalStateException
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    if (frozen) throw new IllegalStateException(errorMsg("frozen"));
	    cpe.setProperty("trust.selfsigned", value? "true": "false");
	    return this;
	}

	/**
	 * Set whether or not a user allows the use of the loopback
	 * interface in a certificate.
	 * @param value true if the loopback interface is allowed for
	 *        SSL connections; false otherwise
	 * @return this object
	 */
	public UserInfo setAllowLoopback(boolean value)
	    throws IllegalStateException
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    if (frozen) throw new IllegalStateException(errorMsg("frozen"));
	    cpe.setProperty("trust.selfsigned", value? "true": "false");
	    return this;
	}


	/**
	 * Set a user's description field.
	 * @param description a description for the entries under the
	 *        current key
	 * @return this object
	 */
	public UserInfo setDescription(String description)
	    throws IllegalStateException
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    if (frozen) throw new IllegalStateException(errorMsg("frozen"));
	    cpe.setProperty(key + ".description", description);
	    return this;
	}

	/**
	 * Set a user's user name.
	 * @param username a user name
	 * @return this object
	 */
	public UserInfo setUser(String username)
	    throws IllegalStateException
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    this.username = username;
	    cpe.setProperty(key + ".user", username);
	    return this;
	}

	/**
	 * Set a user's URI base.
	 * If the path is relative, the string "/" will be
	 * automatically prepended.
	 * @param base an absolute path
	 * @return this object
	 */
	protected UserInfo setBase(String base)
	    throws IllegalStateException
	{
	    baseNeeded = false;
	    if (frozen) throw new IllegalStateException(errorMsg("frozen"));
	    if (base.startsWith("/")) {
		base = uriSchemeAuthority + base;
	    } else {
		base = uriSchemeAuthority + "/" + base;
	    }
	    if (!base.endsWith("/")) {
		base = base + "/";
	    }
	    this.base = base;
	    if (cpe != null) {
		cpe.setProperty(key + ".base", base);
	    }
	    if (savedPath != null) {
		setURI(savedPath);
		savedPath = null;
	    }
	    if (addUserNeeded) {
		if (cpe != null) {
		    addUser(savedGZip);
		} else {
		    addUser();
		}
	    }
	    return this;
	}

	String savedPath = null;

	/**
	 * Set a user's starting URI.
	 * @param path a relative path and query that will be appended to the
	 *        base path.
	 * @return this object
	 */
	public UserInfo setURI(String path)
	{
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("noSBL"));
	    }
	    if (base == null) {
		savedPath = path;
		return this;
	    }
	    savedPath = null;
	    this.uriS = base + path;
	    cpe.setProperty(key + ".uri", "$(" + key + ".base)" + path);
	    return this;
	}

	private static SecureBasicUtilities.Mode[] modes
	    = SecureBasicUtilities.Mode.class.getEnumConstants();

	boolean addUserNeeded = false;
	boolean savedGZip = false;

	/**
	 * Finish creating an SBL file and add the user to the
	 * authenticator.
	 * @param gzip true if GZIP compression should be used;
	 *             false otherwise
	 * @return this object
	 * @throws IllegalStateException if the constructor did not
	 *         provide an SBL file or the user was already added
	 */
	public UserInfo addUser(boolean gzip) {
	    if (cpe == null) {
		throw new IllegalStateException(errorMsg("wrongAddUser"));
	    }
	    if (baseNeeded) {
		savedGZip = gzip;
		addUserNeeded = true;
		return this;
	    }
	    addUserNeeded = false;
	    sblcompressed = gzip;
	    sbldata = cpe.storeBytes(gzip);
	    auth.add(this);
	    frozen = true;
	    return this;
	}

	/**
	 * Add the user to the authenticator without creating an SBL file.
	 * @return this object
	 * @throws IllegalStateException if the constructor
	 *         provided an SBL file or the user was already added
	 */
	protected UserInfo addUser() {
	    if (cpe != null) {
		throw new IllegalStateException(errorMsg("wrongAddUser"));
	    }
	    if (baseNeeded) {
		addUserNeeded = true;
		return this;
	    }
	    addUserNeeded = false;
	    auth.add(this);
	    frozen = true;
	    return this;
	}
    }

    /**
     * Container class for key ids.
     * Instances of this class is returned by calls
     * {@link EjwsAuthenticator#storeGPGKey(File,String)}.
     */
    public static class GPGKeyIDs {
	String email;
	String fpr;

	GPGKeyIDs(String email, String fpr) {
	    this.email = email;
	    this.fpr = fpr;
	}

	/**
	 * Get the email address for a GPG key that was just stored
	 * @return the email address
	 */
	public String getEmailAddress() {
	    return email;
	}

	/**
	 * Get the fingerprint for a GPG key that was just stored
	 * @return the key's fingerprint
	 */
	public String getFingerprint() {
	    return fpr;
	}
    }


    /**
     * Store an ASCII-armored GPG public key for this authenticator.
     * The program SBL has an option under the File menu to copy the
     * key to the system clipboard.
     * @param key the public key
     * @return an object containing the key's email address and fingerprint
     * @throws NullPointerException if the GPG home directory had not been set
     * @throws IllegalArgumentException if the key is ill-formed
     * @throws IllegalStateException if the key cannot be stored
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     * @see #setGPGHome(File)
     */
    public GPGKeyIDs storeGPGKey(String key)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	return storeGPGKey(gpghome, key);
    }
    /**
     * Store an ASCII-armored GPG public key.
     * The program SBL has an option under the File menu to copy the
     * key to the system clipboard.
     * @param gpghome the home directory that GPG will use
     * @param key the public key
     * @return an object containing the key's email address and fingerprint
     * @throws NullPointerException if the GPG home directory is null
     * @throws IllegalArgumentException if the key is ill-formed
     * @throws IllegalStateException if the key cannot be stored
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     */
    public static GPGKeyIDs storeGPGKey(File gpghome, String key)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	ProcessBuilder pb = new ProcessBuilder("gpg", "--homedir",
					       gpghome.getCanonicalPath(),
					       "--batch",
					       "--with-colons",
					       "--import-options",
					       "show-only",
					       "--import", "-");
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	try {
	    Process p = pb.start();
	    Thread thread = new Thread(() -> {
		    try {
			OutputStream os = p.getOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(os,
								      "UTF-8");
			w.write(key);
			w.flush();
			w.close();
		    } catch (Exception e) {}
	    });
	    InputStream is = p.getInputStream();
	    Reader isr = new InputStreamReader(is, "UTF-8");
	    LineNumberReader r = new LineNumberReader(isr);
	    thread.start();
	    String line;
	    int status = -1;
	    String fpr = null;
	    String email = null;
	    while ((line = r.readLine()) != null) {
		if (status == 2) {
		    continue;
		} else if (line.startsWith("pub:")) {
		    status = 0;
		} else if (status == 0 && line.startsWith("fpr")) {
		    String[] entries = line.split(":");
		    fpr = entries[9];
		    status = 1;
		} else if (status == 1) {
		    String[] entries = line.split(":");
		    String userdata = entries[9];
		    int ind1 = userdata.lastIndexOf("<");
		    int ind2 = userdata.lastIndexOf(">");
		    if (ind1 > -1) {
			email = userdata.substring(ind1+1, ind2);
		    }
		    status = 2;
		}
	    }
	    thread.join();
	    if (p.waitFor() != 0 || fpr == null) {
		throw new IllegalArgumentException(errorMsg("badGPGKey"));
	    }

	    if (email == null || email.strip().length() == 0) {
		String msg = errorMsg("missingEmail", fpr);
		throw new IllegalArgumentException(msg);
	    }
	    pb = new ProcessBuilder("gpg", "--homedir",
				    gpghome.getCanonicalPath(),
				    "--batch", "--with-colons", "-k", email);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p1 = pb.start();
	    is = p1.getInputStream();
	    isr = new InputStreamReader(is, "UTF-8");
	    r = new LineNumberReader(isr);
	    status = -1;
	    String fpr1 = "";
	    String email1 = "";
	    while ((line = r.readLine()) != null) {
		if (status == 2) {
		    continue;
		} else if (line.startsWith("pub:")) {
		    status = 0;
		} else if (status == 0 && line.startsWith("fpr")) {
		    String[] entries = line.split(":");
		    fpr1 = entries[9];
		    status = 1;
		} else if (status == 1) {
		    String[] entries = line.split(":");
		    String userdata = entries[9];
		    int ind1 = userdata.lastIndexOf("<");
		    int ind2 = userdata.lastIndexOf(">");
		    if (ind1 > -1) {
			email1 = userdata.substring(ind1+1, ind2);
		    }
		    status = 2;
		}
	    }
	    if (p1.waitFor() == 0) {
		// have an existing entry.
		if (fpr1.equals(fpr) && email1.equals(email)) {
		    // exact match - the user downloaded the same key
		    // twice. Process anyway in case a subkey changed.
		} else {
		    System.err.println("fpr = " + fpr);
		    System.err.println("fpr1 = " + fpr1);
		    System.err.println("email1 = " + email1);
		    System.err.println("email = " + email);
		    String msg = errorMsg("keyConflict", email);
		    throw new IllegalStateException(msg);
		}
	    }
	    // if the p.waitFor() returned non-zero, that means no key
	    // was found, so we can safely enter this one.
	    pb = (gpghome == null)?
		new ProcessBuilder("gpg", "--homedir",
				   gpghome.getCanonicalPath(),
				   "--batch",
				   "--import", "-"):
		new ProcessBuilder("gpg", "--homedir",
				    gpghome.getCanonicalPath(),
				    "--batch",
				    "--trust-model", "tofu",
				    "--tofu-default-policy", "good",
				    "--import", "-");
	    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p2 = pb.start();
	    OutputStream os = p2.getOutputStream();
	    OutputStreamWriter w = new OutputStreamWriter(os, "UTF-8");
	    Reader sr = new StringReader(key);
	    try {
		sr.transferTo(w);
		w.flush();
	    } finally {
		w.close();
	    }
	    if (p2.waitFor() != 0) {
		String msg = errorMsg("keyConflict", email);
		throw new IllegalStateException(msg);
	    }
	    return new GPGKeyIDs(email, fpr);
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	}
	return null;
    }

    /**
     * Configure a GPG public key's trust level for this authenticator.
     * The choice for a key's trust level is binary because the key
     * is not being distributed. When the
     * third argument is false, the key's trust is "unknown" and
     * when true, the key's trust is "ultimate".
     * <P>
     * Normally this method is not needed because of the use of the
     * TOFU (Trust On First Use) GPG trust policy.
     * @param email the public key's email field
     * @param trust true if the key is "ultimately" trusted; false if the
     *        key is not trusted
     * @throws NullPointerException if the GPG home directory had not been set
     * @throws IllegalArgumentException if the key is ill-formed
     * @throws IllegalStateException if the key cannot be stored
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     * @see #setGPGHome(File)
     */
    public void trustGPGKey(String email, boolean trust)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	trustGPGKey(gpghome, email, trust);
    }

    /**
     * Configure a GPG public key's trust level.
     * The choice for a key's trust level is binary because the key
     * is not being distributed. When the
     * third argument is false, the key's trust is "unknown" and
     * when true, the key's trust is "ultimate".
     * <P>
     * Normally this method is not needed because of the use of the
     * TOFU (Trust On First Use) GPG trust policy.
     * @param gpghome the home directory that GPG will use
     * @param email the public key's email field
     * @param trust true if the key is "ultimately" trusted; false if the
     *        key is not trusted
     * @throws NullPointerException if the GPG home directory is null
     * @throws IllegalArgumentException if the key is ill-formed
     * @throws IllegalStateException if the key cannot be stored
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     */
    public static void trustGPGKey(File gpghome, String email, boolean trust)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	ProcessBuilder pb =  new ProcessBuilder("gpg", "--homedir",
						gpghome.getCanonicalPath(),
						"--batch",
						"--with-colons",
						"--fingerprint",
						email);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	try {
	    Process p = pb.start();
	    InputStream is = p.getInputStream();
	    Reader isr = new InputStreamReader(is, "UTF-8");
	    LineNumberReader r = new LineNumberReader(isr);
	    String line;
	    int status = -1;
	    String fpr = null;
	    String email1 = null;
	    while ((line = r.readLine()) != null) {
		if (status == 2) {
		    continue;
		} else if (line.startsWith("pub:")) {
		    status = 0;
		} else if (status == 0 && line.startsWith("fpr")) {
		    String[] entries = line.split(":");
		    fpr = entries[9];
		    status = 1;
		} else if (status == 1) {
		    String[] entries = line.split(":");
		    String userdata = entries[9];
		    int ind1 = userdata.lastIndexOf("<");
		    int ind2 = userdata.lastIndexOf(">");
		    if (ind1 > -1) {
			email1 = userdata.substring(ind1+1, ind2);
		    }
		    status = 2;
		}
	    }

	    if (p.waitFor() != 0 || fpr == null) {
		throw new IllegalArgumentException(errorMsg("badGPGKey"));
	    }

	    if (email1 == null || email1.strip().length() == 0) {
		String msg = errorMsg("missingEmail", fpr);
		throw new IllegalArgumentException(msg);
	    }

	    if (!email.equals(email1)) {
		String msg = errorMsg("ambiguousEmail", email);
		throw new IllegalArgumentException(msg);
	    }

	    pb = new ProcessBuilder("gpg", "--homedir",
				    gpghome.getCanonicalPath(),
				    "--batch", "--import-ownertrust");
	    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    p = pb.start();
	    OutputStream os = p.getOutputStream();
	    Writer w = new OutputStreamWriter(os, "UTF-8");
	    w.write(fpr);
	    w.write(trust? ":6:": ":1:");
	    w.write("\n");
	    w.close();
	    if (p.waitFor() != 0) {
		String msg = errorMsg("trustUpdate", email);
		throw new IllegalStateException(msg);
	    }
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	} catch (InterruptedException e) {
	    System.err.println(e.getMessage());
	}
    }

    /**
     * Get the SBL file for a user
     * @param user the user
     * @return the SBL file as a byte array; null if there is none
     */
    public abstract byte[]  getSBL(String user);

    /**
     * Determine if the SBL file is compresssed using GZIP.
     * @return true if the SBL file is compressed; false otherwise
     * @see #getSBL(String)
     */
    public abstract boolean isSBLCompressed(String user);

    public static class Entry {
	Set<String> roles = null;

	/**
	 * Constructor.
	 */
	public Entry() {
	}

	boolean active = true;


	/**
	 * Set whether or not this entry is active.
	 * Authentication should fail if an entry is not active.
	 * If this method was not called or overridden,
	 * {@link #isActive()} will return true.
	 * @param active true if the entry is active; false otherwise
	 */
	public void setActive(boolean active) {
	    this.active = active;
	    return;
	}


	/**
	 * Determine whether or not this entry is active.
	 * Authentication should fail if an entry is not active.
	 * @return  true if the entry is active; false otherwise
	 */
	public boolean isActive() {
	    return active;
	}

	/**
	 * Constructor specifying roles.
	 * @param roles the roles; null if there are none
	 */
	public Entry(Set<String> roles) {
	    this.roles = roles;
	}

	/**
	 * Get the roles stored in this entry
	 * @return a set of roles; null if not applicable or if there
	 *         are no roles defined
	 */
	public Set<String> getRoles(){return roles;}

	/**
	 * Get the password
	 * @return the password; null if it does not exist
	 */ 
	public  String getPassword() {return null;}

	/**
	 *
	 */
	public SecureBasicUtilities getSecureBasicUtilities() {return null;}

	boolean sblCompressed = false;

	byte[] sbldata = null;

	/**
	 * Determine if the SBL file for this entry (if it exists)
	 * is GZIP compressed.
	 * @return true if the SBL file is compressed using GZIP;
	 *          false otherwise
	 */
	public boolean isSBLCompressed() {return sblCompressed;}

	/**
	 * Set if an SBL file should be compressed.
	 * @param value true if the SBL file should be compressed; false
	 *        otherwise
	 */
	protected void setSBLCompressed(boolean value) {
	    sblCompressed = value;
	}
	    
	/**
	 * Get an SBL file for this entry.
	 * @return the SBL file as an array of bytes, possibly compressed
	 */
	public byte[] getSBL() {return sbldata;}

	/**
	 * Set the SBL file.
	 * @param data the byte array containing the SBL file
	 */
	protected void setSBL(byte[] data) {
	    sbldata = data;
	}
    }

    /**
     * Constructor.
     * Realms are strings denoting a name space for users.
     * @param ews the {@link EmbeddedWebServer}
     * @param realm the realm
     */
    protected EjwsAuthenticator(EmbeddedWebServer ews, String realm) {
	super(realm);
	this.ews = ews;
    }


    /**
     * The {@link Appendable} used for tracing.
     */
    protected Appendable tracer = null;

    /**
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * @param tracer the Appendable for tracing requests and responses
     */
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }

    /**
     * The login function.
     * This function is called when a login is successful.
     */
    BiConsumer<EjwsPrincipal,HttpExchange> loginFunction = null;

    /**
     * The logout function.
     * This function is called when a logout is successful.
     */
    protected BiConsumer<EjwsPrincipal,HttpExchange> logoutFunction = null;

    /**
     * The authorization function.
     * This function is called when a request is authorized and the
     * login function or logout function was not called during the same
     * transaction.
     */
    protected BiConsumer<EjwsPrincipal,HttpExchange> authFunction = null;

    /**
     * Set the login function.
     * This function will be called using the current HttpExchange
     * when a login is (a) successful and (b) the function is not null.
     * It can be used to set headers or perform other operations as
     * required by an application.
     * <P>
     * The function will be called when the request URI matches a
     * designated login URI, with the current {@link EjwsPrincipal} and
     * {@link HttpExchange} as its arguments
     * In any transaction, at most one of the login, logout, and
     * authorized functions will be called.
     * @param function the function; null to disable
     * @see FileHandler#setLoginAlias(String)
     * @see FileHandler#setLoginAlias(String,String)
     * @see FileHandler#setLoginAlias(String,URI)
     */
    public void setLoginFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	loginFunction = function;
    }

    /**
     * Set the authorized function.
     * This function will be called when a request is authorized.
     * Its arguments are a principal and the HTTP exchange. The
     * later can be used to set cookies or perform other operations.
     * In any transaction, at most one of the login, logout, and
     * authorized functions will be called.
     * @param function the 'authorized' function.
     */
    public void setAuthorizedFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	authFunction = function;
    }

    /**
     * Set the logout function.
     * This function will be called using the current HttpExchange
     * when a logout is (a) successful and (b) the function is not null.
     * It can be used to set headers or perform other operations as
     * required by an application.
     * <P>
     * The function will be called when the request URI matches a
     * designated logout URI, with the current {@link EjwsPrincipal} and
     * {@link HttpExchange} as its arguments.  The {@link HttpExchange}
     * will be null if the login session has timed out.
     * In any transaction, at most one of the login, logout, and
     * authorized functions will be called.
     * @param function the function; null to disable
     * @see FileHandler#setLogoutAlias(String,URI)
     */
     public void setLogoutFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	logoutFunction = function;
    }

    /**
     * Get a user's authentication mode.
     * @return the mode
     */
    public abstract SecureBasicUtilities.Mode getMode();

    /**
     * Add a user specified by an instance of {@link UserInfo}.
     * @param info the user data
     */
    public abstract void add(UserInfo info);

    /**
     * The status of user with regard to adding the user to
     * an authenticator.
     */
    public enum AddStatus {
	/**
	 * It is OK to add a user and make the
	 * user active.
	 */
	OK,
	/**
	 * It is OK to add a user but the user
	 * should not be active.
	 */
	PENDING,
	/**
	 * Adding the user should be rejected.
	 */
	REJECTED
    }

    private Function<String,AddStatus> defaultUserStatusFunction
	= (uname) -> {return null;};

    private Function<String,AddStatus> userStatusFunction
	= defaultUserStatusFunction;

    /**
     * Set the user-status function.
     * A user status function takes a user name as its argument and
     * returns
     * <UL>
     *   <LI><STRONG>AddStatus.OK</STRONG> if the user's account is to
     *      be added and will be active.
     *   <LI><STRONG>AddStatus.PENDING</STRONG> if the user's account is
     *       to be added but will not be active.
     *   <LI><STRONG>AddStatus.REJECTED</STRONG> if the user may not have
     *       an account at this time.
     *   <LI><STRONG>null</STRONG> if all users will be active or pending,
     *       depending on the value returned by {@link #isActiveDefault()}.
     * </UL>
     * The default function simply returns null so that whether or not a
     * user is active is determined by the value returned by
     * {@link #isActiveDefault()}.
     * @param function the user-status function; null for the default
     * @see #getUserStatus(String)
     */
    public void setUserStatusFunction(Function<String,AddStatus> function) {
	userStatusFunction = (function == null)? defaultUserStatusFunction:
	    function;
    }

    protected AddStatus getUserStatus(String username) {
	if (username == null) {
	    return AddStatus.REJECTED;
	} else {
	    AddStatus result =  userStatusFunction.apply(username);
	    if (result == null) {
		return isActiveDefault()? AddStatus.OK: AddStatus.PENDING;
	    } else {
		return result;
	    }
	}
    }

}

//  LocalWords:  EmbeddedWebServer authTwoServers authPrefix PRE auth
//  LocalWords:  BLOCKQUOTE EjwsSecureBasicAuth SecureBasicUtilities
//  LocalWords:  usrkey SBL setDescription localhost setUser setBase
//  LocalWords:  setURI createSBL getKey URI authenticator getSBL ews
//  LocalWords:  GZIP EjwsAuthenticator UserInfo gpghome GPG gpg http
//  LocalWords:  https certError trustStore pw selfsigned loopback
//  LocalWords:  SSL prepended uri setMode IllegalStateException cpe
//  LocalWords:  errorMsg NullPointerException setProperty gzip
//  LocalWords:  Appendable HttpExchange
//  LocalWords:  EjwsPrincipal FileHandler setLoginAlias
//  LocalWords:  setLogoutAlias
