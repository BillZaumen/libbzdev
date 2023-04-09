package org.bzdev.ejws;
import org.bzdev.net.HeaderOps;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.HttpSessionOps;
import org.bzdev.net.HttpServerRequest;
import org.bzdev.net.HttpServerResponse;
import org.bzdev.net.ServerCookie;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.CollectionScanner;
import org.bzdev.util.EncapsulatingIterator;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.IteratorEnumeration;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.*;
import com.sun.net.httpserver.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//@exbundle org.bzdev.ejws.lpack.EmbeddedWebServer

/**
 * Base class for mapping URIs to resources.
 *
 * For details on the format of a WEB-INF/web.xml file, please
 * see <A href="http://download.oracle.com/docs/cd/E12840_01/wls/docs103/webapp/web_xml.html">web.xml Deployment Descriptor Elements</a>.
 * WebMap recognizes the following elements:
 * <ul>
 *    <li><code>web-app</code>
 *    <ul>
 *      <li><code>mime-mapping</code>
 *          <ul>
 *            <li><code>extension</code>
 *            <li><code>mime-type</code>
 *          </ul>
 *      <li><code>welcome-file-list</code>
 *          <ul>
 *            <li><code>welcome-file</code>
 *          </ul>
 *      <li><code>error-page</code>
 *          <ul>
 *            <li><code>exception-code</code>
 *            <li><code>exception-type</code>
 *            <li><code>location</code>
 *          </ul>
 *      <li><code>jsp-config</code>
 *          <ul>
 *            <li><code>jsp-property-group</code>
 *                <ul>
 *                   <li><code>url-pattern</code>
 *                   <li><code>page-encoding</code>
 *                    <li><code>is-xml</code>
 *          </ul>
 *          </ul>
      </ul>
 * </ul>
 * Other cases are ignored.
 * <P>
 * Subclasses of WebMap are expected to have a single-argument constructor
 * that accepts an Object as its sole argument.
 * The method {@link #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)}
 * must be implemented.  Essentially, this method returns a WebMap.Info object
 * that representing a response to an HTTP request.
 * <P>
 * The constructor for a subclass should call
 * {@link WebMap#setMethods(HttpMethod... methods)} to change the methods
 * it supports from the default (HEAD and GET), and
 * {@link WebMap#setAllowsQuery(boolean)} if queries are allowed (by default
 * they are not allowed).
 */

abstract public class WebMap {

    static String errorMsg(String key, Object... args) {
	return EmbeddedWebServer.errorMsg(key, args);
    }

    EnumSet<HttpMethod> methodSet =
	EnumSet.of(HttpMethod.TRACE, HttpMethod.HEAD, HttpMethod.GET);
    /**
     * Indicate the methods this WebMap can implement
     * If not called, this web map can implement {@link HttpMethod#TRACE},
     * {@link HttpMethod#HEAD}, and {@link HttpMethod#GET}.  When this
     * method is called, the list of methods is cleared and the method
     * {@link HttpMethod#TRACE} is automatically added
     * whether or not it is present in the argument list.
     * @param methods the HTTP methods that this web map can implment
     */
    protected void setMethods(HttpMethod... methods) {
	methodSet.clear();
	methodSet.add(HttpMethod.TRACE);
	for (HttpMethod method: methods) {
	    methodSet.add(method);
	}
    }

    /**
     * Indicate if this WebMap can implement a specified HTTP method.
     * @param method the method
     * @return true if this WebMap accepts the method; false otherwise.
     */
    public boolean acceptsMethod(HttpMethod method) {
	return methodSet.contains(method);
    }

    private LinkedList<String> welcomeList = new LinkedList<String>();

    /**
     * Get the list of welcome pages associated with this web map.
     * @return a list of welcome pages.
     */
    protected LinkedList<String> getWelcomeList() {return welcomeList;}

    private  Set<String> suffixForGZip = new HashSet<String>();

    private HashMap<String,String> suffixToMimeType =
	new HashMap<String,String>();

    /**
     * Get the HashMap associating media types with file-name extensions.
     * @return the map
     */
    protected HashMap<String,String> getSuffixToMimeTypeMap() {
	return suffixToMimeType;
    }

    private boolean queriesAllowed = false;

    /**
     * Set whether or not queries are allowed.
     * @param value true if queries are allowed; false otherwise.
     */
    protected void setAllowsQuery(boolean value) {
	queriesAllowed = value;
    }

    /**
     * Indicate if a URI from an HTTP request can contain a query.
     * @return true if queries are allowed; false otherwise
     */
    public boolean allowsQuery() {return queriesAllowed;}

    private boolean displayDir = false;


    /**
     * Indicate if a directory should be displayed when pointing to a directory.
     * The directory will be provided as an HTML page.
     * Subclasses should use this method to determine whether or not
     * a directory page should be generated.
     * @return true if a directory should be displayed; false otherwise
     */
    public boolean getDisplayDir() {return displayDir;}

    /**
     * Set whether or not a directory should be displayed when
     * pointing to a directory.
     * The directories will be provided as HTML pages during periods in which
     * the argument is true.  Note that this setting effects all HTTP requests.
     * Attempts to change the setting for particular requests can cause
     * unpredictable behavior due to race conditions.
     * @param value true if a directory should be displayed; false otherwise
     */
    public void setDisplayDir(boolean value) {
	displayDir = value;
    }


    File root = null;
    URI rootURI = null;
    ZipFile zipfile = null;
    String rootResourcePath = null;

    static Set<String> suffixForGZipDefault = new HashSet<String>();
    static {
	suffixForGZipDefault.add("gz");
    }


    static Map<String,String> suffixToMTypeDefault =
	new HashMap<String,String>();
    static {
	suffixToMTypeDefault.put("html", "text/html");
	suffixToMTypeDefault.put("htm", "text/html");
	suffixToMTypeDefault.put("xhtml", "application/xhtml+xml");
	suffixToMTypeDefault.put("xhtm", "application/xhtml+xml");
	suffixToMTypeDefault.put("xht", "application/xhtml+xml");
	suffixToMTypeDefault.put("css", "text/css");

	suffixToMTypeDefault.put("js", "text/javascript");
	suffixToMTypeDefault.put("txt", "text/plain");
	suffixToMTypeDefault.put("pdf", "application/pdf");
	suffixToMTypeDefault.put("xml", "application/xml");
	suffixToMTypeDefault.put("xsl","application/xslt+xml");
	suffixToMTypeDefault.put("ps", "application/postscript");
	suffixToMTypeDefault.put("jsp", "application/jsp");
	suffixToMTypeDefault.put("svg", "image/svg+xml");
	suffixToMTypeDefault.put("ogg", "application/ogg");
	suffixToMTypeDefault.put("zip", "application/zip");

	suffixToMTypeDefault.put("doc", "application/msword");
	suffixToMTypeDefault.put("ppt","application/vnd.ms-powerpoint");
	suffixToMTypeDefault.put
	    ("odt","application/vnd.oasis.opendocument.text");
	suffixToMTypeDefault.put
	    ("odg", "application/vnd.oasis.opendocument.graphics");
	suffixToMTypeDefault.put
	    ("odp", "application/vnd.oasis.opendocument.presentation");

	// Add in image types recognized by Java on this computer
	Iterator<ImageWriter>it;
	Map<Class,String> cmap = new HashMap<Class,String>();
	for(String mt: ImageIO.getWriterMIMETypes()) {
	    it = ImageIO.getImageWritersByMIMEType(mt);
	    if (it != null) {
		while (it.hasNext()) {
		    Class c = it.next().getClass();
		    cmap.put(c, mt);
		}
	    }
	}
	for (String ext: ImageIO.getWriterFileSuffixes()) {
	    it = ImageIO.getImageWritersBySuffix(ext);
	    if (it != null) {
		while (it.hasNext()) {
		    Class c = it.next().getClass();
		    String mtype = cmap.get(c);
		    if (mtype == null) continue;
		    suffixToMTypeDefault.put(ext, mtype);
		}
	    }
	}
    }

    /**
     * Convert an instance of {@link Headers} to an instance of
     * {@link HeaderOps}.
     * @param headers the headers
     * @return the converted object
     */
    public static HeaderOps asHeaderOps(final Headers headers) {
	return new HeaderOps() {
	    @Override
	    public void clear() {
		headers.clear();
	    }
	    @Override
	    public boolean containsKey(Object key) {
		return headers.containsKey(key);
	    }
	    @Override
	    public boolean containsValue(Object value) {
		return headers.containsValue(value);
	    }
	    @Override
	    public Set<Map.Entry<String,List<String>>> entrySet() {
		return headers.entrySet();
	    }
	    @Override
	    public boolean equals(Object o) {
		return this == o;
	    }
	    @Override
	    public List<String> get(Object key) {
		return headers.get(key);
	    }
	    @Override
	    public int hashCode() {
		return headers.hashCode();
	    }
	    @Override
	    public boolean isEmpty() {
		return headers.isEmpty();
	    }
	    @Override
	    public Set<String> keySet() {
		return headers.keySet();
	    }
	    @Override
	    public List<String>put(String key, List<String>list) {
		return headers.put(key,list);
	    }
	    @Override
	    public void putAll(Map<? extends String, ? extends List<String>>m) {
		headers.putAll(m);
	    }
	    @Override
	    public List<String>remove(Object key) {
		return headers.remove(key);
	    }
	    @Override
	    public int size() {
		return headers.size();
	    }
	    @Override
	    public Collection<List<String>>values() {
		return headers.values();
	    }
	};
    }

