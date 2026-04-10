package org.bzdev.ejws;

import com.sun.net.httpserver.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLSession;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.ConfigPropUtilities;
import org.bzdev.util.ConfigProperties;

//@exbundle org.bzdev.ejws.lpack.SecureBasicAuth

/**
 * Implementation of BasicAuthenticator supporting secure basic
 * authentication.  
 * <P>
 * As a protocol, secure basic authentication is identical to basic
 * authentication as described in RFC 7617.  The differences are in
 * how passwords are created and compared, and in how realms are
 * named.  Generally, a secure-basic-authentication password is a
 * URL-safe, base 64 encoding of a sequence of bytes. The first four
 * bytes is a time stamp of a 32 bit two's complement integer, stored
 * in little-endian byte order, providing the time at which the
 * password was created as the number of seconds since
 * 1970-01-01T00:00:00Z. The next four bytes is a 32-bit CRC of the
 * first 4 bytes of the sequence followed by the password as an array
 * of bytes using the UTF-8 character encoding.  The remainder of the
 * sequence is either
 * <OL>
 *   <LI> a SHA-256 message digest of (1) the first eight bytes of the sequence
 *        and (2) a password using the UTF-8 character encoding.
 *   <LI> a digital signature of (1) the first eight bytes of the sequence
 *        and (2) a password using the UTF-8 character encoding.
 *   <LI> a digital signature of (1) the first eight bytes of the sequence,
 *        (2) the DER encoding of the public key provided in an
 *        SSL certificate, and (3) a password using the UTF-8 character
 *        encoding.
 * </OL>
 * In all cases, both sides of the connection must use the same
 * password.  When a digital signature is used, the server must store
 * a user's public key and the name of the algorithm used to create the
 * signature.  To distinguish these cases, the realm (described in
 * RFC 7616) is prefaced with the following:
 * <OL>
 *   <LI> <STRONG>[D]</STRONG>. This corresponds to Case 1 above.
 *   <LI> <STRONG>[S]</STRONG>.  This corresponds to Case 2 above.
 *   <LI> <STRONG>[SC]</STRONG>. This corresponds to Case 3 above.
 * </OL>
 * Case 3 is the most secure. When an SSL or TLS connection is
 * established, a server will provide a client with the server's
 * certificate and the SSL or TLS protocol will ensure that the server
 * that provided the certificate has that certificate's private
 * key. This can stop a variety of man-in-the-middle and spoofing
 * attempts, at least to the point of inadvertently disclosing login
 * credentials: in either case the certificate will be different,
 * which means that the password that is generated will not be useful.
 * A CRC is used because this provides a cheap way of rejecting
 * authentication attempts that have the wrong password.
 * <P>
 * NOTE: For compatibility with openssl, one should use the keytool
 * program, or
 * {@link SecureBasicUtilities#createPEMPair(File,String,String,String,String,char[])},
 * to generate a key pair as a PKCS #12 file will then be created.
 * The openssl equivalent to
 * <BLOCKQUOTE><PRE><CODE>
 * keytool -genkey -keyalg EC -groupname secp256r1 \
 *         -sigalg SHA256withECDSA -dname CN=nobody@nowhere.com \
 *         -alias key -keypass password -storepass password \
 *         -keystore ecstore.pfx
 * </CODE></PRE></BLOCKQUOTE>
is
 * <BLOCKQUOTE><PRE><CODE>
 *  openssl ecparam -name prime256v1 -genkey -noout -out eckey.pem
 *  openssl req -new -x509 -key eckey.pem -out eccert.pem -days 360
 *  openssl pkcs12 -export -inkey eckey.pem -in eccert.pem \
 *          -name key -out ecstore.pfx
 * </CODE></PRE></BLOCKQUOTE>
 * although the choice of a signature algorithm (used to self sign) may
 * be different.  To add to the confusion, for the elliptic curve used
 * in this example, keytool prefers the name secp256r1 whereas openssl
 * prefers prime256v1.  When openssl is given the name secp256r1, it will
 * indicate that is is using prime256v1, whereas when keytool is given
 * the name prime256v1, it generates an error message. Also keytool must
 * use the same password for the file as for each entry it stores if the
 * file is to be compatible with openssl.
 * @see SecureBasicUtilities
 */
