package org.bzdev.ejws;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.Properties;
import java.util.regex.Pattern;

import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.net.ServerCookie;
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
     * If the directory does not exist, it will be created and
     * its POSIX file permissions (if applicable) will be set to
     * read, write, execute for the owner with no group or other
     * permissions.  A key named keysigner will be automatically
     * added if it does not already exist.
     * <P>
     * The methods
     * {@link #createUser(String,String,String[],Set)},
     * {@link #createUser(String,String,Set)},
     * {@link #storeGPGKey(String,EjwsAuthenticator.GPGKeyIDs)},
     * and {@link #trustGPGKey(String,boolean)} will throw a
     * {@link NullPointerException} if this method is not called
     * with a non-null argument.
     * @param gpghome the home directory that GPG will use
     * @return true on success; false on failure.
     */
    public boolean setGPGHome(File gpghome) {
	if (!gpghome.exists()) {
	    try {
		gpghome.mkdirs();
	    } catch (Exception e) {
		return false;
	    }
	    try {
		Files.setPosixFilePermissions
		    (gpghome.toPath(),
		     EnumSet.of(PosixFilePermission.OWNER_READ,
				PosixFilePermission.OWNER_WRITE,
				PosixFilePermission.OWNER_EXECUTE));
	    } catch (Exception e){}
	}
	this.gpghome = gpghome;
	boolean result = setupKeySigner();
	if (result) {
	    try {
		trustedKeyIDs = getTrustedKeyIDs();
	    } catch (Exception e) {}
	}
	if (needGPGHomeForLoad) {
	    loadFromDirs();
	    needGPGHomeForLoad = false;
	}

	return result;
    }


    File sbldir = null;
    private String date = LocalDate.now()
	.format(DateTimeFormatter.ISO_LOCAL_DATE);

    private static long sblIndex = 0;

    /**
     * Set the directory used to store SBL user-specific configuration files.
     * The file names in this directory will be an email address
     * (e.g., user@example.com), followed by either "--a", "--p", or "--r".
     * The "--a" indicates that a user's account is active, "--p"
     * indicates that the account is pending, and "--r" indicates that the
     * account was rejected.  A file whose name ends in "--t" is a temporary
     * file.
     * @param sbldir the directory; null to disable
     * @throws IllegalArgumentException if sbldir is not null and is not
     *         a directory
     */
    public void setSBLDir(File sbldir) throws IllegalArgumentException {
	if (sbldir != null) {
	    if (!sbldir.isDirectory()) {
		throw new IllegalArgumentException
		    (errorMsg("sbldir", sbldir.toString()));
	    }
	}
	this.sbldir = sbldir;
	if (needSBLDirForLoad) {
	    loadFromDirs();
	    needSBLDirForLoad = false;
	}
    }

    /**
     * Get the SBL directory.
     * @return the SBL directory
     */
    protected File getSBLDir() {
	return sbldir;
    }

    public EjwsUserTable
	<? extends EjwsAuthenticator,? extends EjwsAuthenticator.Entry>
	getUserTable()
    {
	return null;
    }

    /**
     * Read SBL data from an input stream, storing it as a string
     * @param is the input stream
     * @return the SBL data; null if there is an error
     */
    protected String readSBLData(InputStream is) {
	try {
	    Reader r = new InputStreamReader(is, UTF8);
	    StringWriter sw = new StringWriter();
	    r.transferTo(sw);
	    return sw.toString();
	} catch (IOException ieo) {
	    return null;
	}
    }

    /**
     * Store SBL data.
     * @param s the SBL data as a {@link java.lang.String}
     * @param status {@link AddStatus#OK} if the corresponding account
     *        will be active; {@link AddStatus#PENDING} if the
     *        corresponding account will be pending;
     *        {@link AddStatus#REJECTED} if the account is immediately
     *        rejected
     */
    protected void storeSBLData(String s, AddStatus status)
	throws Exception
    {
	if (sbldir != null) {
	    String uname = getUserNameFromSBL(s);
	    String suffix =
		(status == AddStatus.OK)? "--a":
		(status == AddStatus.PENDING)? "--p":
		"--r";

	    File ofile = new File(sbldir, uname + "--t");
	    File tfile = new File (sbldir, uname + suffix);
	    Path opath = ofile.toPath();
	    Path tpath = tfile.toPath();

	    FileOutputStream os = new FileOutputStream(ofile);
	    Writer w = new OutputStreamWriter(os, UTF8);
	    w.write(s);
	    w.flush();
	    w.close();
	    try {
		Files.move(opath, tpath, StandardCopyOption.ATOMIC_MOVE);
	    } catch (AtomicMoveNotSupportedException ea) {
		Files.move(opath, tpath);
	    }
	}
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
	defaultActive = value;
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

    /**
     * Set the name of the truststore file.
     * The value may be sent to clients setting up an account.
     * @param truststore the name of the truststore file
     */
    public void setTruststore(String truststore) {
	this.truststore = truststore;
    }


    private char[] truststorePW = null;
    private static char[] defaultTrustStorePW = "changeit".toCharArray();

    /**
     * Set the truststore password.
     * The value may be sent to clients setting up an account.
     * @param pw the password
     */
    public void setTruststorePW(char[] pw) {
	this.truststorePW = pw;
    }

    private boolean selfSigned = false;

    /**
     * Set whether or not certificats may be self signed
     * The value may be sent to clients setting up an account.
     * @param selfSigned true if certificates can be self-signed; false if
     *        a certificate chain ends at a root certificate
     */
    public void setSelfSigned(boolean selfSigned) {
	this.selfSigned = selfSigned;
    }

    private boolean allowLoopback = false;

    /**
     * Set if a loopback interface may be used for secure connections
     * The value may be sent to clients setting up an account.
     * @param allowLoopback true if a looback interface may be used;
     *        false otherwise
     */
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

    /**
     * Get the login alias.
     * This is a convenience method.
     * @return the login alias
     * @see FileHandler#getLoginAlias()
     */
    public String getLoginAlias() {
	return ews.getFileHandler(prefix).getLoginAlias();
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
	boolean isActive = (getAdminFingerprint(userName) != null)?
	    true: defaultActive;
	ui.setActive(isActive);
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
	if (!isEmailAddress(email)) {
	    throw new IllegalArgumentException
		(errorMsg("illformedEmail", email));
	}
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
	boolean isActive = (getAdminFingerprint(userName) != null)?
	    true: defaultActive;
	ui.setActive(isActive);

	if (prefix != null) {
	    ui.setBase(prefix);
	} else {
	    setBaseList.add(ui);
	}
	// ui.addUser();
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
	// System.out.println("createUser: key = " + key);
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
		boolean isActive = (getAdminFingerprint(userName) != null)?
		    true: defaultActive;
		ui.setActive(isActive);
		if (prefix != null) {
		    ui.setBase(prefix);
		} else {
		    setBaseList.add(ui);
		}
		ui.setRoles(roles);
		// ui.addUser();
		return ui;
	    } else {
		// System.out.println("userName = " + userName);
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
     * Generate a URI for an admin page.
     * The host name in the URI will be preferentially taken from
     * the server's certificate when SSL is used.
     * @param username the user name; null for just the login URL
     * @return the URL
     */
    protected String generateAdminURI(String username) {
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
	String adminAlias = handler.getAdminAlias();
	String ourprefix = prefix.endsWith("/")? prefix: prefix + "/";
	return uriSchemeAuthority + ourprefix + adminAlias
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

     private URI proxy = null;

    /**
     * Get the reverse proxy.
     * When a reverse proxy is configured, the ".base" field
     * in an SBL file provided by the server will be a URI whose
     * host name and port matches that of the reverse proxy and
     * whose path starts with the reverse proxy's path.
     * @return the reverse proxy; null if there isn't one
     */
    public URI getReverseProxy() {
	return proxy;
    }

    /**
     * Set the reverse proxy.
     * When a reverse proxy is configured, the ".base" field
     * in an SBL file provided by the server will be a URI whose
     * host name and port matches that of the reverse proxy and
     * whose path starts with the reverse proxy's path.
     * @param proxy the reverse proxy; null if there isn't one
     */
    protected void setReverseProxy(URI proxy) {
	this.proxy = proxy;
    }



    static SecureRandom random = new SecureRandom();

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
			(new InputStreamReader(p.getInputStream(), UTF8));
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

	MessageDigest md = null;
	private void setDigest() {
	    if (gpghome != null) {
		if (username != null) {
		    md.update(username.getBytes(UTF8));
		}
		if (base != null) {
		    md.update(base.getBytes(UTF8));
		}
		if (uriS != null) {
		    md.update(uriS.getBytes(UTF8));
		}
		byte[] digest = md.digest();
		/*
		System.out.print(username);
		for (int i = 0; i < digest.length; i++) {
		    System.out.print(":" + digest[i]);
		}
		System.out.println();
		*/
		Base64.Encoder enc = Base64.getEncoder();
		String digestS = enc.encodeToString(digest);
		// System.out.println(username + " digest = " + digestS);
		cpe.setProperty("ebase64." + key + ".digest",
				digestS, gpghome, recipients);
	    }
	}

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
	    try {
		checkRecipients(gpghome, recipients);
	    } catch (Exception e) {
		System.err.println("... check Recipients failed");
	    }
	    this.gpghome = gpghome;
	    if (auth.proxy == null) {
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
	    } else {
		uriSchemeAuthority = auth.proxy.toString();
		if (uriSchemeAuthority.endsWith("/")) {
		    int len = uriSchemeAuthority.length();
		    uriSchemeAuthority =
			uriSchemeAuthority.substring(0, len-1);
		}
	    }

	    cpe.setProperty("title", title);
	    mode = auth.getMode();
	    String[] keypair = null;
	    switch (mode) {
	    case SIGNATURE_WITHOUT_CERT:
	    case SIGNATURE_WITH_CERT:
		try {
		    keypair = SecureBasicUtilities.createPEMPair(null,null);
		} catch (Exception e) {
		    throw new UnexpectedExceptionError(e);
		}
		this.publicKeyPEM = keypair[1];
		cpe.setProperty("ebase64.keypair.privateKey", keypair[0],
				gpghome, recipients);
		break;
	    default:
		break;
	    }
	    // cpe.setProperty(key +".mode", "2");
	    password = genpw();
	    cpe.setProperty("ebase64." + key +".password", password,
			    gpghome, recipients);
	    int i = 0;
	    while (i < modes.length) {
		if (mode.equals(modes[i])) break;
		i++;
	    }
	    String modeS = ""+i;
	    cpe.setProperty(key + ".mode", modeS);
	    try {
		md = MessageDigest.getInstance("SHA-256");
		if (keypair != null) {
		    md.update(keypair[0].getBytes(UTF8));
		}
		md.update(password.getBytes(UTF8));
		md.update(modeS.getBytes(UTF8));
	    } catch (Exception e) {
	    }
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
	    if (auth.proxy == null) {
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
	    } else {
		uriSchemeAuthority = auth.proxy.toString();
		if (uriSchemeAuthority.endsWith("/")) {
		    int len = uriSchemeAuthority.length();
		    uriSchemeAuthority =
			uriSchemeAuthority.substring(0, len-1);
		}
	    }
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
	    setDigest();
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
     * {@link EjwsAuthenticator#storeGPGKey(String,GPGKeyIDs)}.
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
     * Store an ASCII-armored GPG public key for use by this authenticator.
     * The program SBL has an option under the File menu to copy the
     * key to the system clipboard. Alternatively, if a login alias is
     * configured, a URL referencing the login alias with a query
     * containing
     * <UL>
     *   <LI><B>user=</B><I>EMAIL_ADDRESS</I>
     *   <LI><B>uploadtype=pgpkey</B>
     * will return an SBL file that triggers a series of events that will
     * download the corresponding public key and possibly set up a user
     * account.
     * <P>
     * The second argument should be computed by calling
     * {@link #showGPGKey(String) showGPGKey(key)}
     * </UL>
     * @param key the public key
     * @param keyids and object containing the key's email address and
     *        fingerprint
     * @throws NullPointerException if the GPG home directory had not been set
     * @throws IllegalArgumentException if the key is ill-formed
     * @throws IllegalStateException if the key cannot be stored
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     * @see #setGPGHome(File)
     * @see #showGPGKey(String)
     */
    public void storeGPGKey(String key, GPGKeyIDs keyids)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	storeGPGKey(gpghome, key, keyids);
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
    private static synchronized void
	storeGPGKey(File gpghome, String key, GPGKeyIDs keyids)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}

	String email = keyids.getEmailAddress();
	String fpr = keyids.getFingerprint();
	try {
	    /*
	    ProcessBuilder pb = new ProcessBuilder("gpg", "--homedir",
						   gpghome.getCanonicalPath(),
						   "--batch",
						   "--with-colons",
						   "--import-options",
						   "show-only",
						   "--import", "-");
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
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
	    Reader isr = new InputStreamReader(is, UTF8);
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
		} else if (status == 0 && line.startsWith("fpr:")) {
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
	    */
	    ProcessBuilder pb = new ProcessBuilder("gpg", "--homedir",
						   gpghome.getCanonicalPath(),
						   "--batch", "--with-colons",
						   "-k", email);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p1 = pb.start();
	    InputStream is = p1.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is, UTF8);
	    LineNumberReader r = new LineNumberReader(isr);
	    int status = -1;
	    String fpr1 = "";
	    String email1 = "";
	    String line = null;
	    while ((line = r.readLine()) != null) {
		if (status == 2) {
		    continue;
		} else if (line.startsWith("pub:")) {
		    status = 0;
		} else if (status == 0 && line.startsWith("fpr:")) {
		    String[] entries = line.split(":");
		    fpr1 = entries[9];
		    status = 1;
		} else if (status == 1 && line.startsWith("uid:")) {
		    String[] entries = line.split(":");
		    String userdata = entries[9];
		    int ind1 = userdata.lastIndexOf("<");
		    int ind2 = userdata.lastIndexOf(">");
		    if (ind1 > -1 && ind2 > -1) {
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
	    // return new GPGKeyIDs(email, fpr);
	} catch (Exception e) {
	    System.err.println("in storeGPGKey");
	    System.err.println(e.getMessage());
	}
	// return null;
    }

    /**
     * Show an ASCII-armored GPG public key for use by this authenticator.
     * The program SBL has an option under the File menu to copy the
     * key to the system clipboard. Alternatively, if a login alias is
     * configured, a URL referencing the login alias with a query
     * containing
     * <UL>
     *   <LI><B>user=</B><I>EMAIL_ADDRESS</I>
     *   <LI><B>uploadtype=pgpkey</B>
     * will return an SBL file that triggers a series of events that will
     * download the corresponding public key and possibly set up a user
     * account.
     * </UL>
     * @param key the public key
     * @return an object containing the key's email address and fingerprint
     * @throws NullPointerException if the GPG home directory had not been set
     * @throws IllegalArgumentException if the key is ill-formed
     * @throws IllegalStateException if the key cannot be stored
     * @throws IOException if an IO error occurs while constructing a
     *         cannonical path
     * @see #setGPGHome(File)
     */
    public GPGKeyIDs showGPGKey(String key)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	return showGPGKey(gpghome, key);
    }

    /**
     * Show an ASCII-armored GPG public key.
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
    private static synchronized GPGKeyIDs showGPGKey(File gpghome, String key)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg", "--homedir",
						   gpghome.getCanonicalPath(),
						   "--batch",
						   "--with-colons",
						   "--import-options",
						   "show-only",
						   "--import", "-");
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
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
	    Reader isr = new InputStreamReader(is, UTF8);
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
		} else if (status == 0 && line.startsWith("fpr:")) {
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
	    return new GPGKeyIDs(email, fpr);
	} catch (Exception e) {
	    System.err.println("in showGPGKey");
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
	if (!isEmailAddress(email)) {
	    throw new IllegalArgumentException
		(errorMsg("illformedEmail", email));
	}
	trustGPGKey(gpghome, email, trust, trustedKeyIDs);
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
    private static synchronized void
	trustGPGKey(File gpghome, String email, boolean trust,
		    Set<String>trustedKeyIDs)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	if (!isEmailAddress(email)) {
	    throw new IllegalArgumentException
		(errorMsg("illformedEmail", email));
	}
	try {
	    ProcessBuilder pb =  new ProcessBuilder("gpg", "--homedir",
						    gpghome.getCanonicalPath(),
						    "--batch",
						    "--with-colons",
						    "--fingerprint",
						    email);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    InputStream is = p.getInputStream();
	    Reader isr = new InputStreamReader(is, UTF8);
	    LineNumberReader r = new LineNumberReader(isr);
	    String line;
	    int status = -1;
	    String fpr = null;
	    String kid = null;
	    String email1 = null;
	    while ((line = r.readLine()) != null) {
		if (status == 2) {
		    continue;
		} else if (line.startsWith("pub:")) {
		    String[] entries = line.split(":");
		    kid = entries[4];
		    status = 0;
		} else if (status == 0 && line.startsWith("fpr:")) {
		    String[] entries = line.split(":");
		    fpr = entries[9];
		    status = 1;
		} else if (status == 1) {
		    String[] entries = line.split(":");
		    String userdata = entries[9];
		    int ind1 = userdata.lastIndexOf("<");
		    int ind2 = userdata.lastIndexOf(">");
		    if (ind1 > -1 && ind2 > -1) {
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
	    if (trust) {
		trustedKeyIDs.add(kid);
	    } else {
		trustedKeyIDs.remove(kid);
	    }
	    if (p.waitFor() != 0) {
		String msg = errorMsg("trustUpdate", email);
		throw new IllegalStateException(msg);
	    }
	} catch (IOException e) {
	    System.err.println("in trustGPGKey");
	    System.err.println(e.getMessage());
	} catch (InterruptedException e) {
	    System.err.println("in trustGPGKey");
	    System.err.println(e.getMessage());
	} catch (IllegalStateException e) {
	    System.err.println("in trustGPGKey");
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

	boolean active = false;


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
	 * Set whether or not this entry is active.
	 * Authentication should fail if an entry is not active.
	 * If this method was not called or overridden,
	 * {@link #isActive()} will return true.
	 */
	public void makeActive() {
	    this.active = true;
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
     * Remove a user.
     * @param name the user's name
     */
    public abstract boolean removeUser(String name);

    /**
     * Make a user active
     * @param name the user's name
     */
    public abstract boolean makeUserActive(String name);

    /**
     * Make a user active, specifying if the user is one for whom
     * GPG is used to provide the data needed to log in.
     * @param name the user's name
     * @param gpg true if GPG is used; false if an SBL directory is
     *        used
     */
    public abstract boolean removeUser(String name, boolean gpg);

    /**
     * Make a user active, specifying if the user is one for whom
     * GPG is used to provide the data needed to log in.
     * @param name the user's name
     * @param gpg true if GPG is used; false if an SBL directory is
     *        used
     */
    public abstract boolean makeUserActive(String name, boolean gpg);


    private boolean removeUser(String name, String target) {
	return removeUser(name, name.equals(target));
    }

    private boolean makeUserActive (String name, String target) {
	return makeUserActive(name, name.equals(target));
    }


    // so we can set gpghome and sbldir after we call loadFromDirs()
    private boolean needGPGHomeForLoad = false;
    private boolean needSBLDirForLoad = false;

    /**
     * Load user-account data obtained from GPG or an SBL directory
     * @see #gpghome()
     * @see #setGPGHome(File)
     * @see #getSBLDir()
     * @see #setSBLDir(File)
     */
    public  void loadFromDirs() throws UnsupportedOperationException {
	if (gpghome == null) {
	    needGPGHomeForLoad = true;
	}
	if (sbldir == null) {
	    needSBLDirForLoad = true;
	}
    };

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
     * The account-request monitoring function.
     * This function is called when a user requests an account on
     * this server.
     */
    protected BiConsumer<String,AddStatus> onAccountRequest = null;


    /**
     * The account-active monitoring function.
     * This function is called when an account is made active
     */
    protected BiConsumer<String,Boolean> onAccountActive = null;


    /**
     * The account-removal monitoring function.
     * This function is called when an account is removed from the server.
     */
    protected BiConsumer<String,Boolean> onAccountRemoval = null;


    /**
     * The login function.
     * This function is called when a login is successful.
     */
    protected BiConsumer<EjwsPrincipal,HttpExchange> loginFunction = null;

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
     * Set the function called when a user requests an account.
     * This function will be called when a request to add an account
     * is processed. Its first argument is the name of the user.
     * Its second argument is the status of the request
     * (OK, PENDING, or REJECTED).
     * @param function the function; null to disable
     */
    public void setOnAccountRequest
	(BiConsumer<String,AddStatus> function)
    {
	onAccountRequest = function;
    }

    /**
     * Set the function called when an account becomes active.
     * This function will be called when a request to make an account active
     * is processed. Its first argument is the name of the user.
     * Its second argument is the status this operation (true for
     * success; false for failure).
     * @param function the function; null to disable
     */
    public void setOnAccountActive
	(BiConsumer<String,Boolean> function)
    {
	onAccountActive = function;
    }

    /**
     * Set the function called when an account is removed.
     * This function will be called when a request to make an account active
     * is processed. Its first argument is the name of the user.
     * Its second argument is the status this operation (true for
     * success; false for failure).
     * @param function the function; null to disable
     */
    public void setOnAccountRemoval
	(BiConsumer<String,Boolean> function)
    {
	onAccountRemoval = function;
    }

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

    private Map<String,String> adminMap = new HashMap<String,String>();

    /**
     * Add an entry to the map associating email addresses with
     * the fingerprint of a corresponding GPG key.
     * @param email the email address
     * @param fingerprint the corresponding GPG key's fingerprint
     */
    public void addToAdminMap(String email, String fingerprint) {
	adminMap.put(email, fingerprint);
    }

    /**
     * Get the PGP fingerprint for an email address associated with
     * the admin account
     * @param email the email address
     * @return the fingerprint; null if there isn't one associated with
     *         the admin account
     */
    public String getAdminFingerprint(String email) {
	return adminMap.get(email);
    }

    /**
     * Get thef users' email addresses for those users
     * associated with the admin account
     * @return the email addresses
     */
    public Set<String> getAdminUsers() {
	return adminMap.keySet();
    }

    /**
     * Get the status for a request to set up an account.
     * When a function has been provided by calling
     * {@link #setUserStatusFunction(Function)}, the value returned by
     * the provided function will be used unless the value returned is
     * null, in which case {@link #isActiveDefault()} determines the
     * result (either active or pending).
     * @param username the name (typically the email address) of a user
     * @return {@link AddStatus#REJECTED} if the account will definitely
     *         not be allowed; {@link AddStatus#PENDING} if the account
     *         request requires further processing; {@link AddStatus#OK}
     *         if the account request is immediately accepted
     * @see #setUserStatusFunction(Function)
     */
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

    private Set<String> deleteSet = new HashSet<>();

    /**
     * Add a user to the set of users that will be deleted.
     * @param uname the user name (e.g., the user's email address)
     */
    protected synchronized void addToDeleteSet(String uname) {
	deleteSet.add(uname);
    }

    /**
     * Remove a user to the set of users that will be deleted.
     * @param uname the user name (e.g., the user's email address)
     */
    public synchronized void removeFromDeleteSet(String uname) {
	deleteSet.remove(uname);
    }

    /**
     * Determine if a user is in the set of users that will be deleted.
     * @param uname the user name (e.g., the user's email address)
     */
    public synchronized boolean inDeleteSet(String uname) {
	return deleteSet.contains(uname);
    }

    private Set<String> trustedKeyIDs = null;

    /**
     * Get the GPG key IDs for ultimately trusted users.
     * The values returned are in the format used for keysigning
     * (the last 16 characters in the full fingerprint).
     * @return a set of the key IDs
     */
    public Set<String> getTrustedKeyIDs() throws Exception {
	return getTrustedKeyIDs(gpghome);
    }

    private static synchronized Set<String> getTrustedKeyIDs(File gpghome)
	throws Exception
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg", "--homedir",
						   gpghome.getCanonicalPath(),
						   "--with-colons",
						   "--keyid-format", "LONG",
						   "-k");
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    LineNumberReader r = new LineNumberReader
		(new InputStreamReader(p.getInputStream(), UTF8));
	    int cnt = -1;
	    String line;
	    Set<String> keyids = new HashSet<>();
	    while ((line = r.readLine()) != null) {
		if (line.startsWith("pub:u:")) {
		    String[] entries = line.split(":");
		    keyids.add(entries[4]);
		}
	    }
	    int status = p.waitFor();
	    if (status == 0) {
		return keyids;
	    } else {
		return null;
	    }
	} catch (Exception e) {
	    System.err.println("in getTrustedKeyIDs - returning null");
	    return null;
	}
    }

    /**
     * Determine if a user is ultimately trusted.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     * @param name the user name
     * @return true if the user is ultimately trusted; false otherwise
     * @see setGPGHome(File)
     */
    public boolean isTrustedKey(String name) {
	return isTrustedKey(gpghome, name);
    }

    private static synchronized boolean
	isTrustedKey(File gpghome, String name)
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg", "--homedir",
						   gpghome.getCanonicalPath(),
						   "--with-colons",
						   "--keyid-format", "LONG",
						   "-k", name);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    LineNumberReader r = new LineNumberReader
		(new InputStreamReader(p.getInputStream(), UTF8));
	    int cnt = -1;
	    String line;
	    boolean result = false;
	    while ((line = r.readLine()) != null) {
		if (line.startsWith("pub:u:")) {
		    return true;
		}
	    }
	    int status = p.waitFor();
	    return result;
	} catch (Exception e) {
	    System.err.println("in isTrustedKey");
	    return false;
	}
    }

    /**
     * Determine if there is a GPG Key for a specified user.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     * @param name the user name
     * @return true if there is an associated GPG key; false otherwise
     */
    public boolean hasGPGKey(String name) {
	return hasGPGKey(gpghome, name);
    }

    public static synchronized boolean hasGPGKey(File gpghome, String name) {

	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--homedir",
						   gpghome.getCanonicalPath(),
						   "-k", name);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    return p.waitFor() == 0;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Set up a key signer.
     * If not already available, a key named keysigner will be created.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     */
    protected boolean setupKeySigner() {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	if (!gpghome.isDirectory()) {
	    return false;
	}

	synchronized(EjwsAuthenticator.class) {
	    if (gpghome.listFiles().length > 0) {
		if (hasGPGKey(gpghome, "keysigner")) {
		    String fpr = getFingerprint("keysigner");
		    if (fpr != null) {
			addToAdminMap("keysigner", fpr);
		    }
		    return true;
		}
	    }

	    try {
		ProcessBuilder pb =
		    new ProcessBuilder("gpg",
				       "--homedir", gpghome.getCanonicalPath(),
				       "--pinentry-mode", "loopback", "--batch",
				       "--passphrase", "",
				       "--quick-generate-key",
				       "keysigner", "ed25519",
				       "default", "never");
		pb.redirectError(ProcessBuilder.Redirect.DISCARD);
		pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
		Process p = pb.start();
		boolean result = (p.waitFor() == 0);
		if (result) {
		    String fpr = getFingerprint("keysigner");
		    addToAdminMap("keysigner", fpr);
		}
		return result;
	    } catch (Exception e) {
		System.err.println("in setupKeysigner");
		return false;
	    }
	}
    }

    /**
     * Get the GPG fingerprint for an email address.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     * @param email the email address
     * @return the fingerpint; null if there is none
     */
    public String getFingerprint(String email) {
	return getFingerprint(gpghome, email);
    }

    private static synchronized String
	getFingerprint(File gpghome, String email)
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	if (!email.equals("keysigner") && !isEmailAddress(email)) {
	    throw new IllegalArgumentException
		(errorMsg("illformedEmail", email));
	}
	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--homedir",
						   gpghome.getCanonicalPath(),
						   "--with-colons",
						   "--keyid-format", "LONG",
						   "-k", email);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    InputStream is = p.getInputStream();
	    Reader isr = new InputStreamReader(is, UTF8);
	    LineNumberReader r = new LineNumberReader(isr);
	    String line;
	    int status = -1;
	    String fpr = null;
	    String email1 = null;
	    while ((line = r.readLine()) != null) {
		if (line.startsWith("pub:")) {
		    status = 0;
		    email1 = null;
		} else if (status == 0 && line.startsWith("fpr:")) {
		    String[] entries = line.split(":");
		    fpr = entries[9];
		    status = 1;
		} else if (status == 1 && line.startsWith("uid:")) {
		    String[] entries = line.split(":");
		    String userdata = entries[9].trim();
			    int ind1 = userdata.lastIndexOf("<");
		    int ind2 = userdata.lastIndexOf(">");
		    if (ind1 > -1 && ind2 > -1) {
			email1 = userdata.substring(ind1+1, ind2);
		    }
		    if (email.equals(email1)) {
			return fpr;
		    } else if (userdata.equals("keysigner")) {
			return fpr;
		    } else {
			status = -1;
		    }
		} else {
		    status = -1;
		}
	    }
	    p.waitFor();
	    return null;
	} catch (Exception e) {
	    return null;
	}
    }

    private static String p = "[^]\\[@.()<>,;:\"\\p{Space}\\p{Cntrl}\\\\]+";
    private static Pattern emailAddressPattern = Pattern.compile
	(p + "([.]" + p + ")*@" + p + "([.]" + p + ")*");

    /**
     * Determine if a string is a syntactically valid email address.
     * The email address must be the local part of an email address,
     * followed by an '@', in turn followed by a domain. For example,
     * <CODE>user@example.com</CODE>.  This is often delimited by
     * "&tl;" and "&gt;".  Those delimiters must not be included.
     * @param string the string to check
     * @return true if the argument is a syntactically valid email address;
     *         false otherwise
     */
    public static boolean isEmailAddress(String string) {
	if (string == null) return false;
	return emailAddressPattern.matcher(string).matches();
    }


    /**
     * Sign a key given an email address.
     * The methods {@link #setGPGHome(File)} and/or {@link #setSBLDir(File)}
     * should be called before this method is used.
     * @param email the email address
     * @param gpg true if a GPG key will be signed; false if
     *        a directory containing SBL data will be manipulated
     * @see #setGPGHome(File)
     * @see #setSBLDir(File)
     */
    public boolean signKey(String email, boolean gpg) {
	if (gpg) {
	    return signKeyGPG(gpghome, email);
	} else {
	    return signKeySBL(sbldir, email);
	}
    }

    /**
     * Sign a key given an email address and target.
     * The methods {@link #setGPGHome(File)} and/or {@link #setSBLDir(File)}
     * should be called before this method is used.
     * This is a convenience method.
     * @param email the email address
     * @param target the email address if GPG is used; a file name for
     *        a file in the SBL directory otherwise.
     * @see #setGPGHome(File)
     * @see #setSBLDir(File)
     */
    protected boolean signKey(String email, String target) {
	if (email.equals(target)) {
	    return signKeyGPG(gpghome, email);
	} else {
	    return signKeySBL(sbldir, email);
	}
    }

    private static synchronized boolean signKeySBL(File sbldir, String email) {
	if (sbldir == null) {
	    throw new NullPointerException(errorMsg("noSBLDir"));
	}
	try {
	    File src = new File(sbldir, email+"--p");
	    File dst = new File(sbldir, email+"--a");
	    Path spath = src.toPath();
	    Path dpath = dst.toPath();
	    try {
		Files.move(spath, dpath, StandardCopyOption.ATOMIC_MOVE);
	    } catch (AtomicMoveNotSupportedException ea) {
		Files.move(spath, dpath);
	    }
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    private static synchronized boolean signKeyGPG(File gpghome, String email) {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	if (!isEmailAddress(email)) {
	    throw new IllegalArgumentException
		(errorMsg("illformedEmail", email));
	}
	try {
	    String fpr = getFingerprint(gpghome, email);
	    if (fpr == null) return false;
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--homedir",
						   gpghome.getCanonicalPath(),
						   "-u", "keysigner",
						   "--batch",
						   "--quick-lsign-key",
						   fpr, email);
	    // pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    // pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
	    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
	    Process p = pb.start();
	    return p.waitFor() == 0;
	} catch (Exception e) {
	    System.err.println("in signKey");
	    System.err.println(e.getMessage());
	    return false;
	}
    }

    /**
     * Determine if a user has a key that was signed by a key
     * recognized by this authenticator.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     * @param email the user's email address
     * @return true if the user is valid (e.g., the user's GPG key has
     *         been signed); false otherwise
     * @see #setGPGHome(File)
     */
    public boolean validGPGUser(String email) {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	if (!isEmailAddress(email)) {
	    throw new IllegalArgumentException
		(errorMsg("illformedEmail", email));
	}
	synchronized(EjwsAuthenticator.class) {
	    try {
		ProcessBuilder pb =
		    new ProcessBuilder("gpg",
				       "--homedir", gpghome.getCanonicalPath(),
				       "--with-colons",
				       "--keyid-format", "LONG",
				       "--list-sigs", email);
		pb.redirectError(ProcessBuilder.Redirect.DISCARD);
		Process p = pb.start();
		LineNumberReader r = new LineNumberReader
		    (new InputStreamReader(p.getInputStream(), UTF8));
		int cnt = -1;
		String line;
		int status = 0;
		boolean result = false;
		while ((line = r.readLine()) != null) {
		    if (line.startsWith("pub:u:")
			|| line.startsWith("pub:f:")) {
			status = 1;
		    } else if (status == 1 && line.startsWith("fpr:")) {
			status = 2;
		    } else if (status == 2 && line.startsWith("uid:")) {
			String[] entries = line.split(":");
			String userdata = entries[9];
			int ind1 = userdata.lastIndexOf("<");
			int ind2 = userdata.lastIndexOf(">");
			String email1 = null;
			if (ind1 > -1 && ind2 > -1) {
			    email1 = userdata.substring(ind1+1, ind2);
			    if (email1.equals(email)) {
				status = 3;
			    } else {
				return false;
			    }
			}
		    } else if (status == 3 && line.startsWith("sig:")) {
			String[] entries = line.split(":");
			if(trustedKeyIDs.contains(entries[4])) {
			    result = true;
			    break;
			}
		    } else if (status == 3 && line.startsWith("sub:")) {
			status = 0;
		    }
		}
		status = p.waitFor();
		if (status == 0) {
		    return result;
		} else {
		    return false;
		}
	    } catch (Exception e) {
		System.err.println("in validGPGUser");
		return false;
	    }
	}
    }

    /**
     * Get GPG user names.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     * @param signed true if the corresponding keys were signed; false
     *        otherwise
     * @return the user names (typically email addresses)
     */
    public Set<String> getGPGUsers(boolean signed) {
	return getGPGUsers(gpghome, signed, trustedKeyIDs, adminMap);
    }

    private static synchronized Set<String>
	getGPGUsers(File gpghome, boolean signed,
		    Set<String>trustedKeyIDs, Map<String,String>adminMap)
    {
	if (gpghome == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--homedir",
						   gpghome.getCanonicalPath(),
						   "--with-colons",
						   "--keyid-format", "LONG",
						   "--list-sigs");
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    LineNumberReader r = new LineNumberReader
		(new InputStreamReader(p.getInputStream(), UTF8));
	    int cnt = -1;
	    String line;
	    int status = 0;
	    Set<String> result = new TreeSet<String>();
	    String fpr = null;
	    String email = null;
	    while ((line = r.readLine()) != null) {
		if (line.startsWith("pub:u:") || line.startsWith("pub:f")) {
		    status = 1;
		    email = null;
		    fpr = null;
		} else if (signed == false && line.startsWith("pub:")) {
		    // when signed is false, we'll accept everything
		    // that contains what looks like an email address.
		    email = null;
		    fpr = null;
		    status = 1;
		} else if (line.startsWith("fpr:")) {
		    String[] entries = line.split(":");
		    fpr = entries[9];
		    status = 2;
		} else if (status == 2 && line.startsWith("uid:")) {
		    String[] entries = line.split(":");
		    String fullid = entries[9].trim();
		    email = null;
		    int ind = fullid.lastIndexOf('<');
		    int lenm1 = fullid.length() -1;
		    if (ind >= 0 && fullid.charAt(lenm1) == '>') {
			email = fullid.substring(ind+1, lenm1);
		    } else {
			ind = fullid.indexOf('@');
			if (ind > -1) {
			    int ind2 = fullid.lastIndexOf('@');
			    if (ind == ind2) {
				email = fullid;
			    }
			}
		    }
		    if (email == null || fpr.equals(adminMap.get(email))) {
			// Skip these - we don't want to modify them.
			status = 0;
		    } else {
			status = 3;
		    }
		} else if (status == 3 && line.startsWith("sig:")) {
		    String[] entries = line.split(":");
		    if (email != null && trustedKeyIDs.contains(entries[4])) {
			if (signed) {
			    result.add(email);
			    email = null;
			    status = 0;
			} else {
			    // skip ones that were signed with a recognized key
			    email = null;
			    status = 0;
			}
		    }
		} else if (status != 0 && line.startsWith("sub:")) {
		    if (!signed && email != null) {
			result.add(email);
		    }
		    email = null;
		    status = 0;
		}
	    }
	    status = p.waitFor();
	    if (status == 0) {
		return result;
	    } else {
		return null;
	    }
	} catch (Exception e) {
	    System.err.println("in getGPGUsers");
	    return null;
	}
    }

    /**
     * Get the users whose data is kept in the SBL directory
     * The method {@link #setSBLDir(File)} should be called before
     * this method is used.
     * @param signed true if the data was signed; false otherwise
     * @return the user names
     */
    public synchronized Set<String> getSBLUsers(boolean signed) {
	return getSBLUsers(sbldir, signed);
    }

    private static Set<String> getSBLUsers(File sbldir, boolean signed) {
	if (sbldir == null) {
	    throw new NullPointerException(errorMsg("noGPGHome"));
	}
	Set<String> result = new HashSet<String>();
	String suffix = (signed)? "--a": "--p";
	for (File f: sbldir.listFiles()) {
	    String name = f.getName();
	    if (name.endsWith(suffix)) {
		result.add(name.substring(0, name.lastIndexOf(suffix)));
	    }
	}
	return result;
    }

    /**
     * Delete a GPG key given the key's fingerprint.
     * {@link #setGPGHome(File)} must have been called before this
     * method is used.
     * @param fpr the fingerprint
     * @see #setGPGHome(File)
     */
    public void deleteWithFingerprint(String fpr) {
	// called by processAdminRequests, so gpghome was checked.

	try {
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--homedir",
						   gpghome.getCanonicalPath(),
						   "--batch",
						   "--delete-keys", fpr);
	    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    Process p = pb.start();
	    p.waitFor();
	} catch (Exception e) {
	    System.err.println("in deleteWithFingerprint");
	}
    }


    private static String startTime = "" + System.currentTimeMillis();
    private static long instance = 0;
    private static final String COOKIE_NAME = "org.bzdev.ejws.auth";


    private static String genInt() {
	char[] pw = new char[16];
	for (int i = 0; i < 16; i++) {
	    char ch = (char)(random.nextInt(10) + '0');
	    pw[i] = ch;
	}
	return new String(pw);
    }

    /**
     * Create a server cookie
     * @param t the instance of {@link com.sun.net.httpserver.HttpExchange}
     *        used for the current connection
     * @return the new cookie
     */
    protected static ServerCookie createServerCookie(HttpExchange t) {
	String value = null;
	synchronized(EjwsSecureBasicAuth.class) {
	    value  = startTime + "-" + (++instance) + "-" + genInt();
	}
	ServerCookie cookie = ServerCookie.newInstance(COOKIE_NAME,
						       value);
	cookie.setHttpOnly(true);
	cookie.setVersion(1);
	cookie.setMaxAge(-1);
	cookie.setPath(t.getHttpContext().getPath());
	Headers reqhdrs = t.getRequestHeaders();
	String hs = reqhdrs.getFirst("host").trim();
	int indv6e = hs.indexOf(']');
	int ind = hs.lastIndexOf(':');
	boolean hasPort = (ind >= 0 && ind > indv6e);
	String host = hasPort? hs.substring(0, ind): hs;
	if (indv6e > 0 && host.charAt(0) == '[') {
	    host = host.substring(1, indv6e);
	}
	cookie.setDomain(host);
	return cookie;
    }

    /**
     * Find a server cookie
     * @param t the instance of {@link com.sun.net.httpserver.HttpExchange}
     *        used for the current connection
     * @return the  cookie
     */
    protected static ServerCookie findServerCookie(HttpExchange t) {

	ServerCookie[] cookies = ServerCookie
	    .fetchCookies(WebMap.asHeaderOps(t.getRequestHeaders()));
	for (int i = 0; i < cookies.length; i++) {
	    String name = cookies[i].getName();
	    if (name != null && name.equals(COOKIE_NAME)) {
		return cookies[i];
	    }
	}
	return null;
    }

    /**
     * Set a server cookie
     * @param t the instance of {@link com.sun.net.httpserver.HttpExchange}
     *        used for the current connection
     * @param cookie the cookie
     */
    protected static void setCookie(HttpExchange t, ServerCookie cookie) {
	cookie.addToHeaders(WebMap.asHeaderOps(t.getResponseHeaders()));
    }

    /**
     * Remove an entry from the password map.
     * This is called when logging out.
     * @param username the user name
     */
    public void removePWInfo(String username) {
    }


    /**
     * Process a request to remove or active user accounts
     * Either {@link #removeUser(String,String)} or
     * {@link #makeUserActive(String,String)} will be called to remove
     * or active a user respectively.
     * <P>
     * This method is called by {@link FileHandler} to implement a
     * simple account manager.
     * @param deleteMap a map whose keys are user names or email addresses
     *        and whose values are either the same or a file name, where
     *        the map is used to determine which users shoudl be deleted
     * @param activateMap a map whose keys are user names or email addresses
     *        and whose values are either the same or a file name, where
     *        the map is used to determine which users should be activated
     */
    public void processAdminRequests(Map<String,String> deleteMap,
				     Map<String,String> activateMap)
    {
	if (gpghome == null && sbldir == null) {
	    throw new NullPointerException(errorMsg("noDirs"));
	}
	synchronized (EjwsAuthenticator.class) {
	    if (deleteMap != null) {
		for (Map.Entry<String,String> entry: deleteMap.entrySet()) {
		    String email = entry.getKey();
		    String target = entry.getValue();
		    if (false) {
			System.out.println("deleteMap: " + email + " "
					   + target);
			continue;
		    }
		    if (!isEmailAddress(email)
			&& getAdminFingerprint(email) != null) {
			// don't delete an admin account!
			continue;
		    }
		    if (isTrustedKey(email)) {
			// Don't delete a key we might use for signing.
			continue;
		    }
		    removeUser(email, target);
		}
	    }
	    if (activateMap != null) {
		for (Map.Entry<String,String> entry: activateMap.entrySet()) {
		    String email = entry.getKey();
		    String target = entry.getValue();
		    if (false) {
			System.out.println("activateSet: " + email
					   + " " + target);
			continue;
		    }
		    if (signKey(email, target)) {
			makeUserActive(email, target);
		    } else {
			System.err.println("... could not sign key for "
					   + email);
		    }
		}
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
