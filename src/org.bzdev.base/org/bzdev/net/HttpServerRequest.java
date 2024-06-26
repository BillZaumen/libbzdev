package org.bzdev.net;
import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.text.DateFormat;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bzdev.util.IteratorEnumeration;

//@exbundle org.bzdev.net.lpack.Net

class HttpServerRequestConstants {
    static final String EMPTY_STRING = "";
}

/**
 * Servlet-compatibility interface for HTTP requests.
 * This interface is used by {@link ServletAdapter} for arguments
 * of the methods
 * {@link ServletAdapter#doGet(HttpServerRequest,HttpServerResponse)},
 * {@link ServletAdapter#doPost(HttpServerRequest,HttpServerResponse)},
 * {@link ServletAdapter#doPut(HttpServerRequest,HttpServerResponse)}, and
 * {@link ServletAdapter#doDelete(HttpServerRequest,HttpServerResponse)}.
 * {@link HttpServerRequest} is modeled after
 * {@link javax.servlet.http.HttpServletRequest}, but with several
 * differences.  In particular,
 * <UL>
 *  <LI> There is no "authenticate" or "getAuthType" method.
 *  <LI> There are no "login" or "logout" methods.
 *  <LI> There is no "getHttpServletMapping" method.
 *  <LI> The "getPart" and "getParts" methods are not implemented:
 *       one can use {@link FormDataIterator} instead.
 *  <LI> The "getRemoteUser" method is missing (a logged-in user
 *       can be found using {@link HttpServerRequest#getUserPrincipal()}).
 *  <LI> The "getSession" method is not implemented, athough
 *       {@link HttpServerRequest#getRequestedSessionID()},
 *       {@link HttpServerRequest#isRequestedSessionIDValid()}, and
 *       {@link HttpServerRequest#changeSessionID()} are implemented.
 *       When {@link org.bzdev.ejws.EmbeddedWebServer} is used, sessions are
 *       created by calling the method
 *       {@link org.bzdev.ejws.EmbeddedWebServer#addSessionFilter(String,HttpSessionOps)},
 *       and as a result, HttpServerRequest#isRequestedSessionIDValid()}
 *       will always return true. It may return false, however, when
 *       this method is used with other servers.
 * </UL>
 *
 * @see HttpServerResponse
 * @see javax.servlet.http.HttpServletRequest
 * @see javax.servlet.http.HttpServletResponse
 */

public interface HttpServerRequest {

    /**
     * Get an attribute.
     * @param name the attribute name
     * @return the attribute
     */
    Object getAttribute(String name);

    /**
     * Set an attribute.
     * @param name the attribute name
     * @param value the attribute value
     */
    void setAttribute(String name, Object value);

    /**
     * Get a context attribute.
     * A context is an instance of {@link com.sun.net.httpserver.HttpContext}
     * when using {@link org.bzdev.ejws.EmbeddedWebServer} and
     * ServletContext when using a servlet (see the the Jarkarta Servlet
     * API v4.0.3 for a description of this class).
     * @param name the attribute name
     * @return the attribute
     */
    Object getContextAttribute(String name);

    /**
     * Set a context attribute.
     * A context is an instance of {@link com.sun.net.httpserver.HttpContext}
     * when using {@link org.bzdev.ejws.EmbeddedWebServer} and
     * ServletContext when using a servlet (see the the Jarkarta Servlet
     * API v4.0.3 for a description of this class).
     * @param name the attribute name
     * @param value the attribute value
     */
    void setContextAttribute(String name, Object value);

    /**
     * Get the protocol used in the request.
     * For example, HTTP/1.1 for HTTP version 1.1
     * @return the protocol in the form PROTOCOL/MAJOR.MINOR
     */
    String getProtocol();

    /**
     * Get the cookies for this request.
     * @return a list of cookies
     */
    ServerCookie[] getCookies();

    /**
     * Get the preferred locale for this request.
     * The locale is chosen based on the Accept-Language header.
     * @return the preferred locale
     */
    Locale getLocale();

    /**
     * Get the locales provided by the Accept-Language header, ordered
     * by preference, highest preference first.
     * @return an enumeration for these locales
     */
    public Enumeration<Locale> getLocales();

    /**
     * Get the value of the request header for the specified type.
     * If there are more then one header with the same type (or name),
     * the first will be returned
     * @param type the type (or name) of the header.
     * @return the value of the header; null if there
     *         is no values for this type
     */
    String getHeader(String type);

