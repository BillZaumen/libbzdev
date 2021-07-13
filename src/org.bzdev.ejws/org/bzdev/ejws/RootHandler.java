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

/**
 * HttpHandler for the path "/".
 * Used as a default handler.  It checks a prefix map to see if
 * appending a '/' to a path (if necessary) will match a path in
 * the prefix map and if so, starts an HTTP redirect. Otherwise
 * the handler generates an error response.
 * <p>
 * This handler can be installed when
 */
class RootHandler implements HttpHandler {

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
	// System.out.println("total bytes sent = " + total);
    }


    WebMap map = new RootWebMap();
    Map<String,EmbeddedWebServer.PrefixData> prefixMap = null;

    /**
     * Constructor.
     * @param prefixMap the prefix map
     */
    public RootHandler(Map<String,EmbeddedWebServer.PrefixData> prefixMap) {
	this.prefixMap = prefixMap;
    }

    String protocol = "http";

    @Override
    public void handle(final HttpExchange t) throws IOException {
	String method = t.getRequestMethod();
	// System.out.println(t.getRequestMethod());
	if (!(method.equals("GET") || method.equals("HEAD"))) {
	    if (method.equals("POST")) {
		InputStream tis = t.getRequestBody();
		try {
		    tis.read();
		} catch (Exception e) {
		}
	    }
	    InputStream is = null;
	    try {
		WebMap.Info info = map.getErrorInfo(405, protocol, t);
		Headers headers = t.getResponseHeaders();
		headers.set("Content-Type", info.getMIMEType());
		t.sendResponseHeaders(405, info.getLength());
		OutputStream os = t.getResponseBody();
		is = info.getInputStream();
		// copyStream(is, os);
		is.transferTo(os);
		// System.out.print("all ");
	    } catch (EjwsException e) {
		// nothing to do for this exception
	    } finally {
		// System.out.println("error data sent");
		if (is != null) is.close();
		t.close();
	    }
	    return;
	}
  	InputStream is = t.getRequestBody();
	try {
	    is.read();
	} catch (Exception e) {
	    try {
		WebMap.Info info =
		    map.getErrorInfo(500, protocol, t);
		Headers headers = t.getResponseHeaders();
		headers.set("Content-Type", info.getMIMEType());
		t.sendResponseHeaders(500, info.getLength());
		OutputStream os = t.getResponseBody();
		try {
		    is = info.getInputStream();
		    // copyStream(is, os);
		    is.transferTo(os);
		} finally {
		    is.close();
		}
	    } catch (Exception ee) {
	    } finally {
		t.close();
	    }
	    return;
	}
	try {
	    URI uri = t.getRequestURI();
	    String path = uri.getPath();
	    if (!path.endsWith("/")) {
		path = path + "/";
	    }
	    // System.out.println("trying "  + path);
	    if (prefixMap.containsKey(path)) {
		// System.out.println("matching path");
		// true if the path did not originally end in "/" because
		// if it did, a different handler would have been used - this
		// is the default handler for the path '/' and is used only
		// when a path for '/' was not already defined.
		Headers reqHeaders = t.getRequestHeaders();
		String host = reqHeaders.getFirst("host");
		Headers headers = t.getResponseHeaders();
		headers.set("Location", protocol +"://" + host + path);
		t.sendResponseHeaders(302, -1);
	    } else {
		// System.out.println(" no matching path \"" + path + "\"");
		/*
		  System.out.println("try to see if our path is a "
		  + "subset of existing prefixes");
		*/
		String p = path;
		String rpath = null;
		Set<String> pset = prefixMap.keySet();
		if (path.equals("/")) {
		    rpath = path;
		} else {
		    for (String pp: pset) {
			if (pp.startsWith(path)) {
			    rpath = path;
			    break;
			}
		    }
		}
		if (rpath != null) {
		    // either path = "/" or path starts with a "/" terminated
		    // substring that is in prefixMap.  In this case, we
		    // create a directory listing.
		    WebMap.Info info =
			EjwsUtilities.printHtmlDir(pset, path, "UTF-8", null);
		    Headers headers = t.getResponseHeaders();
		    headers.set("Content-Type", info.getMIMEType());
		    t.sendResponseHeaders(200, info.getLength());
		    OutputStream os = t.getResponseBody();
		    is = info.getInputStream();
		    // copyStream(is, os);
		    is.transferTo(os);
		} else {
		    WebMap.Info info = map.getErrorInfo(404, protocol, t);
		    Headers headers = t.getResponseHeaders();
		    headers.set("Content-Type", info.getMIMEType());
		    t.sendResponseHeaders(404, info.getLength());
		    OutputStream os = t.getResponseBody();
		    is = info.getInputStream();
		    // copyStream(is, os);
		    is.transferTo(os);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	} finally {
	    t.close();
	}
    }
}

//  LocalWords:  HttpHandler prefixMap http getRequestMethod UTF
//  LocalWords:  substring
