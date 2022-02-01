package org.bzdev.net;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Servlet-compatibility interface for HTTP responses.
 */

public interface HttpServerResponse {

    /**
     * Add a cookie to the response headers.
     * @param cookie the cookie
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    public void addCookie(ServerCookie cookie) throws IllegalStateException;

    /**
     * Add a date header.
     * @param name the name of the header
     * @param date the date provided in milliseconds since 00:00:00
     *        January 1, 1970 GMT.
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    default void addDateHeader(String name, long date)
	throws IllegalStateException
    {
	addHeader(name,
		  DateTimeFormatter.RFC_1123_DATE_TIME.format
		  (Instant.ofEpochMilli(date)));
    }

    /**
     * Add a header.
     * This method is used when multiple headers with the same name
     * are needed.
     * @param name the name of the header
     * @param value the value of the header
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    void addHeader(String name, String value) throws IllegalStateException;
    
    /**
     * Add an integer header.
     * @param name the ename of the header
     * @param value the value of the header
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    default void addIntHeader(String name, int value)
	throws IllegalStateException
    {
	addHeader(name, Integer.toString(value));
    }

    /**
     * Determine if a response header with the specified name exists.
     * @param name the header name
     * @return true if the header exists; false otherwise
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    boolean containsHeader(String name) throws IllegalStateException;

    /**
     * Encode a URL for use in  {@link #sendRedirect(String)}.
     * This method is needed by the Servlet specification where
     * encoding may be necessary to include a session ID.
     * @param url the URL
     * @return the encoded URL
     */
    String encodeRedirectURL(String url);

    /**
     * Encode a URL so that it includes a session ID if necessary.
     * This method is needed by the Servlet specification where
     * encoding may be necessary to include a session ID.
     * @param url the URL
     * @return the encoded URL
     */
    String encodeURL(String url);

    /**
     * Get the names of the current response header names.
     * @return a collection of the response header names
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    Collection<String> getHeaderNames() throws IllegalStateException;

    /**
     * Get the headers with a given name.
     * @param name the name
     * @return a collection of the header values for a given name
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    Collection<String> getHeaders(String name) throws IllegalStateException;

    /**
     * Sends a response with a given HTML response code for an error
     * with no content.  If this method is called,
     * {@link #sendResponseHeaders(int,long)} should not be called.
     * @param rc the response code
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     * @exception IOException an IO error occurred
     */
    void sendError(int rc) throws IllegalStateException, IOException;

    /**
     * Sends an HTTP redirect response with a given location.
     * @param location a URL providing the location
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     * @exception IOException an IO error occurred
     */
    void sendRedirect(String location)
	throws IllegalStateException, IOException;

    /**
     * Add a date header.
     * @param name the name of the header
     * @param date the date provided in milliseconds since 00:00:00
     *        January 1, 1970 GMT.
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    default void setDateHeader(String name, long date)
	throws IllegalStateException
    {
	setHeader(name,
		  DateTimeFormatter.RFC_1123_DATE_TIME.format
		  (Instant.ofEpochMilli(date)));
    }

    /**
     * Set the specified header.
     * @param name the name of the header
     * @param value the value for the header
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    void setHeader(String name, String value) throws IllegalStateException;

    /**
     * Set an integer header.
     * @param name the ename of the header
     * @param value the value of the header
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    default void setIntHeader(String name, int value)
	throws IllegalStateException
    {
	setHeader(name, Integer.toString(value));
    }

    /**
     * Set whether or not a response uses compression or some other
     * transfer encoding.
     * <P>
     * @param encoding the encoding ("identity", "gzip", "compress", and
     *        "deflate" are standard values); null defaults to "identity"
     */
    void setEncoding(String encoding);

    /**
     * Set the response code and content length.
     * If the length is 0, indicating that the length is not known,
     * chunked transfer encoding is automatically used. As a result,
     * one should generally avoid the explicit use of the
     * transfer-encoding header.
     * <P>
     * If this method is called, {@link #sendError(int)} should not
     * be called.
     * <P>
     * When used in a servlet, this method should call the one-argument
     * servlet-response method setStatus, and also call setContentLength
     * or setContentLengthLong. Headers may not be modified after this
     * method is called.
     * @param code the response code
     * @param length the length of the response; 0 if the length
     *        is not known, and -1 if there is no response.
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     */
    void sendResponseHeaders(int code, long length)
	throws IllegalStateException; 

    /**
     * Get the output stream for responses.
     * @return the output stream.
     * @exception IllegalStateException an implementation-dependent
     *            error occurred (for {@link org.bzdev.ejws.WebMap.Info}
     *            the constructor was not given an instance of
     *            {@link org.bzdev.ejws.WebMap.RequestInfo})
     * @exception IOException an IO error occurred
     */
    OutputStream getOutputStream() throws IOException, IllegalStateException;

    /**
     * Determine if this response has been committed.
     * A response is committed if its status code and headers have
     * been written.
     * @returnt true if this response has been committed; false otherwise
     */
    boolean isCommitted();
}

//  LocalWords:  Servlet IllegalStateException ename sendRedirect url
//  LocalWords:  sendResponseHeaders rc gzip sendError servlet
//  LocalWords:  serveletresponse