    /**
     * Get a header's value as an integer.
     * @param name the name of the header
     * @return the header's value as an integer
     * @exception IllegalArgumentException a header with the given name
     *            does not exist or its value is not an integer
     */
    default int getIntHeader(String name) throws IllegalArgumentException {
	try {
	    return Integer.parseInt(getHeader(name));
	} catch (Exception e) {
	    String msg = NetErrorMsg.errorMsg("intHeader", name);
	    throw new IllegalArgumentException(msg, e);
	}
    }
    
    /**
     * Get a header's value as a date.
     * @param name the name of the header
     * @return the header's value as an date, represented by a long
     *         integer giving the number of milliseconds since
     *         January 1, 1970, 00:00:00 GMT
     * @exception IllegalArgumentException a header with the given name
     *            does not exist or its value is not a date
     */
   default long getDateHeader(String name) throws IllegalArgumentException {
	try {
	    return DateFormat.getInstance()
		.parse(getHeader(name)).getTime();
	} catch (Exception e) {
	    String msg = NetErrorMsg.errorMsg("dateHeader", name);
	    throw new IllegalArgumentException(msg, e);
	}
    }

    /**
     * Get an enumeration of all request-header names.
     * @return an enumeration of the names of the request headers;
     *         null if not supported
     */
    Enumeration<String> getHeaderNames();

    /**
     * Get the values of the request headers for the specified type.
     * The enumeration will contain a single element if there is only a single
     * header with specified type.
     * With multiple headers, each header's value is separate element
     * in the enumeration.
     * <P>
     * Note: an enumeration is returned for compatibility with
     * the servlet specification
     * @param type the type or name of the header.
     * @return an enumeration containing the values of the header; null
     *         if there are no values for this type
     */
    Enumeration<String> getHeaders(String type);

    /**
     * Get the request method.
     * @return the method.
     */
    HttpMethod getMethod();

    /**
     * Returns a context path.
     * The default implementation returns an empty string.
     * When used by a Servlet, this method should behave
     * identically to the HttpServletRequest method getContextPath().
     * @return the context path
     */
    default String getContextPath() {
	return HttpServerRequestConstants.EMPTY_STRING;
    }

    /**
     * Return the query string
     * @return the query string
     */
    String getQueryString();

    /**
     * Returns a java.util.Map of the parameters of this request.
     * Request parameters are extra information sent with the request.
     * For HTTP, parameters are contained in a URL's query string
     * or posted form data with content type application/x-www-form-urlencoded.
     * @return an immutable java.util.Map containing parameter names
     *         as keys and parameter values as map values.
     */
    Map<String,String[]> getParameterMap();

    /**
     * Returns the value of a request parameter as a String, or null
     * if the parameter does not exist.
     * Request parameters are extra information sent with the
     * request. For HTTP requests, parameters are contained in the
     * query string or posted form data with content type
     * application/x-www-form-urlencoded.
     * <P>
     * Users should only use this method when sure that the parameter
     * has only one value. If the parameter might have more than one
     * value, use getParameterValues(java.lang.String).  When used
     * with a multivalued parameter, the value returned is equal to
     * the first value in the array returned by getParameterValues.
     * <P>
     * When used with a servlet, if the parameter data was sent in the
     * request body, such as occurs with an HTTP POST request, then
     * reading the body directly via getEncodedInputStream or
     * getDecodedInputStream() can interfere with the execution of
     * this method.
     * @param name the name of the parameter
     * @return the value of the parameter; null if there is none
     */
    default String getParameter(String name) {
	String[] values = getParameterMap().get(name);
	   return (values == null)? null: values[0];
    }

    /**
     * Returns an array of String objects containing all of the values
     * the given request parameter has, or null if the parameter does
     * not exist.
     * For HTTP POST requests, the content must be form data with content
     * type application/x-www-form-urlencoded.
     * <P>
     * When used with a servlet, if the parameter data was sent in the
     * request body, such as occurs with an HTTP POST request, then
     * reading the body directly via getEncodedInputStream or
     * getDecodedInputStream() can interfere with the execution of
     * this method.
     * @param name the name of the parameter
     * @return the values of the parameter
     */
    default String[] getParameterValues(String name) {
	return getParameterMap().get(name);
    }