public class EjwsSecureBasicAuth extends EjwsAuthenticator {

    // private Appendable tracer = null;

    /*
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * @param tracer the Appendable for tracing requests and responses
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }
     */

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.SecureBasicAuth");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static final String SBLDATA = "application/vnd.bzdev.sblogindata";

    private static SecureBasicUtilities.Mode
	proposedMode(EmbeddedWebServer ews)
    {
	Certificate[][] certs = ews.getCertificates();

	SecureBasicUtilities.Mode proposed =
	    (!ews.usesHTTPS())? SecureBasicUtilities.Mode.DIGEST:
	    (certs == null || certs.length == 0)?
	    SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT:
	    SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT;
	return proposed;
    }

    /**
     * User entry.
     * This object contains a user's password, roles, and public-key
     * operations.
     * @see org.bzdev.net.SecureBasicUtilities
     */
   public static class Entry extends EjwsBasicAuthenticator.Entry {
       // pw and roles defined by superclasses.
       SecureBasicUtilities keyops;
       String pem = null;
       boolean sblCompressed = false;
       byte[] sbldata;
	
       /**
	* Constructor.
	* @param pw a password for a user
	* @param roles a set of roles that a user may have
	*/
       public Entry(String pw, Set<String>roles) {
	   super(pw, roles);
	   this.keyops = new SecureBasicUtilities();
       }

       /**
	* Constructor with a PEM-encoded public-key.
	* @param pem the PEM encoded public key or a certificate
	*        containing that public key.
	* @param pw a password for a user
	* @param roles a set of roles that a user may have
	*/
       public Entry(String pem, String pw, Set<String>roles) {
	   super (pw, roles);
	   try {
	       this.pem = pem;
	       this.keyops = new SecureBasicUtilities(pem);
	   } catch (IOException eio) {
	       String msg = "PEM";
	       throw new IllegalArgumentException(msg, eio);
	   } catch (GeneralSecurityException e) {
	       String msg = "PEM";
	       throw new IllegalArgumentException(msg, e);
	   }
       }
	
       /**
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
	*/
       public Entry(String pem, String pw, Set<String>roles,
		    boolean sblCompressed, byte[] sbldata)
       {
	   this(pem, pw, roles);
	   this.sblCompressed = sblCompressed;
	   this.sbldata = sbldata;
       }

       /**
	* Get the PEM encoded public key
	* @return the PEM encoded key; null if there is none
	*/
       public String getPEM() {return pem;}

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

       /**
	* Get an instance of an object that provides secure basic
	* authentication operations. This object is created by
	* this class's constructors.
	* @return the object
	*/
       public SecureBasicUtilities getSecureBasicUtilities() {
	   return  keyops;
       }

   }

    Map<String,Entry>map = null;


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


    // Certificate[] certificates;
    SecureBasicUtilities.Mode mode;

    ThreadLocal<Certificate[]> certchain = new ThreadLocal<>() {
	    @Override protected Certificate[] initialValue() {
		return null;
	    }
	};

    /**
     * Get the mode.
     * @return the mode
     */
    public SecureBasicUtilities.Mode getMode() {
	return mode;
    }

    private String unencodedRealm ;

    // allow a small but ample time window for clocks to be
    // out of sync, plus propagation delay.
    private int lowerTimeDiffLimit = -10;
    private int upperTimeDiffLimit = 150; 
    // How long to use a verified password
    private int passphraseTimeout = 1200;

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
     * @param lowerTimeDiffLimit the lower limit for the time difference
     *        in seconds (the default is -10 seconds).
     * @param upperTimeDiffLimit the upper limit for the time difference
     *        in seconds (the default is 150 seconds)
     * @param passphraseTimeout the time interval in seconds for which a
     *        password is valid (the default is 1200); 0 to disable this
     *        timeout
     * @throws IllegalArgumentException if the first argument is larger
     *         than zero, if the second argument is less than zero,
     *         or if the third argument, when not zero, is less than
     *         the second argument
     */
    public void setTimeLimits(int lowerTimeDiffLimit,
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
    }

    /**
     * Constructor for {@link SecureBasicUtilities.Mode#DIGEST} mode.
     * @param ews the {@link EmbeddedWebServer} to which this authenticator
     *        will be added
     * @param realm the HTTP realm
     */
    public EjwsSecureBasicAuth(EmbeddedWebServer ews, String realm) {
	this(ews, realm, proposedMode(ews),
	     new ConcurrentHashMap<String,Entry>());
    }

