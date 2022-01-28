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
			/*
			  System.out.println("mimePattern.length = "
			  + mimePattern.length
			  + ", mimePattern[0] = "
			  + mimePattern[0]);
			*/
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

    String protocol = "http";

    /**
     * Constructor specifying a WebMap's class and using defaults for flags.
     * @param protocol the protocol used by the HTTP server - HTTP or HTTPS
     * @param root argument used to initialize an instance of
     *        <code>clasz</code>.
     * @param clasz the subclass of WebMap to use for locating resources
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
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
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * If {@link #setTracer(Appendable,boolean)} was previously called,
     * the handling of stack traces will not be change.
     * @param tracer the Appendable for tracing requests and responses
     */
    public void setTracer(Appendable tracer) {
	this.tracer = tracer;
    }

    /**
     * Set an Appendable for tracing with a stacktrace.
     * This method should be used only for debugging.
     * The stacktrace will be printed if {@link handle(HttpExchange)}
     * throws an exception and the first argument is not null.
     * @param tracer the Appendable for tracing requests and responses
     * @parm stacktrace true for a stack trace; false otherwise
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
     * @length the length of the response; 0 if the length is not known and
     *         -1 if there is no response
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
	    System.out.println("session.getID() = \"" + session.getID()
			       + "\"");
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
	    System.out.println(host);
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
		    // System.out.println("base = \"" + base + "\"");
		    // System.out.println("path = \"" + path + "\"");
		    WebMap.Info info = null;
		    boolean welcomeTest =
			((path.length() == 0 || path.equals(base)
			  || path.equals(base1))
			 && query == null);
		    // welcomeTest = welcomeTest && !nowebxml;
		    welcomeTest = welcomeTest && map.welcomeInfoAvailable();
		    if (welcomeTest) {
			if (path.length() == 0 || path.equals("/")) {
			    info = map.getWelcomeInfo();
			} else {
			    info = map.getWelcomeInfo(base1);
			}
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
		    tracer.append("(" + ct + ") "+ method +": "
				  + t.getRequestURI().toString()
				  + "server error " + ee.getClass().toString()
				  + ": " + ee.getMessage());
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
	    if (stacktrace && tracer != null) {
		PrintWriter pw = new PrintWriter(new AppendableWriter(tracer),
						 true);
		eio.printStackTrace(pw);
		pw.flush();
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
//  LocalWords:  getRequestMethod uri toString getPath getLength
//  LocalWords:  printStackTrace