    /**
     * HTTP Request data.
     * This data structure is passed as an argument to
     * {@link #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)}
     * and allows implementations of that method
     * <UL>
     *  <LI> to get the request method (HEAD, GET, POST, PUT, etc.).
     *  <LI> to get the value(s) associated a header given its key.
     *  <LI> to get the content type of a request.
     *  <LI> to get the content length of a request.
     *  <LI> A stream to read the request's contents. The method
     *       {@link RequestInfo#getDecodedInputStream()} will
     *       provide the contents after any content-encoding has
     *       been processed. This is useful if data was compressed
     *       and will be stored somewhere in compressed form. The
     *       method {@link RequestInfo#getDecodedInputStream()}
     *       provides an input stream that will decompress the data
     *       if necessary. When the actual data is used, this method
     *       is preferred.
     * </UL>
     * Note: the constructor is visible only in this package.
     * It is used by FileHandler. This class implements the interface
     * HttpServerRequest because that interface was designed to be
     * easily implemented by a servlet. This design
     * decision was made to facilitate migrating code written for an
     * EJWS server to a web server that supports servlet.
     */
    public static class RequestInfo implements HttpServerRequest {
	HttpExchange exchange;
	HttpMethod method;
	Headers headers;
	HeaderOps headerops;
	String contentEncoding = Info.IDENTITY;
	String mediaType = "application/octet-stream";
	boolean useEncoded = false;
	int contentLength = -1;
	long contentLengthLong = -1L;
	InputStream is = null;
	InputStream is2 = null;
	List<Locale> locales = null;

	@Override
	public Object getAttribute(String name) {
	    return exchange.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
	    exchange.setAttribute(name, value);
	}

	@Override
	public Object getContextAttribute(String name) {
	    return exchange.getHttpContext().getAttributes().get(name);
	}

	@Override
	public void setContextAttribute(String name, Object value) {
	    exchange.getHttpContext().getAttributes().put(name, value);
	}

	String sessionID = null;
	boolean sessionIDSet = false;

	@Override
	public String getRequestedSessionID()  {
	    if (sessionIDSet) {
		return sessionID;
	    }
	    EjwsSession session = (EjwsSession)
		exchange.getAttribute("org.bzdev.ejws.session");
	    if (session != null) {
		sessionIDSet = true;
		sessionID = session.getID();
		return sessionID;
	    }
	    return null;
	}


	@Override
	public String changeSessionID() throws IllegalStateException {
	    EjwsSession session = (EjwsSession)
		exchange.getAttribute("org.bzdev.ejws.session");
	    if (session != null) {
		sessionID = session.changeSessionID();
		sessionIDSet = true;
		return sessionID;
	    } else {
		throw new IllegalStateException(errorMsg("noSession"));
	    }
	}

	@Override
	public void setSessionState(Object state)
	    throws IllegalStateException
	{
	    EjwsSession session = (EjwsSession)
		exchange.getAttribute("org.bzdev.ejws.session");
	    if (session != null) {
		sessionID = session.getID();
		HttpSessionOps ops = session.manager.sessionOps;
		if (ops == null) {
		    throw new IllegalStateException(errorMsg("noSession"));
		} else {
		    ops.put(sessionID, state);
		}
	    } else {
		throw new IllegalStateException(errorMsg("noSession"));
	    }
	}

	@Override
	public Object getSessionState() {
	    EjwsSession session = (EjwsSession)
		exchange.getAttribute("org.bzdev.ejws.session");
	    if (session == null) {
		return null;
	    } else {
		HttpSessionOps ops = session.manager.sessionOps;
		if (ops == null) {
		    return null;
		} else {
		    return ops.get(session.getID());
		}
	    }
	}

	@Override
	public boolean isRequestedSessionIDValid() {
	    if (sessionIDSet == false) getRequestedSessionID();
	    if (sessionID == null) return false;
	    return true;
	}

	@Override
	public boolean setMaxInactiveInterval(int interval) {
	    EjwsSession session = (EjwsSession)
		exchange.getAttribute("org.bzdev.ejws.session");
	    if (session != null) {
		session.setMaxInactiveInterval(interval);
		return true;
	    }
	    return false;
	}

	@Override
	public int getMaxInactiveInterval() {
	    EjwsSession session = (EjwsSession)
		exchange.getAttribute("org.bzdev.ejws.session");
	    if (session != null) {
		int value = session.getMaxInactiveInterval();
		if (value < 0) value = 0;
		return value;
	    }
	    return -1;
	}

	@Override
	public HttpMethod getMethod() {
	    return method;
	}

	@Override
	public String getRequestURI() {
	    return exchange.getRequestURI().getPath();
	}

	@Override
	public String getRequestURL() {
	    try {
	    // The uri just contains the path and query
		URI uri = exchange.getRequestURI();
		String scheme = isSecure()? "https": "http";
		InetSocketAddress saddr = exchange.getLocalAddress();
		String hostname = saddr.getHostString();
		int port = saddr.getPort();
		URI newuri = new URI(scheme, hostname + ":" + port,
				     null, null, null);
		return newuri.resolve(uri).toURL().toString();
	    } catch (Exception e) {
		return null;
	    }
	}

	@Override
	public String getProtocol() {
	    return exchange.getProtocol();
	}

	@Override
	public Principal getUserPrincipal() {
	    return exchange.getPrincipal();
	}

	@Override
	public String getQueryString() {
	    return exchange.getRequestURI().getRawQuery();
	}

	ServerCookie[] cookies = null;

	@Override
	public ServerCookie[] getCookies() {
	    if (cookies == null) {
		cookies = ServerCookie.fetchCookies(headerops);
	    }
	    if (cookies == null) cookies = new ServerCookie[0];
	    return cookies;
	}

	@Override
	public Locale getLocale() {
	    return locales.get(0);
	}

	/**
	 * Get the locales provided by the Accept-Language header, ordered
	 * by preference, highest preference first.
	 * <P>
	 * Note: an enumeration is returned instead of an iterator for
	 * compatibility with the servlet specification.
	 * @return an enumeration for these locales
	 */
	@Override
	public Enumeration<Locale> getLocales() {
	    return new IteratorEnumeration<Locale>(locales.iterator());
	}

	@Override
	public String getHeader(String type) {
	    return headers.getFirst(type);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
	    return new IteratorEnumeration<String>(headers.keySet().iterator());
	}

	@Override
	public Enumeration<String> getHeaders(String type) {
	    return new IteratorEnumeration<String>
		(headers.get(type).iterator());
	}

	@Override
	public boolean isUserInRole(String role) {
	    HttpPrincipal p = exchange.getPrincipal();
	    if (p == null) return false;
	    if (p instanceof EjwsPrincipal) {
		EjwsPrincipal ep = (EjwsPrincipal) p;
		return ep.getRoles().contains(role);
	    } else {
		return false;
	    }
	}


	/**
	 * Get the content type.
	 * @return the media type for the content of a request
	 */
	@Override
	public String getContentType() {
	    return mediaType;
	}

	@Override
	public String getCharacterEncoding() {
	    return getFromHeader("content-type", "charset");
	}

	@Override
	public String getMediaType() {
	    if (mediaType == null) return "application/octet-stream";
	    int firstsc = mediaType.indexOf(';');
	    if (firstsc == -1) {
		return mediaType;
	    } else {
		return mediaType.substring(0, firstsc).trim();
	    }
	}

	@Override
	public String getFromHeader(String key, String parameter)
	{
	    return WebMap.getFromHeader(asHeaderOps(headers), key, parameter);
	}

	@Override
	public int getContentLength() {
	    return contentLength;
	}

	@Override
	public long getContentLengthLong() {
	    return contentLengthLong;
	}

	@Override
	public InputStream getEncodedInputStream() {
	    if (is2 != null) return null;
	    if (is != null) {
		useEncoded = true;
	    }
	    return is;
	}

	@Override
	public InputStream getDecodedInputStream() {
	    if (useEncoded) return null;
	    if (is == null) return is2;
	    try {
		if (contentEncoding.equalsIgnoreCase(Info.IDENTITY)) {
		    is2 = is;
		} else if (contentEncoding.equalsIgnoreCase("gzip")) {
		    is2 = new java.util.zip.GZIPInputStream(is);
		} else if (contentEncoding.equalsIgnoreCase("deflate")) {
		    is2 = new java.util.zip.InflaterInputStream(is);
		} else {
		    return null;
		}
	    } catch (IOException e) {
		// this should never occur. If 'is' is not null, we
		// have a valid InputStream so adding one of the compression
		// input streams should just work.
	    }
	    is = null;
	    return is2;
	}

	@Override
	public boolean isSecure() {
	    return (exchange instanceof HttpsExchange);
	}

	Map<String,String[]> parameterMap = null;

	@Override
	public Map<String,String[]> getParameterMap() {
	    return (parameterMap == null)? Collections.emptyMap():
		Collections.unmodifiableMap(parameterMap);
	}