    /**
     * Constructor for {@link SecureBasicUtilities.Mode#DIGEST} mode,
     * specifying a map.
     * <P>
     * A user-supplied map can be implemented so as to allow one to obtain
     * passwords and roles from a database or some other form of persistent
     * storage. If entries can be added while a server using this
     * authenticator is running, the map should have a thread-safe
     * implementation.
     * @param realm the HTTP realm
     * @param map a map associating user names with entries containing
     *        a password, roles, and optionally a public key and related
     *        data
     */
    public EjwsSecureBasicAuth(EmbeddedWebServer ews,
			       String realm,
			       Map<String,Entry> map)
    {
	this(ews, realm, proposedMode(ews), map);
	/*
	super(SecureBasicUtilities
	      .encodeRealm(realm, proposedMode(ews)));
	unencodedRealm = realm;
	// mode = SecureBasicUtilities.Mode.DIGEST;
	this.map = map;
	*/
    }

    /*
    private static SecureBasicUtilities.Mode getMode(Certificate cert) {
	if (cert == null)
	    return SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT;
	return SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT;
    }
    */

    /**
     * Get the {@link SecureBasicUtilities.Mode} appropriate the use
     * of digital signatures.
     * @param certs a certificate chain; null or an array of length 0 if
     *        there are no certificates
     * @return SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT if the
     *         argument is null or an array of length 0;
     *         SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT otherwise
     *@throws IllegalArgumentException if one certificate chain's length
     *        is zero and another's length is not zero
     */
    public static SecureBasicUtilities.Mode getMode(Certificate[][] certs)
	throws IllegalArgumentException
    {
	if (certs == null || certs.length == 0)
	    return SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT;
	int firstcnt = 0;
	boolean hasCount = false;

	for (int i = 0; i < certs.length; i++) {
	    Certificate[] cert = certs[i];
	    if (hasCount == false) {
		firstcnt = cert.length;
		hasCount = true;
	    } else {
		if ((firstcnt == 0 && cert.length != 0)
		    || (firstcnt > 0 && cert.length == 0)) {
		    throw new IllegalArgumentException(errorMsg("certlength"));
		}
	    }
	}
	return getMode(firstcnt);
    }

    private static SecureBasicUtilities.Mode getMode(int cnt) {
	if (cnt == 0) return SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT;
	return SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT;
    }

    /**
     * Constructor for {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT}
     * mode and
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT} mode given
     * multiple certificates.
     * <P>
     * If the second argument is null or does not contain any certificates,
     * the mode is {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT};
     * otherwise the mode is
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT}
     * @param mode {@link SecureBasicUtilities.Mode#PASSWORD PASSWORD},
     *        {@link SecureBasicUtilities.Mode#DIGEST DIGEST},
     *        {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT SIGNATURE_WITH_CERT},
     *        {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT SIGNATURE_WITHOUT_CERT}
     * @param realm the HTTP realm
     */
    public EjwsSecureBasicAuth(EmbeddedWebServer ews,
			       String realm,
			       SecureBasicUtilities.Mode mode)
    {
	this(ews, realm, mode, new ConcurrentHashMap<String,Entry>());
    }

    /**
     * Constructor for {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT}
     * mode and
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT} mode given
     * multiple certificates, specifying a map.
     * <P>
     * If the second argument is null or does not contain any certificates,
     * the mode is {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT};
     * otherwise the mode is
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT}
     * <P>
     * A user-supplied map can be implemented so as to allow one to obtain
     * passwords and roles from a database or some other form of persistent
     * storage. If entries can be added while a server using this
     * authenticator is running, the map should have a thread-safe
     * implementation.
     * @param realm the HTTP realm
     * @param mode {@link SecureBasicUtilities.Mode#PASSWORD PASSWORD},
     *        {@link SecureBasicUtilities.Mode#DIGEST DIGEST},
     *        {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT SIGNATURE_WITH_CERT},
     *        {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT SIGNATURE_WITHOUT_CERT}
     * @param map a map associating user names with entries containing
     *        a password, roles, and optionally a public key and related
     *        data
     */
    public EjwsSecureBasicAuth(EmbeddedWebServer ews,
			       String realm,
			       SecureBasicUtilities.Mode mode,
			       Map<String,Entry> map)
    {
	super(ews, SecureBasicUtilities.encodeRealm(realm, mode));
	unencodedRealm = realm;
	this.map = map;
	this.mode = mode;
    }

