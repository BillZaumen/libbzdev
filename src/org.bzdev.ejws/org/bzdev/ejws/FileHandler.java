package org.bzdev.ejws;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.MessageFormat;
import java.security.SecureRandom;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.bzdev.io.AppendableWriter;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.ServerCookie;
import org.bzdev.net.HeaderOps;
import org.bzdev.util.ErrorMessage;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

//@exbundle org.bzdev.ejws.lpack.EmbeddedWebServer

/**
 * HttpHandler for files.
 * A FileHandler can process HTTP GET or HEAD methods, not other methods
 * (e.g., PUT or POST).  The constructors for a FileHandler will provide
 * the class name of a subclass of WebMap and an argument that will be
 * used as a single-argument constructor for an instance of a subclass of
 * WebMap.  This instance is used to obtain data necessary for generating
 * a response to an HTTP request. The constructors for FileHandler will
 * configure its WebMap based on flags provided by the constructors. It
 * will also check if the WebMap provides a Web-Inf/web.xml file, and will
 * parse that file.  This file can specify the locations of welcome pages,
 * mappings from file extensions to media types, the location of JSP-like
 * pages for error reporting, and the page encoding that various files
 * use.
 * <P>
 * The method named
 * {@link FileHandler#handle(com.sun.net.httpserver.HttpExchange)}
 * (defined by {@link com.sun.net.httpserver.HttpHandler}) handles
 * HTTP requests.  If an error occurs, and error page is obtained from
 * the FileHandler's WebMap.  If the requested URL's path matches that
 * of the prefix for this FileHandler, a welcome page is requested
 * from this FileHandler's WebMap On success, the resource provided in
 * the HTML response is obtained from the WebMap.
 * <P>
 * The status codes that may be returned in an HTTP response are:
 * <UL>
 *  <LI>100&mdash;Continue.
 *  <LI>200&mdash;OK.
 *  <LI>302&mdash;Found.
 *  <LI>404&mdash;Not Found
 *  <LI>405&mdash;Method Not Allowed
 *  <LI>406&mdash;Not Accepted
 *  <LI>417&mdash;Expectation Failed
 *  <LI>500&mdash;Internal Server Error
 * </UL>
 */

public class FileHandler implements HttpHandler {

    static String errorMsg(String key, Object... args) {
	return EmbeddedWebServer.errorMsg(key, args);
    }

    // URI rootURI;
    // String rootPath;
    // URI svrURI = new URI(null, null, "/", null, null);
    WebMap map;
    boolean nowebxml;

    // public  WebMap getMap() {return map;}

    static int indexcntr = 0;
    static class MIMEAcceptor {
	static String errorMsg(String key, Object... args) {
	    return EmbeddedWebServer.errorMsg(key, args);
	}

	class Entry implements Comparable<Entry> {
	    int index;
	    int wildcardPrecedence;
	    double q = 1.0;
	    String[] mimePattern;
	    HashMap<String,String> params = new HashMap<String,String>();

	    Entry(String[] itemElements)
		throws IllegalArgumentException
		{
		    if (itemElements == null || itemElements.length == 0)
			throw new IllegalArgumentException
			    (errorMsg("badItemElements"));
		    mimePattern = itemElements[0].trim().split("/");
		    if (mimePattern.length == 1
			&& mimePattern[0].equals("*")) {
			mimePattern = new String[2];
			mimePattern[0] = "*";
			mimePattern[1] = mimePattern[0];
		    }
		    if (mimePattern.length < 2 || mimePattern[0] == null
			|| mimePattern[1] == null) {
			throw new IllegalArgumentException
			    (errorMsg("badMIMEPattern"));
		    }
		    mimePattern[0] =
			mimePattern[0].trim().toLowerCase(Locale.ENGLISH);
		    mimePattern[1] =
			mimePattern[1].trim().toLowerCase(Locale.ENGLISH);
		    if (mimePattern[0].charAt(0) == '*'
			&& mimePattern[1].charAt(0) != '*') {
			throw new IllegalArgumentException
			    (errorMsg("badMIMEPattern"));
		    }

		    wildcardPrecedence = 0;
		    if (mimePattern[1].charAt(0) == '*') {
			wildcardPrecedence++;

		    }
		    if (mimePattern[0].charAt(0) == '*') {
			wildcardPrecedence++;
		    }
		    synchronized (Entry.class) {
			index = indexcntr++;
		    }
		    for (int i = 1; i < itemElements.length; i++) {
			if (itemElements[i] == null) continue;
			String[] array = itemElements[i].split("=");
			switch(array.length) {
			case 0:
			    break;
			case 1:
			    String key1 =
				array[0].trim().toLowerCase(Locale.ENGLISH);
			    params.put(key1, key1);
			    break;
			case 2:
			    String key2 =
				array[0].toLowerCase(Locale.ENGLISH).trim();
			    String value2 = array[1].trim();
			    if (key2.equals("charset")) {
				value2 = value2.toLowerCase(Locale.ENGLISH);
			    }

			    if (key2.equals("q")) {
				q = Double.parseDouble(array[1].trim());
			    } else {
				params.put(key2, value2);
			    }
			    break;
			default:
			    String key3 =
				array[0].toLowerCase(Locale.ENGLISH).trim();
			    String value3 = array[1];
			    for (int j = 2; j < array.length; j++) {
				value3 = value3 + "=" + array[j];
			    }
			    value3 = value3.trim();
			    if (key3.equals("q")) {
				q = Double.parseDouble(value3);
			    } else {
				params.put(key3, value3);
			    }
			    break;
			}
		    }
		}
	    public int compareTo(Entry e) {
		if(q == e.q) {
		    if (wildcardPrecedence == e.wildcardPrecedence) {
			// what "more specific" means for parameters is not
			// clear.  We assume that it refers to the number
			// of parameters listed.
			int sz1 = params.size();
			int sz2 = e.params.size();
			if (sz1 == sz2) {
			    if (index == e.index) return 0;
			    else {
				return (index < e.index)? -1: 1;
			    }
			} else {
			    return sz2 - sz1;
			}
		    } else {
			return (wildcardPrecedence - e.wildcardPrecedence);
		    }
		} else {
		    return (q > e.q)? -1: 1;
		}
	    }
	    public String toString() {
		String result = mimePattern[0] +"/" + mimePattern[1];
		if (q < 1.0) {
		    result = result +"; q=" + q;
		}
		for (Map.Entry<String,String> entry: params.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if (key == value) {
			result = result + "; " + key;
		    } else {
			result = result + "; " + key + "=" + value;
		    }
		}
		return result;
	    }
	}
	LinkedList<Entry> list = new LinkedList<Entry>();

