package org.bzdev.ejws;
import com.sun.net.httpserver.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.util.ConfigProperties;


public abstract class EjwsAuthenticator extends BasicAuthenticator {

    static String errorMsg(String key, Object... args) {
	return EjwsSecureBasicAuth.errorMsg(key, args);
    }

    EmbeddedWebServer ews = null;
    String prefix = null;

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

    ArrayList<UserInfo> setBaseList = new ArrayList<>();

    /**
     * Create an instance of {@link EjwsAuthenticator.UserInfo}.
     * For example,
     * <BLOCKQUOTE><PRE><CODE>
     * EmbeddedWebServer ews = ...;
     * EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, "test-realm");
     * String recipients[] = {
     *   "user@example.com"
     * };
     * URI logoutURI = ...;
     * auth.add(auth.createUser(ews, "Example", null, recipients)
     *          .setUser("user@example.com")
     *          .setDescription("login for user@example.com")
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
     * @param gpghome the GPG home directory to use; null for a default
     * @param recipients the GPG recipients
     */
    public UserInfo createUser(String userName, String title,
			   String gpghome, String[] recipients)
    {
	UserInfo ui = new UserInfo(ews, this, "user", title,
				   gpghome, recipients);
	ui.setUser(userName);
	if (prefix != null) {
	    ui.setBase(prefix);
	} else {
	    setBaseList.add(ui);
	}
	return ui;
    }

    /**
     * Class to generate user info.
     * Normally instances of this class are created by calling
     * {@link EjwsAuthenticator#createUser(String,String,String,String[])},
     * which will call configure an authenticator to call
     * {@link EjwsAuthenticator.UserInfo#setBase(String)}
     * when the value (equal to a prefix) is available.
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
     *            .UserInfo(auth, "usrkey", "SBL Example", null, recipients)
     *            .setDescription("Example using localhost")
     *            .setUser("user@example.com")
     *            .setBase("https://example.com/")
     *            .setURI("https://example.com/login.html")
     *            .createSBL(true);
     * </CODE></PRE></BLOCKQUOTE>
     */
    public static class UserInfo {

	boolean frozen = false;
	boolean baseNeeded = true;

	String genpw() {
	    SecureRandom random = new SecureRandom();
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
	 * Get the  public key.
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
	ConfigProperties cpe = new ConfigProperties(mediaType);

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
	 * variable name
	 * @param ews the web server
	 * @param auth the {@link EjwsAuthenticator} for this
	 *             {@link EjwsAuthenticator.UserInfo}
	 * @param key the user's key
	 * @param title a title for the SBL file
	 * @param gpghome the GPG home directory; null for the default
	 * @param recipients the recipients' names,  key IDs or other
	 *        strings acceptable for the gpg -r option
	 */
	protected UserInfo(EmbeddedWebServer ews,
			   EjwsAuthenticator auth, String key, String title,
			   String gpghome, String[] recipients)
	{
	    this.auth = auth;
	    this.key = key;
	    this.recipients = recipients;
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
	    uriSchemeAuthority = scheme + ":" + host + ":" + port;

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
	    cpe.setProperty(key +".password", password);
	    mode = auth.getMode();
	    int i = 0;
	    while (i < modes.length) {
		if (mode.equals(modes[i])) break;
		i++;
	    }
	    cpe.setProperty(key + ".mode", ""+i);
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
	    cpe.setProperty(key + ".base", base);
	    if (savedPath != null) {
		setURI(savedPath);
		savedPath = null;
	    }
	    if (addUserNeeded) {
		addUser(savedGZip);
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
	 * Finish creating an SBL file and add an entry to the
	 * authenticator.
	 * @param gzip true if GZIP compression should be used;
	 *             false otherwise
	 * @return this object
	 */
	public UserInfo addUser(boolean gzip) {
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

	/**
	 * Constructor specifying roles.
	 * @param roles the roles
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
    protected abstract void add(UserInfo info);

    
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
