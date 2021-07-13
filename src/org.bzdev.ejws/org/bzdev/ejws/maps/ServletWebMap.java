package org.bzdev.ejws.maps;
import org.bzdev.ejws.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.ServletAdapter;
import org.bzdev.net.HttpMethod;
import java.io.IOException;
import java.util.Map;

//@exbundle org.bzdev.ejws.maps.lpack.WebMap

/**
 * WebMap for servlet compatibility.
 * This web map uses an implementation of the interface
 * {@link org.bzdev.net.ServletAdapter} to provide application-specific
 * behavior.
 * To configure this web map, use an instance of the class
 * {@link ServletWebMap.Config}.
 * @see org.bzdev.net.ServletAdapter
 */
public class ServletWebMap extends WebMap {
 
    static String errorMsg(String key, Object... args) {
	return WebMapErrorMsg.errorMsg(key, args);
    }

    /**
     * ServletWebMap configuration.
     * This class provides the argument used in constructing
     * an instance of {@link ServletWebMap}. The constructor's
     * arguments provide
     * <UL>
     *   <IT><B>a servlet adapter.</B> This object determines the behavior
     *       of a {@link ServletWebMap}. Its interface is designed so that
     *       it can plugged into a servlet designed as a 'wrapper' class,
     *       in addition to its use with
     *       {@link org.bzdev.ejws.EmbeddedWebServer} so that the same
     *       code can be used in both environments.
     *   <IT><B>a parameter map.</B> This is a {@link java.util.Map map}
     *     whose keys and values are both strings. It is used to provide
     *     data for configuring a servlet adapter. These parameters should
     *     have the same keys as those that would be used by the
     *     ServletConfig method getInitParameters when configuring a servlet.
     *   <IT><B>a flag.</B> The value is 'true' if the servlet adapter
     *     uses the 'query' field in a URL or URI, and false
     *     otherwise. When false, {@link org.bzdev.ejws.FileHandler}
     *     will automatically generate a 404 response code if a query
     *     is present.
     *   <IT><B>a variable number of HTTP methods.</B> These list the
     *        HTTP methods that are supported. If no methods are provided
     *        a default is used.
     * </UL>
     */
    public static class Config {
	ServletAdapter sa;
	Map<String,String> parameters;
	boolean allowsQuery;
	HttpMethod[] methods;

	/**
	 * Constructor.
	 * @param sa the servlet adapter.
	 * @param parameters configuration parameters; null or an empty
	 *        map if there are none
	 * @param allowsQuery true if the servlet adapter can process queries;
	 *        false otherwise
	 * @param methods the HTTP methods that the servlet adapter can
	 *        handle
	 */
	public Config(ServletAdapter sa,
		      Map<String,String> parameters,
		      boolean allowsQuery,
		      HttpMethod... methods)
	{
	    this.sa = sa;
	    this.parameters = parameters;
	    this.allowsQuery = allowsQuery;
	    this.methods = methods.clone();
	}
    }


    ServletAdapter sa = null;
    Map<String,String>parameters = null;

    /**
     * Constructor.
     * @param root an instance of {@link org.bzdev.net.ServletAdapter}
     */
    public ServletWebMap(Object root) throws IllegalArgumentException {
	if (root instanceof Config) {
	    Config config = (Config) root;
	    if (config.methods.length > 0) {
		setMethods(config.methods);
	    }
	    setAllowsQuery(config.allowsQuery);
	    sa = config.sa;
	    parameters = config.parameters;
	} else {
	    throw new IllegalArgumentException(errorMsg("ServletWebMapArg"));
	}
    }

    @Override
    protected void configure() throws Exception {
	// super.configure(); not needed - we are a direct subclass of WebMap
	sa.init(parameters);
    }
    @Override
    protected void deconfigure() {
	sa.destroy();
	// super.deconfigure(); not needed - we are a direct subclass of WebMap
    }


    @Override
    protected WebMap.Info getInfoFromPath(String prepath,
					  String epath,
					  String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
	throws IOException, EjwsException
    {
	// System.out.println("prepath = " + prepath);
	// System.out.println("epath = " + epath);
	// System.out.println("query = " + query);
	WebMap.Info info = new WebMap.Info(requestInfo);
	try {
	    switch (requestInfo.getMethod()) {
	    case HEAD:
		// info will provide a bit bucket as its output stream
		// when the method is HEAD so we can just fall through.
	    case GET:
		sa.doGet(requestInfo, info);
		break;
	    case POST:
		sa.doPost(requestInfo, info);
		break;
	    case PUT:
		sa.doPut(requestInfo, info);
		break;
	    case DELETE:
		sa.doDelete(requestInfo, info);
		break;
	    default:
		info.sendResponseHeaders(405, -1);
	    }
	    return info;
	} catch (ServletAdapter.ServletException e) {
	    // change a ServletException into an EjwsException
	    // This will allow FileManager to handle the exception
	    // appropriately. The reason an EjwsException was not
	    // thrown directly is to avoid the need to include the
	    // EJWS module in a module that provides a servlet adapter,
	    // as such a module may be used independently from EJWS and
	    // including the ejws module is overkill when all that is
	    // wanted is a single exception class.
	    StackTraceElement[] stackTrace = e.getStackTrace();
	    EjwsException ee = new EjwsException(e.getMessage(), e.getCause());
	    ee.setStackTrace(stackTrace);
	    throw ee;
	}
    }  
}

//  LocalWords:  exbundle WebMap servlet ServletWebMap Config URI sa
//  LocalWords:  ServletConfig getInitParameters allowsQuery prepath
//  LocalWords:  ServletWebMapArg deconfigure epath ServletException
//  LocalWords:  EjwsException FileManager EJWS ejws