	RequestInfo(HttpExchange exch)
	{
	    exchange = exch;
	    method = HttpMethod.forName(exch.getRequestMethod());
	    headers = exch.getRequestHeaders();
	    headerops = asHeaderOps(headers);
	    /*
	    headerops = new HeaderOps() {
		    @Override
		    public void add(String key, String value) {
			headers.add(key, value);
		    }
		    @Override
		    public String getFirst(String key) {
			return headers.getFirst(key);
		    }
		    @Override
		    public List<String> get(Object key) {
			return headers.get(key);
		    }
		    @Override
		    public void set(String key, String value) {
			headers.set(key, value);
		    }
		    @Override
		    public void clear() {
			headers.clear();
		    }
		    @Override
		    public boolean containsKey(Object key) {
			return headers.containsKey(key);
		    }
		    @Override
		    public boolean containsValue(Object value) {
			return headers.containsValue(value);
		    }
		    @Override
		    public Set<Map.Entry<String,List<String>>> entrySet() {
			return headers.entrySet();
		    }
		    @Override
		    public boolean equals(Object o) {
			return this == o || headers.equals(o);
		    }
		    @Override
		    public int hashCode() {
			return headers.hashCode();
		    }
		    @Override
		    public boolean isEmpty() {
			return headers.isEmpty();
		    }
		    @Override
		    public Set<String> keySet() {
			return headers.keySet();
		    }
		    @Override
		    public List<String> put(String key, List<String>value) {
			return headers.put(key, value);
		    }
		    @Override
		    public void putAll(Map<? extends String,
				       ? extends List<String>> m) {
			headers.putAll(m);
		    }
		    @Override
		    public List<String> remove(Object key) {
			return headers.remove(key);
		    }
		    @Override
		    public int size() {
			return headers.size();
		    }
		    @Override
		    public Collection<List<String>> values() {
			return headers.values();
		    }
		};
	    */
	    List<String> values = headers.get("content-encoding");
	    if (values != null && values.size() == 1) {
		contentEncoding = values.get(0);
	    }
	    values = headers.get("content-type");
	    if (values != null && values.size() == 1) {
		mediaType = values.get(0);
	    }
	    is = exch.getRequestBody();
	    values = headers.get("content-length");
	    if (values != null && values.size() == 1) {
		try {
		    contentLength = Integer.parseInt(values.get(0));
		} catch (Exception e) {
		    contentLength = -1;
		}
		try {
		    contentLengthLong = Long.parseLong(values.get(0));
		} catch (Exception e) {
		    contentLengthLong = -1;
		}
	    }
	    if (method == HttpMethod.GET || method == HttpMethod.POST) {
		if (method == HttpMethod.GET) {
		    String qstr = getQueryString();
		    if (qstr != null) {
			parameterMap = WebDecoder
			    .formDecodeMV(qstr, false,
					  Charset.forName("UTF-8"));
		    }
		} else if (method == HttpMethod.POST) {
		    if (getMediaType()
			.equals("application/x-www-form-urlencoded")) {
			int len = (contentLengthLong > Integer.MAX_VALUE)? -1:
			    contentLength;
			if (len != 0) {
			    ByteArrayOutputStream baos =
				new ByteArrayOutputStream((len == -1)? 4096:
							  len);
			    try {
				is.transferTo(baos);
				String charEncoding = getCharacterEncoding();
				if (charEncoding == null) {
				    charEncoding = "UTF-8";
				}
				String qstr =
				    baos.toString(charEncoding);
				is = new
				    ByteArrayInputStream(baos.toByteArray());
				parameterMap =
				    WebDecoder.formDecodeMV(qstr, false,
							    Charset
							    .forName("UTF-8"));
			    } catch(IOException eio) {
			    } catch (Exception ex) {
			    }
			}
		    }
		}
	    }

	    values = headers.get("Accept-Language");
	    if (values != null && values.size() == 1) {
		List<Map<String,String>> list =
		    headerops.parseAll("Accept-Language", false);
		if (list != null) {
		    list.sort(new Comparator<Map<String,String>>() {
			    public int compare(Map<String,String> o1,
					Map<String,String> o2) {
				String qs1 = o1.get("q");
				String qs2 = o2.get("q");
				double q1 = (qs1 == null)? 1.0:
				    Double.parseDouble(qs1);
				double q2 = (qs2 == null)? 1.0:
				    Double.parseDouble(qs2);
				if (q1 == q2) return 0;
				// backwards to get descending order: largest
				// value first.
				if (q1 < q2) return 1;
				else return -1;
			    }
			});

		    int size = list.size();
		    if (size > 0) {
			locales = new ArrayList<Locale>(size);
			String lang = list.get(0).get("accept-language");
			if (lang != null) {
			    locales.add(new Locale(lang));
			}
		    }
		}
		if (locales == null) {
		    locales = new ArrayList<Locale>(1);
		    locales.add(Locale.getDefault());
		}
	    }
	}
    }

    /**
     * Info for accessing a resource.
     * an Instance of this class is returned by the WebMap methods
     * getWebxml, getWelcomeInfo, getInfoFromPath, getErrorInfo, and
     * getInfo.
     * This class can be used in two modes:
     * <UL>
     *   <LI> In the first mode, for successful requests, the Info
     *     object will provide an {@link java.io.InputStream} that will
     *     allow an object to be read.  If the response is an HTTP
     *     redirect, the input stream should be null and the length
     *     should be -1, and the location should not be null.  The
     *     method {@link Info#setRedirect(boolean)} must be called with an
     *     argument equal to <code>true</code> if the response is an HTTP
     *     redirect. If a content-encoding (e.g., gzip) is used, the method
     *     {@link Info#setEncoding(String)} must be called.  The input
     *     stream must then provide encoded data.
     *     All the other values can be set using the constructor
     *     {@link Info#Info(InputStream,long,String,String)}.
     *   <LI> In the second mode, one will create an instance of
     *     {@link Info} by calling the constructor
     *     {@link Info#Info(RequestInfo)}, and then using several methods
     *     to create a response:
     *     <UL>
     *        <LI> {@link Info#setHeader(String,String)} or
     *          {@link Info#addHeader(String,String)} as many times
     *          as needed to create most response headers.
     *        <LI> {@link Info#sendResponseHeaders(int,long)} to create the
     *          content-length header and to set the response code
     *          (this should be done after the other headers were
     *          created).
     *        <LI> {@link Info#getOutputStream()} to get the output stream
     *          used to write the response.  The length of the response must
     *          match the one provided in the (previous) call to
     *          {@link Info#sendResponseHeaders(int,long)}, and the stream
     *          should be closed when the response has been written.
     *     </UL>
     * </UL>
     * Generally, one mode or the other will be more convenient for
     * programming. The first mode is preferred in a simple GET request
     * where the response data is going to be read from an input stream.
     * Additional {@link Info} methods are used by {@link FileHandler}
     * and typically not by subclasses of {@link WebMap}.
     * When compatibility with servlets is desired, the second mode should
     * be used, and the methods used should be those that are specified
     * by the interface {@link HttpServerResponse}.
     */
    public static class Info implements HttpServerResponse {
	static final String IDENTITY = "identity";
	long length;
	InputStream is = null;
	String mimeType;
	String location;
	boolean redirect = false;
	boolean encoded = false;
	String encoding = IDENTITY;
	HttpMethod requestMethod = null;

	/**
	 * Specify if the response is an HTTP redirect.
	 * The default value is false. The value set is ignored if
	 * the constructor {@link Info(WebMap.RequestInfo)} was used so
	 * that {@link #sendResponseHeaders(int,long)} sets the response
	 * status code.
	 * @param value true if the response is an HTTP redirect;
	 *              false otherwise
	 */
	public void setRedirect(boolean value) {
	    redirect = value;
	}
	/**
	 * Indicate if a response is an HTTP redirect.
	 * The value returned is ignored if the constructor
	 * {@link Info(WebMap.RequestInfo)} was used so
	 * that {@link #sendResponseHeaders(int,long)} sets the response
	 * status code.
	 * @return true if the response is a redirect; false otherwise.
	 */
	public boolean getRedirect() {return redirect;}

	/**
	 * Set whether or not a response uses compression or some other
	 * transfer encoding.
	 * @param encoding the encoding ("identity", "gzip", "compress", and
	 *        "deflate" are standard values); null defaults to "identity"
	 */
	@Override
	public void setEncoding(String encoding) {
	    if (encoding == null) encoding = IDENTITY;
	    encoding = encoding.trim().toLowerCase();
	    if (encoding == null) {
		encoded = false;
		this.encoding = IDENTITY;
	    } else {
		encoded = !encoding.equals(IDENTITY);
		this.encoding = encoding;
	    }
	}

	/**
	 * Indicate if a response uses compression.
	 * @return true if the response uses gzip compression; false otherwise
	 */
	public boolean getEncoded() {
	    return encoded;
	}

	/**
	 * Get the current encoding.
	 * @return the current encoding; null if there is none defined.
	 */
	public String getEncoding() {
	    return encoding;
	}