    private Certificate[] defaultCertChain = null;

    /**
     * Set a default certificate chain.
     * This method allows passwords to be checked or generated when a
     * reverse proxy provides SSL encryption.  When HTTPS is used and
     * this method is called with a non-null argument, that
     * certificate chain will override the certificate provided by an
     * SSL connection.
     * @param chain the certificate chain
     * @return this authenticator
     */
    public EjwsSecureBasicAuth setCertificateChain(Certificate[] chain) {
	defaultCertChain = chain;
	return this;
    }


    /**
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
     */
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


    /**
     * Add a user name and password for this authenticator's HTTP realm.
     * @param username the user name
     * @param password the password
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
     * @throws IllegalStateException if the mode is not appropriate or
     *         if the user has already been added
    */
    public void add(String username, String password)
	throws UnsupportedOperationException, IllegalStateException
    {
	if (map.containsKey(username)) {
	    if (password.equals(map.get(username).getPassword())) {
		return;
	    }
	    throw new IllegalStateException(errorMsg("hasUser", username));
	}
	if (mode != SecureBasicUtilities.Mode.DIGEST) {
	    throw new IllegalStateException(errorMsg("wrongMode", mode));
	}
	map.put(username, new Entry(password, null));
    }

    /**
     * Add a user name, the user's password and the user's roles for
     * this authenticator's HTTP realm.
     * @param username the user name
     * @param password the user's password
     * @param roles the user's roles
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
     * @throws IllegalStateException if the mode is not appropriate or
     *         if the user has already been added
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
	if (mode != SecureBasicUtilities.Mode.DIGEST) {
	    throw new IllegalStateException(errorMsg("wrongMode", mode));
	}
	if (utable != null) {
	    utable.addEntry(username, new Entry(password, roles));
	} else {
	    map.put(username, new Entry(password, roles));
	}
    }

    /**
     * Add a user name, the user's password, the user's public key,
     * and the user's signature algorithm for this authenticator's HTTP realm.
     * @param username the user name
     * @param pem A PEM file providing the signature algorithm and
     *            the user's certificate or public key
     * @param password the user's password
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
     * @throws IllegalStateException if the mode is not appropriate or
     *         if the user has already been added
     */
    public void add(String username, String pem, String password)
	throws UnsupportedOperationException
    {
	add(username, pem, password, null);
    }

    /**
     * Add a user name, the user's password, the user's public key,
     * the user's signature algorithm and the user's roles for
     * this authenticator's HTTP realm.
     * @param username the user name
     * @param pem A PEM file providing the signature algorithm and
     *            the user's certificate or public key
     * @param password the user's password
     * @param roles the user's roles
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
     * @throws IllegalStateException if the mode is not appropriate or
     *         if the user has already been added
     */
    public void add(String username, String pem, String password,
		    Set<String> roles)
	throws UnsupportedOperationException
    {
	if (map.containsKey(username)) {
	    Entry entry = map.get(username);
	    if (pem.equals(entry.getPEM())
		&& password.equals(entry.getPassword())) {
		return;
	    }
	    throw new IllegalStateException(errorMsg("hasUser", username));
	}
	if (mode == SecureBasicUtilities.Mode.DIGEST) {
	    throw new IllegalStateException(errorMsg("wrongMode", mode));
	}
	if (utable != null) {
	    utable.addEntry(username, new Entry(pem, password, roles));
	} else {
	    map.put(username, new Entry(pem, password, roles));
	}
    }

    EjwsUserTable<EjwsSecureBasicAuth.Entry> utable = null;


    public void setUserTable(EjwsUserTable<EjwsSecureBasicAuth.Entry> utable) {
	this.utable = utable;
    }