	String[] split(String s, String delim) {
	    String[] items = s.split(delim);
	    boolean instring = false;
	    int start = 0;
	    for (int i = 0; i < items.length; i++) {
		if (items[i] == null) continue;
		if (items[i].contains("\"")) {
		    start = items[i].indexOf('"');
		    start++;
		    int j = i+1;
		    instring = true;
		    while (instring || start < items[i].length()) {
			if (start == items[i].length()) {
			    if (j < items[i].length()) {
				items[i] = items[i] + "," + items[j];
				items[j] = null;
				j++;
			    } else {
				break;
			    }
			}
			if (items[i].charAt(start) == '\\'
			    && start+1 != items[i].length()
			    && items[i].charAt(start+1) == '"') {
			    start ++;
			} else if (items[i].charAt(start) == '"') {
			    instring = false;
			}
			start++;
	    }
		}
	    }
	    return items;
	}

	MIMEAcceptor(Headers headers) throws IOException {
	    List<String> acceptStringList = headers.get("Accept");
	    PriorityQueue<Entry>queue = new PriorityQueue<Entry>();
	    for (String element: acceptStringList) {
		// String[] items = element.split(",");
		String[] items = split(element, ",");
		for (String item: items) {
		    if (item == null) continue;
		    // System.out.println("item = " + item);
		    // String[] itemElements = item.split(";");
		    String[] itemElements = split(item, ";");
		    try {
			Entry entry = new Entry(itemElements);
			queue.offer(entry);
		    } catch (Exception e) {
			String msg = errorMsg("mimeAcceptor", e.getMessage());
			throw new IOException(msg, e);
			// e.printStackTrace();
			// System.exit(1);
		    }
		}
	    }
	    Entry item;
	    while ((item = queue.poll()) != null) {
		list.add(item);
	    }
	}

	String match(String mtype) {
	    String[] array = split(mtype, ";");
	    Entry mtent;
	    try {
		mtent= new Entry(array);
	    } catch (Exception e) {
		return null;
	    }
	    for (Entry entry: list) {
		if (entry.mimePattern[0].charAt(0) == '*'
		    || entry.mimePattern[0].equals(mtent.mimePattern[0])) {
		    if (entry.mimePattern[1].charAt(0) == '*' ||
			entry.mimePattern[1].equals(mtent.mimePattern[1])) {
			boolean failed = false;
			for (Map.Entry<String,String>kv:
				 entry.params.entrySet()) {
			    String key = kv.getKey();
			    String value = kv.getValue();
			    String mtvalue = mtent.params.get(key);
			    if (mtvalue == null) {
				failed = true;
				break;
			    } else if (!mtvalue.equals(value)) {
				failed = true;
				break;
			    }
			}
			if (failed) continue;
			return mtype;
		    }
		}
	    }
	    return null;
	}

	public String toString() {
	    String result = "Accept: ";
	    for (Entry entry: list) {
		result = result + entry.toString() + ", ";
	    }
	    return result;
	}
    }

    private String loginAlias = null;
    private String loginTarget = null;
    private URL loginURL = null;
    private boolean loginRequired = false;

    /**
     * Determine if a login is required.
     * When a login is required, {@link EjwsSecureBasicAuth}
     * will reject an authorization request unless the user has first
     * been authorized when visiting the page denoted by the login alias.
     * @return true if a login is required; false otherwise
     */
    public boolean isLoginRequired() {
	return loginRequired;
    }


    /**
     * Set the login-alias string.
     * This string is the path component of a URL and may not contain
     * a "/". An HTTP request whose path is the context path, followed
     * by a "/" if the context path does not end in "/", followed by
     * the login-alias, that URL will be redirected to a location
     * specified by the context path.  In addition, that URL may be
     * treated specially by authenticators.
     * @param alias the alias string; null to remove it
     * @throws IllegalArgumentException if the first argument contains
     *        a "/"
     * @see EjwsSecureBasicAuth#setLoginFunction
     * @see EjwsSecureBasicAuth#setLogoutFunction
     * @see EjwsBasicAuthenticator#setLoginFunction
     * @see EjwsBasicAuthenticator#setLogoutFunction
     */
    public void setLoginAlias(String alias)
	throws IllegalArgumentException
    {
	if (alias == null) {
	    loginAlias = null;
	    loginTarget = null;
	    loginURL = null;
	} else {
	    setLoginAlias(alias, "");
	}
    }

    /**
     * Set the login-alias string with a location to visit when the login is
     * successful, with the location represented as a relative path.
     * The login alias is the path component of a URL and may not contain
     * a "/". An HTTP request whose path is the context path, followed
     * by a "/" if the context path does not end in "/", followed by
     * the login-alias, that URL will be redirected to a location
     * specified by the context path.  In addition, the login URL may be
     * treated specially by authenticators.
     * @param alias the alias string; null to remove it
     * @param target the relative path from this handler's context to
     *        the page that should be visited after a successful login.
     * @throws IllegalArgumentException if the first argument contains
     *        a "/" or the second argument starts with a "/"
     * @see EjwsSecureBasicAuth#setLoginFunction
     * @see EjwsSecureBasicAuth#setLogoutFunction
     * @see EjwsBasicAuthenticator#setLoginFunction
     * @see EjwsBasicAuthenticator#setLogoutFunction
     */
    public void setLoginAlias(String alias, String target)
	throws IllegalArgumentException
    {
	setLoginAlias(alias, target, false);
    }

