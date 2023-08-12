package org.bzdev.ejws;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Implementation of BasicAuthenticator using either an in-memory table
 * of user names and passwords or a user-supplied table.
 */
public class EjwsBasicAuthenticator extends BasicAuthenticator {
    /**
     * User entry.
     * This object contains a user's password and roles.
     */
    public static class Entry {
	String pw;
	Set<String> roles;

	/**
	 * Constructor.
	 * @param pw a password for a user
	 * @param roles a set of roles that a user may have
	 */
	public Entry(String pw, Set<String>roles) {
	    this.pw = pw; this.roles = roles;
	}

	/**
	 * Get the password stored in this entry.
	 * @return the password
	 */
	public String getPassword() {return pw;}

	/**
	 * Get the roles stored in this entry
	 * @return a set of roles; null if not applicable
	 */
	public Set<String> getRoles(){return roles;}

    }

    private Appendable tracer = null;

    /**
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * @param tracer the Appendable for tracing requests and responses
     */
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }

    private String loginPath = null;
    private boolean loginPathUsed = true;
    BiConsumer<EjwsPrincipal,HttpExchange> loginFunction = null;



    private String logoutPath = null;
    private boolean logoutPathUsed = true;
    private BiConsumer<EjwsPrincipal,HttpExchange> logoutFunction = null;

    private BiConsumer<EjwsPrincipal,HttpExchange> authFunction = null;


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

    Map<String,Entry>map = null;

    /**
     * Constructor.
     * @param realm the HTTP realm
     */
    public EjwsBasicAuthenticator(String realm) {
	this(realm, new ConcurrentHashMap<String,Entry>());
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
    public EjwsBasicAuthenticator(String realm, Map<String,Entry> map)
    {
	super(realm);
	this.map = map;
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
	map.put(username, new Entry(password, roles));
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
	    } else if (foundLogout && logoutFunction != null) {
		logoutFunction.accept((EjwsPrincipal)p, t);
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
	    return result;
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
	return password.equals(entry.pw);
    }
}

//  LocalWords:  BasicAuthenticator username Appendable getFirst UTF
//  LocalWords:  authenticator's getRequestHeaders hdr keySet pw URI
//  LocalWords:  HttpExchange EjwsPrincipal FileHandler setLoginAlias
//  LocalWords:  setLogoutAlias authenticator
//  LocalWords:  UnsupportedOperationException
