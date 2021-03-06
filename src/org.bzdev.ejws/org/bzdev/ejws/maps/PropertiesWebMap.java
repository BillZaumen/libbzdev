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

//@exbundle org.bzdev.ejws.maps.lpack.WebMap

/**
 * WebMap for resources in a {@link java.util.Properties} object.
 * The sole argument to a propertyWebMap's constructor is a
 * {@link java.util.Properties} instance whose keys are path
 * names.
 */
public class PropertiesWebMap extends WebMap {

    Properties properties = null;


    static String errorMsg(String key, Object... args) {
	return WebMapErrorMsg.errorMsg(key, args);
    }

    TreeSet<String>pathSet = new TreeSet<String>();


    /**
     * Constructor.
     * @param properties a File denoting a directory
     */
    public PropertiesWebMap(Object properties)
	throws IllegalArgumentException
    {
	if (properties != null && properties instanceof Properties) {
	    this.properties = (Properties) properties;
	} else {
	    throw new
		IllegalArgumentException(errorMsg("constrArgNotProperties"));
	}
	for (Object key: this.properties.keySet()) {
	    if (key instanceof String) {
		pathSet.add((String)key);
	    }
	}
    }

    public void setProperties(Properties properties) {
	this.properties = properties;
	pathSet.clear();
	for (Object key: properties.keySet()) {
	    if (key instanceof String) {
		pathSet.add((String)key);
	    }
	}
    }

    /**
     * Reinitialize the state of this object.
     * This method should be called if the {@link Properties} instance
     * passed to this object's constructor is modified.
     * are modified.
     */
    public void update() {
	pathSet.clear();
	for (Object key: properties.keySet()) {
	    if (key instanceof String) {
		pathSet.add((String)key);
	    }
	}
    }

    /**
     * Get an Info object for a resource.
     * Only the path component is used.
     * @param prepath the initial portion of the request URI - the part
     *        before the path portion of the URI
     * @param epath the relative path to the resource
     * @param query the query portion of the request URI
     * @param fragment the fragment portion of the request URI
     * @param requestInfo an object encapsulating request data
     *        (headers, input streams, etc.)
     * @return an Info object describing properties of a resource and
     *         providing an input stream to the resource
     */
    @Override
    protected WebMap.Info getInfoFromPath(String prepath,
					  String epath,
					  String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
    {
	try {
	    long length = -1;
	    String mimeType = null;
	    boolean compress = false;
	    if (properties != null) {
		if (!epath.endsWith("/") && !pathSet.contains(epath)) {
		    epath = epath + "/";
		}
		if (epath.startsWith("/")) {
		    epath = epath.substring(1);
		}
		if (epath.endsWith("/") || epath.length() == 0) {
		    // System.out.println("epath = " + epath);
		    SortedSet<String> tailset = pathSet.tailSet(epath, true);
		    if (tailset.size() > 0) {
			String first = tailset.first();
			if (first.startsWith(epath)) {
			    return EjwsUtilities.printHtmlDir(pathSet, epath,
							      "UTF-8", this);
			} else {
			    return null;
			}
		    } else {
			return null;
		    }
		} else {
		    Object obj = properties.get(epath);
		    if (obj == null) {
			for (String ep: gzipPaths(epath)) {
			    obj = properties.get(ep);
			    if (obj != null) {
				compress = true;
				break;
			    }
			    obj = null;
			}
		    }
		    if (obj != null && obj instanceof byte[]) {
			byte[] resource = (byte[]) obj;
			length = resource.length;
			ByteArrayInputStream is =
			    new ByteArrayInputStream(resource);
			mimeType = getMimeType(epath);
			WebMap.Info result =
			    new WebMap.Info(is, length, mimeType, null);
			if (compress) {
			    result.setEncoding("gzip");
			}
			return result;
		    } else {
			return null;
		    }
		}
	    } else {
		return null;
	    }
	} catch (IOException ee) {
	    return null;
	}
    }
}

//  LocalWords:  exbundle WebMap PropertiesWebMap's constrArgNotFile prepath
//  LocalWords:  badFileExt epath UTF toString