    /**
     * Set the login-alias string with a location to visit when the
     * login is successful, with the location represented as a
     * relative path, and with a flag indicating if a login is
     * required.
     * The login alias is the path component of a URL and may not contain
     * a "/". An HTTP request whose path is the context path, followed
     * by a "/" if the context path does not end in "/", followed by
     * the login-alias, that URL will be redirected to a location
     * specified by the context path.  In addition, the login URL may be
     * treated specially by authenticators.
     * <P>
     * When a login is required, an authenticaor that supports this
     * behavior will reject all authorization requests from a user
     * until the user is authorized by visiting the login-alias page.
     * A logout will restore this behavior.
     * @param alias the alias string; null to remove it
     * @param target the relative path from this handler's context to
     *        the page that should be visited after a successful login.
     * @param required true if a login is required; false (the default)
     *        otherwise
     * @throws IllegalArgumentException if the first argument contains
     *        a "/" or the second argument starts with a "/"
     * @see EjwsSecureBasicAuth#setLoginFunction
     * @see EjwsSecureBasicAuth#setLogoutFunction
     * @see EjwsBasicAuthenticator#setLoginFunction
     * @see EjwsBasicAuthenticator#setLogoutFunction
     */
    public void setLoginAlias(String alias, String target, boolean required)
	throws IllegalArgumentException
    {
	if (alias != null) {
	    if (alias.contains("/")) {
		throw new IllegalArgumentException(errorMsg("aliasSlash"));
	    } else if (alias.equals(logoutAlias)) {
		throw new IllegalStateException(errorMsg("aliasConflict"));
	    }
	}
	if (target == null) {
	    target = "";
	} else {
	    target = target.trim();
	    if (target.length() > 0 && target.charAt(0) == '/') {
		throw new IllegalArgumentException(errorMsg("targetSlash"));
	    }
	}
	loginAlias = alias;
	loginTarget = target;
	loginURL = null;
	loginRequired = required;
    }

    /**
     * Set the login-alias string with a location to visit when the login is
     * successful, with the location represented as a URL.
     * The login alias is the path component of a URL and may not contain
     * a "/". An HTTP request whose path is the context path, followed
     * by a "/" if the context path does not end in "/", followed by
     * the login-alias, that URL will be redirected to a location
     * specified by the target.  In addition, the login URL may be
     * treated specially by authenticators.
     * @param alias the alias string; null to remove it
     * @param target the relative path from this handler's context to
     *        the page that should be visited after a successful login.
     * @throws IllegalArgumentException if the first argument contains
     *        a "/" or the URI provided by the second argument is not
     *        absolute
     * @throws MalformedURLException if the second argument could not be
     *         converted to a URL
     * @see EjwsSecureBasicAuth#setLoginFunction
     * @see EjwsSecureBasicAuth#setLogoutFunction
     * @see EjwsBasicAuthenticator#setLoginFunction
     * @see EjwsBasicAuthenticator#setLogoutFunction
     */
    public void setLoginAlias(String alias, URI target)
	throws IllegalArgumentException, MalformedURLException
    {
	setLoginAlias(alias, target, false);
    }
    /**
     * Set the login-alias string with a location to visit when the login is
     * successful, with the location represented as a URL, and with a flag
     * indicating if a login is required.
     * The login alias is the path component of a URL and may not contain
     * a "/". An HTTP request whose path is the context path, followed
     * by a "/" if the context path does not end in "/", followed by
     * the login-alias, that URL will be redirected to a location
     * specified by the target.  In addition, the login URL may be
     * treated specially by authenticators.
     * <P>
     * When a login is required, an authenticaor that supports this
     * behavior will reject all authorization requests from a user
     * until the user is authorized by visiting the login-alias page.
     * A logout will restore this behavior.
     * @param alias the alias string; null to remove it
     * @param target the relative path from this handler's context to
     *        the page that should be visited after a successful login.
     * @throws IllegalArgumentException if the first argument contains
     *        a "/" or the URI provided by the second argument is not
     *        absolute
     * @param required true if a login is required; false (the default)
     *        otherwise
     * @throws MalformedURLException if the second argument could not be
     *         converted to a URL
     * @see EjwsSecureBasicAuth#setLoginFunction
     * @see EjwsSecureBasicAuth#setLogoutFunction
     * @see EjwsBasicAuthenticator#setLoginFunction
     * @see EjwsBasicAuthenticator#setLogoutFunction
     */
    public void setLoginAlias(String alias, URI target, boolean required)
	throws IllegalArgumentException, MalformedURLException
    {
	if (alias != null) {
	    if (alias.contains("/")) {
		throw new IllegalArgumentException(errorMsg("aliasSlash"));
	    } else if (alias.equals(logoutAlias)) {
		throw new IllegalStateException(errorMsg("aliasConflict"));
	    }
	}
	loginAlias = alias;
	loginTarget = null;
	loginURL = target.toURL();
	loginRequired = required;
    }



    /**
     * Get the login-alias string.
     * @return the string; null if none has been set
     */
    public String getLoginAlias() {
	return loginAlias;
    }


    private String logoutAlias = null;
    private URI logoutURI = null;
    private InetAddress logoutAddr = null;
    private int logoutPort = -1;
    private String logoutPath = null;
    private EmbeddedWebServer ews = null;

    /**
     * Set the embedded web server that uses this file handler.
     * @param ews the embedded web server for this file handler; null
     *        if there is none
     */
    protected void setEWS(EmbeddedWebServer ews) {
	this.ews = ews;
    }

    /**
     * Set the logout-alias string with a location to visit when the logout is
     * successful, with the location represented as a URL.
     * The logout alias is the path component of a URL and may not contain
     * a "/". An HTTP request whose path is the context path, followed
     * by a "/" if the context path does not end in "/", followed by
     * the logout-alias, that URL will be redirected to a location
     * specified by the target.  In addition, the logout URL may be
     * treated specially by authenticators.
     * <P>
     * The URI provided by the second argument must not be one that
     * will be authenticated by this handler's context's authenticator.
     * The test for this is more precise when the web server is created
     * by an instance of {@link EmbeddedWebServer} because
     * {@link EmbeddedWebServer} allows one to find all the context paths.
     * Otherwise a simple test using this handler's context path is used:
     * For the current server, if the URI's path starts with the context
     * path, the logout URI is rejected.
     * @param alias the alias string; null to remove it
     * @param target the URI for the page that should be visited after
     *        a successful logout.
     * @throws IllegalArgumentException if the first argument contains
     *        a "/" or the URI provided by the second argument is not
     *        absolute
     * @throws UnknownHostException if the host component of a URI
     *         passed as this method's second argument is not recognized
     *         by a name server
     * @see EjwsSecureBasicAuth
     * @see EjwsBasicAuthenticator
     */
    public void setLogoutAlias(String alias, URI target)
	throws IllegalArgumentException, UnknownHostException
    {
	if (alias == null && target == null) {
	    logoutAlias = null;
	    logoutURI = null;
	    return;
	}
	if (alias.contains("/")) {
	    throw new IllegalArgumentException(errorMsg("aliasSlash"));
	} else if (alias.equals(loginAlias)) {
	    throw new IllegalStateException(errorMsg("aliasConflict"));
	}
	logoutAlias = alias;
	logoutURI = target;
	String host = target.getHost();
	logoutAddr = (host == null)? null: InetAddress.getByName(host);
	int port = target.getPort();
	logoutPath = target.getPath();
    }

