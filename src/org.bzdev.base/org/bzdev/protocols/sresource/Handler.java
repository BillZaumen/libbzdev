package org.bzdev.protocols.sresource;
//@exbundle org.bzdev.protocols.sresource.lpack.Handler

import org.bzdev.protocols.Handlers;
import org.bzdev.util.SafeFormatter;

import java.net.*;
import java.io.*;
import java.util.jar.JarFile;
import java.util.ResourceBundle;

public class Handler extends URLStreamHandler {

    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.protocols.sresource.lpack.Handler");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static String pathsep;
    {
	try {
	    pathsep = System.getProperty("path.separator");
	} catch (Throwable t) {
	    pathsep = null;
	}
    }
    protected URLConnection openConnection(URL u) throws IOException {
	String path = u.getPath();
	String ref = u.getRef();
	if (path == null) throw new MalformedURLException(errorMsg("pathNull"));
	String resource = path.startsWith("/")? path.substring(1): path;
					   
	URL url  = ClassLoader.getSystemClassLoader().getResource(resource);
	if (ref != null) url = new URL(url, "#" + ref);
	if (url == null) {
	    throw new FileNotFoundException(errorMsg("resourceNotFound"));
	}
	return url.openConnection();
    }

    /*
    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
	String ref = (limit == spec.length())? null: spec.substring(limit+1);
	System.out.println("spec = " + spec);
	System.out.println("ref = " + ref);
	super.setURL(u, "sresource", "", -1, null, null,
		     spec.substring(start, limit),
		     null, ref);
    }
    */

    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
	String ref = (limit == spec.length())? null: spec.substring(limit+1);
	if (start == 10) {
	    // The spec starts with "resource:" so absolute
	    super.setURL(u, "sresource", "", -1, null, null,
			 spec.substring(start, limit), null, ref);
	} else {
	    // relative path
	    String upath = u.getPath();
	    String spath  = spec.substring(start, limit);
	    if (upath == null) {
		upath = "";
	    } else if (spath.length() > 0){
		int ind = upath.lastIndexOf("/");
		if (ind == -1) {
		    upath="";
		} else {
		    upath = upath.substring(0, ind+1);
		}
	    }
	    if (spath.startsWith("/")) spath = spath.substring(1);
	    String path = upath + spath;
	    super.setURL(u, "sresource", "", -1, null, null,
			 path, null, ref);
	}
    }

    @Override
    public boolean sameFile(URL u1, URL u2) {
	String p1 = u1.getProtocol();
	String p2 = u2.getProtocol();
	if (p1.equals("sresource") && p2.equals("sresource")) {
	    String path1 = u1.getPath();
	    String path2 = u2. getPath();
	    if (path1 != null && path2 != null) {
		return path1.equals(path2);
	    } else if (path1 == null || path2 == null) {
		return false;
	    }
	} else if (p1.equals("sresource") || p2.equals("sresource")) {
	    return false;
	}
	return super.sameFile(u1, u2);
    }
}

//  LocalWords:  exbundle pathNull resourceNotFound
