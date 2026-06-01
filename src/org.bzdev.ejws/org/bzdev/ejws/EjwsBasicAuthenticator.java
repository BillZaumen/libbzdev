package org.bzdev.ejws;

import java.net.InetAddress;
import java.time.Instant;
import com.sun.net.httpserver.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.net.ServerCookie;
import org.bzdev.net.WebDecoder;
import org.bzdev.net.WebEncoder;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.ConfigPropUtilities;
import org.bzdev.util.ConfigProperties;


//@exbundle org.bzdev.ejws.lpack.SecureBasicAuth


/**
 * Implementation of BasicAuthenticator using either an in-memory table
 * of user names and passwords or a user-supplied table.
 */
public class EjwsBasicAuthenticator
    extends EjwsAuthenticator<EjwsBasicAuthenticator>
{

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.SecureBasicAuth");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static final String SBLDATA = "application/vnd.bzdev.sblogindata";
    static final String FORMDATA = "application/x-www-form-urlencoded";

    private static SecureBasicUtilities.Mode
	proposeMode(EmbeddedWebServer server)
	throws IllegalStateException
    {
	return SecureBasicUtilities.Mode.PASSWORD;
    }


    /**
     * User entry.
     * This object contains a user's password and roles.
     */
    public static class Entry extends EjwsAuthenticator.Entry {
	// roles defined by superclass
	// SecureBasicUtilities keyops;
	// String pem = null;
	boolean sblCompressed = false;
	byte[] sbldata;


	protected String pw;

	/**
	 * Constructor.
	 * @param pw a password for a user
	 */
	public Entry(String pw) {
	    super();
	    this.pw = pw;
	}


	/**
	 * Constructor with roles.
	 * @param pw a password for a user
	 * @param roles a set of roles that a user may have
	 */
	public Entry(String pw, Set<String> roles) {
	    super(roles);
	    this.pw = pw;
	}

	/*
	* Constructor with a PEM-encoded public-key.
	* @param pem the PEM encoded public key or a certificate
	*        containing that public key; null for secure-basic digest
	*        authentication.
	* @param pw a password for a user
	* @param roles a set of roles that a user may have
       public Entry(String pem, String pw, Set<String>roles) {
	   super(pw, roles);
	   / *
	   try {
	       this.pem = pem;
	       this.keyops = (pem == null)? null:
		   new SecureBasicUtilities(pem);
	   } catch (IOException eio) {
	       String msg = "PEM";
	       throw new IllegalArgumentException(msg, eio);
	   } catch (GeneralSecurityException e) {
	       String msg = "PEM";
	       throw new IllegalArgumentException(msg, e);
	   }
	   * /
       }
	*/
       /*
	* Constructor with a PEM-encoded public-key and SBL data.
	* The term SBL refers to the file created with the program
	* <STRONG>sbl</STRONG>.
	* @param pem the PEM encoded public key or a certificate
	*        containing that public key.
	* @param pw a password for a user
	* @param roles a set of roles that a user may have
	* @param sblCompressed true if the sbldata is compressed,
	*        false otherwise
	* @param sbldata the SBL file as a byte array, possibly
	*        compressed
       public Entry(String pem, String pw, Set<String>roles,
		    boolean sblCompressed, byte[] sbldata)
       {
	   this(pem, pw, roles);
	   this.sblCompressed = sblCompressed;
	   this.sbldata = sbldata;
       }
	*/

       /*
	* Get the PEM encoded public key
	* @return the PEM encoded key; null if there is none
       public String getPEM() {return pem;}
	*/

	/**
	 * Get the password stored in this entry.
	 * @return the password
	 */
	public String getPassword() {return pw;}

       /**
	* Get the roles stored in this entry.
	* @return a set of roles; null if not applicable
	*/
       public Set<String> getRoles(){return roles;}

       /*
	* Get an instance of an object that provides secure basic
	* authentication operations. This object is created by
	* this class's constructors.
	* @return the object
       public SecureBasicUtilities getSecureBasicUtilities() {
	   return  keyops;
       }
	*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive(String user) {
	Entry entry = map.get(user);
	return (entry == null)? false: entry.isActive();
    }

    // private Appendable tracer = null;

    /*
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * @param tracer the Appendable for tracing requests and responses
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }
     */

    private String loginPath = null;
    private boolean loginPathUsed = true;
    private boolean loginRequired = false;
    // BiConsumer<EjwsPrincipal,HttpExchange> loginFunction = null;



    private String logoutPath = null;
    private boolean logoutPathUsed = true;
    // private BiConsumer<EjwsPrincipal,HttpExchange> logoutFunction = null;

    // private BiConsumer<EjwsPrincipal,HttpExchange> authFunction = null;

    ThreadLocal<Integer> utdl = new ThreadLocal<>();
    FileHandler fileHandler = null;
    ThreadLocal<ServerCookie> cookieTL = new ThreadLocal<>();
    String savedAlias = null;


    ThreadLocal<InetAddress> addr = new ThreadLocal<>();
    // used when loginRequired is true
    private ThreadLocal<Boolean> foundLoginTL = new ThreadLocal<>();
    private ThreadLocal<Integer> flCode = new ThreadLocal<>();
    private ThreadLocal<Boolean> foundLogoutTL = new ThreadLocal<>();
    private ThreadLocal<String> usernameTL = new ThreadLocal<>();
    private ThreadLocal<String> msgTL = new ThreadLocal<>();


    /*
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
    public void setLoginFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	loginFunction = function;
    }
     */

    /*
     * Set the authorized function.
     * This function will be called when a request is authorized.
     * Its arguments are a principal and the HTTP exchange. The
     * later can be used to set cookies or perform other operations.
     * In any transaction, at most one of the login, logout, and
     * authorized functions will be called.
     * @param function the 'authorized' function.
    public void setAuthorizedFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	authFunction = function;
    }
     */

    /*
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
    public void setLogoutFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	logoutFunction = function;
    }
     */


    private Map<String,Entry> map = null;

    /**
     * {@inheritDoc}
     */
    protected Map<String, ? extends EjwsAuthenticator.Entry> getAuthMap() {
	return map;
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getUsers() {
	return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getUsers(boolean active) {
	TreeSet<String>userSet = new TreeSet<>();
	for (Map.Entry<String,Entry> entry: map.entrySet()) {
	    if (entry.getValue().isActive() == active) {
		userSet.add(entry.getKey());
	    }
	}
	return userSet;
    }


    /**
     * {@inheritDoc}
     */
    public byte[] getSBL(String user) {
	Entry entry = map.get(user);
	if (entry == null) return null;
	return entry.getSBL();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSBLCompressed(String user) {
	Entry entry = map.get(user);
	if (entry == null) return false;
	return entry.isSBLCompressed();
    }

    private String unencodedRealm;

    // allow a small but ample time window for clocks to be
    // out of sync, plus propagation delay.
    private int lowerTimeDiffLimit = -10;
    private int upperTimeDiffLimit = 150;

    // How long to use a verified password
    private int passphraseTimeout = 1200;

    /**
     * Set the time limit for a passphrase/password.
     * <P>
     * @param passphraseTimeout the time interval in seconds for which a
     *        password is valid (the default is 1200); 0 to disable the
     *        timeout
     * @return this authenticator
     * @throws IllegalArgumentException if the argument is less than
     *         zero.
     */
    public EjwsBasicAuthenticator setTimeLimit(int passphraseTimeout)
	throws IllegalArgumentException
    {
	if (passphraseTimeout < 0) {
	    String msg =
		errorMsg("negativePassphraseTimeout", passphraseTimeout);
	    throw new IllegalArgumentException(msg);
	}
	this.passphraseTimeout = passphraseTimeout;
	return this;
    }

    /**
     * Set time-offset limits.
     * For the modes {@link SecureBasicUtilities.Mode#DIGEST},
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT}, and
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT}, each
     * password that is generated contains a time stamp in units
     * of seconds. The time difference is the difference between the
     * current time and the time stamp associated with a password.
     * <P>
     * The first argument will generally be negative to handle the case
     * in which the clock for the client generating the password is ahead
     * of the server's clock.  The second argument will generally be
     * positive to handle the case in which the client's clock is behind
     * the server's clock and to additionally account for propagation
     * delay and to limit the number of times a password has to be
     * recomputed.  To allow for software that does not implement
     * secure basic authentication, the value should be above the expected
     * maximum length of a user's session.
     * <P>
     * The lowerTimeDiffLimit and upperTImeDiffLimit arguments are used
     * for the admin account, not other accounts.
     * @param lowerTimeDiffLimit the lower limit for the time difference
     *        in seconds (the default is -10 seconds).
     * @param upperTimeDiffLimit the upper limit for the time difference
     *        in seconds (the default is 150 seconds)
     * @param passphraseTimeout the time interval in seconds for which a
     *        password is valid (the default is 1200); 0 to disable this
     *        timeout
     * @return this authenticator
     * @throws IllegalArgumentException if the first argument is larger
     *         than zero, if the second argument is less than zero,
     *         or if the third argument, when not zero, is less than
     *         the second argument
     */
    public EjwsBasicAuthenticator setTimeLimits(int lowerTimeDiffLimit,
						int upperTimeDiffLimit,
						int passphraseTimeout)
	throws IllegalArgumentException
    {
	if (lowerTimeDiffLimit > 0) {
	    String msg = errorMsg("positiveLowerTDL", lowerTimeDiffLimit);
	    throw new IllegalArgumentException(msg);
	}
	if (upperTimeDiffLimit < 0) {
	    String msg = errorMsg("negativeUpperTDL", upperTimeDiffLimit);
	    throw new IllegalArgumentException(msg);
	}
	if (passphraseTimeout != 0 && passphraseTimeout < upperTimeDiffLimit) {
	    int uTDL = upperTimeDiffLimit;
	    String msg =
		errorMsg("passphraseTimeout", passphraseTimeout, uTDL);
	    throw new IllegalArgumentException(msg);
	}
	this.lowerTimeDiffLimit = lowerTimeDiffLimit;
	this.upperTimeDiffLimit = upperTimeDiffLimit;
	this.passphraseTimeout = passphraseTimeout;
	return this;
    }


    /**
     * Constructor.
     * @param realm the HTTP realm
     */
    public EjwsBasicAuthenticator(EmbeddedWebServer ews, String realm) {
	this(ews, realm, new ConcurrentHashMap<String,Entry>());
    }

    /**
     * Constructor providing a map.
     * A user-supplied map can be implemented so as to allow one to obtain
     * passwords and roles from a database or some other form of persistent
     * storage. If entries can be added while a server using this
     * authenticator is running, the map should have a thread-safe
     * implementation.
     * @param realm the HTTP realm
     * @param map a map associating a user name with a table entry.
     */
    public EjwsBasicAuthenticator(EmbeddedWebServer ews,
				  String realm,
				  Map<String,Entry> map)
    {
	super(ews, realm);
	setThisObject(this);
	unencodedRealm = realm;
	this.map = map;
    }

    // private Certificate[] defaultCertChain = null;

    /*
     * Set a default certificate chain.
     * This method allows passwords to be checked or generated when a
     * reverse proxy provides SSL encryption.  When HTTPS is used and
     * this method is called with a non-null argument, that
     * certificate chain will override the certificate provided by an
     * SSL connection.
     * @param chain the certificate chain
     * @return this authenticator
    public EjwsSecureBasicAuth setCertificateChain(Certificate[] chain) {
	defaultCertChain = chain;
	return this;
    }
     */


    /*
     * Set a default certificate chain given a keystore and alias.
     * This method allows passwords to be checked or generated when a
     * reverse proxy provides SSL encryption.  When HTTPS is used and
     * this method is called with a non-null argument, that
     * certificate chain will override the certificate provided by an
     * SSL connection.
     * @param ksis the input stream used to read a Java key store
     * @param password the store password for the keystore
     * @param alias the alias used to find the certificate chain
     * @return this authenticator
     * @throws KeyStoreException if a keystore exception occurred
     * @throws IOException if an IO error occurred while reading the
     *         input stream
     * @throws CertificateException if a certificate chain could not
     *         be created
     * @see KeyStore
    public EjwsSecureBasicAuth setCertificateChain(InputStream ksis,
						   char[] password,
						   String alias)
	throws KeyStoreException, IOException, CertificateException
    {
	try {
	    KeyStore ks = KeyStore.getInstance("JKS");
	    if (password == null) password = "changeit".toCharArray();
	    ks.load(ksis, password);
	    defaultCertChain = ks.getCertificateChain(alias);
	    return this;
	} catch (NoSuchAlgorithmException e) {
	    // should not occcur.
	    throw new UnexpectedExceptionError(e);
	}
    }
     */

    /*
     * Set a default certificate chain by using a proxy's certificate chain.
     * This method allows passwords to be checked or generated when a
     * reverse proxy provides SSL encryption.  When HTTPS is used and
     * this method is called with a non-null argument, that
     * certificate chain will override the certificate provided by an
     * SSL connection.  In addition, when a reverse proxy is
     * configured, the ".base" field in an SBL file provided by the
     * server will be a URI whose host name and port matches that of
     * the reverse proxy and whose path starts with the reverse
     * proxy's path.
     * <P>
     * Note: the current implementation assumes that the proxy's certificate
     * can be obtained by opening a TCP connection the proxy itself based
     * on its host and port. In addition, the certificate must be available
     * before the server used by this authenticator has started.
     * <P>
     * If there is an error, the certificate chain will be set to null;
     * @param proxy the URI of the proxy
     * @return this authenticator
     * @throws CertificateException if the certificate chain could not be
     *         computed or obtained
     * @throws IllegalArgumentException if an argument was incorrect
    public EjwsSecureBasicAuth setCertificateChain(URI proxy) throws
	CertificateException, IllegalArgumentException
    {
	setReverseProxy(proxy);
	if (proxy == null) return this;
	String scheme = proxy.getScheme();
	if (scheme == null) {
	    throw new IllegalArgumentException("noScheme");
	}
	scheme = scheme.trim().toLowerCase();

	if (scheme == null || !scheme.equals("https")) {
	    throw new IllegalArgumentException("notHttps");
	}
	String host = proxy.getHost();
	if (host == null) {
	    throw new IllegalArgumentException("noHost");
	}
	host = host.trim();
	if (host.length() == 0) {
	    throw new IllegalArgumentException("noHost");
	}
	int port = proxy.getPort();
	if (port < 0) {
	    if (scheme.equals("https")) {
		port = 443;
	    } else {
		throw new IllegalArgumentException("noPort");
	    }
	}
	Certificate[] chain = null;
	SSLSocketFactory factory = (SSLSocketFactory)
	    SSLSocketFactory.getDefault();
	try (SSLSocket socket = (SSLSocket)factory.createSocket(host, port)) {
	    SSLSession session = socket.getSession();
	    chain = session.getPeerCertificates();
	    if (chain.length == 0) {
		throw new CertificateException("noChain");
	    }
	} catch (CertificateException ec) {
	    throw ec;
	} catch (Exception e) {
	    throw new CertificateException("noChain");
	}
	defaultCertChain = chain;
	return this;
    }
     */


    /**
     * Add a user name and password for this authenticator's HTTP realm.
     * The new entry will use password authentication.
     * @param username the user name
     * @param password the password
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
     * @throws IllegalStateException if the map already contains the user
     */
    public void add(String username, String password)
	throws UnsupportedOperationException, IllegalStateException
    {
	if (map.containsKey(username)) {
	    throw new IllegalStateException(errorMsg("hasUser", username));
	}
	if (utable != null) {
	    utable.putEntry(username, new Entry(password, null));
	} else {
	    map.put(username, new Entry(password, null));
	}
    }

    /**
     * Add a user name, the user's password and the user's roles for
     * this authenticator's HTTP realm.
     * The new entry will use password authentication.
     * @param username the user name
     * @param password the user's password
     * @param roles the user's roles
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
     * @throws IllegalStateException if the map already contains the user
     */
    public void add(String username, String password, Set<String> roles )
	throws UnsupportedOperationException, IllegalStateException
    {
	if (map.containsKey(username)) {
	    if (password.equals(map.get(username).getPassword())) {
		return;
	    }
	    throw new IllegalStateException(errorMsg("hasUser", username));
	}
	if (utable != null) {
	    utable.putEntry(username, new Entry(password, roles));
	} else {
	    map.put(username, new Entry(password, roles));
	}
    }

    EjwsUserTable<EjwsBasicAuthenticator,EjwsBasicAuthenticator.Entry>
	utable = null;



    /**
     * Set this authenticator's user table.
     * @param utable the user table
     * @return this authenticator
     */
    public EjwsBasicAuthenticator
	setUserTable(EjwsUserTable
		     <EjwsBasicAuthenticator,EjwsBasicAuthenticator.Entry>
		     utable)
    {
	this.utable = utable;
	utable.setMap(map);
	utable.setAuth(this);
	return this;
    }

    /**
     * Get this authenticator's user table
     * @return the user table.
     */
    public EjwsUserTable<EjwsBasicAuthenticator,EjwsBasicAuthenticator.Entry>
	getUserTable()
    {
	return utable;
    }


    /**
     * {@inheritDoc}
     */
    public void add(EjwsAuthenticator.UserInfo info)
	throws IllegalStateException
    {
	String userName = info.getUserName();
	if (map.containsKey(userName)) {
	    // Due to the authenticator we may try twice.
	    // Check that nothing has changed.
	    Entry oldEntry = map.get(userName);
	    if (info.getPassword().equals(oldEntry.getPassword())) {
		return;
	    }
	    throw new IllegalStateException(errorMsg("hasUser", userName));
	}
	Entry entry = new Entry(info.getPassword(), info.getRoles());
	// entry.setActive(info.isActive());
	if (info.isActive()) {
	    entry.makeActive();
	    if (onAccountActive  != null) {
		onAccountActive.accept(userName, true);
	    }
	}
	entry.setSBLCompressed(info.isSBLCompressed());
	entry.setSBL(info.getSBL());
	if (utable != null) {
	    utable.putEntry(userName, entry);
	} else {
	    map.put(userName, entry);
	}
    }

    /**
     * Remove a user.
     * @param name the user name
     * @param gpg true to delete GPG permanent entries;
     *        false for SBL permanent entries
     */
    public boolean removeUser(String name, boolean gpg) {
	SBLStore store = getSBLStore();
	boolean status = false;
	try {
	    if (gpg) {
		if (hasGPGKey(name)) {
		    try {
			String fpr = getFingerprint(name);
			if (fpr != null) {
			    deleteWithFingerprint(fpr);
			}
		    } catch (Exception e) {
			System.err.println("GPG delete failed: "
					   + e.getMessage());
		    }
		}
	    } else if (store != null) {
		if (store.containsUser(name)) {
		    try {
			store.removeUser(name);
		    } catch (Exception e) {
		    }
		}
		status = (map.remove(name) != null);
		return status;

		/*
		  } else {
		  File target = new File(sbldir, name +"--a");
		  if (!target.exists()) {
		  target = new File(sbldir, name + "--p");
		  }
		  if (!target.exists()) {
		  target = new File(sbldir, name + "--r");
		  }
		  if (!target.exists()) {
		  target = new File(sbldir, name);
		  if (target.exists()) {
		  // clean up only - a previous case failed.
		  target.delete();
		  }
		  return false;
		  }
		  target.delete();
		  boolean status = (map.remove(name) != null);
		*/
	    }
	    status = (map.remove(name) != null);
	    return status;
	} finally {
	    if (onAccountRemoval != null) {
		onAccountRemoval.accept(name, status);
	    }
	}
    }

    /**
     * Remove a user.
     * @param name the name of the user
     */
    public boolean removeUser(String name) {
	/*
	try {
	    if (utable != null) {
		return utable.removeEntry(name);
	    } else {
		return removeUser(name, false);
	    }
	} catch (Exception e) {
	    return false;
	}
	*/
	boolean status = false;
	File gpgdir = gpghome();
	// File sbldir = getSBLDir();
	SBLStore store = getSBLStore();
	try {
	    if (utable != null) {
		status = utable.removeEntry(name);
		return status;
	    } else {
		if (gpgdir != null && hasGPGKey(name)) {
		    try {
			String fpr = getFingerprint(name);
			if (fpr != null) {
			    deleteWithFingerprint(fpr);
			}
		    } catch (Exception e) {
			System.err.println("GPG delete failed: "
					   + e.getMessage());
		    }
		    status = (map.remove(name) != null);
		    return status;
		} else if (store != null) {
		    if (store.containsUser(name)) {
			try {
			    store.removeUser(name);
			} catch (Exception e) {
			}
		    }
		    status = (map.remove(name) != null);
		    return status;
		    /*
		} else if (sbldir != null) {
		    File target = new File(sbldir, name +"--a");
		    if (!target.exists()) {
			target = new File(sbldir, name + "--p");
		    }
		    if (!target.exists()) {
			target = new File(sbldir, name + "--r");
		    }
		    if (!target.exists()) {
			target = new File(sbldir, name);
			if (target.exists()) {
			    // clean up only - a previous case failed.
			    target.delete();
			}
			map.remove(name);
			status = false;
			return status;
		    }
		    target.delete();
		    status = (map.remove(name) != null);
		    return status;
		    */
		}
		status = false;
		return status;
	    }
	} catch (Exception e) {
	    status = false;
	    return status;
	} finally {
	    if (onAccountRemoval != null) {
		onAccountRemoval.accept(name, status);
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    public boolean makeUserActive(String name, boolean gpg) {
	boolean status = false;
	SBLStore store = getSBLStore();
	try {
	    EjwsBasicAuthenticator.Entry entry = map.get(name);
	    if (entry == null) {
		status = false;
		return status;
	    }
	    if (gpg) {
		try {
		    if (!validGPGUser(name)
			&& !getTrustedKeyIDs().contains(name)) {
			signKey(name, true);
		    }
		} catch (Exception e) {
		    status = false;
		    return status;
		}
	    } else if (store != null) {
		if (store.containsUser(name)) {
		    try {
			store.makeActive(name);
		    } catch (Exception e) {}
		}
		/*
	    } else if (sbldir != null) {
		try {
		    File pending = new File(sbldir, name + "--p");
		    File target = new File(sbldir, name + "--a");
		    if (pending.exists()) {
			Path ppath = pending.toPath();
			Path tpath  = target.toPath();
			try {
			    Files.move(ppath, tpath,
				       StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
			    Files.move(ppath, tpath);
			}
		    }
		} catch (Exception e) {
		    status = false;
		    return status;
		}
		*/
	    } else {
		status = false;
		return status;
	    }
	    entry.makeActive();
	    status = true;
	    return status;
	} finally {
	    if (onAccountActive != null) {
		onAccountActive.accept(name, status);
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    protected boolean makeUserActiveInMap(String name) {
	boolean status = false;
	try {
	    Entry entry = map.get(name);
	    if (entry != null) {
		entry.makeActive();
		status = true;
		return status;
	    } else {
		status = false;
		return status;
	    }
	} finally {
	    if (onAccountActive != null) {
		onAccountActive.accept(name, status);
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    protected boolean removeUserFromMap(String name) {
	// return map.remove(name) != null;
	boolean status = false;
	try {
	    status =  map.remove(name) != null;
	    return status;
	} finally {
	    if (onAccountRemoval != null) {
		onAccountRemoval.accept(name, status);
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    public boolean makeUserActive(String name) {
	/*
	if (utable != null) {
	    return utable.makeActive(name);
	} else {
	    return makeUserActive(name, false);
	}
	*/
	boolean status = false;
	try {
	    EjwsBasicAuthenticator.Entry entry = map.get(name);
	    if (entry != null && entry.isActive()) {
		return status;
	    }
	    if (utable != null) {
		status =  utable.makeActive(name);
		return status;
	    } else {
		// File sbldir = getSBLDir();
		SBLStore store = getSBLStore();
		if (entry != null) {
		    try {
			if (!getTrustedKeyIDs().contains(name)) {
			    if (hasGPGKey(name)) {
				if (validGPGUser(name) == false) {
				    signKey(name, true);
				}
			    } else if (store != null) {
				if (!entry.isActive()
				    && store.containsUser(name)) {
				    store.makeActive(name);
				}
				/*
			    } else if (sbldir != null) {
				File pending = new File(sbldir, name + "--p");
				File target = new File(sbldir, name + "--a");
				if (pending.exists()) {
				    Path ppath = pending.toPath();
				    Path tpath  = target.toPath();
				    try {
					Files.move(ppath, tpath,
						   StandardCopyOption
						   .ATOMIC_MOVE);
				    }catch(AtomicMoveNotSupportedException e) {
					Files.move(ppath, tpath);
				    }
				}
				*/
			    }
			    entry.makeActive();
			    status = true;
			    return status;
			}
		    } catch (Exception e){}
		} else {
		    try {
			if (hasGPGKey(name)) {
			    if (validGPGUser(name) == false) {
				signKey(name, true);
			    }
			    String alias = savedAlias;
			    String uriString = generateRequestURI(null);
			    String recipients[] = {name};
			    createUser(name, uriString, recipients, null)
				.setURI(alias)
				.setActive(true)
				.addUser(true);
			    status = true;
			    return status;
			} else if (store != null) {
			    if (store.containsUser(name)) {
				String s = store.getSBLData(name);
				if (s != null) {
				    store.makeActive(name);
				    createUser(s, null)
					.setActive(true)
					.addUser();
				    status = true;
				}
			    }
			    return status;
			    /*
			} else if (sbldir != null) {
			    File pending = new File(sbldir, name + "--p");
			    File target = new File(sbldir, name + "--a");
			    if (pending.exists()) {
				Path ppath = pending.toPath();
				Path tpath  = target.toPath();
				try {
				    Files.move(ppath, tpath,
					       StandardCopyOption.ATOMIC_MOVE);
				}catch(AtomicMoveNotSupportedException e) {
				    Files.move(ppath, tpath);
				}
				String s =
				    readSBLData(new FileInputStream(target));
				createUser(s, null)
				    .setActive(true)
				    .addUser();
				status = true;
				return status;
			    }
			    */
			}
		    } catch (Exception e){};
		    status = false;
		    return status;
		}
	    }
	    return status;
	} finally {
	    if (onAccountActive != null) {
		onAccountActive.accept(name, status);
	    }
	}
    }

    private boolean notLoadedFromGPG = true;
    private boolean notLoadedFromSBL = true;

    /**
     * {@inheritDoc}
     */
    public EjwsBasicAuthenticator loadFromDirs()
	throws UnsupportedOperationException
    {
	// throw new UnsupportedOperationException();
	super.loadFromDirs();
	String alias = getLoginAlias();
	if (alias != null && notLoadedFromGPG && gpghome() != null) {
	    notLoadedFromGPG = false;
	    Set<String> userSet = getGPGUsers(true);
	    /*
	    for (String usr: userSet) {
		System.out.println("userSet contains " + usr);
	    }
	    for (String usr: getAdminUsers()) {
		System.out.println("admin users contains " + usr);
	    }
	    */
	    userSet.addAll(getAdminUsers());
	    String uriString = generateRequestURI(null);
	    for (String email: userSet) {
		String recipients[] = {email};
		try {
		    // System.out.println("email = " + email);
		    if (email.equals("admin") || email.equals("keysigner")) {
			continue;
		    }
		    createUser(email, uriString, recipients, null)
			.setURI(alias)
			.setActive(true)
			.addUser(true);
		} catch (IOException eio) {
		}
	    }
	    boolean isActive = isActiveDefault();
	    for (String email: getGPGUsers(false)) {
		if (userSet.contains(email)) {
		    // an entry in getAdminUsers may not have been
		    // signed, but was added above regardless.
		    continue;
		}
		String recipients[] = {email};
		// String uriString = generateRequestURI(null);
		// String alias = getLoginAlias();
		try {
		    createUser(email, uriString, recipients, null)
			.setURI(alias)
			.setActive(isActive)
			.addUser(true);
		} catch (IOException eio) {
		}
	    }
	}
	SBLStore store = getSBLStore();
	// File sbldir = getSBLDir();
	if (notLoadedFromSBL && store != null) {
	    notLoadedFromSBL = false;
	    store.mapStream().forEach((ent) -> {
		    try {
			String name = ent.getKey();
			SBLStore.Entry value = ent.getValue();
			if (value.getPWMode() == false) {
			    String s = value.getData();
			    System.out.println("user = " + name
					       + ", isActive = "
					       + value.isActive());
			    createUser(s, null)
				.setActive(value.isActive())
				.addUser();
			} else {
			    System.out.println("pwmode = true for " + name);
			}
		    } catch (IOException eio) {
			System.err.println(eio.getMessage());
		    }
		});
	}
	/*
	if (notLoadedFromSBL && sbldir != null) {
	    notLoadedFromSBL = false;
	    try {
		for (File f: sbldir.listFiles()) {
		    // System.out.println("... checking " + f.getName());
		    if (f.getName().endsWith("--a")) {
			FileReader r = new FileReader(f, UTF8);
			StringWriter w = new StringWriter();
			r.transferTo(w);
			String s = w.toString();
			// System.out.println("... creating user");
			createUser(s, null)
			    .setActive(true)
			    .addUser();
		    } else if (f.getName().endsWith("--p")) {
			FileReader r = new FileReader(f, UTF8);
			StringWriter w = new StringWriter();
			r.transferTo(w);
			String s = w.toString();
			createUser(s, null)
			    .setActive(false)
			    .addUser();
		    } else {
			System.err.println(f.getCanonicalPath()
					   + " should be deleted");
		    }
		}
	    } catch (IOException eio) {
		System.err.println(eio.getMessage());
	    }
	}
	*/
	return this;
    }


    /**
     * Handle exceptions by sending an error response
     * @param t the {@link HttpExchange} being processed
     * @param e an exception that is being handled
     */
    public void handleError(HttpExchange t,  Exception e)
	throws IOException
    {
	String msg = e.getMessage();
	String prefix = errorMsg("Response409");
	msg = (msg == null)? prefix:
	    prefix + ": " + msg;
	if (tracer != null) {
	    String ct = "" + Thread.currentThread().getId();
	    try {
		tracer.append("(" + ct + ") " + msg + "\n");
	    } catch (IOException e3) {}
	} else {
	    System.err.println(msg);
	}
	byte[] response =  msg.getBytes(UTF8);
	int len = response.length;
	t.getResponseHeaders().set("content-type", "text/plain; charset=utf-8");
	t.sendResponseHeaders(409, len);
	t.getResponseBody().write(response);
    }

    /**
     * Authenticate an HTTP request.
     * @param t the HTTP exchange object
     * @return the authentication result
     * @see Authenticator#authenticate(HttpExchange)
     */
    @Override
    public Authenticator.Result authenticate(HttpExchange t) {
	if (tracer != null) {
	    String ct = "" + Thread.currentThread().getId();
	    try {
		tracer.append("(" + ct + ") authenticating "
			      + t.getRequestURI().toString() + "\n");
		Headers headers = t.getRequestHeaders();

		String s = headers.getFirst("Authorization");
		if (s == null || s.trim().length() == 0) {
		    tracer.append("(" + ct + ") ... no Authorization header\n");
		}
	    } catch (IOException e) {}
	}

	ServerCookie cookie = findServerCookie(t);
	if (cookie == null) {
	    cookie = createServerCookie(t);
	}
	cookieTL.set(cookie);

	String alias = savedAlias;

	if (loginPathUsed && loginPath == null) {
	    HttpHandler handler = t.getHttpContext().getHandler();
	    if (handler instanceof FileHandler) {
		fileHandler = (FileHandler) handler;
		alias = fileHandler.getLoginAlias();
		savedAlias = alias;
		if (alias != null) {
		    String root = t.getHttpContext().getPath();
		    if (!root.endsWith("/")) root = root + "/";
		    loginPath = root + alias;
		    loginRequired = fileHandler.isLoginRequired();
		} else {
		    loginPathUsed = false;
		}
	    } else {
		loginPathUsed = false;
	    }
	}
	boolean foundLogin = false;
	if (loginPathUsed) {
	    URI requestURI = t.getRequestURI();
	    if (t.getRequestURI().getPath().equals(loginPath)) {
		String query = requestURI.getRawQuery();
		if (query != null) {
		    Map<String,String> fmap = WebDecoder.formDecode(query,
								    false);
		    String username =fmap.get("user");
		    String afpr = getAdminFingerprint(username);
		    String type = fmap.get("uploadtype");
		    String authorization = fmap.get("authcode");

		    if (type != null && type.equals("no-upload")) {
			// Just send a message to the user, typically
			// after certain authentication failures.
			String msg = fmap.get("msg");
			String dest = fileHandler.getLogoutURI()
			    .toASCIIString();
			if (dest == null) dest = "/";
			msg = errorMsg(msg, dest);
			byte[] data = msg.getBytes(UTF8);
			Headers rhdrs = t.getResponseHeaders();
			rhdrs.set("Content-type",
				  "text/html; charset=utf-8");
			try {
			    setCookie(t, cookie);
			    t.sendResponseHeaders(202, data.length);
			    t.getResponseBody().write(data);
			} catch (Exception e) {}
			return new Authenticator.Success(null);
		    } else if (!(getCanAddAccount() || afpr != null)
			       && type != null
			       && !type.equals("login")) {
			String msg;
			if (type == "pgpkey" || type == "sbl"
			    || type == "password") {
			    msg = errorMsg("noAccountCreation");
			} else {
			    String uriS = requestURI.toString();
			    msg = errorMsg("badAccountCreation", uriS);
			}
			byte[] data = msg.getBytes(UTF8);
			Headers rhdrs = t.getResponseHeaders();
			rhdrs.set("Content-type",
				  "text/plain; charset=utf-8");
			try {
			    setCookie(t, cookie);
			    t.sendResponseHeaders(403, data.length);
			    t.getResponseBody().write(data);
			} catch (IOException eio){}
			return new Authenticator.Success(null);
		    }
		    if (username != null) {
			boolean deleteRequest = false;
			if (type != null && type.equals("login")) type = null;
			if (type != null && type.equals("delete")) {
			    /*
			    if (username.equals("admin")
				|| username.equals("keysigner")
				|| getAdminUsers().contains(username)) {
				String msg = errorMsg("noDelete", username);
				byte[] data = msg.getBytes(UTF8);
				Headers rhdrs = t.getResponseHeaders();
				rhdrs.set("Content-type",
					  "text/html; charset=utf-8");
				rhdrs.set("Cache-Control", "no-cache");
				try {
				    setCookie(t, cookie);
				    t.sendResponseHeaders(202, data.length);
				    t.getResponseBody().write(data);
				} catch (Exception e) {}
				return new Authenticator.Success(null);
			    }
			    */
			    deleteRequest = true;
			    // first we have to log in.
			    type = null;
			} else {
			    // just in case.
			    removeFromDeleteSet(username);
			}
			if (type != null && map.containsKey(username)) {
			    // trying to add an existing user.
			    String msg = errorMsg("accountExists", username);
			    byte[] data = msg.getBytes(UTF8);
			    try {
				setCookie(t, cookie);
				t.sendResponseHeaders(403, data.length);
				t.getResponseBody().write(data);
			    } catch (IOException eio){}
			    return new Authenticator.Success(null);
			}
			if (type == null && map.containsKey(username)) {
			    if (map.get(username).isActive() == false) {
				// We have a pending account.
				String msg = errorMsg("pending", username);
				byte[] data = msg.getBytes(UTF8);
				Headers rhdrs = t.getResponseHeaders();
				rhdrs.set("Content-type",
					  "text/html; charset=utf-8");
				try {
				    setCookie(t, cookie);
				    t.sendResponseHeaders(202, data.length);
				    t.getResponseBody().write(data);
				} catch (Exception e) {}
				return new Authenticator.Success(null);
			    }
			}
			byte[] array = (type == null)? getSBL(username):
			    (type.equals("pgpkey") ||type.equals("sbl"))?
			    requestFromUser(username,type,authorization):
			    null;
			/*
			System.out.println("array.length = " +
					   ((array == null)? -1: array.length));
			*/
			if (array != null) {
			    boolean isGZIP = (type == null)?
				isSBLCompressed(username):
				false;
			    // System.out.println("isGZIP = " + isGZIP);
			    Headers rheaders = t.getResponseHeaders();
			    rheaders.set("Content-Type",
					 "application/vnd.bzdev.sblauncher");
			    rheaders.set("Cache-Control", "no-cache");
			    if (type == null) {
				// in case we log out and then log in again
				// for the same user.
				logoutSet.remove(new PWInfoKey(username,
							       cookie));
			    }
			    if (isGZIP) {
				rheaders.set("Content-Encoding", "gzip");
			    }
			    try {
				setCookie(t, cookie);
				t.sendResponseHeaders(200, array.length);
				OutputStream os = t.getResponseBody();
				os.write(array);
				os.flush();
				os.close();
				// sent a response containing the SBL file.
				// If we return null, the Auth filter will not
				// continue along the filter chain, but this is
				// not documented.
			    } catch (IOException eio) {}
			    // return null;
			    return new Authenticator.Success(null);
			} else if (type.equals("password")) {
			    String un = WebEncoder.htmlEncode(username);
			    String a = authorization;
			    String msg = (authorization != null)?
				errorMsg("setPasswordAuth", un, loginPath, a):
				errorMsg("setPassword", un, loginPath);
			    byte[] data = msg.getBytes(UTF8);
			    Headers rhdrs = t.getResponseHeaders();
			    rhdrs.set("Content-type",
				      "text/html; charset=utf-8");
			    try {
				setCookie(t, cookie);
				t.sendResponseHeaders(200, data.length);
				t.getResponseBody().write(data);
			    } catch (Exception e) {}
			    return new Authenticator.Success(null);
			} else {
			    try {
				String string = requestURI.toString();
				String msg;
				if (type == null || type.equals("login")) {
				    if (map.containsKey(username)) {
					msg =
					    errorMsg("useSBL", username);
				    } else {
					msg =
					    errorMsg("noUserAccount", username);
				    }
				} else {
				    msg =
					errorMsg("badAccountCreation", string);
				}
				byte[] data = msg.getBytes(UTF8);
				Headers rhdrs = t.getResponseHeaders();
				rhdrs.set("Content-type",
					  "text/html; charset=utf-8");
				setCookie(t, cookie);
				t.sendResponseHeaders(403, data.length);
				t.getResponseBody().write(data);
			    } catch (IOException eio){}
			    // return null;
			    return new Authenticator.Success(null);
			}
		    }
		    // otherwise just continue as normal.

		} else if (t.getRequestMethod().equalsIgnoreCase("POST")) {
		    // System.out.println("*** processing POST");
		    Headers hdrs = t.getRequestHeaders();
		    String contentType = hdrs.getFirst("content-type");
		    String contentLengthS = hdrs.getFirst("content-length");
		    String authorization = hdrs
			.getFirst("x-org-bzdev-authcode");
		    long length = 0;
		    if (contentLengthS != null) {
			try {
			    length = Long.valueOf(contentLengthS);
			} catch (Exception e) {
			}
		    }
		    InputStream is = t.getRequestBody();
		    try {
			if (contentType.equals("application/pgp-keys")) {
			    InputStreamReader r = new
				InputStreamReader(is, UTF8);
			    StringWriter w = new StringWriter();
			    r.transferTo(w);
			    String value = w.toString();
			    try {
				/*
				EjwsAuthenticator.GPGKeyIDs keyids
				    = storeGPGKey(gpghome(), value);
				*/
				EjwsAuthenticator.GPGKeyIDs keyids
				    = showGPGKey(value);
				String email = keyids.getEmailAddress();
				String fpr = keyids.getFingerprint();
				String afpr = getAdminFingerprint(email);
				boolean isAdmin = fpr.equals(afpr);
				if (getCanAddAccount() || isAdmin) {
				    String uriString = generateRequestURI(null);
				    String recipients[] = {email};
				    AddStatus status = isAdmin?
					AddStatus.OK: getUserStatus(email);
				    if (status != AddStatus.REJECTED) {
					storeGPGKey(value, keyids);
				    }
				    if (status == AddStatus.PENDING
					&& authorization != null) {
					if (authorization
					    .equals(authCode.getCode(email))) {
					    System.out.println("signing key for"
							       + email);
					    signKey(email, true);
					    status = AddStatus.OK;
					    System.out.println
						("authorization processed");
					}
				    }
				    // boolean active = isActiveDefault();
				    String msg =
					errorMsg("pleaseVisit", uriString);
				    int rc = 201;
				    switch(status) {
				    case PENDING:
					msg =
					    errorMsg("processingAC", uriString);
					rc = 202;
					createUser(email, uriString,
						   recipients, null)
					    .setURI(alias)
					    .setActive(false)
					    .addUser(true);
					break;
				    case OK:
					createUser(email, uriString,
						   recipients, null)
					    .setURI(alias)
					    .setActive(true)
					    .addUser(true);
					uriString = generateRequestURI(email);
					Headers rhdrs = t.getResponseHeaders();
					rhdrs.set("Location", uriString);
					break;
				    case REJECTED:
					rc = 403;
					msg =
					   errorMsg("badAccountCreation",email);
					break;
				    }
				    /*
				    if (isAdmin) {
					createUser(email, uriString,
						   recipients, null)
					    .setURI(alias)
					    .setActive(true)
					    .addUser(true);
					uriString = generateRequestURI(email);
					Headers rhdrs = t.getResponseHeaders();
					rhdrs.set("Location", uriString);
				    } else {
					rc = 403;
					msg =
					   errorMsg("badAccountCreation",email);
				    }
				    */
				    byte[] data = msg.getBytes(UTF8);
				    Headers rhdrs = t.getResponseHeaders();
				    rhdrs.set("Content-type",
					      "text/html; charset=utf-8");
				    rhdrs.set("Cache-Control", "no-cache");
				    setCookie(t, cookie);
				    t.sendResponseHeaders(rc, data.length);
				    t.getResponseBody().write(data);
				    if (onAccountRequest != null) {
					onAccountRequest.accept(email, status);
				    }
				    return new Authenticator.Success(null);
				    // return null;
				} else {
				    setCookie(t, cookie);
				    t.sendResponseHeaders(404, -1);
				    // return null;
				    return new Authenticator.Success(null);
				}
			    } catch (Exception e) {
				handleError(t, e);
			    }
			} else if (contentType.equals(SBLDATA)) {
			    // System.out.println("saw contentType " + SBLDATA);
			    try {
				if (getCanAddAccount()) {
				    String s = readSBLData(is);
				    String uname = getUserNameFromSBL(s);
				    AddStatus status = getUserStatus(uname);
				    if (status != AddStatus.REJECTED) {
					storeSBLData(s, status);
				    }
				    if (status == AddStatus.PENDING
					&& authorization != null) {
					if (authorization
					    .equals(authCode.getCode(uname))) {
					    signKey(uname, false);
					    status = AddStatus.OK;
					}
				    }
				    String uriString = generateRequestURI(null);
				    String msg =
					errorMsg("pleaseVisit", uriString);
				    int rc = 201;
				    switch(status) {
				    case PENDING:
					msg =
					    errorMsg("processingAC", uriString);
					rc = 202;
					createUser(s, null)
					    .setActive(false)
					    .addUser();
					break;
				    case OK:
					createUser(s, null)
					    .setActive(true)
					    .addUser();
					break;
				    case REJECTED:
					rc = 403;
					msg =
					   errorMsg("badAccountCreation",uname);
					break;
				    }
				    byte[] bytes = msg.getBytes(UTF8);
				    Headers rhdrs = t.getResponseHeaders();
				    rhdrs.set("content-type",
					      "text/html; charset=utf-8");
				    rhdrs.set("Cache-Control", "no-cache");
				    setCookie(t, cookie);
				    t.sendResponseHeaders(rc, bytes.length);
				    OutputStream os = t.getResponseBody();
				    os.write(bytes);
				    if (onAccountRequest != null) {
					onAccountRequest.accept(uname, status);
				    }
				} else {
				    setCookie(t, cookie);
				    t.sendResponseHeaders(404, -1);
				}
			    } catch (Exception e) {
				handleError(t, e);
			    }
			} else if(contentType.equals(FORMDATA)) {
			    if (getCanAddAccount()) {
				// InputStream is = t.getRequestBody();
				Map<String,String> qmap =
				    WebDecoder.formDecode(is);
				String un = qmap.get("user");
				String authcode = qmap.get("authcode");
				String pw1 = qmap.get("pw1");
				String pw2 = qmap.get("pw2");
				/*
				ServerCookie authCookie =
				    findAuthServerCookie(t);
				*/
				AddStatus status = AddStatus.OK;
				String uriString = generateRequestURI(null);
				String msg = null;
				if (un == null || pw1 == null || pw2 == null) {
				    msg = errorMsg("badPWSetup");
				    status = AddStatus.REJECTED;
				} else if (!pw1.equals(pw2)) {
				    msg = errorMsg("badPasswords");
				    status = AddStatus.REJECTED;
				}  else if (map.containsKey(un)) {
				    String une = WebEncoder.htmlEncode(un);
				    msg = errorMsg("accountExists", une);
				    status = AddStatus.REJECTED;
				} else {
				    status = getUserStatus(un);
				    String une = WebEncoder.htmlEncode(un);
				    msg = (status == AddStatus.REJECTED
					   || status == null)?
					errorMsg("badAccountCreation", une):
					(status == AddStatus.PENDING)?
					errorMsg("processingAC", uriString):
					errorMsg("pleaseVisit", uriString);
				}
				SBLStore store = getSBLStore();
				boolean authorized = false;
				if (status == AddStatus.PENDING
				    && authcode != null) {
				    if (authcode.equals(authCode.getCode(un))) {
					status = AddStatus.OK;
					authorized = true;
					// For this case, skip asking the
					// user for the password just provided.
					// just replicate what we did in
					// checkCredentials.
					long now = Instant.now()
					    .getEpochSecond();
					PWInfoKey key = new
					    PWInfoKey(un, cookie);
					pwmap.put(key, new PWInfo
						  (now+passphraseTimeout, pw1));
					loginMap.put(cookie.getValue(), un);
				    }
				}
				int rc = 201;
				switch(status) {
				case PENDING:
				    rc = 202;
				    add(un, pw1);
				    map.get(un).setActive(false);
				    if (store != null) {
					store.append(un, true, pw1, false);
				    }
				    break;
				case OK:
				    add(un, pw1);
				    map.get(un).setActive(true);
				    if (store != null) {
					store.append(un, true, pw1, true);
				    }
				    t.getResponseHeaders()
					.set("Location", uriString);
				    if (authorized) {
					uriString = generateRequestURI(null);
					String proto = t.getProtocol()
					    .toUpperCase();
					rc = ((proto.startsWith("HTTP") &&
					       proto.endsWith("/1.0")))?
					    302: 303;
				    } else {
					uriString = generateRequestURI(un);
				    }
				    System.out.println("uriString = "
						       + uriString);
				    break;
				case REJECTED:
				    rc = 403;
				    break;
				}
				byte[] data = msg.getBytes(UTF8);
				Headers rhdrs = t.getResponseHeaders();
				rhdrs.set("Content-type",
					  "text/html; charset=utf-8");
				rhdrs.set("Cache-Control", "no-cache");
				setCookie(t, cookie);
				t.sendResponseHeaders(rc, data.length);
				t.getResponseBody().write(data);
				if (onAccountRequest != null) {
				    onAccountRequest.accept(un, status);
				}

				HttpPrincipal p = authorized?
				    new EjwsPrincipal(un, unencodedRealm,
						      map.get(un).roles):
				    null;
				return new Authenticator.Success(p);
			    }
			} else {
			    setCookie(t, cookie);
			    t.sendResponseHeaders(415, -1);
			}
		    } catch (Exception eio2) {
			try {
			    eio2.printStackTrace();
			    setCookie(t, cookie);
			    t.sendResponseHeaders(205, -1);
			} catch(IOException eio) {}
		    }
		    // return null;
		    return new Authenticator.Success(null);
		}
		foundLogin = true;
		String cvalue = cookie.getValue();
		// System.out.println("foundLogin: cookie = " + cvalue);
		if (loginMap.containsKey(cvalue)) {
		    // to log out of an existing login so that
		    // the browser will ask for a user name and
		    // password.
		    String olduser = loginMap.get(cvalue);
		    // System.out.println("removing " + olduser);
		    removePWInfo(olduser);
		}
	    }
	}
	if (loginRequired) {
	    foundLoginTL.set(foundLogin);
	    flCode.set(0);
	}

	if (logoutPathUsed && logoutPath == null) {
	    HttpHandler handler = t.getHttpContext().getHandler();
	    if (handler instanceof FileHandler) {
		fileHandler = (FileHandler) handler;
		alias = fileHandler.getLogoutAlias();
		if (alias != null) {
		    String root = t.getHttpContext().getPath();
		    if (!root.endsWith("/")) root = root + "/";
		    logoutPath = root + alias;
		} else {
		    logoutPathUsed = false;
		}
	    } else {
		logoutPathUsed = false;
	    }
	}
	boolean foundLogout = false;
	if (logoutPathUsed
	    && t.getRequestURI().getPath().equals(logoutPath)) {
	    foundLogout = true;
	}

	if (loginRequired) {
	    foundLogoutTL.set(foundLogout);
	}

	InetAddress iaddr = t.getRemoteAddress().getAddress();

	if (tracer != null) {
	    String s = t.getRequestHeaders().getFirst("Authorization");
	    if (s == null || s.trim().length() == 0) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") ... no Authorization header\n");
		} catch (IOException e){}
	    }
	}

	addr.set(iaddr);
	msgTL.set(null);
	Authenticator.Result result = super.authenticate(t);
	if (result instanceof Authenticator.Success) {
	    HttpPrincipal p = ((Authenticator.Success)result).getPrincipal();
	    String un = p.getUsername();
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") authenticated user \""
				  + un + "\" for realm " + p.getRealm() + "\n");
		} catch (IOException e) {}
	    }
	    p = new EjwsPrincipal(p, map.get(un).roles);
	    if (foundLogin && loginFunction != null) {
		loginFunction.accept((EjwsPrincipal)p, t);
	    } else if (foundLogout) {
		if (logoutFunction != null) {
		    logoutFunction.accept((EjwsPrincipal)p, t);
		}
		PWInfoKey key = new PWInfoKey(un, cookie);
		if (pwmap.containsKey(key)) {
		    pwmap.remove(key);
		    loginMap.remove(cookie);
		    /*
		    System.out.println("... loginMap removed "
				       + cookie.getValue());
		    */
		}
	    } else if (!foundLogin && !foundLogout && authFunction != null) {
		authFunction.accept((EjwsPrincipal)p, t);
	    }
	    setCookie(t, cookie);
	    return new	Authenticator.Success(p);
	} else {
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") authentication failed\n");
		} catch (IOException e) {}
	    }
	    if (loginRequired) {
		int code = flCode.get();
		if (code != 0) {
		    if (code == 403) {
			Headers rhdrs = t.getResponseHeaders();
			/*
			rhdrs.set("Location",
				  fileHandler.getLogoutURI().toASCIIString());
			*/
			if (loginPath != null && msgTL.get() != null) {
			    rhdrs.set("Location",
				      loginPath + "?uploadtype=no-upload&msg="
				      +msgTL.get());
			    rhdrs.set("Cache-Control", "no-cache");
			    String proto = t.getProtocol().toUpperCase();
			    code = ((proto.startsWith("HTTP") &&
				     proto.endsWith("/1.0")))?
				302: 303;
			}
		    } else if (code == 307) {
			String proto = t.getProtocol().toUpperCase();
			code = ((proto.startsWith("HTTP") &&
				 proto.endsWith("/1.0")))?
			    302: 307;
			Headers rhdrs = t.getResponseHeaders();
			rhdrs.set("Location",
				  loginPath + "?uploadtype=no-upload&msg="
				  +msgTL.get());
			rhdrs.set("Cache-Control", "no-cache");
			String un = usernameTL.get();
			EjwsPrincipal p = new EjwsPrincipal(un,
							    realm,
							    map.get(un).roles);
			if (logoutFunction != null) {
			    logoutFunction.accept(p, null);
			}
		    }
		    setCookie(t, cookie);
		    return (code == 302 || code == 303)?
			new Authenticator.Failure(code):
			result;
		}
	    }
	    setCookie(t, cookie);
	    return result;
	}
    }

    private static class PWInfoKey
    {
	String username;
	// InetAddress address; // user's IP address
	String cvalue;
	public PWInfoKey(String username, ServerCookie cookie) {
	    this.username = username;
	    this.cvalue = cookie.getValue();
	}
	@Override
	public boolean equals(Object o) {
	    if (o instanceof PWInfoKey) {
		PWInfoKey obj = (PWInfoKey)o;
		return username.equals(obj.username)
		    && cvalue.equals(obj.cvalue);
	    }
	    return false;
	}

	@Override
	public int hashCode() {
	    int hashcode = 1;
	    hashcode = 127*hashcode +
		((username == null)? 0: username.hashCode());
	    hashcode = 127*hashcode
		+ ((cvalue == null)? 0: cvalue.hashCode());
	    return hashcode;
	}
    }


    private static class PWInfo {
	long expires;
	// InetAddress addr;		// client IP address
	String password;
	public PWInfo(long time, /*InetAddress addr,*/ String pw) {
	    expires = time;
	    password = pw;
	    // this.addr = addr;
	}
    }

    // private Map<PWInfoKey,PWInfo> pwmap = new ConcurrentHashMap<>();
    private Map<PWInfoKey,PWInfo> pwmap = Collections
	.synchronizedMap(new HashMap<PWInfoKey, PWInfo>());

    // Cache-control deosn't prevent an old login from being used by
    // a browser.
    private Set<PWInfoKey> logoutSet = Collections
	.synchronizedSet(new HashSet<PWInfoKey>());

    private Map<String,String> loginMap = Collections.
	synchronizedMap(new HashMap<String,String>());


    /**
     * Remove an entry from the password map.
     * This is called when logging out.
     * @param username the user name
     */
    public void removePWInfo(String username) {
	ServerCookie cookie = cookieTL.get();
	/*
	System.out.println("removePWInfo called: " + username + ", "
			   + cookie.getValue());
	*/
	PWInfoKey key = new PWInfoKey(username, cookie);
	pwmap.remove(key);
	loginMap.remove(cookie.getValue());
	/*
	System.out.println("logoutSet adding " + username + ", "
			   + cookie.getValue());
	*/
	logoutSet.add(key);
    }

    private long TOFFSET = 30*60;

    /**
     * Get the mode.
     * @return SecureBasicUtilities.Mode.PASSWORD
     */
    public SecureBasicUtilities.Mode getMode() {
	return SecureBasicUtilities.Mode.PASSWORD;
    }


    /**
     * Remove cached passwords whose timeout has expired.
     * The method can be called periodically to eliminate passwords
     * when a user has not explicitly logged out.
     */
    public void prune() {
	long now = Instant.now().getEpochSecond();
	Iterator<Map.Entry<PWInfoKey,PWInfo>> it = pwmap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<PWInfoKey,PWInfo> info = it.next();
	    if (info.getValue().expires < now - TOFFSET) {
		String un = info.getKey().username;
		it.remove();
		EjwsPrincipal p = new EjwsPrincipal(un, realm,
						    map.get(un).roles);
		if (logoutFunction != null) {
		    logoutFunction.accept(p, null);
		}
	    }
	}
    }

    /**
     * Check credentials.
     * This method is called for each incoming request to verify
     * the given name and password in the context of this Authenticator's realm.
     * @param username the user name
     * @param password the password
     */
    @Override
    public boolean checkCredentials(String username, String password) {
	long now = Instant.now().getEpochSecond();
	InetAddress iaddr = addr.get();
	ServerCookie cookie = cookieTL.get();
	PWInfoKey key = new PWInfoKey(username, cookie);
	if (logoutSet.contains(key)) {
	    logoutSet.remove(key);
	    return false;
	}
	PWInfo pwinfo = pwmap.get(key);
	// pwinfo is always null when passphraseTimeout is zero
	if (pwinfo != null) {
	    if ((pwinfo.expires - now) >= 0) {
		if (pwinfo.password.equals(password)) {
		    pwinfo.expires = passphraseTimeout + now;
		    if (tracer != null) {
			String ct = "" + Thread.currentThread().getId();
			try {
			    tracer.append("(" + ct + ") ... authentication "
					  + "cache hit found\n");
			} catch (IOException e){}
		    }
		    return true;
		}
	    } else {
		if (pwinfo.password.equals(password)) {
		    pwmap.remove(key);
		    loginMap.remove(cookie.getValue());
		    if (tracer != null) {
			String ct = "" + Thread.currentThread().getId();
			try {
			    tracer.append("(" + ct + ") ... authentication "
					  + "extended timeout expired\n");
			} catch (IOException e){}
		    }
		    if (loginRequired) {
			// the login timed out, so send the user
			// to the login page.
			flCode.set(307);
			msgTL.set("loginTimedout");
			usernameTL.set(username);
			logoutSet.add(key);
		    }
		    return false;
		}
	    }
	} else if (loginRequired) {
	    boolean foundLogin = foundLoginTL.get();
	    boolean foundLogout = foundLogoutTL.get();
	    if (!foundLogin && !foundLogout) {
		flCode.set(403);
		msgTL.set("loginLocation");
		logoutSet.add(key);
		return false;
	    }
	}
	Entry entry = map.get(username);
	if (entry == null) {
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") ... no entry for user \""
				  + username +"\"\n");
		} catch (IOException e){}
	    }
	    return false;
	}
	if (!entry.isActive()) {
	    if (!password.equals(entry.pw)) {
		// authentication mode is PASSWORD, so if this is wrong,
		// we should just fail without specifying a response code,
		// in which case the user should be asked for a new user name
		// and password.
		return false;
	    }
	    flCode.set(403);
	    if (loginRequired) {
		msgTL.set("pending0");
		logoutSet.add(key);
	    }
	    return false;
	}
	boolean result = password.equals(entry.pw);
	if (result == true && passphraseTimeout > 0) {
	    pwmap.put(key, new PWInfo(now + passphraseTimeout,
				      password));
	    loginMap.put(cookie.getValue(), username);
	}
	return result;
    }
}

//  LocalWords:  BasicAuthenticator username Appendable getFirst UTF
//  LocalWords:  authenticator's getRequestHeaders hdr keySet pw URI
//  LocalWords:  HttpExchange EjwsPrincipal FileHandler setLoginAlias
//  LocalWords:  setLogoutAlias authenticator exbundle superclass
//  LocalWords:  UnsupportedOperationException setTracer BiConsumer
//  LocalWords:  loginFunction logoutFunction authFunction addr
//  LocalWords:  loginRequired setLoginFunction setAuthorizedFunction
//  LocalWords:  setLogoutFunction passphraseTimeout InetAddress
//  LocalWords:  IllegalArgumentException negativePassphraseTimeout
//  LocalWords:  pwinfo