    /**
     * Get the logout-alias string.
     * @return the string; null if none has been set
     */
    public String getLogoutAlias() {
	return logoutAlias;
    }

    /**
     * Get the logout URI.
     * The URI is the one to which a user will be redirected.
     * @return the logout URI
     */
    public URI getLogoutURI() {
	return  logoutURI;
    }


    String protocol = "http";

    /**
     * Constructor specifying a WebMap's class and using defaults for flags.
     * @param protocol the protocol used by the HTTP server - HTTP or HTTPS
     * @param root argument used to initialize an instance of
     *        <code>clasz</code>.
     * @param clasz the subclass of WebMap to use for locating resources
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @exception Exception an error occurred
     */
    public FileHandler(String protocol, Object root,
		       Class<? extends WebMap> clasz,
		       boolean nowebxml)
	throws Exception
    {
	this(protocol, root, clasz, nowebxml, false, true);
    }

    /**
     * Constructor specifying a WebMap's class.
     * @param protocol the protocol used by the HTTP server - HTTP or HTTPS
     * @param root argument used to initialize an instance of
     *        <code>clasz</code>.
     * @param clasz the subclass of WebMap to use for locating resources
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @param displayDir true to request that directory resource be displayed;
     *                   false if a directory resource should not be displayed
     * @param hideWebInf true if the WEB-INF directory should be hidden;
     *                   false if it should be visible
     * @exception IOException an IO error occurred
     * @exception SAXException a SAX exception occurred
     */
    public FileHandler(String protocol, Object root,
		       Class<? extends WebMap> clasz,
		       boolean nowebxml, boolean displayDir,
		       boolean hideWebInf)
	throws IOException, SAXException
    {
	super();
	this.protocol = protocol;
	this.nowebxml = nowebxml;
	// rootURI = root.toURI();
	// rootPath = root.getCanonicalPath();
	// String name = root.getName();
	map = WebMap.newInstance(root, clasz);
	map.setDisplayDir(displayDir);
	map.setWebInfHidden(hideWebInf);
	// InputStream is = WebMap.getWebxml(root);
	// WebMap.Info info  = nowebxml? null: map.getWebxml();
	WebMap.Info info = null;
	if (nowebxml == false) {
	    try {
		info = map.getWebxml();
	    } catch (Exception e) {
		info = null;
	    }
	}
	InputStream is = (info == null)? null: info.getInputStream();
	String location = (info == null)? "": info.getLocation();
	if (info != null) {
	    try {
		WebxmlParser parser = new WebxmlParser();
		parser.parse(is, location, map);
	    } catch (ParserConfigurationException pce) {
		throw new UnexpectedExceptionError(pce);
	    }

	}
    }

    /**
     * Constructor specifying a WebMap's class name and using defaults for
     * flags.
     * @param protocol the protocol used by the HTTP server - HTTP or HTTPS
     * @param root argument used to initialize an instance of
     *        <code>clasz</code>.
     * @param className the name of a subclass of WebMap, an instance of
     *        which will be used to map paths to resources.
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @exception IOException an IO error occurred
     * @exception SAXException a SAX exception occurred
   */
    public FileHandler(String protocol, Object root, String className,
		       boolean nowebxml)
	throws IOException, SAXException
    {
	this(protocol, root, className, nowebxml, false, true);
    }

    /**
     * Constructor specifying a WebMap's class name.
     * @param protocol the protocol used by the HTTP server - HTTP or HTTPS
     * @param root argument used to initialize an instance of
     *        <code>clasz</code>.
     * @param className the name of a subclass of WebMap, an instance of
     *        which will be used to map paths to resources.
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @param displayDir true to request that directory resource be displayed;
     *                   false if a directory resource should not be displayed
     * @param hideWebInf true if the WEB-INF directory should be hidden;
     *                   false if it should be visible
     * @exception IOException an IO error occurred
     * @exception SAXException a SAX exception occurred
     */
    public FileHandler(String protocol, Object root, String className,
		       boolean nowebxml, boolean displayDir,
		       boolean hideWebInf)
	throws IOException, SAXException
    {
	super();
	this.protocol = protocol;
	this.nowebxml = nowebxml;
	// rootURI = root.toURI();
	// rootPath = root.getCanonicalPath();
	// String name = root.getName();
	map = WebMap.newInstance(root, className);
	map.setDisplayDir(displayDir);
	map.setWebInfHidden(hideWebInf);
	// InputStream is = WebMap.getWebxml(root);
	// WebMap.Info info  = nowebxml? null: map.getWebxml();
	WebMap.Info info = null;
	if (nowebxml == false) {
	    try {
		info = map.getWebxml();
	    } catch (Exception e) {
		info = null;
	    }
	}
	InputStream is = (info == null)? null: info.getInputStream();
	String location = (info == null)? "": info.getLocation();
	if (info != null) {
	    try {
		WebxmlParser parser = new WebxmlParser();
		parser.parse(is, location, map);
	    } catch (ParserConfigurationException pce) {
		throw new UnexpectedExceptionError(pce);
	    }
	}
    }

