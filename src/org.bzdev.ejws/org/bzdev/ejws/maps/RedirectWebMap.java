package org.bzdev.ejws.maps;
import org.bzdev.ejws.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.net.*;
import com.sun.net.httpserver.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import javax.net.ssl.*;
import java.security.cert.*;

//@exbundle org.bzdev.ejws.maps.lpack.WebMap

/**
 * WebMap for resources that are accessed via an HTTP redirect.
 * The sole argument to the constructor is a URL or URI to which
 * a request URI's path, excluding the prefix, will be appended.
 */
public class RedirectWebMap extends WebMap {
    private URI base;
    private String sbase;

    static String errorMsg(String key, Object... args) {
	return WebMapErrorMsg.errorMsg(key, args);
    }

    /**
     * Indicate if a URI from an HTTP request can contain a query.
     * for a RedirectWebMap, this is true if the URI is not a JAR
     * URI.
     * @return true if queries are allowed; false otherwise
     */
    public boolean allowsQuery() {return sbase == null;}


    /**
     * Constructor.
     * @param arg a URL or a string representation of a URL, the latter
     *        mandatory for JAR URLs
     */
    public RedirectWebMap(Object arg) {
	if (arg instanceof URL) {
	    try {
		base = ((URL) arg).toURI();
	    } catch (URISyntaxException e) {
		// e.printStackTrace();
		String msg = errorMsg("needURI", arg.toString());
		throw new IllegalArgumentException(msg, e);
	    }
	} else if (arg instanceof String) {
	    sbase = (String) arg;
	    boolean jarflag = sbase.startsWith("jar:");
	    if (!sbase.endsWith("/")) {
		if (jarflag) {
		    if (sbase.contains("!/")) {
			sbase = sbase + "/";
		    } else {
			sbase = sbase + "!/";
		    }
		} else {
		    sbase = sbase + "/";
		}
	    }
	    if (!jarflag) {
		try {
		    base = (new URL(sbase)).toURI();
		    sbase = null;
		} catch (MalformedURLException em) {
		    String msg = errorMsg("needURL", sbase);
		    throw new IllegalArgumentException(msg, em);
		} catch (URISyntaxException e) {
		    String msg = errorMsg("needURI", arg);
		    throw new IllegalArgumentException(msg, e);
		}
	    }
	} else {
	    String msg = errorMsg("expectedURL", arg.getClass().getName());
	    throw new IllegalArgumentException(msg);
	}
    }

    @Override
    public WebMap.Info getWebxml() {
	// we are just redirecting so no point in parsing a Web-Inf/web.xml file
	return null;
    }

    @Override
    public WebMap.Info getWelcomeInfo() throws IOException {
	URL url;
	try {
	    if (base != null) {
		url = base.toURL();
	    } else if (sbase != null) {
		url = new URL(sbase);
	    } else {
		return null;
	    }
	} catch (MalformedURLException e) {
	    // System.out.println(e.getMessage());
	    String arg = (base != null)? base.toString():
		(sbase != null)? sbase: "null";
	    String msg = errorMsg("needURL", arg);
	    throw new IllegalArgumentException(msg, e);
	}
	WebMap.Info result = new WebMap.Info(null, -1, null,
					     url.toString());
	result.setRedirect(true);
	return result;
    }


    /**
     * Get an Info object for a resource.
     * Only the path component is used.
     * @param prepath the initial portion of the request URI - the part
     *        before the path portion of the URI
     * @param path the relative path to the resource
     * @param query the query portion of the request URI
     * @param fragment the fragment portion of the request URI
     * @param requestInfo an object encapsulating request data
     *        (headers, input streams, etc.)
     * @return an Info object describing properties of a resource and
     *         providing an input stream to the resource
    */
    @Override
    protected WebMap.Info getInfoFromPath(String prepath,
					  String path,
					  String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
    {
	/*
	System.out.println("getInfoFrom Path called with path " + path
			   + ", query " + query);
	*/
	if (path.startsWith("/")) {
	    path = path.substring(1);
	}
	if (path.length() == 0 && query == null) {
	    // we aren't providing a subpath and/or a query, so we
	    // should get those from the URL used to configure this
	    // web masp.
	    query = base.getRawQuery();
	}
	URL url;
	try {
	    URI uri = new URI(null, null, path, query, fragment);
	    if (base != null) {
		// url = base.resolve(uri).toURL();
		url = (new URI(base.getScheme(), base.getAuthority(),
			       ((path==null || path.length() == 0)?
				base.getPath():
				base.resolve(uri).getPath()),
			       query, fragment)).toURL();
	    } else if (sbase != null) {
		// System.out.println(sbase + path);
		url = new URL(sbase + uri.toString());
	    } else {
		return null;
	    }
	} catch (MalformedURLException e) {
	    // System.out.println(e.getMessage());
	    String msg = errorMsg("badURLI");
	    throw new IllegalArgumentException(msg, e);
	} catch (URISyntaxException ee) {
	    String msg = errorMsg("badURLI");
	    throw new IllegalArgumentException(msg, ee);
	}
	WebMap.Info result = new WebMap.Info(null, -1, null,
					     url.toString());
	result.setRedirect(true);
	// System.out.println("getInfoFromPath returning " + url.toString());
	return result;
    }
}

//  LocalWords:  exbundle WebMap URI's RedirectWebMap arg needURI url
//  LocalWords:  printStackTrace needURL expectedURL  prepath
//  LocalWords:  getMessage getInfoFrom querty sbase badURLI toString
//  LocalWords:  getInfoFromPath