    /**
     * {@inheritDoc}
     */
    public void add(EjwsAuthenticator.UserInfo info)
	throws IllegalStateException
    {

	String username = info.getUserName();
	// System.out.println("add called for " + username);
	/*
	try {
	    throw new RuntimeException("test exception");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	*/
	if (map.containsKey(username)) {
	    // Due to the authenticator we may try twice.
	    // Check that nothing has changed.
	    Entry oldEntry = map.get(username);
	    if (info.getPublicKey().equals(oldEntry.pem)
		&& info.getPassword().equals(oldEntry.getPassword())) {
		return;
	    }
	    throw new IllegalStateException(errorMsg("hasUser", username));
	}
	Entry entry = new Entry(info.getPublicKey(),
				info.getPassword(),
				info.getRoles());
	entry.setActive(info.isActive());
	entry.setSBLCompressed(info.isSBLCompressed());
	entry.setSBL(info.getSBL());
	if (utable != null) {
	    utable.addEntry(username, entry);
	} else {
	    map.put(username, entry);
	}
    }

    private String loginPath = null;
    private boolean loginPathUsed = true;
    private boolean loginRequired = false;
    // private BiConsumer<EjwsPrincipal,HttpExchange> loginFunction = null;
    private ThreadLocal<Boolean> foundLoginTL = new ThreadLocal<>();
    private ThreadLocal<Integer> flCode = new ThreadLocal<>();

    private String logoutPath = null;
    private boolean logoutPathUsed = true;
    // private BiConsumer<EjwsPrincipal,HttpExchange> logoutFunction = null;
    private ThreadLocal<Boolean> foundLogoutTL = new ThreadLocal<>();
    private ThreadLocal<String> usernameTL = new ThreadLocal<>();

    // private BiConsumer<EjwsPrincipal,HttpExchange> authFunction = null;