	/**
	 * Constructor.
	 * For an HTTP redirect, the location must not be null,
	 * the length should be -1, the input stream should be null, and
	 * {@link #setRedirect(boolean)} must be called.
	 * @param is an input stream provided an HTML resource
	 * @param length the number of bytes in the resource; 0 if unknown
	 *        and -1 if no response data will be sent
	 * @param mimeType the media type of the resource
	 * @param location the location for an HTTP redirect; null if there
	 *        is none
	 */
	public Info(InputStream is, long length,
		    String mimeType, String location) {
	    this.is = is;
	    this.length = length;
	    if (mimeType != null) {
		this.mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);
		this.mimeType = this.mimeType.replaceAll("[ \t]+", "");
	    }
	    this.location = location;
	}

	HttpExchange exchange = null;
	Headers headers = null;
	HeaderOps headerops = null;

	/**
	 * Constructor given a {@link RequestInfo} instance.
	 * @param rinfo a {@link RequestInfo} used to complete
	 *        an HTTP transaction
	 */
	public Info(RequestInfo rinfo) {
	    exchange = rinfo.exchange;
	    HttpMethod requestMethod =
		HttpMethod.forName(exchange.getRequestMethod());
	    headers = exchange.getResponseHeaders();
	    headerops = asHeaderOps(headers);
	}

	/**
	 * Set the value of a response header that has a single value.
	 * @param key the key for the header
	 * @param value the value for the header
	 * @exception IllegalStateException this object's constructor's
	 *        argument was not an instance of WebMap.RequestInfo
	 */
	@Override
	public void setHeader(String key, String value)
	    throws IllegalStateException
	{
	    if (exchange == null) {
		throw new IllegalStateException(errorMsg("notExchange"));
	    }
	    key = key.trim();
	    value = value.trim();
	    if (key.equalsIgnoreCase("content-encoding")) {
		if (value.equalsIgnoreCase("identity")) {
		    encoded = false;
		    encoding = IDENTITY;
		} else {
		    encoded = true;
		    encoding = value;
		}
	    }
	    headers.set(key, value);
	}

	/**
	 * Add a response header.
	 * A new header will be added in addition to any headers with
	 * the same name.
	 * @param key the key for the header
	 * @param value a value to add to the header
	 * @exception IllegalStateException this object's constructor's
	 *        argument was not an instance of WebMap.RequestInfo
	 */
	@Override
	public void addHeader(String key, String value)
	    throws IllegalStateException
	{
	    if (exchange == null) {
		throw new IllegalStateException(errorMsg("notExchange"));
	    }
	    key = key.trim();
	    value = value.trim();
	    headers.add(key, value);
	}

	@Override
	public String encodeURL(String url) {return url;}

	@Override
	public String encodeRedirectURL(String url) {return url;}

	@Override
	public Collection<String> getHeaders(String name)
	    throws IllegalStateException
	{
	    return headers.get(name);
	}

	@Override
	public Collection<String> getHeaderNames()
	    throws IllegalStateException
	{
	    return headers.keySet();
	}

	@Override
	public boolean containsHeader(String name)
	    throws IllegalStateException
	 {
	    return headers.containsKey(name);
	}

	@Override
	public void addCookie(ServerCookie cookie)
	    throws IllegalStateException
	 {
	    cookie.addToHeaders(headerops);
	}


	@Override
	public void sendRedirect(String location)
	    throws IllegalStateException
	 {
	    if (exchange == null) {
		throw new IllegalStateException(errorMsg("notExchange"));
	    }
	    headers.set("Location", location);
	    redirect = true;
	    sendResponseHeaders(302, -1);
	}

	@Override
	public void sendError(int rc)
	    throws IllegalStateException
	 {
	    sendResponseHeaders(rc, -1);
	}

	private boolean sentResponse = false;

	/**
	 * Set the response code and content length.
	 * If the length is 0, indicating that the length is not known,
	 * chunked transfer encoding is automatically used. As a result,
	 * one should generally avoid the explicit use of the
	 * transfer-encoding header.
	 * @exception IllegalStateException this object's constructor's
	 *        argument was not an instance of WebMap.RequestInfo
	 * @param code the response code
	 * @param length the length of the response; 0 if the length
	 *        is not known, and -1 if there is no response.
	 */
	@Override
	public void sendResponseHeaders(int code, long length)
	    throws IllegalStateException
	{
	    if (exchange == null) {
		throw new IllegalStateException(errorMsg("notExchange"));
	    }
	    try {
		FileHandler.sendResponseHeaders(exchange, code, length);
		/*
		if (getEncoded() && !headers.containsKey("content-encoding")) {
		    headers.set("content-encoding", getEncoding());
		}
		EjwsSession session = (EjwsSession)
		    exchange.getAttribute("org.bzdev.ejws.session");
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
		    cookie.setDomain(exchange.getHttpContext()
				     .getServer()
				     .getAddress()
				     .getHostString());
		    cookie.addToHeaders(headerops);
		}
		exchange.sendResponseHeaders(code, length);
		*/
	    } catch (IOException e) {}
	    sentResponse = true;
	}

	@Override
	public boolean isCommitted() {
	    return sentResponse = true;
	}

	OutputStream os = null;

	/**
	 * Get the output stream for responses.
	 * <P>
	 * If the request method was HEAD, everything written to this
	 * stream will be discarded.
	 * @return the output stream.
	 * @exception IllegalStateException this object's constructor's
	 *        argument was not an instance of WebMap.RequestInfo
	 * @exception IOException an IO error occurred
	 */
	@Override
	public OutputStream getOutputStream()
	    throws IllegalStateException, IOException
	{
	    if (exchange == null) {
		throw new IllegalStateException(errorMsg("notExchange"));
	    }
	    if (requestMethod == HttpMethod.HEAD) {
		os = OutputStream.nullOutputStream();
	    } else {
		os = exchange.getResponseBody();
	    }
	    return os;
	}

	/**
	 * Check if the response was generated and sent.
	 * @return true if the response was sent; false otherwise
	 */
	public boolean handledResponse() {
	    return is == null && exchange != null && sentResponse;
	}

	/**
	 * Get the length of a resource.
	 * @return the resource length; 0 if unknown and -1 if no response
	 *         is sent.
	 */
	public long getLength() {return length;}
	/**
	 * Get an input stream to read a resource
	 * @return an input stream; null if not found or if an HTTP
	 *         redirect should be used instead or if
	 *         {@link #handledResponse()} would return true
	 */
	public InputStream getInputStream()
	 {
	     return is;
	 }

