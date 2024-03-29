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
 * WebMap for resources accessible from the application's class path.
 * The sole argument to the constructor is a system resource name to which
 * a request URI's path, excluding the prefix, will be appended.
 */
public class ResourceWebMap extends WebMap implements WebMap.ColorSpec {

    LinkedList<String> welcomeList = new LinkedList<String>();
    HashMap<String,String> suffixToMimeType =
	new HashMap<String,String>();

    File root = null;
    URI rootURI = null;
    ZipFile zipfile = null;
    String rootResourcePath = null;

    static String errorMsg(String key, Object... args) {
	return WebMapErrorMsg.errorMsg(key, args);
    }

    /**
     * DirWebMap configurator.
     * An instance of this object can be used as the argument
     * for a {@link DirWebMap}'s constructor.
     */
    public static class Config {
	String root;
	String color;
	String bgcolor;
	String linkColor;
	String visitedColor;

	/**
	 * Constructor.
	 * The arguments linkColor and visitedColor can both be null
	 * but if one is not null, the other must also not be null.
	 * @param root the initial portion of a resource name (if it does not
	 *        end in a '/', a '/' will be appended automatically)
	 * @param color the CSS color for text
	 * @param bgcolor the CSS color for the background
	 * @param linkColor the CSS color for links; null to ignore
	 * @param visitedColor the CSS color for visited links; null to ignore
	 * @throws IllegalArgumentException if color or bgcolor are missing
	 *         or if only one of linkColor or visitedColor is null.
	 */
	public Config(String root, String color, String bgcolor,
		      String linkColor, String visitedColor)
	    throws IllegalArgumentException
	{
	    if (root == null || color == null || bgcolor == null) {
		throw new IllegalArgumentException(errorMsg("nullArgs1or2"));
	    }
	    if ((linkColor == null || visitedColor == null)
		&& (linkColor != visitedColor)) {
		throw new IllegalArgumentException(errorMsg("nullArgs3or4"));
	    }
	    this.root = root;
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
     * @param root the initial portion of a resource name (if it does not
     *        end in a '/', a '/' will be appended automatically) or an
     *        instance of {@link ResourceWebMap.Config}.
     * @throws IOException if an IO error ocurred
     * @throws IllegalArgumentException if the argumnent is not a string
     *         and not the value null
     */
    public ResourceWebMap(Object root)
	throws IOException, IllegalArgumentException
    {
	if (root != null && root instanceof String) {
	    rootResourcePath = (String) root;
	    if (!rootResourcePath.endsWith("/")) {
		rootResourcePath = rootResourcePath + "/";
	    }
	    if (rootResourcePath.startsWith("/")) {
		rootResourcePath = rootResourcePath.substring(1);
	    }
	} else if (root != null && root instanceof ResourceWebMap.Config) {
	    ResourceWebMap.Config config = (ResourceWebMap.Config) root;
	    rootResourcePath = config.root;
	    if (!rootResourcePath.endsWith("/")) {
		rootResourcePath = rootResourcePath + "/";
	    }
	    if (rootResourcePath.startsWith("/")) {
		rootResourcePath = rootResourcePath.substring(1);
	    }
	    color = config.color;
	    bgcolor = config.bgcolor;
	    linkColor = config.linkColor;
	    visitedColor = config.visitedColor;
	} else {
	    throw new
		IllegalArgumentException(errorMsg("constrArgNotString"));
	}
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
	long length = -1;
	InputStream is = null;
	String mimeType = null;
	//System.out.println("rootResourcePath = \"" + rootResourcePath + "\"");
	// System.out.println("prepath = " + prepath);
	// System.out.println("path = " + path);
	try {
	    if (rootResourcePath != null) {
		String rpath = rootResourcePath + path;
		// System.out.println("rpath = " + rpath);
		URL url = ClassLoader.getSystemResource(rpath);
		// System.out.println("url = " + url);
		if (url == null) return null;
		String proto = url.getProtocol();
		if (proto.equals("file")) {
		    try {
			File f = new File(url.toURI());
			if (f.isFile() && f.canRead()) {
			    length = f.length();
			    is = new FileInputStream(f);
			    mimeType = getMimeType(f.getPath());
			} else if (getDisplayDir() && f.isDirectory()
				   && f.canRead()) {
			    return EjwsUtilities.printHtmlDir
				(f, prepath + path, "UTF-8", this);
			} else {
			    return null;
			}
		    } catch (Exception e) {
			return null;
		    }
		} else if (proto.equals("jar")) {
		    JarURLConnection jarc = (JarURLConnection)
			url.openConnection();
		    ZipEntry entry = jarc.getJarEntry();
		    if (entry.isDirectory()) {
			if (getDisplayDir()) {
			    ZipFile zipfile = jarc.getJarFile();
			    return EjwsUtilities.printHtmlDir
				(zipfile, prepath, rootResourcePath, path,
				 "UTF-8", this);
			} else {
			    return null;
			}
		    } else {
			length = entry.getSize();
			is = jarc.getInputStream();
			mimeType = getMimeType(path);
		    }
		}
		if (is != null) {
		    if (length != -1) {
			return new Info(is, length, mimeType, url.toString());
		    } else {
			ByteArrayOutputStream os =
			    new ByteArrayOutputStream(1024<<4);
			// EjwsUtilities.copyStream(is, os);
			is.transferTo(os);
			length = os.size();
			is = new ByteArrayInputStream(os.toByteArray());
			return new Info(is, length, mimeType,
					url.toString());
		    }
		}
	    }
	} catch (IOException e) {
	    return null;
	}
	return null;
    }
}

//  LocalWords:  exbundle WebMap URI's constrArgNotString prepath