    /*
     * Set the login function.
     * This function will be called using the current HttpExchange
     * when a login is (a) successful and (b) the function is not null.
     * It can be used to set headers or perform other operations as
     * required by an application.
     * <P>
     * The function will be called when the request URI matches a
     * designated login URI, with the current {@link EjwsPrincipal} and
     * {@link HttpExchange} as its arguments.
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

    ThreadLocal<Integer> utdl = new ThreadLocal<>();
    ThreadLocal<InetAddress> addr = new ThreadLocal<>();
    FileHandler fileHandler = null;

    String savedAlias = null;
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
	    } catch (IOException e) {}
	}
	if (defaultCertChain != null) {
	    certchain.set(defaultCertChain);
	} else if (t instanceof HttpsExchange) {
	    HttpsExchange st = (HttpsExchange) t;
	    SSLSession session = st.getSSLSession();
	    certchain.set(session.getLocalCertificates());
	} else {
	    certchain.set(null);
	}
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
	// System.out.println("loginPathUsed = " + loginPathUsed);
	boolean foundLogin = false;
	if (loginPathUsed) {
	    URI requestURI = t.getRequestURI();
	    // System.out.println("requestURI = " + requestURI.toString());
	    // System.out.println("... path = " + requestURI.getPath());
	    // System.out.println("... loginPath = " + loginPath);
	    if (requestURI.getPath().equals(loginPath)) {
		String query = requestURI.getRawQuery();
		// System.out.println("query = " + query);
		// System.out.println("map.size() = " + map.size());
		if (query != null /* && map.size() > 0*/) {
		    Map<String,String> fmap = WebDecoder.formDecode(query,
								    false);
		    String username =fmap.get("user");
		    String type = fmap.get("uploadtype");
		    if (!getCanAddAccount() && type != null
			&& !type.equals("login")) {
			String msg;
			if (type =="pgpkey" || type == "sbl") {
			    msg = errorMsg("noAccountCreation");
			} else {
			    String uriS = requestURI.toString();
			    msg = errorMsg("badAccountCreation", uriS);
			}
			byte[] data = msg.getBytes(UTF8);
			try {
			    t.sendResponseHeaders(403, data.length);
			    t.getResponseBody().write(data);
			} catch (IOException eio){}
			return new Authenticator.Success(null);
		    }
		    /*
		    System.out.println("user = " + username
				       +", type = " + type);
		    */
		    if (username != null) {
			if (type != null && type.equals("login")) type = null;
			if (type != null && map.containsKey(username)) {
			    // trying to add an existing user.
			    String msg = errorMsg("accountExists", username);
			    byte[] data = msg.getBytes(UTF8);
			    try {
				t.sendResponseHeaders(403, data.length);
				t.getResponseBody().write(data);
			    } catch (IOException eio){}
			    return new Authenticator.Success(null);
			}
			byte[] array = (type == null)? getSBL(username):
			    (type.equals("pgpkey") || type.equals("sbl"))?
			    requestFromUser(username,type):
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
			    if (isGZIP) {
				rheaders.set("Content-Encoding", "gzip");
			    }
			    try {
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
			} else {
			    try {
				String string = requestURI.toString();
				String msg;
				if (type == null || type.equals("login")) {
				    msg =
					errorMsg("noUserAccount", username);
				} else {
				    msg =
					errorMsg("badAccountCreation", string);
				}
				byte[] data = msg.getBytes(UTF8);
				Headers rhdrs = t.getResponseHeaders();
				rhdrs.set("Content-type",
					  "text/html; charset=utf-8");
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
				EjwsAuthenticator.GPGKeyIDs keyids
				    = storeGPGKey(gpghome(), value);
				if (getCanAddAccount()) {
				    String email = keyids.getEmailAddress();
				    String uriString = generateRequestURI(null);
				    String recipients[] = {email};
				    UserInfo ui = createUser(email,
							     uriString,
							     recipients,
							     null)
					.setURI(alias)
					.addUser(true);
				    uriString = generateRequestURI(email);
				    Headers rhdrs = t.getResponseHeaders();
				    rhdrs.set("Location", uriString);
				    boolean active = isActiveDefault();
				    String msg;
				    if (active) {
					msg=errorMsg("pleaseVisit", uriString);
				    } else {
					msg=errorMsg("processingAC", uriString);
				    }
				    byte[] data = msg.getBytes(UTF8);
				    rhdrs.set("Content-type",
					      "text/html; charset=utf-8");
				    int rc = active? 201: 202;
				    t.sendResponseHeaders(rc, data.length);
				    t.getResponseBody().write(data);
				    return new Authenticator.Success(null);
				    // return null;
				} else {
				    
				    t.sendResponseHeaders(404, -1);
				    // return null;
				    return new Authenticator.Success(null);
				}
			    } catch (Exception e) {
				handleError(t,  e);
			    }
			} else if (contentType.equals(SBLDATA)) {
			    // System.out.println("saw contentType " + SBLDATA);
			    try {
				String s = storeSBLData(is);
				if (getCanAddAccount()) {
				    createUser(s, null).addUser();
				    String uriString = generateRequestURI(null);
				    boolean active = isActiveDefault();
				    String msg;
				    if (active) {
					msg=errorMsg("pleaseVisit", uriString);
				    } else {
					msg=errorMsg("processingAC", uriString);
				    }
				    byte[] bytes = msg.getBytes(UTF8);
				    t.getResponseHeaders()
					.set("content-type",
					     "text/html; charset=utf-8");
				    int rc = active? 201: 202;
				    t.sendResponseHeaders(rc, bytes.length);
				    OutputStream os = t.getResponseBody();
				    os.write(bytes);
				} else {
				    t.sendResponseHeaders(404, -1);
				}
			    } catch (Exception e) {
				handleError(t, e);
			    }
			} else {
			    t.sendResponseHeaders(415, -1);
			}
		    } catch (Exception eio2) {
			try {
			    eio2.printStackTrace();
			    t.sendResponseHeaders(205, -1);
			} catch(IOException eio) {}
		    }
		    // return null;
		    return new Authenticator.Success(null);
		}
		foundLogin = true;
	    }
	}
	// System.out.println("... past special processing");
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

	if (tracer != null) {
	    String s = t.getRequestHeaders().getFirst("Authorization");
	    if (s == null || s.trim().length() == 0) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") ... no Authorization header\n");
		} catch (IOException e){}
	    }
	}
	/*
	System.out.println("hdr = "
			   + t.getRequestHeaders().getFirst("Authorization"));
	*/

	InetAddress iaddr = t.getRemoteAddress().getAddress();
	addr.set(iaddr);
	Authenticator.Result result = super.authenticate(t);
	// System.out.println("result.class = " + result.getClass());
	if (result instanceof Authenticator.Success) {
	    // System.out.println("success");
	    HttpPrincipal p = ((Authenticator.Success)result).getPrincipal();
	    String un = p.getUsername();
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") authenticated user \""
				  + un + "\" for realm " + p.getRealm() + "\n");
		} catch (IOException e) {}
	    }
	    String realm = SecureBasicUtilities.decodeRealm(p.getRealm());
	    p = new EjwsPrincipal(un, realm, map.get(un).roles);
	    if (foundLogin && loginFunction != null) {
		loginFunction.accept((EjwsPrincipal)p, t);
	    } else if (foundLogout) {
		if (logoutFunction != null) {
		    logoutFunction.accept((EjwsPrincipal)p, t);
		}
		PWInfoKey key = new PWInfoKey(un, iaddr);
		pwmap.remove(key);
	    } else if (!foundLogin && !foundLogout && authFunction != null) {
		authFunction.accept((EjwsPrincipal)p, t);
	    }
	    return new Authenticator.Success(p);
	} else {
	    /*
	    int code = -1;
	    if (result instanceof Authenticator.Retry) {
		code = ((Authenticator.Retry)result).getResponseCode();
	    } else if (result instanceof Authenticator.Failure) {
		code = ((Authenticator.Failure)result).getResponseCode();
	    }
	    System.out.println("authentication failed, code = " + code);
	    */
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") authentication failed\n");
		} catch (IOException e) {}
	    }
	    if (loginRequired) {
		int code = flCode.get();
		if (code != 0) {
		    if (code == 307) {
			String proto = t.getProtocol().toUpperCase();
			code = ((proto.startsWith("HTTP") &&
				 proto.endsWith("/1.0")))?
			    302: 307;
			t.getResponseHeaders().set("Location",
						   fileHandler.getLogoutURI()
						   .toASCIIString());
			String un = usernameTL.get();
			EjwsPrincipal p = new EjwsPrincipal(un,
							    unencodedRealm,
							    map.get(un).roles);
			if (logoutFunction != null) {
			    logoutFunction.accept(p, null);
			}
		    }
		    return new Authenticator.Failure(code);
		}
	    }
	    return result;
	}
    }

    private static class PWInfoKey
    {
	String username;
	InetAddress address; // user's IP address
	public PWInfoKey(String username, InetAddress address) {
	    this.username = username;
	    this.address = address;
	}
	@Override
	public boolean equals(Object o) {
	    if (o instanceof PWInfoKey) {
		PWInfoKey obj = (PWInfoKey)o;
		return username.equals(obj.username)
		    && address.equals(obj.address);
	    }
	    return false;
	}

	@Override
	public int hashCode() {
	    int hashcode = 1;
	    hashcode = 127*hashcode +
		((username == null)? 0: username.hashCode());
	    hashcode = 127*hashcode
		+ ((address == null)? 0: address.hashCode());
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

    private Map<PWInfoKey,PWInfo> pwmap = new ConcurrentHashMap<>();

    private long TOFFSET = 30*60;

    /**
     * Remove cached passwords whose timeout has expired.
     * The method can be called periodically to eliminate passwords
     * when a user has not explicitly logged out and has not sent
     * any HTTP requests for some time.
     */
    public void prune() {
	long now = Instant.now().getEpochSecond();
	Iterator<Map.Entry<PWInfoKey,PWInfo>> it = pwmap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<PWInfoKey,PWInfo> info = it.next();
	    if (info.getValue().expires < now - TOFFSET) {
		String un = info.getKey().username;
		it.remove();
		EjwsPrincipal p = new EjwsPrincipal(un, unencodedRealm,
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
	Certificate[] certificates = certchain.get();

	// use a cache so we don't have to check a digital signature or
	// message digest.
	long now = Instant.now().getEpochSecond();
	// System.out.println("checking " + username + ", " + password);
	InetAddress iaddr = addr.get();
	PWInfoKey key = new PWInfoKey(username, iaddr);
	/*
	System.out.println("username = " + username + ", iaddr = "
			   + iaddr.toString());
	*/
	PWInfo pwinfo = pwmap.get(key);
	if (pwinfo != null) {
	    /*
	    System.out.println("pwinfo.expires - now = "
			       +(pwinfo.expires - now));
	    */
	    if (passphraseTimeout == 0 || (pwinfo.expires - now) >= 0) {
		if (pwinfo.password.equals(password)) {
		    pwinfo.expires = ((passphraseTimeout == 0)?
				      TOFFSET: passphraseTimeout) + now;
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
		    if (tracer != null) {
			String ct = "" + Thread.currentThread().getId();
			try {
			    tracer.append("(" + ct + ") ... authentication "
					  + "extended timeout expired\n");
			} catch (IOException e){}
		    }
		    if (loginRequired) {
			// the login timed out, so send the user
			// to the logout page.
			flCode.set(307);
			usernameTL.set(username);
		    }
		    return false;
		}
	    }
	} else if (loginRequired) {
	    boolean foundLogin = foundLoginTL.get();
	    boolean foundLogout = foundLogoutTL.get();
	    if (!foundLogin && !foundLogout) {
		flCode.set(403);
		return false;
	    }
	}
	Entry entry = map.get(username);
	// System.out.println("entry = " + entry);
	if (entry == null) return false;
	if (!entry.isActive()) {
	    flCode.set(403);
	    return false;
	}
	SecureBasicUtilities ops = entry.keyops;
	// System.out.println("ops = " + ops);
	byte[] sigarray = SecureBasicUtilities.decodePassword(password);
	if (sigarray == null) return false;
	int timediff = SecureBasicUtilities.getTimeDiff(sigarray);
	if (timediff < lowerTimeDiffLimit || timediff > upperTimeDiffLimit) {
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") ... authentication "
				  + "timeout expired\n");
		} catch (IOException e){}
	    }
	    return false;
	}
	try {
	    if (ops.checkPassword(sigarray, certificates,
				  entry.getPassword())) {
		pwmap.put(key,
			  new PWInfo(now + ((passphraseTimeout == 0)?
					    TOFFSET: passphraseTimeout),
				     password));
		if (tracer != null) {
		    String ct = "" + Thread.currentThread().getId();
		    try {
			String msgtail = (certificates == null)?
			    "OK (no certificates to check)":
			    "OK (found a matching certificate)";

			tracer.append("(" + ct + ") ... authentication "
				      + msgtail + "\n");
		    } catch (IOException eio){}
		}
		return true;
	    }
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    String msgtail = " FAILED (full password check";
		    tracer.append("(" + ct + ") ... authentication "
				  + msgtail + "\n");
		} catch (IOException eio){}
	    }

	} catch (GeneralSecurityException e) {
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		try {
		    tracer.append("(" + ct + ") ... SECURITY EXCEPTION\n");
		} catch (IOException eio){}
	    }
	    return false;
	} catch (Exception e) {
	    if (tracer != null) {
		try {
		    String ct = "" + Thread.currentThread().getId();
		    tracer.append("(" + ct + ") ... EXCEPTION\n");
		} catch (IOException eio){}
	    }
	}
	return false;
    }
}