	/**
	 * Get the MIME type associated with a resource.
	 * @return the MIME type; null if unknown
	 */
	public String getMIMEType() {return mimeType;}
	/**
	 * Get a location for an HTTP redirect.
	 * @return the location; null if there is none
	 */
	public String getLocation() {return location;}
    }

    /**
     * Create a new instance of a WebMap subclass given a class name.
     * @param root a parameter used by the subclass' constructor
     * @param className the name of the subclass
     * @return the new web map
     * @exception IOException an IO error occurred
     * @exception IllegalArgumentException an argument prevented this
     *            method from running
     */
    public static WebMap newInstance(Object root, String className)
	throws IOException, IllegalArgumentException {
	try {
	    Class<? extends WebMap> clasz =
		Class.forName(className).asSubclass(WebMap.class);
	    return newInstance(root, clasz);
	} catch (SecurityException e6) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e6);
	} catch (ClassNotFoundException e7) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e7);
	}

    }

    /**
     * Create a new instance of a WebMap subclass.
     * A constructor for the specified class we be invoked,
     * using <code>root</code> as its single argument.  Subclasses of
     * WebMap should define a constructor that takes an Object as its
     * sole argument.  Each subclass will specify details for the objects
     * the subclass' constructor accepts.
     * @param root a parameter used by the subclass' constructor
     * @param clasz the class of the new instance
     * @return the new web map
     * @exception IOException an IO error occurred
     * @exception IllegalArgumentException an argument prevented this
     *            method from running
     */
    public static WebMap newInstance(Object root,
					Class<? extends WebMap> clasz)
	throws IOException, IllegalArgumentException
    {
	try {
	    Constructor<? extends WebMap> constructor =
		clasz.getConstructor(Object.class);
	    return constructor.newInstance(root);
	} catch (InstantiationException e1) {
	    String msg = errorMsg("illegalArgCN", clasz.getName());
	    throw new IllegalArgumentException(msg, e1);
	} catch (IllegalAccessException e2) {
	    String msg = errorMsg("illegalArgCN", clasz.getName());
	    throw new IllegalArgumentException(msg, e2);
	} catch (IllegalArgumentException e3) {
	    String msg = errorMsg("illegalArgCN", clasz.getName());
	    throw new IllegalArgumentException(msg, e3);
	} catch (InvocationTargetException e4) {
	    Throwable e = e4.getTargetException();
	    if (e instanceof IOException) {
		throw (IOException) e;
	    } else if (e instanceof IllegalArgumentException) {
		throw (IllegalArgumentException) e;
	    } else {
		String msg = errorMsg("illegalArgument");
		throw new IllegalArgumentException(msg, e);
	    }
	} catch (NoSuchMethodException e5) {
	    String msg = errorMsg("illegalArgCN", clasz.getName());
	    throw new IllegalArgumentException(msg, e5);
	} catch (SecurityException e6) {
	    String msg = errorMsg("illegalArgCN", clasz.getName());
	    throw new IllegalArgumentException(msg, e6);
	}
    }

    /**
     * Get the WebMap.Info object for the WEB-INF/web.xml file associated
     * with this instance of WebMap.
     * This method is called when a constructor for {@link FileHandler}
     * is executing.
     * @return an instance of Info containing an input stream for reading
     *         the web.xml file; null if none exists.
     * @exception IOException an IO error occurred.
     */
    public Info getWebxml() throws IOException {
	try {
	    return getInfoFromPath("WEB-INF/web.xml");
	} catch (EjwsException e) {
	    return null;
	}
    }

    /**
     * Determine if a welcome-file has been defined for this
     * web map.
     * @return true if a welcome file has been defined; false otherwise
     */
    public boolean welcomeInfoAvailable() {
	return !welcomeList.isEmpty();
    }

    /**
     * Find a "welcome' file.
     * A welcome file is defined in the JSP Web Archive specification,
     * and specifies a default file to use for a web site, so that one
     * is not dependent on server-specific conventions such as naming
     * a 'welcome' file index.html. The method
     * {@link #addWelcome(String)} adds possible welcome file locations.
     * @return an instance of Info containing an input stream for reading
     *         the "welcome" file; null if none exists.
     * @exception IOException an IO error occurred
     */
    public Info getWelcomeInfo() throws IOException {
	for (String path: welcomeList) {
	    if (path.startsWith("/")) {
		path = path.substring(1);
	    }
	    try {
		Info info = getInfoFromPath(path);
		if (info != null) {
		    return info;
		}
	    } catch (EjwsException e) {}
	}
	return null;
    }
    /**
     * Find a "welcome' file given a base directory path.
     * A welcome file is defined in the JSP Web Archive specification,
     * and specifies a default file to use for a web site, so that one
     * is not dependent on server-specific conventions such as naming
     * a 'welcome' file index.html. The method
     * {@link #addWelcome(String)} adds possible welcome file locations.
     * @param base the path (which must end in "/").
     * @return an instance of Info containing an input stream for reading
     *         the "welcome" file; null if none exists.
     * @exception IOException an IO error occurred
     */
    public Info getWelcomeInfo(String base) throws IOException {
	if (base == null || base.length() == 0) {
	    return getWelcomeInfo();
	}
	if (base.startsWith("/")) {
	    base = base.substring(1);
	}
	for (String path: welcomeList) {
	    if (path.startsWith("/")) {
		path = path.substring(1);
	    }
	    path = base + path;
	    try {
		Info info = getInfoFromPath(path);
		if (info != null) {
		    return info;
		}
	    } catch (EjwsException e) {}
	}
	return null;
    }


    private HashMap<Object,String> emap = new HashMap<Object,String>();

    /**
     * The mapping between an error type and a location
     * @return the map
     * @see #addErrorEntry(Object,String)
     */
    protected HashMap<Object,String> getEmap() {return emap;}

    /*
    static void copyStream(InputStream is, OutputStream os)
	throws IOException
    {
	byte[] buffer = new byte[4096];
	int len = 0;
	long total = 0;
	while ((len = is.read(buffer)) != -1) {
	    os.write(buffer, 0, len);
	    total += len;
	}
	os.flush();
    }
    */
    static String findPageAttribute(byte[] buf, int start, int end,
				    char[] attributeName) {
	boolean inString = false;
	while (start + attributeName.length < end) {
	    if (inString) {
		if (buf[start] == '\\' && buf[start+1] == '\"') {
		    start += 2;
		    continue;
		} else if (buf[start] == '"') {
		    inString = false;
		    start++;
		} else {
		    start++;
		    continue;
		}
	    }
	    if (buf[start] == '"') {
		inString = true;
		start++;
		continue;
	    }
	    boolean match = true;
	    for (int i = 0; i < attributeName.length; i++) {
		if (((int)buf[start+i] & 0xff) != attributeName[i]) {
		    match = false;
		    break;
		}
	    }
	    if (match) {
		start +=attributeName.length;
		while (start < end) {
		    if (buf[start] == ' ' || buf[start] == '\t'
			|| buf[start] == '\n' || buf[start] == '\r') {
			start++;
		    } else {
			break;
		    }
		}
		if (start < end &&  buf[start] == '=') {
		    start++;
		    break;
		} else {
		    continue;
		}
	    } else {
		start++;
		continue;
	    }
	}
	while (start < end) {
	    if (buf[start] == ' ' || buf[start] == '\t'
		|| buf[start] == '\n' || buf[start] == '\r') {
		start++;
	    } else {
		break;
	    }
	}
	if (buf[start] != '"') return null;
	start++;
	int i = start;
	while (i < end) {
	    if (i+1 < end && buf[i] == '\"' && buf[i+1] == '"') {
		start += 2;
		continue;
	    } else if (buf[i] == '"') {
		String result;
		try {
		    result = new String(buf, start, i-start, "US-ASCII");
		    return result.trim();
		} catch (UnsupportedEncodingException uee){
		    return null;
		}
	    } else {
		i++;
	    }
	}
	return null;
    }

    static String findPageAttribute(byte[] buf, String attr) {
	char[] attributeName = attr.toCharArray();
	for (int i = 0; i < buf.length; i++) {
	    if (buf[i] == '<' && i + 2 < buf.length
		&& buf[i+1] == '%' && buf[i+2] == '@') {
		i += 3;
		while (i < buf.length) {
		    if (buf[i] == ' ' || buf[i] == '\t'
			|| buf[i] == '\n' || buf[i] == '\r') {
			i++;
		    } else {
			break;
		    }
		}
		int end = i;
		while (end + 1 < buf.length ) {
		    if (buf[end] == '%' && buf[end+1] == '>') {
			if (i+4 < end && buf[i] != 'p' && buf[i+1] != 'a' &&
			    buf[i+2] != 'g' && buf[i+3] != 'e' &&
			    !(Character.isWhitespace((char)buf[i+4])
			      || buf[i+4] == '%')) {
			    i = end;
			    break;
			}
			String result = findPageAttribute(buf, i, end,
							  attributeName);
			if (result == null) {
			    i = end;
			} else {
			    return result.trim();
			}
		    }
		    end++;
		}
	    }
	}
	return null;
    }

    static String findContentType(byte[] buf) {
	return findPageAttribute(buf, "contentType");
    }

    static String findPageEncoding(byte[] buf) {
	return findPageAttribute(buf, "pageEncoding")
	    .toUpperCase(Locale.ENGLISH);
    }

    static String getEncodingFromContentType(String contentType) {
	contentType = contentType.toLowerCase(Locale.ENGLISH);
	int ind = contentType.indexOf(";");
	String tail = null;
	if (ind != -1) {
	    tail = contentType.substring(ind).trim();
	}
	while (tail != null) {
	    if (tail.matches("charset[ \t]*=")) {
		tail = tail.substring(7).trim();
		tail = tail.substring(1).trim();
		if (tail.charAt(0) == '\"') {
		    ind = tail.indexOf("'");
		    return tail.substring(0, ind).trim()
			.toUpperCase(Locale.ENGLISH);
		} else if (tail.charAt(0) == '"') {
		    return tail.substring(0, ind).trim()
			.toUpperCase(Locale.ENGLISH);
		} else {
		    ind = tail.indexOf(";");
		    tail = tail.substring(ind).trim();
		}
	    } else {
		ind = tail.indexOf(";");
		if (ind == -1) continue;
		tail = tail.substring(ind).trim();
	    }
	}
	return null;
    }

    static int getErrorStreamOffset(byte[] buf) {
	int start = 0;
	while (start < buf.length &&
	       Character.isWhitespace((char)((int)buf[start] & 0xff)))
	    start++;

	if (start+2 < buf.length &&
	    buf[start] == '<' && buf[start+1] == '%' && buf[start+2] == '@') {
	    boolean inString = false;
	    while (start < buf.length) {
		if (inString) {
		    if (start+1 < buf.length && buf[start] == '\\' &&
			(buf[start+1] == '"' || buf[start+1] == '\\')) {
			start += 2;
			continue;
		    } else if (buf[start] == '"') {
			inString = false;
			start++;
			continue;
		    }
		}
		if (buf[start] == '"') {
		    start++;
		    inString = true;
		}
		if (start +1 < buf.length &&
		    buf[start] == '%' && buf[start+1] == '>') {
		    start += 2;
		    return start;
		}
		start++;
	    }
	    return 0;
	} else {
	    return 0;
	}
    }

    /**
     * Convenience method for obtaining an Info object for a resource
     * given a path.
     * The prepath is assumed to be null (indicating "/" as the root),
     * and the query and fragment portions are assumed to be null.
     * This method just calls getInfoFromPath(null, path, null, null).
     * A prepath is typically the path associated with an
     * HTTP Context - the portion of the path that determines which
     * HTTP handler to use. The remainder is the portion of the path that
     * used as a key to find an actual resource: i.e., the path relative
     * to the root object used to create the web map.
     * @param path the path portion of a URI
     * @return an Info specifying
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    protected Info getInfoFromPath(String path)
	throws IOException, EjwsException
    {
	return getInfoFromPath(null, path, null, null, null);
    }

    /**
     * Get an Info object for a resource
     * The prepath argument is typically the path associated with an
     * HTTP Context - the portion of the path that determines which
     * HTTP handler to use. The remainder is the portion of the path that
     * used as a key to find an actual resource: i.e., the path relative
     * to the root object used to create the web map.
     * <P>
     * Note: This method does not determine compression
     * @param prepath the initial portion of the request URI (the root
     *        URI path - or prefix - for the corresponding file
     *        handler)
     * @param path the remainder of the path portion of a URI (the part
     *        that may differ between URL's passed to this object)
     * @param query the query portion of a URL
     * @param fragment the fragment portion of a URI
     * @param requestInfo an object encapsulating request data
     *        (headers, input streams, etc.)
     * @return an Info object either describing properties of a resource
     *         (e.g., headers, content-length, and a response code)
     *         and providing an input stream for that resource or
     *         handling a request directly; null if the requested resource
     *         could not be found and the caller should handle the error
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception occurred
     */
    protected abstract Info getInfoFromPath(String prepath, String path,
					    String query, String fragment,
					    RequestInfo requestInfo)
	throws IOException, EjwsException;

    /**
     * Get Info for an error document via a convenience method.
     * @param code the HTTP response code
     * @param protocol the protocol (HTTP or HTTPS) for the request
     * @param t the HTTP exchange initiating the request
     * @return an Info object describing properties of a resource
     *         (e.g., headers, content-length, and the response code
     *         for an HTML page describing an error) and providing an
     *         input stream for that resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getErrorInfo(int code, String protocol, HttpExchange t)
	throws IOException, EjwsException {
	return getErrorInfo(code, null, protocol, t);
    }

    /**
     * Get Info for an error document.
     * @param code the HTTP response code
     * @param key an object (e.g., a Throwable) describing the error
     * @param protocol the protocol (http or https) for the request
     * @param t the HTTP exchange initiating the request
     * @return an Info object describing properties of a resource
     *         (e.g., headers, content-length, and the response code
     *         for an HTML page describing an error) and providing an
     *         input stream for that resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getErrorInfo(int code, Object key,
			     String protocol, HttpExchange t)
	throws IOException, EjwsException
    {
	URI uri = t.getRequestURI();
	String host = t.getRequestHeaders().getFirst("Host");
	String uriString = protocol +"://" + host + uri.toString();
	TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	kmap.put("pageContext.errorData.requestURI", uriString);
	kmap.put("pageContext.errorData.statusCode", "" + code);
	if (key instanceof Throwable) {
	    kmap.put("pageContext.errorData.throwable", key.toString());
	}
	final String errFile = (key instanceof Throwable)?
	    "error2.jsp": "error.jsp";
	TemplateProcessor tp = new TemplateProcessor(kmap);
	tp.setDelimiter('{');
	// System.out.println ("root = " + root);
	String epath = emap.get(Integer.valueOf(code));
	// System.out.println("epath = " + epath);
	boolean useDefault = false;
	boolean canRead = false;
	File f = null;
	long length = -1;
	InputStream is = null;
	String mimeType = null;
	String sepath = epath;
	Info rawInfo = null;
	if (epath == null) {
	    // return new Info(null, -1, null);
	    useDefault = true;
	    /*
	    is = ClassLoader
		.getSystemResourceAsStream("org/bzdev/ejws/error.jsp");
	    */
	    is = AccessController.doPrivileged
		(new PrivilegedAction<InputStream>() {
			public InputStream run() {
			    return WebMap.class.getResourceAsStream
			    (errFile);
			};
		});
	} else {
	    if (epath.startsWith("/")) {
		epath = epath.substring(1);
	    } else {
		sepath = "/" + epath;
	    }
	    rawInfo = getInfoFromPath(epath);
	    if (rawInfo != null) {
		canRead = true;
		is = rawInfo.getInputStream();
		length = rawInfo.getLength();
		mimeType = rawInfo.getMIMEType();
		if (is == null) {
		    System.err.println("ejws found null input stream "
				      + " but non-null getInfoFromPath for "
				       + epath);
		}
	    } else {
		useDefault = true;
		/*
		is = ClassLoader
		    .getSystemResourceAsStream("org/bzdev/ejws/error.jsp");
		*/
		is = AccessController.doPrivileged
		    (new PrivilegedAction<InputStream>() {
			    public InputStream run() {
				return WebMap.class.getResourceAsStream
				("error.jsp");
			    };
			});
		if (is == null) {
		    System.err.println("ejws failed to read "
				       + "resource error.jsp");
		}
	    }
	}
	if (useDefault || canRead) {
	    String eMimeType = useDefault? "application/jsp": mimeType;
	    // System.out.println("eMimeType = \"" + eMimeType + "\"");
	    if (useDefault || eMimeType.equals("application/jsp") ||
		eMimeType.startsWith("application/jsp;")) {
		// System.out.println("JSP processing for error page ...");
		ByteArrayOutputStream baos;
		if (useDefault == false) {
		    baos = new ByteArrayOutputStream((int)length);
		    // copyStream(is, baos);
		    is.transferTo(baos);
		    is.close();
		} else {
		    baos = new ByteArrayOutputStream(1024);
		    // copyStream(is,baos);
		    is.transferTo(baos);
		    is.close();
		}
		// System.out.println("parsing page directive");
		byte[] buf = baos.toByteArray();
		String contentType = findContentType(buf);
		// System.out.println(contentType);
		if (contentType == null) {
		    contentType = getContentTypeFromURL(sepath);
		    // System.out.println(contentType);
		}
		// System.out.println("getting encoding");
		String encoding = getEncodingFromContentType(contentType);
		// System.out.println(encoding);
		if (encoding == null) {
		    encoding = findPageEncoding(buf);
		    // System.out.println(encoding);
		}
		if (encoding == null) {
		    encoding = getEncodingFromURL(sepath);
		    // System.out.println(encoding);
		}
		// System.out.println("generating actual MIME type");
		try {
		    if (encoding != null
			&& !contentType.matches(".*;[ \t]*charset[ \t=].*")) {
			mimeType = contentType +"; charset=\"" + encoding
			    +"\"";
		    } else {
			mimeType = contentType;
		    }
		} catch (Exception mte) {
		    mte.printStackTrace();
		    mimeType = null;
		    System.exit(1);
		}
		/*
		System.out.println("ct = " + contentType
				   + ", encoding = " + encoding
				   + ", mimeType = " + mimeType);
		*/
		int blen = (useDefault? 1024: 4*(int)length)
		    + 32 + 8 * uriString.length();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(blen);
		OutputStreamWriter writer = new
		    OutputStreamWriter(bos, encoding);
		int offset = getErrorStreamOffset(buf);
		/*
		System.out.println("error stream offset = " +offset
				   + ", buf.length = " + buf.length);
		*/
		try {
		    tp.processTemplate
			(new InputStreamReader
			 ((new ByteArrayInputStream(buf,
						    offset,
						    buf.length
						    -offset)),
			  encoding),
			 blen, writer);
		    writer.flush();
		} catch (Exception eee) {
		    eee.printStackTrace();
		}
		byte[] buf2 = bos.toByteArray();
		/*
		System.out.println("bos.size() = " + bos.size());
		for (int j = 0; j < buf2.length; j++ ) {
		    System.out.print((char)(((int)buf2[j]) & 0xff));
		}
		System.out.println();
		*/
		writer.close();
		return new Info(new
				ByteArrayInputStream(buf2, 0, bos.size()),
				bos.size(),
				mimeType,
				"");
	    } else {
		return new Info(is, length, eMimeType, "");
	    }
	}
	return null;
    }

    /**
     * Get Info for an error document given the class name of an exception.
     * @param exceptionType the class name of an exception
     * @return an Info object describing properties of a resource
     *         (e.g., headers, content-length, and the response code
     *         for an HTML page describing an error) and providing an
     *         input stream for that resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getErrorInfo(String exceptionType)
	throws IOException, EjwsException
    {
	String epath = emap.get(exceptionType);
	if (epath != null && epath.startsWith("/")) {
	    epath = epath.substring(1);
	}
	return getInfoFromPath(epath);
    }

    private boolean webInfHidden = true;

    /**
     * Set the webInfHidden flag.
     * When an HTTP request's URI's path starts with CONTEXT_PATH/WEB-INF,
     * where CONTEXT_PATH is the path associated with the current object's
     * FileHandler, getInfo by default returns null.  This method allows
     * the default behavior to be overridden.
     * @param value true if the WEB-INF directory should be hidden;
     *              false otherwise
     */
    public void setWebInfHidden(boolean value) {
	webInfHidden = value;
    }

    /**
     * Get the webInfHidden flag.
     * When an HTTP request's URI's path starts with CONTEXT_PATH/WEB-INF,
     * where CONTEXT_PATH is the path associated with the current object's
     * FileHandler, getInfo by default returns null, unless setWebInfHidden
     * was last called with a value of false.
     * @return true if the WEB-INF directory and its subdirectories and
     *         contents can be returned by a call got getInfo; false
     *         otherwise
     * @see #setWebInfHidden(boolean)
     */
    public boolean getWebInfHidden() {return webInfHidden;}


    /**
     * Get an Info object for a resource.
     * In this case, the 'prepath' argument that some other
     * getInfo methods provide is assumed to be null.
     * @param uri the URI naming a resource
     * @return an Info object describing properties of a resource an
     *         providing an input stream to the resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getInfo(URI uri) throws IOException, EjwsException {
	// System.out.println("getInfo(uri): uri = " + uri.toString());
	/*
	String scheme = uri.getScheme();
	String authority = uri.getAuthority();
	*/
	String path = uri.getPath();
	String query = uri.getRawQuery();
	String fragment = uri.getFragment();
	if (path.length() > 0 && path.startsWith("/")) {
	    path = path.substring(1);
	    // System.out.println(path);
	}
	if (webInfHidden && path.startsWith("WEB-INF") &&
	    ((path.length() == "WEB-INF".length())
	     || (path.charAt("WEB-INF".length()) == '/'))) {
	    return null;
	}
	return getInfoFromPath(null, path, query, fragment, null);
    }

    /**
     * Get an Info object for a request.
     * A prepath argument is typically the path associated with an
     * HTTP Context - the portion of the path that determines which
     * HTTP handler to use. The remainder is the portion of the path that
     * used as a key to find an actual resource: i.e., the path relative
     * to the root object used to create the web map.  This will be
     * removed from the request URI's path.  If the prepath argument does
     * not end in "/", a "/" will be automatically added.
     * @param prepath the initial portion of a path from the request URI
     * @param t the request-response object.
     * @return an Info object describing properties of a resource an
     *         providing an input stream to the resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getInfo(String prepath, HttpExchange t)
	throws IOException, EjwsException
    {
	URI uri = t.getRequestURI();
	String scheme = uri.getScheme();
	String authority = uri.getAuthority();
	String path = uri.getPath();
	String query = uri.getRawQuery();
	String fragment = uri.getFragment();
	String base1 = (prepath.endsWith("/"))? prepath: (prepath + "/");
	int plen = path.length();
	int base1Len = prepath.length();
	if (plen >= base1Len) {
	    path = path.substring(base1Len);
	} else {
	    path = path.substring(prepath.length());
	}

	if (path.length() > 0 && path.startsWith("/")) {
	    path = path.substring(1);
	    // System.out.println(path);
	}
	return getInfoFromPath(base1, path, query, fragment,
			       new RequestInfo(t));
    }

    /**
     * Get an Info object for a resource.
     * A prepath argument is typically the path associated with an
     * HTTP Context - the portion of the path that determines which
     * HTTP handler to use. The remainder is the portion of the path that
     * used as a key to find an actual resource: i.e., the path relative
     * to the root object used to create the web map.  This will be
     * removed from the request URI's path.  If the prepath argument does
     * not end in "/", a "/" will be automatically added.
     * @param prepath the initial portion of a path from the request URI
     * @param uri the request URI naming a resource
     * @return an Info object describing properties of a resource an
     *         providing an input stream to the resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getInfo(String prepath, URI uri)
	throws IOException, EjwsException
    {
	// System.out.println("getInfo(uri): uri = " + uri.toString());
	String scheme = uri.getScheme();
	String authority = uri.getAuthority();
	String path = uri.getPath();
	String query = uri.getRawQuery();
	String fragment = uri.getFragment();
	String base1 = (prepath.endsWith("/"))? prepath: (prepath + "/");
	int plen = path.length();
	int base1Len = prepath.length();
	if (plen >= base1Len) {
	    path = path.substring(base1Len);
	} else {
	    path = path.substring(prepath.length());
	}

	if (path.length() > 0 && path.startsWith("/")) {
	    path = path.substring(1);
	    // System.out.println(path);
	}
	return getInfoFromPath(base1, path, query, fragment, null);
    }


    /**
     * Get an Info object for a resource given a path.
     * This method calls getInfo(null, path, null, null) after
     * stripping any leading "/" from the path.
     * @param path the path in a URI requesting a resource
     * @return an Info object describing properties of a resource an
     *         providing an input stream to the resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getInfo (String path) throws IOException, EjwsException {
	if (path.length() > 0 && path.startsWith("/")) {
	    path = path.substring(1);
	    // System.out.println(path);
	}
	// String prepath = null;
	return getInfoFromPath(null, path, null, null, null);
    }

    /**
     * Get an Info object for a resource given a path using a prepath.
     * This method calls getInfoFromPath(prepath, path, null, null)
     * after stripping off any leading "/" from path.
     * @param prepath the initial portion of a path from the request URI
     * @param path the path in a URI requesting a resource
     * @return an Info object describing properties of a resource an
     *         providing an input stream to the resource
     * @exception IOException an IO error occurred.
     * @exception EjwsException an ejws exception, which may be thrown
     *             by getInfoFromPath, occurred
     * @see #getInfoFromPath(String,String,String,String,WebMap.RequestInfo)
     */
    public Info getInfo (String prepath, String path)
	throws IOException, EjwsException
    {
	if (path.length() > 0 && path.startsWith("/")) {
	    path = path.substring(1);
	    // System.out.println(path);
	}
	return getInfoFromPath(prepath, path, null, null, null);
    }


    /**
     * Add a path to the list of paths to 'welcome' files/resources.
     * This method is called by {@link org.bzdev.ejws.WebxmlParser} so
     * when a WEB-INF/web.xml file exists, users or subclasses would
     * generally not use this method explicitly. Note that, when a
     * WEB-INF/web.xml file exists, a WebxmlParser will be created and
     * called by a FileHandler's constructor.
     * @param path the path to add
     */
    public void addWelcome(String path) {
	welcomeList.add(path);

    }

    private boolean isGZipped(String path) {
	int index = path.lastIndexOf(".");
	String suffix;
	if (index == -1) {
	    return false;
	} else if (index == path.length()-1) {
	    return false;
	} else {
	    suffix = path.substring(index+1);
	    if (suffix.length() == 0) return false;
	}
	if (suffixForGZip.contains(suffix)
	    || suffixForGZipDefault.contains(suffix)) {
	    return true;
	}
	return false;
    }

    /**
     * Remove a GZIP suffix from a path.
     * The default provides .gz as the suffix.
     * @param path the path
     * @return the path without the suffix
     * @see #addGzipSuffix(String)
     */
    public String stripGZipSuffix(String path) {
	int index = path.lastIndexOf(".");
	String suffix;
	if (index == -1) {
	    return path;
	} else if (index == path.length()-1) {
	    return path;
	} else {
	    suffix = path.substring(index+1);
	    if (suffix.length() == 0) return path;
	}
	if (suffixForGZip.contains(suffix)
	    || suffixForGZipDefault.contains(suffix)) {
	    return path.substring(0, index);
	}
	return path;
    }

    /**
     * Get the MIME type for a resource given its path.
     * The default implementation bases the mime type on a file
     * suffix.
     * @param path the path to the resource
     * @return the resource's MIME type; application/octet-stream if the
     *         MIME type cannot be definitively determined.
     */
    public String getMimeType(String path) {
	if (isGZipped(path)) return "application/octet-stream";
	String suffix = null;
	int index = path.lastIndexOf(".");
	if (index != -1) {
	    index++;
	    if (index < path.length() - 1) {
		suffix = path.substring(index);
	    }
	}
	String mtype = (suffix == null)? "application/octet-stream":
	    suffixToMimeType.get(suffix);
	if (mtype == null) {
	    mtype = suffixToMTypeDefault.get(suffix);
	}
	if (mtype == null) {
	    mtype = "application/octet-stream";
	}
	return mtype;
    }

    /**
     * Add a mapping between a file-name/path suffix and a MIME type.
     * The suffix is the portion of a file name or path that follows the
     * last '.' in the last component of the path or file name.
     * This method is called by {@link org.bzdev.ejws.WebxmlParser} so
     * when a WEB-INF/web.xml file exists, users or subclasses would
     * generally not use this method explicitly.  Note that, when a
     * WEB-INF/web.xml file exists, a WebxmlParser will be created and
     * called by a FileHandler's constructor.
     * @param suffix the file name or path suffix
     * @param mtype the corresponding MIME type
     */
    public void addMapping(String suffix, String mtype) {
	suffixToMimeType.put(suffix, mtype);
    }

    /**
     * Add a file-name/path suffix that indicates the use of gzip compression.
     * @param suffix the suffix
     */
    public void addGzipSuffix(String suffix) {
	if (!suffixForGZipDefault.contains(suffix)) {
	    suffixForGZip.add(suffix);
	}
    }

    /**
     * Create an iterable that will provide a sequence of paths
     * that are modified by adding a suffix indicating gzip compression.
     * @param path the original path
     * @return an iterable providing a sequence of modified paths.
     */
    public Iterable<String> gzipPaths(String path) {
	if (isGZipped(path)) {
	    return new Iterable<String>() {
		public Iterator<String> iterator() {
		    return Collections.emptyListIterator();
		}
	    };
	}
	CollectionScanner<String> scanner = new CollectionScanner<>();
	scanner.add(suffixForGZip);
	scanner.add(suffixForGZipDefault);
	return new Iterable<String>() {
	    public Iterator<String> iterator() {
		return new EncapsulatingIterator<String,String>
		    (scanner.iterator()) {
		    public String next() {
			String suffix = encapsulatedNext();
			return path + "." + suffix;
		    }
		};
	    }
	};
    }

    /**
     * Add a mapping between an error type and the location of the
     * resource describing the error.
     * A key can be either an Integer whose value is an HTTP error code or
     * a String giving the  class name of an Exception or
     * Throwable as returned by that object's class' method getName().
     * The location will be resolved against the root for this
     * web map to find the actual resource.
     * This method is called by {@link org.bzdev.ejws.WebxmlParser} so
     * when a WEB-INF/web.xml file exists, users or subclasses would
     * generally not use this method explicitly.  Note that, when a
     * WEB-INF/web.xml file exists, a WebxmlParser will be created and
     * called by a FileHandler's constructor.
     * @param key an object indicating the error type
     * @param location the path for the resource describing the error
     */
    public void addErrorEntry(Object key, String location) {
	emap.put(key, location);
    }

    /*
     *
    public void addErrorException(String exception, String location) {
	emap.put(exception, location);
    }
     */

    static class URLMatcher {
	Pattern pattern;
	int len;
	int wcLoc;		// -1 none; 0 middle, 1 end; 2 start
	String encoding;
	boolean isXml;
	URLMatcher(Pattern p, int l, int wcl, String enc, boolean isxml ) {
	    pattern = p;
	    len = l;
	    wcLoc = wcl;
	    encoding = enc;
	    isXml = isxml;
	}
    }

    LinkedList<URLMatcher> urlMatcherList = new LinkedList<URLMatcher>();


    /**
     * Add page encoding data for JSP pages.
     * This method creates a list of URL patterns for JSP pages
     * that define the text encoding to use for pages that that match
     * the pattern.
     * This method is called by {@link org.bzdev.ejws.WebxmlParser} so
     * when a WEB-INF/web.xml file exists, users or subclasses would
     * generally not use this method explicitly.  Note that, when a
     * WEB-INF/web.xml file exists, a WebxmlParser will be created and
     * called by a FileHandler's constructor.
     * @param pattern a URL pattern for the path component of a URL as
     *         described in the servlet specification
     * @param encoding the character encoding to use
     * @param isxml true if the JSP page produces an XML document; false
     *        for an HTML document
     */
    public void addPageEncoding(String pattern, String encoding,
				boolean isxml) {
	pattern = pattern.replaceAll("\\*+", "*");
	int len = 0;
	int wcl = -1;
	if (pattern.endsWith("*")) {
	    wcl = 1;
	} else if (pattern.startsWith("*")) {
	    wcl = 2;
	} else if (pattern.contains("*")) {
	    wcl = 0;
	}
	if (wcl != -1) {
	    for (String s: pattern.split("\\*")) {
		len += s.length();
	    }
	} else {
	    len = pattern.length();
	}
	pattern = pattern.replaceAll(Pattern.quote("*"),".*");
	Pattern cpattern = Pattern.compile(pattern);
	urlMatcherList.add(new URLMatcher(cpattern, len, wcl, encoding, isxml));
    }

    /**
     * Add page encoding data for JSP pages given a list of patterns.
     * @param patterns a list of URL patterns for the path component of a URL as
     *         described in the servlet specification
     * @param encoding the character encoding to use
     * @param isxml true if the JSP page produces an XML document; false
     *        for an HTML document
     */
    public void addPageEncoding(LinkedList<String> patterns, String encoding,
				boolean isxml) {
	for (String pattern: patterns) {
	    addPageEncoding(pattern, encoding, isxml);
	}
    }

    /**
     * Get the content type for a JSP page given its path.
     * @param urlString the path to the JSP page
     * @return "application/xml" or "text/html"
     */
    public String getContentTypeFromURL(String urlString) {
	int maxlength = -1;
	int wcLoc = 3;
	String result = "text/html";
	for (URLMatcher m: urlMatcherList) {
	    if (m.pattern.matcher(urlString).matches()) {
		int newlen = m.len;
		boolean found = false;
		if (m.wcLoc < wcLoc) {
		    found = true;
		} else if (newlen > maxlength) {
		    found = true;
		}
		if (found) {
		    wcLoc = m.wcLoc;
		    maxlength = m.len;
		    result = m.isXml? "application/xml": "text/html";
		}
	    }
	}
	return result;
    }

    /**
     * Get the character encoding for a JSP page given its path.
     * @param urlString the path to the JSP page
     * @return the character encoding
     */
    public String getEncodingFromURL(String urlString) {
	int maxlength = -1;
	int wcLoc = 3;
	String result = null;
	for (URLMatcher m: urlMatcherList) {
	    if (m.pattern.matcher(urlString).matches()) {
		int newlen = m.len;
		boolean found = false;
		if (m.wcLoc < wcLoc) {
		    found = true;
		} else if (newlen > maxlength) {
		    found = true;
		}
		if (found) {
		    wcLoc = m.wcLoc;
		    maxlength = m.len;
		    result = m.encoding;
		}
	    }
	}
	return result;
    }

    /*
    static class OurHeaderOps extends Headers implements HeaderOps {
	Headers headers;
	public OurHeaderOps(Headers headers) {
	    this.headers = headers;
	}
    }
    */


    /**
     * Extract parameters from a header.
     * This method assumes single-valued headers and the normal
     * header syntax.
     * @param headers the headers
     * @param key the name of the header (e.g., "content-type")
     * @param parameter the name of a header parameter (e.g., "charset");
     *        null for the header without its parameters
     * @return the value with quotes and character escapes processed
     */
    public static String getFromHeader(HeaderOps headers,
				       String key,
				       String parameter)
    {
	if (headers == null || key == null) return null;
	Map<String,String> map = headers.parseFirst(key, false);
	if (map == null) return null;
	if (parameter == null) parameter = key;
	return map.get(parameter);
    }

    private boolean isConfigured = false;

    /**
     * Determine if this web map has been configured.
     * While {@link #configure()} is running, this method may return
     * true.
     * @return true if this web map is configured; false otherwise
     * @see #configure()
     * @see #deconfigure()
     */
    boolean isConfigured() {
	return isConfigured;
    }

    void setConfigured(boolean value) {
	isConfigured = value;
    }

    /**
     * Configure a web map.
     * Calling this method should initialize or obtain any resources
     * that the web map will use.
     * <P>
     * Subclasses implementing this method must call super.configure()
     * and should do that before performing any subclass-specific
     * actions.  If an exception occurs in the {@link #configure()}
     * method of a subclass, that exception will be handled by
     * {@link EmbeddedWebServer} by calling {@link #deconfigure}.
     * A consequence of this is that the subclass should track what
     * has actually been configured at each step so that the
     * configuration can be undone safely.
     * @exception Exception an error occurred
     * @see #deconfigure()
     * @see #isConfigured()
     */
    protected void configure() throws Exception {
    }

    /**
     * Deconfigure a web map.
     * Calling this method should release any resources that the
     * web map is using.
     * <P>
     * Subclasses implementing this method must call super.deconfigure()
     * and should do that after performing any subclass-specific actions.
     * @see #configure()
     * @see #isConfigured()
     */
    protected void deconfigure() {
    }
}

