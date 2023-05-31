package org.bzdev.ejws;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.util.SafeFormatter;

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
public class EjwsSecureBasicAuth extends BasicAuthenticator {

    private Appendable tracer = null;

    /**
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * @param tracer the Appendable for tracing requests and responses
     */
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.SecureBasicAuth");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    /**
     * User entry.
     * This object contains a user's password and roles, and
     * (if applicable) the user's public key in PEM format.
     * The PEM file must also contain a header specifying a
     * digital-ignature algorithm.
     * @see org.bzdev.net.SecureBasicUtilities
     */
   public static class Entry {
	String pw;
	Set<String> roles;
	SecureBasicUtilities keyops;
	
	/**
	 * Constructor.
	 * @param pw a password for a user
	 * @param roles a set of roles that a user may have
	 */
	Entry(String pw, Set<String>roles) {
	    this.pw = pw; this.roles = roles;
	    this.keyops = new SecureBasicUtilities();
	}

	/**
	 * Constructor with a PEM-encoded public-key.
	 * @param pem the PEM encoded public key or a certificate
	 *        containing that public key.
	 * @param pw a password for a user
	 * @param roles a set of roles that a user may have
	 */
	Entry(String pem, String pw, Set<String>roles) {
	    this.pw = pw; this.roles = roles;
	    try {
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
	 * Get the password stored in this entry.
	 * @return the password
	 */
	public String getPassword() {return pw;}

	/**
	 * Get the roles stroed in this entry
	 * @return a set of roles; null if not applicable
	 */
	public Set<String> getRoles(){return roles;}
   }

    Map<String,Entry>map = null;

    Certificate[] certificates;
    SecureBasicUtilities.Mode mode;

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
    private int passwordTimeout = 1200;

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
     * @param passwordTimeout the time interval in seconds for which a
     *        password is valid (the default is 1200)
     * @throws IllegalArgumentException if the first argument is larger
     *         than zero, if the second argument is less than zero,
     *         or if the third argument is less than the second argument
     */
    public void setTimeLimits(int lowerTimeDiffLimit,
			      int upperTimeDiffLimit,
			      int passwordTimeout)
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
	if (passwordTimeout < upperTimeDiffLimit) {
	    int uTDL = upperTimeDiffLimit;
	    String msg =
		errorMsg("passwordTimeout", passwordTimeout, uTDL);
	    throw new IllegalArgumentException(msg);
	}
	this.lowerTimeDiffLimit = lowerTimeDiffLimit;
	this.upperTimeDiffLimit = upperTimeDiffLimit;
	this.passwordTimeout = passwordTimeout;
    }

