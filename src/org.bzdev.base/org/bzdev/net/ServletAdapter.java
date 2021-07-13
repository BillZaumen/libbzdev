package org.bzdev.net;
import java.util.Map;
import java.io.IOException;


/**
 * Servlet-adapter interface.
 * This interface is used by {@link org.bzdev.ejws.maps.ServletWebMap}
 * to implement it's application-specific behavior, and can readily
 * be used by a servlet for the same purpose. This allows code to be
 * written for an embedded web server (EJWS) and then moved easily to
 * a different web server that supports servlets.
 * <P>
 * The class {@link org.bzdev.ejws.EmbeddedWebServer} automatically
 * handles the HTTP TRACE method, and the
 * {@link org.bzdev.ejws.FileHandler} class handles the HTTP OPTIONS
 * method. The {@link org.bzdev.ejws.WebMap.Info} class, which implements
 * {@link HttpServerResponse}, will provide a 'bit bucket' output stream
 * when there is an HTTP HEAD request so that the request can be otherwise
 * treated as an HTTP GET request. An implementation of
 * {@link ServletAdapter} should consequently ensure that
 * {@link ServletAdapter#doGet(HttpServerRequest,HttpServerResponse)} is
 * both idempotent and safe.
 * <P>
 * For servlets, A servlet will typically be initialized by providing
 * it with an instance of {@link ServletAdapter}. The
 * {@linkHttpServerRequest} and {@link HttpServerResponse} arguments
 * can be easily constructed from a servlet's HttpServletRequest and
 * HttpServletResonse arguments respectively.
 */
public interface ServletAdapter {

    /**
     * ServletAdapter Exception.
     * This class is provided to match the servlet specification
     * In a Servlet implementation, it should be converted to the
     * exception javax.servlet.ServletException (the name similarity
     * serves as a reminder and simplifies code migration).
     * <P>
     * When a servlet catches this exception, the exception should
     * be converted to a ServletException. For example
     * <BLOCKQUOTE><PRE><CODE>
     *     try {
     *       ...
     *     } catch (ServeletAdapter.ServletException e) {
     *           ServletException se =
     *             new ServletException(e.getMessage(), e.getCause());
     *             se.setStackTrace(e.getStackTrace());
     *             throw se;
     *     }
     * </CODE></PRE></BLOCKQUOTE>
     */
    public static class ServletException extends Exception {
	/**
	 * Constructor given an error message.
	 * @param msg the error message for this exception
	 */
	public ServletException(String msg) {
	    super(msg);
	}
	/**
	 * Constructor given an error message and cause.
	 * @param msg the error message for this exception
	 * @param cause the cause of the exception (typically another
	 *        exception)
	 */
	public ServletException(String msg, Throwable cause) {
	    super(msg, cause);
	}
	/**
	 * Constructor given a cause.
	 * @param cause the cause of the exception (typically another
	 *        exception)
	 */
	public ServletException(Throwable cause) {
	    super(cause);
	}
    }

    /**
     * Configure and initialize this servlet adapter
     * This is called when the service provided by this servlet adapter
     * starts.
     * @param map a map providing values for a set of keys
     * @exception ServletException initialization failed
     */
    default void init(Map<String,String> map) throws ServletException {
    }

    /**
     * Take this servlet adapter out of service.
     * This will release any resources obtained when
     * {@link init(Map)} was called.
     */
    default void destroy() {
    }

    /**
     * Process an HTTP GET request.
     * <P>
     * An {@link ServletAdapter.ServletException} should not be thrown
     * after the method
     * {@link HttpServerResponse#sendResponseHeaders(int,long)},
     * {@link HttpServerResponse#sendRedirect(String)}, or
     * {@link HttpServerResponse#sendError(int)} has been called.
     * Instead, use a {@link java.io.IOException}.
     * @param req the request object
     * @parm res the response object
     * @exception ServletException the request could not be handled
     * @exception IOException an input-output error occurred while the
     *            request was being handled
     */
    default void doGet(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletException
    {
	res.sendResponseHeaders(405, -1);
    }

    /**
     * Process an HTTP POST request.
     * <P>
     * An {@link ServletAdapter.ServletException} should not be thrown
     * after the method
     * {@link HttpServerResponse#sendResponseHeaders(int,long)},
     * {@link HttpServerResponse#sendRedirect(String)}, or
     * {@link HttpServerResponse#sendError(int)} has been called.
     * Instead, use a {@link java.io.IOException}.
     * @param req the request object
     * @parm res the response object
     * @exception ServletException the request could not be handled
     * @exception IOException an input-output error occurred while the
     *            request was being handled
     */
    default void doPost(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletException
    {
	res.sendResponseHeaders(405, -1);
    }

    /**
     * Process an HTTP PUT request.
     * <P>
     * An {@link ServletAdapter.ServletException} should not be thrown
     * after the method
     * {@link HttpServerResponse#sendResponseHeaders(int,long)},
     * {@link HttpServerResponse#sendRedirect(String)}, or
     * {@link HttpServerResponse#sendError(int)} has been called.
     * Instead, use a {@link java.io.IOException}.
     * @param req the request object
     * @parm res the response object
     * @exception ServletException the request could not be handled
     * @exception IOException an input-output error occurred while the
     *            request was being handled
     */
    default void doPut(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletException
    {
	res.sendResponseHeaders(405, -1);
    }

    /**
     * Process an HTTP DELETE request.
     * <P>
     * An {@link ServletAdapter.ServletException} should not be thrown
     * after the method
     * {@link HttpServerResponse#sendResponseHeaders(int,long)},
     * {@link HttpServerResponse#sendRedirect(String)}, or
     * {@link HttpServerResponse#sendError(int)} has been called.
     * Instead, use a {@link java.io.IOException}.
     * @param req the request object
     * @parm res the response object
     * @exception ServletException the request could not be handled
     * @exception IOException an input-output error occurred while the
     *            request was being handled
     */
    default void doDelete(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletException
    {
	res.sendResponseHeaders(405, -1);
    }
}

//  LocalWords:  Servlet servlet EJWS servlets HttpServerResponse PRE
//  LocalWords:  ServletAdapter doGet HttpServerRequest servlet's se
//  LocalWords:  HttpServletRequest HttpServletResonse BLOCKQUOTE msg
//  LocalWords:  ServletException ServeletAdapter getMessage getCause
//  LocalWords:  init sendResponseHeaders sendRedirect sendError req
//  LocalWords:  IOException
