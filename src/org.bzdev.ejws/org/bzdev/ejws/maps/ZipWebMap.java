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
 * WebMap for resources in a ZIP, WAR, or JAR file.
 * The sole argument to a ZipWebMap's constructor is a file or
 * file name referring to a ZIP, WAR, or JAR file, or an instance of
 * {@link ZipWebMap.Config} providing the same information.
 */
public class ZipWebMap extends WebMap implements WebMap.ColorSpec {

    URI rootURI = null;
    ZipFile zipfile = null;

    static String errorMsg(String key, Object... args) {
	return WebMapErrorMsg.errorMsg(key, args);
    }


    /**
     * DirWebMap configurator.
     * An instance of this object can be used as the argument
     * for a {@link DirWebMap}'s constructor.
     */
    public static class Config {
	File root;
	String color;
	String bgcolor;
	String linkColor;
	String visitedColor;

	/**
	 * Constructor.
	 * The arguments linkColor and visitedColor can both be null
	 * but if one is not null, the other must also not be null.
	 * @param root a File representing the directory within which
	 *        resources will be found
	 * @param color the CSS color for text
	 * @param bgcolor the CSS color for the background
	 * @param linkColor the CSS color for links; null to ignore
	 * @param visitedColor the CSS color for visited links; null to ignore
	 * @throws IllegalArgumentException if color or bgcolor are missing
	 *         or if only one of linkColor or visitedColor is null.
	 */
	public Config(Object  root, String color, String bgcolor,
		      String linkColor, String visitedColor)
	    throws IllegalArgumentException
	{
	    if (root instanceof String) {
		root = new File((String)root);
	    } else if (!(root instanceof File)) {
		String msg = errorMsg("constrArgNotFileOrString");
		throw new IllegalArgumentException(msg);
	    }


	    if (root == null || color == null || bgcolor == null) {
		throw new IllegalArgumentException(errorMsg("nullArgs1or2"));
	    }
	    if ((linkColor == null || visitedColor == null)
		&& (linkColor != visitedColor)) {
		throw new IllegalArgumentException(errorMsg("nullArgs3or4"));
	    }
	    this.root = (File)root;
	    this.color = color;
	    this.bgcolor = bgcolor;
	    this.linkColor = linkColor;
	    this.visitedColor = visitedColor;
	}
    }

    String color = "black";
    String bgcolor = "lightgray";
    String linkColor = null;
    String visitedColor = null;

    @Override
    public String getColor() {return color;}

    @Override
    public String getBackgroundColor() {return bgcolor;}

    @Override
    public String getLinkColor() {return linkColor;}

    @Override
    public String getVisitedColor() {return visitedColor;}


    /**
     * Constructor.
     * @param root a ZIP file or a String naming a ZIP file or an instance
     *        of {@link ZipWebMap.Config}
     * @throws IOException if an IO error occurred
     * @throws IllegalArgumentException if root is not an instance of
     *         {@link File}
     */
    public ZipWebMap(Object root)
	throws IOException, IllegalArgumentException
    {
	if (root instanceof ZipWebMap.Config) {
	    ZipWebMap.Config config = (ZipWebMap.Config) root;
	    root = config.root;
	    color = config.color;
	    bgcolor = config.bgcolor;
	    linkColor = config.linkColor;
	    visitedColor = config.visitedColor;
	}

	if (root instanceof String) {
	    root = new File((String)root);
	}
	if (root instanceof File) {
	    setRoot((File) root);
	} else {
	    throw new
		IllegalArgumentException(errorMsg("constrArgNotFileOrString"));
	}
    }

    private void setRoot(File root) throws IOException {
	if (zipfile != null) {
	    try {
		zipfile.close();
	    } finally {
		zipfile = null;
	    }
	}

	String name = root.getName();
	if (name.endsWith(".war")
	    || name.endsWith(".jar")
	    || name.endsWith(".zip")) {
	    rootURI = root.toURI();
	    zipfile = new ZipFile(root);
	} else {
	    throw new IllegalArgumentException(errorMsg("badFileExt", name));
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
	// System.out.format("prepath = \"%s\"\n", prepath);
	// System.out.format("epath = \"%s\"\n", epath);
	try {
	    InputStream is = null;
	    long length = -1;
	    String mimeType = null;
	    boolean compress = false;
	    if (zipfile != null) {
		if (epath.startsWith("/")) {
		    epath = epath.substring(1);
		}
		if (epath.endsWith("/") || epath.length() == 0) {
		    // System.out.println("prepath = " + prepath);
		    // System.out.println("epath = " + epath);
		    if (getDisplayDir() == false) return null;
		    return EjwsUtilities.printHtmlDir(zipfile, prepath, null,
						      epath, "UTF-8", this);
		} else {
		    ZipEntry ze = zipfile.getEntry(epath);
		    if (ze == null) {
			for (String ep: gzipPaths(epath)) {
			    ze = zipfile.getEntry(ep);
			    if (ze != null) {
				compress = true;
				break;
			    }
			}
		    }
		    if (ze == null) return null;

		    is = zipfile.getInputStream(ze);
		    length = (int)ze.getSize();
		    mimeType = getMimeType(epath);
		    URI uri = new URI(null, null, epath, query, fragment);
		    String name =
			"jar:" + rootURI.toString() + "!/"
			+ uri.toString();
		    // System.out.println("name = " + name);
		    // new URL("jar:file:" + rootURI.toString()
		    //	    + "!/" + epath).toString();
		    WebMap.Info result =
			new WebMap.Info(is, length, mimeType, name);
		    if (compress) {
			result.setEncoding("gzip");
		    }
		    return result;
		}
	    }
	    return null;
	} catch (IOException e) {
	    return null;
	} catch (URISyntaxException ee) {
	    return null;
	} catch (NullPointerException eee) {
	    eee.printStackTrace();
	    System.exit(1);
	    return null;
	}
    }
}

//  LocalWords:  exbundle WebMap ZipWebMap's constrArgNotFile prepath
//  LocalWords:  badFileExt epath UTF rootURI toString