    /**
     * Constructor for {@link SecureBasicUtilities.Mode#DIGEST} mode.
     * @param realm the HTTP realm
     */
    public EjwsSecureBasicAuth(String realm) {
	this(realm, new ConcurrentHashMap<String,Entry>());
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
    public EjwsSecureBasicAuth(String realm, Map<String,Entry> map) {
	super(SecureBasicUtilities
	      .encodeRealm(realm, SecureBasicUtilities.Mode.DIGEST));
	unencodedRealm = realm;
	mode = SecureBasicUtilities.Mode.DIGEST;
	this.map = map;
    }

    private static SecureBasicUtilities.Mode getMode(Certificate cert) {
	if (cert == null)
	    return SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT;
	return SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT;
    }

    private static SecureBasicUtilities.Mode getMode(Certificate[] certs) {
	if (certs == null)
	    return SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT;
	int cnt = 0;
	for (int i = 0; i < certs.length; i++) {
	    if (certs[i] != null) cnt++;
	}
	return getMode(cnt);
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
     * @param certs the server certificates.
     * @param realm the HTTP realm
     */
    public EjwsSecureBasicAuth(String realm, Certificate[] certs) {
	this(realm, certs, new ConcurrentHashMap<String,Entry>());
    }

    /**
     * Constructor for {@link SecureBasicUtilities.Mode#SIGNATURE_WITHOUT_CERT}
     * mode and
     * {@link SecureBasicUtilities.Mode#SIGNATURE_WITH_CERT} mode given
     * multiple certificates, specifying a map.
     * <P>
     * A user-supplied map can be implemented so as to allow one to obtain
     * passwords and roles from a database or some other form of persistent
     * storage. If entries can be added while a server using this
     * authenticator is running, the map should have a thread-safe
     * implementation.
     * @param certs the server certificates.
     * @param realm the HTTP realm
     * @param map a map associating user names with entries containing
     *        a password, roles, and optionally a public key and related
     *        data
     */
    public EjwsSecureBasicAuth(String realm,
			       Certificate[] certs,
			       Map<String,Entry> map)
    {
	super(SecureBasicUtilities.encodeRealm(realm, getMode(certs)));
	unencodedRealm = realm;
	this.map = map;
	if (certs != null) {
	    int cnt = 0;
	    for (int i = 0; i < certs.length; i++) {
		if (certs[i] != null) {
		    cnt++;
		}
	    }
	    if (cnt != 0) {
		int j = 0;
		certificates = new Certificate[cnt];
		for (int i = 0; i < certs.length; i++) {
		    Certificate cert = certs[i];
		    if (cert != null) {
			certificates[j++] = cert;
		    }
		}
	    }
	    mode = getMode(cnt);
	} else {
	    mode = getMode(0);
	}
    }


    /**
     * Add a user name and password for this authenticator's HTTP realm.
     * @param username the user name
     * @param password the password
     * @throws UnsupportedOperationException if the map does not allow
     *         entries to be added (the default map does not throw this
     *         exception)
    */
    public void add(String username, String password)
	throws UnsupportedOperationException
    {
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
     */
    public void add(String username, String password, Set<String> roles )
	throws UnsupportedOperationException
    {
	if (mode != SecureBasicUtilities.Mode.DIGEST) {
	    throw new IllegalStateException(errorMsg("wrongMode", mode));
	}
	map.put(username, new Entry(password, roles));
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
     */
    public void add(String username, String pem, String password,
		    Set<String> roles )
	throws UnsupportedOperationException
    {
	if (mode == SecureBasicUtilities.Mode.DIGEST) {
	    throw new IllegalStateException(errorMsg("wrongMode", mode));
	}
	map.put(username, new Entry(pem, password, roles));
    }

    private String loginPath = null;
    private boolean loginPathUsed = true;
    private boolean loginRequired = false;
    private BiConsumer<EjwsPrincipal,HttpExchange> loginFunction = null;
    private ThreadLocal<Boolean> foundLoginTL = new ThreadLocal<>();
    private ThreadLocal<Integer> flCode = new ThreadLocal<>();

    private String logoutPath = null;
    private boolean logoutPathUsed = true;
    private BiConsumer<EjwsPrincipal,HttpExchange> logoutFunction = null;
    private ThreadLocal<Boolean> foundLogoutTL = new ThreadLocal<>();
    private ThreadLocal<String> usernameTL = new ThreadLocal<>();

    private BiConsumer authFunction = null;

    /**
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
     */
    public void setLoginFunction
	(BiConsumer<EjwsPrincipal,HttpExchange> function)
    {
	loginFunction = function;
    }

    /**
     * Set the authorized function.
     * This function will be called when a request is authorized.
     * Its arguments are a principal and the Http exchange. THe
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


    ThreadLocal<Integer> utdl = new ThreadLocal<>();
    ThreadLocal<InetAddress> addr = new ThreadLocal<>();
    FileHandler fileHandler = null;

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
	if (loginPathUsed && loginPath == null) {
	    HttpHandler handler = t.getHttpContext().getHandler();
	    if (handler instanceof FileHandler) {
		fileHandler = (FileHandler) handler;
		String alias = fileHandler.getLoginAlias();
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
	if (loginPathUsed
	    && t.getRequestURI().getPath().equals(loginPath)) {
	    foundLogin = true;
	}
	if (loginRequired) {
	    foundLoginTL.set(foundLogin);
	    flCode.set(0);
	}

	if (logoutPathUsed && logoutPath == null) {
	    HttpHandler handler = t.getHttpContext().getHandler();
	    if (handler instanceof FileHandler) {
		fileHandler = (FileHandler) handler;
		String alias = fileHandler.getLogoutAlias();
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

    private long TOFFSET = 30;

    /**
     *
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
	// use a cache so we don't have to check a digital signature or
	// message digest.
	long now = Instant.now().getEpochSecond();
	// System.out.println("checking " + username + ", " + password);
	username = SecureBasicUtilities.decodeUserName(username, password);
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
	    if ((pwinfo.expires - now) >= 0) {
		if (pwinfo.password.equals(password)) {
		    pwinfo.expires = passwordTimeout + now;
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
			// to the login page.
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
	if (entry == null) return false;
	SecureBasicUtilities ops = entry.keyops;
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
	    if (certificates == null) {
		if (ops.checkPassword(sigarray, null, entry.pw)) {
 		    pwmap.put(key,
			      new PWInfo(now+passwordTimeout,
					 password));
		    if (tracer != null) {
			String ct = "" + Thread.currentThread().getId();
			try {
			    tracer.append("(" + ct + ") ... authentication "
					  + "OK (no certificates to check)\n");
			} catch (IOException eio){}
		    }
		    return true;
		}
	    } else {
		for (int i = 0; i < certificates.length; i++) {
		    if (ops.checkPassword(sigarray,
					  certificates[i],
					  entry.pw)) {
			pwmap.put(key,
				  new PWInfo(now+passwordTimeout,
					     password));
			if (tracer != null) {
			    String ct = "" + Thread.currentThread().getId();
			    try {
				tracer.append("(" + ct + ") ... authentication "
					      + "OK (found a matching "
					      + "certificate)\n");
			    } catch (IOException eio) {}
			}
			return true;
		    }
		}
		return false;
	    }
	} catch (GeneralSecurityException e) {
	    return false;
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
//  LocalWords:  upperTimeDiffLimit passwordTimeout positiveLowerTDL
//  LocalWords:  IllegalArgumentException negativeUpperTDL hdr addr
//  LocalWords:  authenticator's Authenticator HttpExchange getClass
//  LocalWords:  instanceof InetAddress iaddr pwinfo
