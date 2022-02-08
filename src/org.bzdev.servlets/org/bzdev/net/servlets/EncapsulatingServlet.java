package org.bzdev.net.servlets;
import java.io.*;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.bzdev.net.ServerCookie;
import org.bzdev.net.ServletAdapter;
import org.bzdev.net.HttpServerRequest;
import org.bzdev.net.HttpServerResponse;
import org.bzdev.net.HttpMethod;

/**
 * Servlet implemented by encapsulating an instance of {@link ServletAdapter}.
 * Subclasses are expected to provide a constructor with no arguments
 * that creates an instance of {@link ServletAdapter} and passes it to
 * the constructor of this class.  For example,
 * <BLOCKQUOTE><CODE><PRE>
 *  public class OurServlet extends EncapsulatingServlet {
 *     public OurServlet() {
 *       super(new OurAdapter());
 *     }
 *  }
 * </PRE></CODE></BLOCKQUOTE>
 * <P>
 * To use this class, A JAR file containing the servlet API must be
 * on the classpath or accessible via the module path.  For releases
 * whose manifest does not contain an AUTOMATIC-MODULE-NAME declaration,
 * the file name must be servlet-api.jar.  This of course is subject to
 * change.
 * <P>
 * ServletAdapter states are handled internally by setting and getting
 * a session attribute named "state", so the attribute name "state" should
 * be treated a reserved name.  Servlet adapters use a subset of the
 * servlet API.  If more of the API is needed (e.g., for logins),
 * methods such as
 * {@link EncapsulatingServlet#doPost(HttpServletRequest,HttpServletResponse)}
 * should be overridden to add additional functionality.
 */
public abstract class EncapsulatingServlet extends HttpServlet {

    ServletAdapter adapter;

    /**
     * Constructor.
     * @param adapter the servlet adapter to encapsulate.
     */
    protected EncapsulatingServlet(ServletAdapter adapter) {
	this.adapter = adapter;
    }

    private HttpServerRequest serverRequest(final HttpServletRequest req) {
	return new HttpServerRequest() {

	    @Override
	    public Object getAttribute(String name) {
		return req.getAttribute(name);
	    }

	    @Override
	    public void setAttribute(String name, Object value) {
		req.setAttribute(name, value);
	    }

	    @Override
	    public Object getContextAttribute(String name) {
		return getServletContext().getAttribute(name);
	    }

	    @Override
	    public void setContextAttribute(String name, Object value){
		getServletContext().setAttribute(name, value);
	    }

	    @Override
	    public String getProtocol() {
		return req.getProtocol();
	    }

	    @Override
	    public ServerCookie[] getCookies() {
		Cookie[] icookies = req.getCookies();
		if (icookies == null) return null;
		ServerCookie[] ocookies = new ServerCookie[icookies.length];
		for (int i = 0; i < icookies.length; i++) {
		    final Cookie cookie = icookies[i];
		    ocookies[i] = new ServerCookie() {

			    @Override
			    public String getComment() {
				return cookie.getComment();
			    }

			    @Override
			    public String getDomain() {
				return cookie.getDomain();
			    }

			    @Override
			    public int getMaxAge() {
				return cookie.getMaxAge();
			    }

			    @Override
			    public String getName() {
				return cookie.getName();
			    }

			    @Override
			    public String getPath() {
				return cookie.getPath();
			    }

			    @Override
			    public boolean getSecure() {
				return cookie.getSecure();
			    }

			    @Override
			    public String getValue() {
				return cookie.getValue();
			    }

			    @Override
			    public int getVersion() {
				return cookie.getVersion();
			    }

			    @Override
			    public boolean isHttpOnly() {
				return cookie.isHttpOnly();
			    }

			    @Override
			    public void setComment(String comment) {
				cookie.setComment(comment);
			    }

			    @Override
			    public void setDomain(String domain) {
				cookie.setDomain(domain);
			    }

			    @Override
			    public void setMaxAge(int expire) {
				cookie.setMaxAge(expire);
			    }

			    @Override
			    public void setPath(String uri) {
				cookie.setPath(uri);
			    }

			    @Override
			    public void setSecure(boolean secure) {
				cookie.setSecure(secure);
			    }

			    @Override
			    public void setValue(String value) {
				cookie.setValue(value);
			    }

			    @Override
			    public void setVersion(int version) {
				cookie.setVersion(version);
			    }

			    @Override
			    public void setHttpOnly(boolean httpOnly) {
				cookie.setHttpOnly(httpOnly);
			    }
			};
		}
		return ocookies;
	    }

	    @Override
	    public Locale getLocale() {
		return req.getLocale();
	    }

	    @Override
	    public Enumeration<Locale> getLocales() {
		return req.getLocales();
	    }

	    @Override
	    public String getHeader(String type) {
		return req.getHeader(type);
	    }

	    @Override
	    public Enumeration<String> getHeaderNames() {
		return req.getHeaderNames();
	    }

	    @Override
	    public Enumeration<String> getHeaders(String type) {
		return req.getHeaders(type);
	    }

	    @Override
	    public HttpMethod getMethod() {
		return HttpMethod.forName(req.getMethod());
		
	    }

	    @Override
	    public String getContextPath() {
		return req.getContextPath();
	    }

	    @Override
	    public String getQueryString() {
		return req.getQueryString();
	    }

	    @Override
	    public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	    }

	    @Override
	    public String getRequestedSessionID() {
		// return req.getRequestedSessionId();
		// Servelet Adapters are assumed to always have
		// a session unless the server cannot create one
		// (which is not the case for a servlet)
		return req.getSession(true).getId();
	    }

	    @Override
	    public void setSessionState(Object state)
		throws IllegalStateException
	    {
		req.getSession(true).setAttribute("state", state);
	    }

	    public Object getSessionState() {
		return req.getSession(true).getAttribute("state");
	    }

	    @Override
	    public String changeSessionID() throws IllegalStateException {
		req.getSession(true);
		return req.changeSessionId();
	    }

	    @Override
	    public int getMaxInactiveInterval() {
		return req.getSession().getMaxInactiveInterval();
	    }

	    @Override
	    public boolean setMaxInactiveInterval(int interval) {
		req.getSession().setMaxInactiveInterval(interval);
		return true;
	    }

	    @Override
	    public String getRequestURI() {
		return req.getRequestURI();
	    }

	    @Override
	    public String getRequestURL() {
		return req.getRequestURL().toString(); 
	    }

	    @Override
	    public boolean isRequestedSessionIDValid() {
		return req.isRequestedSessionIdValid();
	    }

	    @Override
	    public boolean isSecure() {
		return req.isSecure();
	    }

	    @Override
	    public boolean isUserInRole(String role) {
		return req.isUserInRole(role);
	    }

	    @Override
	    public String getContentType() {
		return req.getContentType();
	    }

	    @Override
	    public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	    }