//  LocalWords:  exbundle URIs href xml WebMap ul li jsp config url
//  LocalWords:  getInfoFromPath HashMap html htm xhtml xhtm xht js
//  LocalWords:  javascript txt pdf ps svg ogg msword ppt xls odt odg
//  LocalWords:  odp getWebxml getWelcomeInfo getErrorInfo getInfo gz
//  LocalWords:  setRedirect boolean className illegalArgCN clasz css
//  LocalWords:  illegalArgument FileHandler addWelcome contentType
//  LocalWords:  pageEncoding charset prepath HTTPS Throwable epath
//  LocalWords:  eMimeType mimeType buf bos xff exceptionType URI's
//  LocalWords:  webInfHidden request's setWebInfHidden uri toString
//  LocalWords:  subdirectories getScheme getAuthority WebxmlParser
//  LocalWords:  FileHandler's mtype getName addErrorException emap
//  LocalWords:  servlet isxml urlString Subclasses RequestInfo xsl
//  LocalWords:  setMethods HttpMethod setAllowsQuery xslt gzip rinfo
//  LocalWords:  getDecodedInputStream ByteArrayBuffer IOException os
//  LocalWords:  getEncodedInputStream InputStream setEncoding ejws
//  LocalWords:  setHeader addToHeader sendResponse getOutputStream
//  LocalWords:  subclasses IllegalStateException notExchange http
//  LocalWords:  requestInfo https ClassLoader iterable octetstream
//  LocalWords:  getSystemResourceAsStream sendResponseHeaders len
//  LocalWords:  copyStream OutputStream baos HttpServerRequest UTF
//  LocalWords:  noSession headerops HeaderOps getFirst containsKey
//  LocalWords:  containsValue entrySet hashCode isEmpty keySet www
//  LocalWords:  putAll urlencoded addHeader HttpServerResponse getID
//  LocalWords:  getEncoded getEncoding EjwsSession getAttribute
//  LocalWords:  parseAll equalsIgnoreCase jsessionid ServerCookie
//  LocalWords:  newInstance setVersion setHttpOnly getServer URL's
//  LocalWords:  getAddress getHostString addToHeaders OurHeaderOps
//  LocalWords:  handledResponse deconfigure EmbeddedWebServer
//  LocalWords:  isConfigured
