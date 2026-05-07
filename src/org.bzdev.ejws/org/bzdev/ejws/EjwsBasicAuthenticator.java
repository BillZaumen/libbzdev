package org.bzdev.ejws;

import java.net.InetAddress;
import java.time.Instant;
import com.sun.net.httpserver.*;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.bzdev.net.WebDecoder;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.ConfigPropUtilities;
import org.bzdev.util.ConfigProperties;


//@exbundle org.bzdev.ejws.lpack.SecureBasicAuth


/**
 * Implementation of BasicAuthenticator using either an in-memory table
 * of user names and passwords or a user-supplied table.
 */
public class EjwsBasicAuthenticator extends EjwsAuthenticator {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.SecureBasicAuth");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static final String SBLDATA = "application/vnd.bzdev.sblogindata";


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
	if (utable != null) {
	    utable.putEntry(username, new Entry(password, null));
	} else {
	    map.put(username, new Entry(password, null));
	}
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
	if (utable != null) {
	    utable.putEntry(username, new Entry(password, roles));
	} else {
	    map.put(username, new Entry(password, roles));
	}
    }

    EjwsUserTable<EjwsBasicAuthenticator,EjwsBasicAuthenticator.Entry>
	utable = null;


    public void
	setUserTable(EjwsUserTable
		     <EjwsBasicAuthenticator,EjwsBasicAuthenticator.Entry>
		     utable)
    {
	this.utable = utable;
	utable.setMap(map);
	utable.setAuth(this);
    }

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
	    throw new IllegalStateException(errorMsg("hasUser", userName));
	}
	Entry entry = new Entry(info.getPassword(), info.getRoles());
	// entry.setActive(info.isActive());
	if (info.isActive()) entry.makeActive();
	// entry.setSBLCompressed(info.isSBLCompressed());
	// entry.setSBL(info.getSBL());
	if (utable != null) {
	    utable.putEntry(userName, entry);
	} else {
	    map.put(userName, entry);
	}
    }

    public boolean removeUser(String name, boolean gpg) {
	if (gpg) {
	    return false;
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
	}
	boolean status = (map.remove(name) != null);
	return status;
    }

    public boolean removeUser(String name) {
	try {
	    if (utable != null) {
		return utable.removeEntry(name);
	    } else {
		return removeUser(name, false);
	    }
	} catch (Exception e) {
	    return false;
	}
    }

    public boolean makeUserActive(String name, boolean gpg) {
	EjwsBasicAuthenticator.Entry entry = map.get(name);
	if (entry == null) {
	    return false;
	}
	if (gpg) {
	    return false;
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
		return false;
	    }
	} else {
	    return false;
	}
	entry.makeActive();
	return true;
    }

    public boolean makeUserActive(String name) {
	if (utable != null) {
	    return utable.makeActive(name);
	} else {
	    return makeUserActive(name, false);
	}
    }

    public void loadFromDirs() throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


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
	if (loginPathUsed) {
	    URI requestURI = t.getRequestURI();
	    if (t.getRequestURI().getPath().equals(loginPath)) {
		String query = requestURI.getRawQuery();
		if (query != null) {
		    Map<String,String> fmap = WebDecoder.formDecode(query,
								    false);
		    String username =fmap.get("user");
		    String type = fmap.get("uploadtype");
		    if (!getCanAddAccount() && type != null
			&& !type.equals("login")) {
			String msg;
			if (type == "sbl") {
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
			if (contentType.equals(SBLDATA)) {
			    // System.out.println("saw contentType " + SBLDATA);
			    try {
				if (getCanAddAccount()) {
				    String s = readSBLData(is);
				    String uname = getUserNameFromSBL(s);
				    AddStatus status = getUserStatus(uname);
				    if (status != AddStatus.REJECTED) {
					storeSBLData(s, status);
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
				    t.getResponseHeaders()
					.set("content-type",
					     "text/html; charset=utf-8");
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
