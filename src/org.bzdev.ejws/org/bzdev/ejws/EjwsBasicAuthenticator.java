package org.bzdev.ejws;

import java.net.InetAddress;
import java.time.Instant;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.util.SafeFormatter;


//@exbundle org.bzdev.ejws.lpack.SecureBasicAuth


/**
 * Implementation of BasicAuthenticator using either an in-memory table
 * of user names and passwords or a user-supplied table.
 */
public class EjwsBasicAuthenticator extends EjwsAuthenticator {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.SecureBasicAuth");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    /**
     * User entry.
     * This object contains a user's password and roles.
     */
    public static class Entry extends EjwsAuthenticator.Entry {
	// roles defined by superclass
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

	/**
	 * Get the password stored in this entry.
	 * @return the password
	 */
	public String getPassword() {return pw;}

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

    ThreadLocal<InetAddress> addr = new ThreadLocal<>();
    // used when loginRequired is true
    private ThreadLocal<Boolean> foundLoginTL = new ThreadLocal<>();
    private ThreadLocal<Integer> flCode = new ThreadLocal<>();
    private ThreadLocal<Boolean> foundLogoutTL = new ThreadLocal<>();
    private ThreadLocal<String> usernameTL = new ThreadLocal<>();


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

    // How long to use a verified password
    private int passphraseTimeout = 1200;
    /**
     * Set the time limit for a passphrase/password.
     * <P>
     * @param passphraseTimeout the time interval in seconds for which a
     *        password is valid (the default is 1200); 0 to disable the
     *        timeout
     * @throws IllegalArgumentException if the argument is less than
     *         zero.
     */
    public void setTimeLimit(int passphraseTimeout)
	throws IllegalArgumentException
    {
	if (passphraseTimeout < 0) {
	    String msg =
		errorMsg("negativePassphraseTimeout", passphraseTimeout);
	    throw new IllegalArgumentException(msg);
	}
	this.passphraseTimeout = passphraseTimeout;
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
	this.map = map;
    }

    /**
     * Add a user name and password for this authenticator's HTTP realm.
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
     * @throws IllegalStateException if the map already contains the user
     */
    public void add(String username, String password, Set<String> roles )
	throws UnsupportedOperationException, IllegalStateException
    {
	if (map.containsKey(username)) {
	    throw new IllegalStateException(errorMsg("hasUser", username));
	}
	map.put(username, new Entry(password, roles));
    }

    /**
     * {@inheritDoc}
     */
    protected void add (EjwsAuthenticator.UserInfo info)
	throws IllegalStateException
    {
	String userName = info.getUserName();
	if (map.containsKey(userName)) {
	    throw new IllegalStateException(errorMsg("hasUser", userName));
	}
	Entry entry = new Entry(info.getPassword(), info.getRoles());
	entry.setActive(info.isActive());
	entry.setSBLCompressed(info.isSBLCompressed());
	entry.setSBL(info.getSBL());
	map.put(userName, entry);
    }


    FileHandler fileHandler = null;

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

	/*
	Headers headers = t.getRequestHeaders();
	String value = headers.getFirst("Authorization");
	if (value != null) {
	    value = value.trim();
	    if (value.substring(0,6).equalsIgnoreCase("basic ")) {
		value = value.substring(6).trim();
		try {
		    value = new
			String(Base64.getDecoder().decode(value), "UTF-8");
		} catch (Exception e) {
		    System.out.println("decoding failed");
		}
		System.out.println(value);
	    } else {
		System.out.println(value);
	    }
	} else {
	    System.out.println("--- all headers ----");
	    for (String hdr: headers.keySet()) {
		System.out.println(hdr);
	    }
	    System.out.println("--------------------");
	}
	*/
	InetAddress iaddr = t.getRemoteAddress().getAddress();
	addr.set(iaddr);
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
		PWInfoKey key = new PWInfoKey(un, iaddr);
		pwmap.remove(key);
	    } else if (!foundLogin && !foundLogout && authFunction != null) {
		authFunction.accept((EjwsPrincipal)p, t);
	    }
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
							    realm,
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
     * Get the mode.
     * @return SecureBasicUtilities.Mode.PASSWORD
     */
    public SecureBasicUtilities.Mode getMode() {
	return SecureBasicUtilities.Mode.PASSWORD;
    }

    /**
     * {@inheritDoc}
     */
    protected void proposeMode(EmbeddedWebServer server)
	throws IllegalStateException
    {}

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
	PWInfoKey key = new PWInfoKey(username, iaddr);
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
	boolean result = password.equals(entry.pw);
	if (result == true && passphraseTimeout > 0) {
	    pwmap.put(key, new PWInfo(now + passphraseTimeout,
				      password));
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