	    @Override
	    public int getContentLength() {
		return req.getContentLength();
	    }

	    @Override
	    public long getContentLengthLong() {
		return req.getContentLengthLong();
	    }

	    @Override
	    public Map<String,String[]> getParameterMap() {
		return req.getParameterMap();
	    }


	    boolean usedInputStream  = false;

	    @Override
	    public InputStream getEncodedInputStream() throws IOException {
		if (usedInputStream) return null;
		usedInputStream = true;
		return req.getInputStream();
	    }

	    @Override
	    public InputStream getDecodedInputStream() throws IOException {
		if (usedInputStream) return null;
		String encoding = req.getHeader("content-encoding");
		InputStream is = req.getInputStream();
		if (encoding.equalsIgnoreCase("identity")) {
		    // nothing to do
		} else if (encoding.equalsIgnoreCase("gzip")) {
		    is = new java.util.zip.GZIPInputStream(is);
		} else if (encoding.equalsIgnoreCase("deflate")) {
		    is = new java.util.zip.InflaterInputStream(is);
		} else {
	            is = null;
		}
		usedInputStream = true;
		// after adding some bookkeeping to track if we have a stream
		return is;
	    }
	};
    }

    private HttpServerResponse serverResponse(final HttpServletResponse res) {
	return new HttpServerResponse() {

	    @Override
	    public void addCookie(ServerCookie cookie)
		throws IllegalStateException
	    {
		Cookie c = new Cookie(cookie.getName(), cookie.getValue());
		c.setComment(cookie.getComment());
		c.setDomain(cookie.getDomain());
		c.setHttpOnly(cookie.isHttpOnly());
		c.setMaxAge(cookie.getMaxAge());
		c.setPath(cookie.getPath());
		c.setSecure(cookie.getSecure());
		c.setVersion(cookie.getVersion());
		res.addCookie(c);
	    }

	    @Override
	    public void addHeader(String name, String value)
		throws IllegalStateException
	    {
		res.addHeader(name, value);
	    }
    
	    @Override
	    public boolean containsHeader(String name)
		throws IllegalStateException
	    {
		return res.containsHeader(name);
	    }

	    @Override
	    public String encodeRedirectURL(String url) {
		return res.encodeRedirectURL(url);
	    }

	    @Override
	    public String encodeURL(String url) {
		return res.encodeURL(url);
	    }
		
	    @Override
	    public Collection<String> getHeaderNames()
		throws IllegalStateException
	    {
		return res.getHeaderNames();
	    }

	    @Override
	    public Collection<String> getHeaders(String name)
		throws IllegalStateException
	    {
		return res.getHeaders(name);
	    }

	    @Override
	    public void sendError(int rc)
		throws IllegalStateException, IOException
	    {
		res.sendError(rc);
		committed = true;
	    }

	    @Override
	    public void sendRedirect(String location)
		throws IllegalStateException, IOException
	    {
		res.sendRedirect(location);
		committed = true;
	    }

	    @Override
	    public void setHeader(String name, String value)
		throws IllegalStateException
	    {
		res.setHeader(name, value);
	    }

	    @Override
	    public void setEncoding(String encoding) {
		res.setHeader("Content-encoding", (encoding == null)?
			      "identity": encoding);
	    }

	    boolean committed = false;

	    @Override
	    public void sendResponseHeaders(int code, long length)
		throws IllegalStateException
	    {
		res.setContentLengthLong(length);
		res.setStatus(code);
		committed = true;
	    }

	    @Override
	    public OutputStream getOutputStream()
		throws IOException, IllegalStateException
	    {
		return res.getOutputStream();
	    }

	    @Override
	    public boolean isCommitted() {
		return committed;
	    }
	};
    }

    /**
     * Called by the server (via a service method) to allow a servlet to
     * handle a GET request.
     * Please see the HttpServlet method doGet for a full description.
     * @param req an {@link javax.servlet.http.ServletRequest} object that
     *        contains the request the client has made to the servlet.
     * @param res an {@link javax.servlet.http.ServletResponse} object that
     *        contains the response the client has made to the servlet.
     * @exception IOException an IO error occurred during processing
     * @exception ServletException if the request for the GET could not
     *        be handled.
     */
    @Override
    protected void
	doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	try {
	    adapter.doGet(serverRequest(req),
			   serverResponse(res));
	} catch (Exception e) {
	    // The adapter doesn't explicitly throw exceptions, but just
	    // in case:
	    throw new ServletException(e.getMessage(), e);
	}
    }


    /**
     * Called by the server (via a service method) to allow a servlet to
     * handle a POST request.
     * Please see the HttpServlet method doPost for a full description.
     * @param req an {@link javax.servlet.http.ServletRequest} object that
     *        contains the request the client has made to the servlet.
     * @param res an {@link javax.servlet.http.ServletResponse} object that
     *        contains the response the client has made to the servlet.
     * @exception IOException an IO error occurred during processing
     * @exception ServletException if the request for the POST could not
     *        be handled.
     */
    @Override
    protected void
	doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	try {
	    adapter.doPost(serverRequest(req),
			   serverResponse(res));
	} catch (Exception e) {
	    // The adapter doesn't explicitly throw exceptions, but just
	    // in case:
	    throw new ServletException(e.getMessage(), e);
	}
    }

    /**
     * Called by the server (via a service method) to allow a servlet to
     * handle a PUT request.
     * Please see the HttpServlet method doPut for a full description.
     * @param req an {@link javax.servlet.http.ServletRequest} object that
     *        contains the request the client has made to the servlet.
     * @param res an {@link javax.servlet.http.ServletResponse} object that
     *        contains the response the client has made to the servlet.
     * @exception IOException an IO error occurred during processing
     * @exception ServletException if the request for the PUT could not
     *        be handled.
     */
    @Override
    protected void
	doPut(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	try {
	    adapter.doPut(serverRequest(req),
			   serverResponse(res));
	} catch (Exception e) {
	    // The adapter doesn't explicitly throw exceptions, but just
	    // in case:
	    throw new ServletException(e.getMessage(), e);
	}
    }

    /**
     * Called by the server (via a service method) to allow a servlet to
     * handle a DELETE request.
     * Please see the HttpServlet method doDelete for a full description.
     * for a full description.
     * @param req an {@link javax.servlet.http.ServletRequest} object that
     *        contains the request the client has made to the servlet.
     * @param res an {@link javax.servlet.http.ServletResponse} object that
     *        contains the response the client has made to the servlet.
     * @exception IOException an IO error occurred during processing
     * @exception ServletException if the request for the PUT could not
     *        be handled.
     */
    @Override
    protected void
	doDelete(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	try {
	    adapter.doDelete(serverRequest(req),
			     serverResponse(res));
	} catch (Exception e) {
	    // The adapter doesn't explicitly throw exceptions, but just
	    // in case:
	    throw new ServletException(e.getMessage(), e);
	}
    }

    /**
     * Initialize a servlet's adapter.
     * Please see the GenericServlet method init()
     * for the official description.  The servlet method
     * init(ServletConfig) will typically be called to initialize
     * the servlet.  That method
     * stores the servlet configuration object and then calls this
     * method - init() - to perform the initialization.
     * @exception ServletException if the initialization fails.
     */
    @Override
    public void init() throws ServletException {
	ServletConfig config = getServletConfig();
	if (config == null) return;
	Map<String,String> map = new LinkedHashMap<String,String>();
	Enumeration<String> enumeration = config.getInitParameterNames();
	while(enumeration.hasMoreElements()) {
	    String key = enumeration.nextElement();
	    String value = config.getInitParameter(key);
	    if (value != null) {
		map.put(key, value);
	    }
	}
	try {
	    adapter.init(map);
	} catch(Exception e) {
	    throw new ServletException(e.getMessage(), e);
	}
    }

    /**
     * Destroy a servlet.
     * Please see
     * {@link javax.servlet.http.HttpServlet#destry()} for a full description.
     */
    @Override
    public final void destroy() {
	adapter.destroy();
    }
}