    /**
     * Constructor using a previously created WebMap.
     * The WebMap provided will not be cloned, and may be modified, so
     * it should not be used with another FileHandler constructor.
     * @param protocol the protocol used by the HTTP server - HTTP or HTTPS
     * @param webmap a subclass of WebMap, which will be used to map
     *        paths to resources.
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @param displayDir true to request that directory resource be displayed;
     *                   false if a directory resource should not be displayed
     * @param hideWebInf true if the WEB-INF directory should be hidden;
     *                   false if it should be visible
     * @exception Exception an error occurred
     */
    public FileHandler(String protocol, WebMap  webmap,
		       boolean nowebxml, boolean displayDir,
		       boolean hideWebInf)
	throws Exception
    {
	super();
	this.protocol = protocol;
	this.nowebxml = nowebxml;
	// rootURI = root.toURI();
	// rootPath = root.getCanonicalPath();
	// String name = root.getName();
	map = webmap;
	map.setDisplayDir(displayDir);
	map.setWebInfHidden(hideWebInf);
	// InputStream is = WebMap.getWebxml(root);
	// WebMap.Info info  = nowebxml? null: map.getWebxml();
	WebMap.Info info = null;
	if (nowebxml == false) {
	    try {
		info = map.getWebxml();
	    } catch (Exception e) {
		info = null;
	    }
	}
	InputStream is = (info == null)? null: info.getInputStream();
	String location = (info == null)? "": info.getLocation();
	WebxmlParser parser = new WebxmlParser();
	if (info != null) {
	    parser.parse(is, location, map);
	}
    }

    int pendingCount = 0;
    /**
     * Get the number of outstanding requests.
     * Provided primarily for debugging or monitoring.
     * @return the number of outstanding requests
     */
    public synchronized int getPendingCount() {
	return pendingCount;
    }

    private synchronized void adjustPendingCount(int incr) {
	pendingCount += incr;
	// System.out.println("pending count = " + pendingCount);
    }

    private Appendable tracer = null;
    private boolean stacktrace = false;

    /**
    * Get the {@link Appendable} used for tracing
    * This method is restricted to this package and currently used
    * only by EmbeddedWebServer.
    * @return the {@link Appendable}; null if tracing is not enabled
    */
    Appendable getTracer() {return tracer;}

    /**
     * Determine if a stack tracing is requested for tracing.
     * This method is restricted to this package and currently used
     * only by EmbeddedWebServer.
     * @return true if stack tracing is enabled; false otherwise.
     */
    boolean getStacktracing() {return stacktrace;}