    /**
     * Returns an Enumeration of String objects containing the names
     * of the parameters contained in this request.
     * If the request has no parameters, the method returns an
     * empty Enumeration.
     * For HTTP POST requests, the content must be form data with content
     * type application/x-www-form-urlencoded.
     * <P>
     * When used with a servlet, if the parameter data was sent in the
     * request body, such as occurs with an HTTP POST request, then
     * reading the body directly via getEncodedInputStream or
     * getDecodedInputStream() can interfere with the execution of
     * this method.
     * @return an enumeration of parameter names
     */
    default Enumeration<String> getParameterNames() {
	return new
	    IteratorEnumeration<String>(getParameterMap().keySet().iterator());
    }

    /**
     * Returns a set of String objects containing the names
     * of the parameters contained in this request.
     * If the request has no parameters, the method returns an
     * empty set. This method is provided as a convenience:
     * unlike enumerations, sets can be used in 'for' loops.
     * For HTTP POST requests, the content must be form data with content
     * type application/x-www-form-urlencoded.
     * <P>
     * When used with a servlet, if the parameter data was sent in the
     * request body, such as occurs with an HTTP POST request, then
     * reading the body directly via getEncodedInputStream or
     * getDecodedInputStream() can interfere with the execution of
     * this method.
     * @return a set of parameter names
     */
    default Set<String> getParameterNameSet() {
	return Collections.unmodifiableSet(getParameterMap().keySet());
    }

    /**
     * Get the Principal for the user authenticating this request.
     * @return the Principal; null if this request has not been
     *         authenticated o.
     */
    Principal getUserPrincipal();


    /**
     * Get the session ID specified by a client;
     * The default assumes that session IDs have not been implemented.
     * @return the session ID; null if there is none
     */
    String getRequestedSessionID();

    /**
     * Change the current session's ID.
     * @return the new session ID
     * @exception IllegalStateException a session does not exist
     */
    String changeSessionID() throws IllegalStateException;

    /**
     * Set the current session's state.
     * @param state the session state
     * @exception IllegalStateException the session does not exist
     */
    void setSessionState(Object state) throws IllegalStateException;

    /**
     * Get the current session state.
     * @return the session state; null if one does not exist.
     */
    Object getSessionState();


    /**
     * Get the maximum inactive interval for this request's session.
     * @return the maximum inactive interval in seconds; 0 if no
     *         timeout; -1 if there is no session
     */
    public int getMaxInactiveInterval();

    /**
     * Set the maximum inactive interval for this request's session.
     * @param interval the maximum inactive interval in seconds; 0 if no
     *         timeout
     * @return true if this method succeeded; false otherwise (i.e.,
     *         if there is no session)
     */
    boolean setMaxInactiveInterval(int interval);

    /**
     * Get the request URI.
     * This is the first line of an HTTP request. It will not include
     * the server or the method. It does not include a query string
     * @return the request URI
     */
    String getRequestURI();

    /**
     * Get the URL sent by the client.
     * The URL is reconstructed based on server information.
     * @return the URL
     */
    String getRequestURL();

    /**
     * Determines if the requested session ID is valid.
     * The default assumes that session IDs have not been implemented
     * and thus returns false.
     * @return true if the current session ID is valid; false otherwise
     */
    boolean isRequestedSessionIDValid();

    /**
     * Determine if the request was made over a secure channel.
     * Note: the test is usually based on the protocol being used, not
     * how it is configured.
     * @return if the channel is secure; false otherwise
     */
    boolean isSecure();

    /**
     * Determine if a user is in a specified role.
     * If roles have not been implemented, this method returns false.
     * @param role the role
     * @return true if the user is in a specified role; false
     *         otherwise
     */
    boolean isUserInRole(String role);

    /**
     * Get the content type.
     * @return the media type for the content of a request
     */
    String getContentType();

    /**
     * Get the content-type's character encoding
     * @return the character encoding; null if not specified or not
     *         applicable
     */
    String getCharacterEncoding();

    /**
     * Get the content length.
     * The content length (more or less) is the length in bytes
     * of the content including any content encoding. If compression
     * is used, the content length should be considered to be an
     * estimate of the length, perhaps off by some factor. It can be
     * useful in this case for estimating the size of a ByteArrayBuffer
     * or similar Java classes that can grow if needed but start with
     * an initial size.
     * @return the length of the content; -1 if not known.
     */
    int getContentLength();