//  LocalWords:  BasicAuthenticator username exbundle endian CRC UTF
//  LocalWords:  OL SHA DER SSL TLS openssl keytool createPEMPair PRE
//  LocalWords:  SecureBasicUtilities PKCS BLOCKQUOTE genkey keyalg
//  LocalWords:  groupname secp sigalg withECDSA dname CN keypass pfx
//  LocalWords:  storepass keystore ecstore ecparam noout eckey pem
//  LocalWords:  req eccert pkcs inkey Appendable lowerTimeDiffLimit
//  LocalWords:  upperTimeDiffLimit passphraseTimeout positiveLowerTDL
//  LocalWords:  IllegalArgumentException negativeUpperTDL hdr addr
//  LocalWords:  authenticator's Authenticator HttpExchange getClass
//  LocalWords:  instanceof InetAddress iaddr pwinfo pw authenticator
//  LocalWords:  UnsupportedOperationException wrongMode URI SBL ews
//  LocalWords:  EjwsPrincipal FileHandler setLoginAlias toString JKS
//  LocalWords:  setLogoutAlias setTracer superclasses sblCompressed
//  LocalWords:  sbldata EmbeddedWebServer encodeRealm proposedMode
//  LocalWords:  unencodedRealm getMode another's certlength HTTPS
//  LocalWords:  ksis KeyStoreException IOException KeyStore changeit
//  LocalWords:  CertificateException occcur BiConsumer loginFunction
//  LocalWords:  logoutFunction authFunction setLoginFunction
//  LocalWords:  setAuthorizedFunction setLogoutFunction
