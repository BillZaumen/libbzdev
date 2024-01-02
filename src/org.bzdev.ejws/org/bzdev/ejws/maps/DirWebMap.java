package org.bzdev.ejws.maps;
import org.bzdev.ejws.*;
import org.bzdev.util.ErrorMessage;

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
 * WebMap for resources located in directories of a local file system.
 * resources will be resolved relative to the root File passed as an argument
 * to the constructor.  This file must be a directory.
 */

public class DirWebMap extends WebMap implements WebMap.ColorSpec {

    // LinkedList<String> welcomeList = new LinkedList<String>();
    // HashMap<String,String> suffixToMimeType =
    // new HashMap<String,String>();

    File root = null;
    URI rootURI = null;

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
	public Config(File root, String color, String bgcolor,
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
     * @param root a File representing the directory within which
     *        resources will be found or an instance of
     *        {@link DirWebMap.Config}
     * @exception IOException an IO error occurred
     * @exception IllegalArgumentException The argument is not an
     *            instance of {@link File}
     */
    public DirWebMap(Object root)
	throws IOException, IllegalArgumentException
    {
	if (root instanceof File) {
	    setRoot((File) root);
	} else if (root instanceof DirWebMap.Config) {
	    DirWebMap.Config config = (DirWebMap.Config) root;
	    setRoot(config.root);
	    color = config.color;
	    bgcolor = config.bgcolor;
	    linkColor = config.linkColor;
	    visitedColor = config.visitedColor;

	} else {
	    throw new
		IllegalArgumentException(errorMsg("constrArgNotFileOrConfig"));
		/*("Argument to constructor is "
		  + "not an instance of java.io.File");*/
	}
    }

    private void setRoot(File root) throws IOException {
	if (root != null) {
	    if (this.root != null) {
		this.root = null;
		this.rootURI = null;
	    }
	    if (root.isDirectory()) {
		this.root = root;
		this.rootURI = root.toURI();
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("rootNotDirectory", root));
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
	    InputStream is = null;
	    long length = -1;
	    String mimeType = null;
	    boolean compress = false;
	    if (root != null) {
		URI ourURI = rootURI.resolve(epath);
		File f = new File(ourURI);
		if (f.exists() == false) {
		    for (String ep: gzipPaths(epath)) {
			ourURI = rootURI.resolve(ep);
			f = new File(ourURI);
			if (f.exists()) {
			    compress = true;
			    break;
			}
			f = null;
		    }
		    if (f == null) return null;
		}
		boolean canRead = f.canRead();
		boolean isFile = f.isFile();
		boolean isDir = f.isDirectory();
		if (canRead && isFile) {
		    is = new FileInputStream(f);
		    length = (int)(f.length());
		    mimeType = getMimeType(epath);
		} else if (canRead && isDir && getDisplayDir()) {
		    if (!epath.endsWith("/")) epath = epath + "/";
		    if (epath.startsWith("/")) epath = epath.substring(1);
		    if (prepath == null) prepath = "";
		    if (!prepath.endsWith("/")) prepath = prepath + "/";
		    epath = prepath + epath;
		    // System.out.println("prepath = " + prepath);
		    // System.out.println("epath = " +epath);
		    return EjwsUtilities.printHtmlDir(f, epath, "UTF-8",
						      this);
		} else {
		    return null;
		}
		WebMap.Info result =
		    new WebMap.Info(is, length, mimeType, f.toString());
		if (compress) {
		    result.setEncoding("gzip");
		}
		return result;
	    }
	    return null;
	} catch (IOException e) {
	    ErrorMessage.display(e);
	    return null;
	}
    }
}

//  LocalWords:  exbundle WebMap LinkedList welcomeList HashMap epath
//  LocalWords:  suffixToMimeType constrArgNotFile rootNotDirectory
//  LocalWords:  prepath UTF