    /**
     * Get the content length as a long integer.
     * The content length (more or less) is the length in bytes
     * of the content including any content encoding. If compression
     * is used, the content length should be considered to be an
     * estimate of the length, perhaps off by some factor. It can be
     * useful in this case for estimating the size of a ByteArrayBuffer
     * or similar Java classes that can grow if needed but start with
     * an initial size.
     * @return the length of the content; -1 if not known.
     */
    long getContentLengthLong();

    /**
     * Get the media type (content type) without any of its parameters.
     * @return the media type without any of its parameters
     */
    default String getMediaType() {
	String mediaType = getHeader("content-type").trim();
	if (mediaType == null) return "application/octet-stream";
	int firstsc = mediaType.indexOf(';');
	if (firstsc == -1) {
	    return mediaType;
	} else {
	    return mediaType.substring(0, firstsc).trim();
	}
    }

    /**
     * Return the IP (Internet Protocol) address of the client, last
     * proxy, or router/switch using NAT (Network Address
     * Translation), that sent this request.
     * @return the address
     */
    String getRemoteAddr();

    /**
     * Extract parameters from a request header.
     * This method assumes that a header contains a single value
     * and that multiple values would be separated by commas.
     * @param key the name of the header (e.g., "content-type")
     * @param parameter the name of a header parameter (e.g., "charset");
     *        null for the header without its parameters
     * @return the value with quotes and character escapes processed
     */
    default String getFromHeader(String key, String parameter) {
	if (key == null) return null;
	String hdr = getHeader(key);
	if (hdr == null) return null;
	Map<String,String> map = new LinkedHashMap<String,String>();
	// HeaderParser is in HeaderOps.java
	HeaderParser.parseFirst(map, key, hdr, 0, false);
	if (map == null) return null;
	if (parameter == null) parameter = key;
	return map.get(parameter);
    }

    /**
     * Get an input stream that will read data in a request
     * without content decoding.
     * This method is useful when the content will be stored
     * as is - e.g., with compression.
     * One may call {@link #getEncodedInputStream()} or
     * {@link #getDecodedInputStream()} but not both. If either
     * is called multiple times, the same input stream will be
     * returned and it may be in a partially-read state.
     * Regardless of which method is used, the stream should be
     * fully read: otherwise the server, or at least the current
     * transaction, may hang.
     * <P>
     * When implemented for a servlet, this method should just return
     * the servlet request's getInputStream() method and ensure that
     * {@link getDecodedInputStream()} will return null;
     * @return the input stream; null if not available
     * @exception IOException an IO error occurred
     */
    InputStream getEncodedInputStream() throws IOException;

    /**
     * Get an input stream that will read data in a request
     * with content decoding.
     * Any compression (gzip or deflate) will be removed.
     * One may call {@link #getEncodedInputStream()} or
     * {@link #getDecodedInputStream()} but not both. If either
     * is called multiple times, the same input stream will be
     * returned and it may be in a partially-read state.
     * Regardless of which method is used, the stream should be
     * fully read: otherwise the server, or at least the current
     * transaction, may hang.
     * <P>
     * In a servlet implementation, the following code fragment can
     * be used in implementing this method:
     * <BLOCKQUOTE><PRE><CODE>
     *     // Need to check that we don't already have a stream.
     *     // If we don't ...
     *     String encoding = servletRequest.getHeader("content-encoding");
     *     InputStream is = servletRequest.getInputStream();
     *     if (encoding.equalsIgnoreCase("identity")) {
     *         // nothing to do
     *     } else if (encoding.equalsIgnoreCase("gzip")) {
     *         is = new java.util.zip.GZIPInputStream(is);
     *     } else if (encoding.equalsIgnoreCase("deflate")) {
     *         is = new java.util.zip.InflaterInputStream(is);
     *     } else {
     *       is = null;
     *     }
     *     // after adding some bookkeeping to track if we have a stream
     *     return is;
     * </CODE></PRE></BLOCKQUOTE>
     * @return the input stream; null if not available
     * @exception IOException an IO error occurred
     */
    InputStream getDecodedInputStream() throws IOException;
}

//  LocalWords:  exbundle Servlet ServletContext servlet Jarkarta URI
//  LocalWords:  IllegalArgumentException intHeader dateHeader
//  LocalWords:  HttpServletRequest getContextPath ByteArrayBuffer
//  LocalWords:  IllegalStateException charset HeaderParser HeaderOps
//  LocalWords:  getEncodedInputStream getDecodedInputStream
//  LocalWords:  getInputStream IOException