    /**
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * If {@link #setTracer(Appendable,boolean)} was previously called,
     * the handling of stack traces will not be change.
     * @param tracer the Appendable for tracing requests and responses;
     *        null to disable tracing
     */
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }

    /**
     * Set an Appendable for tracing with a stack trace.
     * This method should be used only for debugging.
     * The stack trace will be printed if {@link handle(HttpExchange)}
     * throws an exception and the first argument is not null.
     * @param tracer the Appendable for tracing requests and responses;
     *        null to disable tracing
     * @param stacktrace true for a stack trace; false otherwise
     */
    public void setTracer(Appendable tracer, boolean stacktrace) {
	this.tracer = tracer;
	this.stacktrace = stacktrace;
    }


    /**
     * Send the response headers, adding a session cookie if sessions
     * are enabled.
     * The third argument uses the same convention as that used in
     * {@link HttpExchange#sendResponseHeaders(int,long)}.
     * @param exchange the Http exchange
     * @param code the status code
     * @param length the length of the response; 0 if the length is not
     *        known and  -1 if there is no response
     * @exception IOException an IO error occurred
     */
    public static void sendResponseHeaders(HttpExchange exchange,
					   int code,
					   long length)
	throws IOException
    {
	// called just before handle(HttpExchange) calls sendResponseHeaders
	EjwsSession session = (EjwsSession)
	    exchange.getAttribute("org.bzdev.ejws.session");
	HeaderOps headerops = (session == null)? null:
	    WebMap.asHeaderOps(exchange.getResponseHeaders());
	if (session != null) {
	    List<Map<String,String>> maps =
		headerops.parseAll("Set-cookie", true);
	    for (Map<String,String> map: maps) {
		if(map.get("name").equalsIgnoreCase("jsessionid")) {
		    session = null;
		    break;
		}
	    }
	}
	if (session != null) {
	    // the caller has not explicitly set the jsessionid cookie
	    ServerCookie cookie =
		ServerCookie.newInstance("jsessionid",
					 session.getID());
	    cookie.setVersion(1);
	    cookie.setHttpOnly(true);
	    cookie.setPath(exchange.getHttpContext().getPath());
	    Headers reqhdrs = exchange.getRequestHeaders();
	    String hs = reqhdrs.getFirst("host").trim();
	    int indv6e = hs.indexOf(']');
	    int ind = hs.lastIndexOf(':');
	    boolean hasPort = (ind >= 0 && ind > indv6e);
	    String host = hasPort? hs.substring(0, ind): hs;
	    if (indv6e > 0 && host.charAt(0) == '[') {
		host = host.substring(1, indv6e);
	    }
	    cookie.setDomain(host);
	    cookie.addToHeaders(headerops);
	}
	exchange.sendResponseHeaders(code, length);
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
	try {
	    HttpMethod method = HttpMethod.forName(t.getRequestMethod());
	    boolean favicon =
		t.getRequestURI().getPath().startsWith("/favicon.ico");
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		tracer.append("(" + ct + ") "+ method +": "
			      + t.getRequestURI().toString() + "\n");
	    }
	    // System.out.println(t.getRequestMethod());
	    if (!map.acceptsMethod(method)) {
		if (method == HttpMethod.POST || method == HttpMethod.PUT) {
		    InputStream tis = t.getRequestBody();
		    try {
			while(tis.read() != -1);
		    } catch (Exception e) {
			String u = t.getRequestURI().toString();
			String msg = errorMsg("readReqBody", u, e.getMessage());
			ErrorMessage.display(msg);
		    }
		}
		WebMap.Info info = null;
		try {
		    info = map.getErrorInfo(405, protocol, t);
		} catch (EjwsException ejws) {}
		Headers headers = t.getResponseHeaders();
		headers.set("Content-Type", info.getMIMEType());
		long len = info.getLength();
		if (tracer != null) {
		    String ct = "" + Thread.currentThread().getId();
		    tracer.append("(" + ct + ") "+ method +": "
				  + t.getRequestURI().toString()
				  + " not accepted\n");
		}
		sendResponseHeaders(t, 405, len);
		OutputStream os = t.getResponseBody();
		InputStream is = null;
		try {
		    is = info.getInputStream();
		    long clen = is.transferTo(os);
		    if (len != clen) {
			throw new IOException(errorMsg("clen", len, clen));
		    }
		    // System.out.print("all ");
		} finally {
		    // System.out.println("error data sent");
		    if (is != null) is.close();
		}
		return;
	    }
	    InputStream is = null;
	    try {
		if (method != HttpMethod.POST && method != HttpMethod.PUT) {
		    is = t.getRequestBody();
		    while (is.read() != -1);
		}
	    } catch (Exception e) {
		ErrorMessage.display(e);
		try {
		    WebMap.Info info = map.getErrorInfo(e.getClass().getName());
		    if (info == null) info = map.getErrorInfo(500, protocol, t);
		    Headers headers = t.getResponseHeaders();
		    headers.set("Content-Type", info.getMIMEType());
		    long len = info.getLength();
		    if (tracer != null) {
			String ct = "" + Thread.currentThread().getId();
			tracer.append("(" + ct + ") "+ method +": "
				      + t.getRequestURI().toString()
				      + " webmap threw an exception\n");
		    }
		    sendResponseHeaders(t, 500, len);
		    OutputStream os = t.getResponseBody();
		    try {
			is = info.getInputStream();
			long clen = is.transferTo(os);
			if (clen != len) {
			    throw new IOException(errorMsg("clen", len, clen));
			}
			os.flush();
		    } finally {
			if (is != null) is.close();
		    }
		} catch (Exception ee) {
		    ErrorMessage.display(ee);
		}
		return;
	    }
	    if (method == HttpMethod.OPTIONS) {
		StringBuilder sb = new StringBuilder();
		sb.append("OPTIONS, TRACE");
		if (map.acceptsMethod(HttpMethod.GET)) {
		    sb.append(", GET");
		}
		if (map.acceptsMethod(HttpMethod.PUT)) {
		    sb.append(", PUT");
		}
		if (map.acceptsMethod(HttpMethod.POST)) {
		    sb.append(", POST");
		}
		if (map.acceptsMethod(HttpMethod.DELETE)) {
		    sb.append(", DELETE");
		}
		Headers headers = t.getResponseHeaders();
		headers.set("Allow", sb.toString());
		t.sendResponseHeaders(200, -1);
		return;
	    }
	    try {
		URI uri = t.getRequestURI();
		String path = uri.getPath();
		adjustPendingCount(1);
		String query = uri.getRawQuery();
		if (query == null || map.allowsQuery()) {
		    // System.out.println("query null or allowed");
		    String base = /*"/"*/t.getHttpContext().getPath();
		    String base1 = (base.endsWith("/"))? base: (base + "/");

		    if (loginAlias != null && path.equals(base1 + loginAlias)) {
			String location = (loginTarget != null)?
			    uri.resolve(base1 + loginTarget).toString():
			    loginURL.toExternalForm();
			// System.out.println("location = " + location);
			if (method == HttpMethod.PUT
			    || method == HttpMethod.POST) {
			    // Delete any pending data.
			    is = t.getRequestBody();
			    is.transferTo(OutputStream.nullOutputStream());
			    is.close();
			}
			Headers hdrs = t.getResponseHeaders();
			hdrs.set("Location", location);
			String proto = protocol.toUpperCase();
			int code = ((proto.startsWith("HTTP") &&
				     proto.endsWith("/1.0")))?
			    302: 307;
			sendResponseHeaders(t, code, -1);
			return;
		    } else if (logoutAlias != null
			       && path.equals(base1 + logoutAlias)) {
			// Do a check that a logout won't land you on
			// a page using the same authenticator that you
			// are trying to log out of.
			boolean ok = (ews == null);
			if (!ok) {
			    if (logoutPort > 0 && logoutAddr != null) {
				ok = !ews.isServerAddressAndPort(logoutAddr,
								 logoutPort)
				    || !logoutPath.startsWith(base1);
			    } else {
				ok = !logoutPath.startsWith(base1);
			    }
			}
			if (!ok) {
			    for (String p: ews.getPrefixes()) {
				if ((p.length() > base1.length())
				    && p.startsWith(base1)
				    && logoutPath.startsWith(p)) {
				    ok = true;
				    break;
				}
			    }
			}
			if (method == HttpMethod.PUT
			    || method == HttpMethod.POST) {
			    // Delete any pending data.
			    is = t.getRequestBody();
			    is.transferTo(OutputStream.nullOutputStream());
			    is.close();
			}
			String location = logoutURI.toASCIIString();
			if (ok) {
			    // System.out.println("location = " + location);
			    Headers hdrs = t.getResponseHeaders();
			    hdrs.set("Location", location);
			    String proto = protocol.toUpperCase();
			    int code = ((proto.startsWith("HTTP") &&
					 proto.endsWith("/1.0")))?
				302: 307;
			    sendResponseHeaders(t, code, -1);
			} else {
			    KeyMap kmap = new KeyMap();
			    kmap.put("statusCode", "500");
			    kmap.put("requestURL", uri.toString());
			    kmap.put("location", location);
			    TemplateProcessor tp = new TemplateProcessor(kmap);
			    ByteArrayOutputStream bos =
				new ByteArrayOutputStream();
			    try {
				InputStream tis = getClass()
				    .getResourceAsStream("error3.tpl");
				Reader r = new InputStreamReader(tis, "UTF-8");
				tp.processTemplate(r, "UTF-8", bos);
				sendResponseHeaders(t, 500, bos.size());
				OutputStream os = t.getResponseBody();
				bos.writeTo(os);
				os.flush();
			    } catch (Exception e) {
				ErrorMessage.display(e);
			    }
			}
			return;
		    }

		    // System.out.println("base = \"" + base + "\"");
		    // System.out.println("path = \"" + path + "\"");
		    WebMap.Info info = null;
		    boolean welcomeTest =
			((path.length() == 0 || path.equals(base)
			  || path.equals(base1))
			 && query == null);
		    welcomeTest = welcomeTest && map.welcomeInfoAvailable();
		    if (welcomeTest) {
			info = map.getWelcomeInfo();
		    }
		    // info will be non-null only if there is a 'welcome'
		    // page; otherwise we have to request the info object.
		    if (info == null) {
			info = map.getInfo(base, t);
			if (info != null && info.handledResponse()) {
			    /*
			     * The web map handled the response directly.
			     */
			    return;
			}
		    }
		    if (info == null) {
			// System.out.println("getting error info 404 (1)");
			if (favicon == false) {
			    /*
			     * This is what normally happens.  We don't want
			     * to fetch an HTML page for /favicon.ico because
			     * firefox immediately terminates the connection,
			     * causing a 'broken pipe' error. We also won't
			     * return an error page if the client won't accept
			     * the object returned as an error indication
			     * (normally text/html, but it could be anything).
			     */
			    info = map.getErrorInfo(404, protocol, t);
			    Headers reqhdrs = t.getRequestHeaders();
			    MIMEAcceptor acceptor = new MIMEAcceptor(reqhdrs);
			    if (acceptor.match(info.getMIMEType()) == null) {
				info = null;
			    }
			}
			/*
			  System.out.println("info.getLength() = "
			  + info.getLength());
			*/
			Headers headers = t.getResponseHeaders();
			if (info != null) {
			    headers.set("Content-Type", info.getMIMEType());
			    long len = info.getLength();
			    // System.out.println("info == null: 404 (2)");
			    if (tracer != null) {
				String ct = "" + Thread.currentThread().getId();
				tracer.append("(" + ct + ") "+ method +": "
					      + t.getRequestURI().toString()
					      + " not found\n");
			    }
			    sendResponseHeaders(t, 404, len);
			    OutputStream os = t.getResponseBody();
			    try {
				is = info.getInputStream();
				long clen = is.transferTo(os);
				if (clen != len) {
				    throw new
					IOException
					(errorMsg("clen", len, clen));
				}
				// System.out.print("all ");
			    } catch (IOException eio) {
				String emsg = eio.getMessage();
				String msg =
				    errorMsg("copyOut", uri.toString(), emsg);
				throw new IOException(msg, eio);
			    } finally {
				// System.out.println("error data sent");
				if (is != null) is.close();
			    }
			} else {
			    // favicon case and other cases where there is
			    // no webpage to turn. With firefox we were getting
			    // a broken pipe message, so we are sending a
			    // 404 response code with no content diagnosing
			    // the problem.
			    //
			    // System.out.println("sending 404 (3)");
			    sendResponseHeaders(t, 404, -1);
			}
		    } else {
			Headers reqhdrs = t.getRequestHeaders();
			List<String>expect = reqhdrs.get("Expect");
			if (expect != null) {
			    /*
			     * No Expect headers, including a 100-continue
			     * header, are handled because this handler
			     * is meant to return a simple request (e.g.,
			     * for a file).
			     */
			    info.getInputStream().close();
			    info = map.getErrorInfo(417, protocol, t);
			    Headers headers = t.getResponseHeaders();
			    headers.set("Content-Type", info.getMIMEType());
			    long len = info.getLength();
			    if (tracer != null) {
				String ct = "" + Thread.currentThread().getId();
				tracer.append("(" + ct + ") "+ method +": "
					      + t.getRequestURI().toString()
					      + " expect headers not supported"
					      + "\n");
			    }
			    sendResponseHeaders(t, 417, len);
			    OutputStream os = t.getResponseBody();
			    try {
				is = info.getInputStream();
				long clen = is.transferTo(os);
				if (len != clen) {
				    throw new IOException
					(errorMsg("clen", len, clen));
				}
			    } finally {
				if (is != null) is.close();
			    }
			    return;
			}
			if (info.getRedirect()) {
			    Headers hdrs = t.getResponseHeaders();
			    hdrs.set("Location", info.getLocation());
			    sendResponseHeaders(t, 302, -1);
			    return;
			}
			MIMEAcceptor acceptor = new MIMEAcceptor(reqhdrs);
			/*
			  System.out.println(acceptor.toString());
			  System.out.println("matched "
			  +acceptor.match(info.getMIMEType()));
			*/
			String mimetype = info.getMIMEType();
			Headers headers = t.getResponseHeaders();
			if (mimetype == null) {
			    /*
			      System.out.println("content-length = "
			      + headers
			      .getFirst("content-length"));
			    */
			} else if (acceptor.match(mimetype) == null) {
			    info.getInputStream().close();
			    info = map.getErrorInfo(406, protocol, t);
			    headers.set("Content-Type", info.getMIMEType());
			    long len = info.getLength();
			    if (tracer != null) {
				String ct = "" + Thread.currentThread().getId();
				tracer.append("(" + ct + ") "+ method +": "
					      + t.getRequestURI().toString()
					      + " response media type "
					      + "not accepted\n");
			    }
			    sendResponseHeaders(t, 406, len);
			    OutputStream os = t.getResponseBody();
			    try {
				is = info.getInputStream();
				long clen = is.transferTo(os);
				if (len != clen) {
				    throw new IOException
					(errorMsg("clen", len, clen));
				}
			    } finally {
				if (is != null) is.close();
			    }
			    return;
			}
			if (info.getEncoded()) {
			    headers.set("Content-Encoding", info.getEncoding());
			}
			if  (mimetype != null) {
			    headers.set("Content-Type", mimetype);
			}
			if (method == HttpMethod.GET) {
			    if (!headers.containsKey("cache-control")) {
				headers.set("Cache-Control",
					    "max-age=3600, public");
			    }
			}
			headers.set("Accept-Ranges", "none");
			if (method == HttpMethod.GET ||
			    method == HttpMethod.POST) {
			    long len = info.getLength();
			    sendResponseHeaders(t, 200, len);
			    OutputStream os = t.getResponseBody();
			    try {
				is = info.getInputStream();
				long clen = is.transferTo(os);
				if (len != clen) {
				    throw new IOException
					(errorMsg("clen", len, clen));
				}
				// System.out.print("all ");
			    } finally {
				// System.out.println("data sent");
				if (is != null) is.close();
			    }
			} else if (method == HttpMethod.HEAD) {
			    info.getInputStream().close();
			    long len = info.getLength();
			    sendResponseHeaders(t, 200, len);
			} else {
			    info.getInputStream().close();
			    headers.set("Allow", "GET, HEAD, TRACE");
			    info = map.getErrorInfo(405, protocol, t);
			    headers.set("Content-Type", info.getMIMEType());
			    long len = info.getLength();
			    if (tracer != null) {
				String ct = "" + Thread.currentThread().getId();
				tracer.append("(" + ct + ") "+ method +": "
					      + t.getRequestURI().toString()
					      + " method not allowed\n");
			    }
			    sendResponseHeaders(t, 405,len);
			    OutputStream os = t.getResponseBody();
			    try {
				is = info.getInputStream();
				long clen = is.transferTo(os);
				if (clen != len) {
				    throw new IOException
					(errorMsg("clen", len, clen));
				}
			    } finally {
				if (is != null) is.close();
			    }
			}
		    }
		} else {
		    // System.out.println("sending 404 with error info (4)");
		    WebMap.Info info = map.getErrorInfo(404, protocol, t);
		    Headers headers = t.getResponseHeaders();
		    headers.set("Content-Type", info.getMIMEType());
		    long len = info.getLength();
		    if (tracer != null) {
			String ct = "" + Thread.currentThread().getId();
			tracer.append("(" + ct + ") "+ method +": "
				      + t.getRequestURI().toString()
				      + " request may not contain a query\n");
		    }
		    sendResponseHeaders(t, 404, len);
		    OutputStream os = t.getResponseBody();
		    try {
			is = info.getInputStream();
			long clen = is.transferTo(os);
			if (clen != len) {
			    throw new IOException(errorMsg("clen", len, clen));
			}
		    } finally {
			is.close();
		    }
		    return;
		}
	    } catch (EjwsException ee) {
		WebMap.Info info;
		try {
		    info = map.getErrorInfo(500, ee,  protocol, t);
		} catch (EjwsException ejws) {
		    info = null;
		}
		Headers headers = t.getResponseHeaders();
		if (info != null) {
		    headers.set("Content-Type", info.getMIMEType());
		}
		long len = (info == null)? 0: info.getLength();
		if (tracer != null) {
		    String ct = "" + Thread.currentThread().getId();
		    String s1 = t.getRequestURI().toString();
		    String s2 = ee.getClass().toString();
		    String s3 = ee.getMessage();
		    String msg =
			errorMsg("serverError", ct, method, s1, s2, s3);
		    /*
		    tracer.append("(" + ct + ") "+ method +": "
				  + t.getRequestURI().toString()
				  + "server error " + ee.getClass().toString()
				  + ": " + ee.getMessage() + "\n");
		    */
		    tracer.append(msg + "\n");
		    if (stacktrace) {
			PrintWriter pw = new
			    PrintWriter(new AppendableWriter(tracer), true);
			ee.printStackTrace(pw);
			pw.flush();
		    }
		}
		sendResponseHeaders(t, 500, len);
		OutputStream os = t.getResponseBody();
		try {
		    is = (info == null)? null: info.getInputStream();
		    if (is != null) {
			long clen = is.transferTo(os);
			if (clen != len) {
			    throw new IOException(errorMsg("clen", len, clen));
			}
			os.flush();
		    }
		} finally {
		    if (is != null) is.close();
		}
		return;
	    } catch (IOException eeio) {
		// normally for an IO error you would use 404, but
		// normally map.getInfo returns null to indicate that
		// a requested resource is missing. In this case, an
		// IO error indicates something else happened.
		WebMap.Info info;
		try {
		    info = map.getErrorInfo(500, eeio,  protocol, t);
		} catch (EjwsException ejws) {
		    info = null;
		}
		Headers headers = t.getResponseHeaders();
		if (info != null) {
		    headers.set("Content-Type", info.getMIMEType());
		}
		long len = (info == null)? 0: info.getLength();
		if (tracer != null) {
		    String ct = "" + Thread.currentThread().getId();
		    String s1 = t.getRequestURI().toString();
		    String s2 = eeio.getClass().toString();
		    String s3 = eeio.getMessage();
		    String msg =
			errorMsg("serverError", ct, method, s1, s2, s3);
		    /*
		    tracer.append("(" + ct + ") "+ method +": "
				  + t.getRequestURI().toString()
				  + "server error " + ee.getClass().toString()
				  + ": " + ee.getMessage() + "\n");
		    */
		    tracer.append(msg + "\n");
		    if (stacktrace) {
			PrintWriter pw = new
			    PrintWriter(new AppendableWriter(tracer), true);
			eeio.printStackTrace(pw);
			pw.flush();
		    }
		}
		sendResponseHeaders(t, 500, len);
		OutputStream os = t.getResponseBody();
		try {
		    is = (info == null)? null: info.getInputStream();
		    if (is != null) {
			long clen = is.transferTo(os);
			if (clen != len) {
			    throw new IOException(errorMsg("clen", len, clen));
			}
			os.flush();
		    }
		} finally {
		    if (is != null) is.close();
		}
		return;
	    } catch (Exception e) {
		ErrorMessage.display(e);
		return;
	    } finally {
		adjustPendingCount(-1);
		if (tracer != null) {
		    String ct = "" + Thread.currentThread().getId();
		    tracer.append("(" + ct + ") response code: "
				  + t.getResponseCode() + "\n");
		}
	    }
	} catch (IOException eio) {
	    if (tracer != null) {
		String ct = "" + Thread.currentThread().getId();
		String cn = "java.io.IOException";
		String m = eio.getMessage();
		String msg = errorMsg("runtimeException", cn, m);
		tracer.append("(" + ct + ") " + msg + "\n");
		if (stacktrace) {
		    PrintWriter pw = new
			PrintWriter(new AppendableWriter(tracer), true);
		    eio.printStackTrace(pw);
		    pw.flush();
		}
	    }
	    throw eio;
	} finally {
	    t.close();
	}
    }
}

//  LocalWords:  exbundle HttpHandler FileHandler WebMap JSP URL's
//  LocalWords:  FileHandler's mdash rootURI rootPath svrURI getMap
//  LocalWords:  badItemElements mimePattern badMIMEPattern charset
//  LocalWords:  itemElements http WebMap's HTTPS clasz nowebxml xml
//  LocalWords:  displayDir hideWebInf toURI getCanonicalPath getName
//  LocalWords:  InputStream getWebxml className webmap pendingCount
//  LocalWords:  getRequestMethod uri toString getPath getLength clen
//  LocalWords:  printStackTrace mimeAcceptor authenticators boolean
//  LocalWords:  IllegalArgumentException IOException SAXException
//  LocalWords:  Appendable setTracer stacktrace HttpExchange firefox
//  LocalWords:  sendResponseHeaders jsessionid readReqBody logoutURI
//  LocalWords:  getAttribute SecureBasicAuthLogout welcomeTest html
//  LocalWords:  copyOut favicon webpage acceptor getFirst
