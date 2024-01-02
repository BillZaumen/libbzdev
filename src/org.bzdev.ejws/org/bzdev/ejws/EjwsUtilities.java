package org.bzdev.ejws;

import org.bzdev.util.TemplateProcessor;

import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.*;

/**
 *  Useful methods for writing handlers and subclasses of WebMap.
 */
public class EjwsUtilities {
    /**
     * HTML encode a string.
     * HTML encoding replaces the special characters "&lt;", "&gt;", and
     * "&amp;" with "&amp;lt;", "&amp;gt;", and * "&amp;amp;" respectively.
     * @param string the string to encode
     * @return the encoded string
     */
    public static String htmlEncode(String string) {
	StringTokenizer tk = new StringTokenizer(string, "<>&", true);
	StringBuilder sb = new StringBuilder(64 + string.length());

	while (tk.hasMoreTokens()) {
	    String s = tk.nextToken();
	    if (s.equals("&")) sb.append("&amp;");
	    else if (s.equals(">")) sb.append("&gt;");
	    else if (s.equals("<")) sb.append("&lt;");
	    else sb.append(s);
	}
	return sb.toString();

    }

    /**
     * HTML encode a string, encoding double quotes as well.
     * HTML encoding replaces the special characters "&lt;", "&gt;", "&quote;"
     * and "&amp;" with "&amp;lt;", "&amp;gt;", "&amp;quote;" and "&amp;amp;"
     * respectively.
     * @param string the string to encode
     * @return the encoded string
     */
    public static String htmlQuoteEncode(String string) {
    	StringTokenizer tk = new StringTokenizer(string, "\"<>&", true);
	StringBuilder sb = new StringBuilder(64 + string.length());

	while (tk.hasMoreTokens()) {
	    String s = tk.nextToken();
	    if (s.equals("&")) sb.append("&amp;");
	    else if (s.equals("\"")) sb.append("&quot;");
	    else if (s.equals(">")) sb.append("&gt;");
	    else if (s.equals("<")) sb.append("&lt;");
	    else sb.append(s);
	}
	return sb.toString();
    }

    /**
     * Print a directory listing in HTML format and encapsulate it in a
     * WebMap.Info instance.
     * The uri argument is used for printing the directory's name, not
     * for locating the directory.
     * @param dir a directory
     * @param uri a URI or path for the directory
     * @param encoding the character encoding used by the HTML generated
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @return a WebMap.Info instance for the directory listing
     * @exception IOException an IO exception occurred
     */
    public static WebMap.Info printHtmlDir(File dir, String uri,
					   String encoding,
					   WebMap.ColorSpec colorSpec)
	throws IOException
    {
	if (!dir.canRead()) {
	    return null;
	}
	File[] files = dir.listFiles();
	ByteArrayOutputStream bos =
	    new ByteArrayOutputStream(1024 + 128 * files.length);
	printHtmlDir(dir, files, uri, encoding, bos, colorSpec);
	return new WebMap.Info(new ByteArrayInputStream(bos.toByteArray()),
			       bos.size(),
			       "text/html;charset=\"" + htmlEncode(encoding)
			       + "\"",
			       null);
    }


    /**
     * Print a directory listing in HTML format to an output stream.
     * The uri argument is used for printing the directory's name, not
     * for locating the directory.
     * @param dir a directory
     * @param uri a URI or path for the directory
     * @param encoding the character encoding used by the HTML generated
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @param os the output stream on which to print the file
     * @exception IOException an IO exception occurred
     */
    public static void printHtmlDir(File dir, String uri,
				    String encoding, OutputStream os,
				    WebMap.ColorSpec colorSpec)
	throws IOException
    {
	if (!(dir.isDirectory() && dir.canRead())) {
	    return;
	}
	printHtmlDir(dir, dir.listFiles(), uri, encoding, os, colorSpec);
    }

    private static TemplateProcessor.KeyMap EMPTY_MAP
	= new TemplateProcessor.KeyMap();

    private static void printHtmlDir(File dir, File[] files, String uri,
				     String encoding, OutputStream os,
				     WebMap.ColorSpec colorSpec)
	throws IOException
    {
	WebMap webmap = (colorSpec instanceof WebMap)? (WebMap)colorSpec:
	    null;
	boolean hideWebInf = (webmap == null)? false: webmap.getWebInfHidden();
	if (dir.isDirectory()) {
	    TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	    map.put("dirname", uri);
	    map.put("encoding", encoding);
	    int i = 0;
	    for (File file: files) {
		if (hideWebInf && file.isDirectory()
		    && file.getName().equals("WEB-INF")) {
		    continue;
		}
		i++;
	    }
	    TemplateProcessor.KeyMap[] dirmaps =
		new TemplateProcessor.KeyMap[i];
	    i = 0;
	    for (File file: files) {
		if (hideWebInf && file.isDirectory()
		    && file.getName().equals("WEB-INF")) {
		    continue;
		}
		TemplateProcessor.KeyMap dirmap =
		    new TemplateProcessor.KeyMap();
		try {
		    File parent = file.getParentFile();
		    boolean isdir = file.isDirectory();
		    if (isdir == false)  {
			String fn = file.getName();
			fn = (webmap == null)? fn: webmap.stripGZipSuffix(fn);
			file = new File(parent, fn);
		    }
		    URI furi = (parent == null)? file.toURI():
			parent.toURI().relativize(file.toURI());
		    dirmap.put("href",
			       htmlQuoteEncode(uri + furi.toString()));
		    String name = isdir? file.getName() + "/": file.getName();
		    dirmap.put("entry", htmlEncode(name));
		    if (isdir) {
			dirmap.put("isDirectory", EMPTY_MAP);
		    }
		} finally {
		    dirmaps[i++] = dirmap;
		}
	    }
	    map.put("bgcolor", colorSpec.getBackgroundColor());
	    map.put("color", colorSpec.getColor());
	    map.put("linkColor", colorSpec.getLinkColor());
	    map.put("visitedColor", colorSpec.getVisitedColor());
	    map.put("items", dirmaps);
	    TemplateProcessor processor = new TemplateProcessor(map);
	    Reader rd = new InputStreamReader
		(EjwsUtilities.class.getResourceAsStream("directory.tpl"),
		 "UTF-8");
	    /*
	    processor.processSystemResource("org/bzdev/ejws/directory.tpl",
					    encoding, os);
	    */
	    processor.processTemplate(rd, encoding, os);
	}
    }


    /**
     * Print a directory listing in HTML format to an output stream.
     * For each zip entry, the string zpath (the third argument) is stripped
     * from the start of the name if present and the entry is ignored if there
     * is no match. For the remainder of the string, all characters after the
     * first '/' are also stripped off. Additional filtering occurs if the
     * last argument's value is "true" - in that case any name that equals
     * "WEB-INF" or starts with "WEB-INF/" before the zpath string is removed
     * is ignored.
     * The directory name will consist of the URI string concatenated with
     * the zpath string. In a typical case, the URI will end with a '/'
     * and the zpath string will not start with a '/'.
     * @param file a zip file,
     * @param uri a URI or path for the zip file or a directory in
     *        the zip file
     * @param zprepath the initial part of a zip-file entry's name
     * @param zpath the remainder of the zip-file entry's name
     * @param encoding the character encoding used by the HTML generated
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @return a WebMap.Info instance for the directory listing
     * @exception IOException an IO exception occurred
     */
    public static WebMap.Info
	printHtmlDir(File file, String uri, String zprepath, String zpath,
		     String encoding, WebMap.ColorSpec colorSpec)
	throws IOException
    {
	return printHtmlDir(new FileInputStream(file), uri, zprepath, zpath,
			    encoding, colorSpec);
    }

    /**
     * Print a directory listing in HTML format to an output stream.
     * For each zip entry, the string zpath (the third argument) is stripped
     * from the start of the name if present and the entry is ignored if there
     * is no match. For the remainder of the string, all characters after the
     * first '/' are also stripped off. Additional filtering occurs if the
     * last argument's value is "true" - in that case any name that equals
     * "WEB-INF" or starts with "WEB-INF/" before the zpath string is removed
     * is ignored.
     * The directory name will consist of the URI string concatenated with
     * the zpath string. In a typical case, the URI will end with a '/'
     * and the zpath string will not start with a '/'.
     * @param is an input stream used to read a file in ZIP format
     * @param uri a URI or path name for the zip file or a directory in
     *        the zip file
     * @param zprepath the initial part of a zip-file entry's name
     * @param zpath the initial part of a zip-file entry's name
     * @param encoding the character encoding used by the HTML generated
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @return a WebMap.Info instance for the directory listing
     * @exception IOException an IO exception occurred
     */
    public static WebMap.Info
	printHtmlDir(InputStream is, String uri, String zprepath,
		     String zpath, String encoding,
		     WebMap.ColorSpec colorSpec)
	throws IOException
    {
	ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
	printHtmlDir(is, uri, zprepath, zpath, encoding, bos, colorSpec);
	return new WebMap.Info(new ByteArrayInputStream(bos.toByteArray()),
			       bos.size(),
			       "text/html;charset=\"" + htmlEncode(encoding)
			       + "\"",
			       null);
    }

    /**
     * Print a directory listing in HTML format to an output stream.
     * For each zip entry, the string zpath (the third argument) is stripped
     * from the start of the name if present and the entry is ignored if there
     * is no match. For the remainder of the string, all characters after the
     * first '/' are also stripped off. Additional filtering occurs if the
     * last argument's value is "true" - in that case any name that equals
     * "WEB-INF" or starts with "WEB-INF/" before the zpath string is removed
     * is ignored.
     * The directory name will consist of the URI string concatenated with
     * the zpath string. In a typical case, the URI will end with a '/'
     * and the zpath string will not start with a '/'.
     * @param file a ZIP file,
     * @param uri a URI for the directory
     * @param zpath the initial part of a zip-file entry's name
     * @param encoding the character encoding used by the HTML generated
     * @param os the output stream on which to print the file
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @exception IOException an IO exception occurred
     */
    public static void printHtmlDir(File file, String uri, String zpath,
				    String encoding, OutputStream os,
				    WebMap.ColorSpec colorSpec)
	throws IOException
    {
	printHtmlDir(new FileInputStream(file), uri, null, zpath,
		     encoding, os, colorSpec);
    }

    /**
     * Print a directory listing in HTML format to an output stream given an
     * input stream that will read a ZIP file.
     * For each zip entry, the string zpath (the third argument) is stripped
     * from the start of the name if present and the entry is ignored if there
     * is no match. For the remainder of the string, all characters after the
     * first '/' are also stripped off. Additional filtering occurs if the
     * last argument's value is "true" - in that case any name that equals
     * "WEB-INF" or starts with "WEB-INF/" before the zpath string is removed
     * is ignored.
     * The directory name will consist of the URI string concatenated with
     * the zpath string. In a typical case, the URI will end with a '/'
     * and the zpath string will not start with a '/'.
     * @param is an input stream for a file in Zip format
     * @param uri a URI for the directory
     * @param zprepath the initial part of a zip-file entry's name
     * @param zpath the initial part of a zip-file entry's name
     * @param encoding the character encoding used by the HTML generated
     * @param os the output stream on which to print the file
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @exception IOException an IO exception occurred
     */
    public static void printHtmlDir(InputStream is, String uri,
				    String zprepath, String zpath,
				    String encoding, OutputStream os,
				    WebMap.ColorSpec colorSpec)
	throws IOException
    {
	WebMap webmap = (colorSpec instanceof WebMap)? (WebMap)colorSpec:
	    null;
	boolean hideWebInf = (webmap == null)? false: webmap.getWebInfHidden();
	TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	if (!uri.endsWith("/")) {
	    uri = uri + "/";
	}
	if (zprepath == null) {
	    zprepath = "";
	} else if (!zprepath.endsWith("/") && zprepath.length() > 1) {
	    zprepath  = zprepath + "/";
	}
	if (zpath.charAt(0) == '/') {
	    zpath = zpath.substring(1);
	}
	String zipPath = zprepath + zpath;
	map.put("dirname", uri + zpath);
	map.put("encoding", encoding);
	// InputStream reads a zip file
	TreeSet<String> set = new TreeSet<String>();
	ZipInputStream zis = new ZipInputStream(is);
	ZipEntry entry;
	while((entry = zis.getNextEntry()) != null) {
	    String name = entry.getName();
	    if (name.startsWith(zipPath) &&
		(!hideWebInf || !(name.startsWith("WEB-INF/")
				  || name.equals("WEB-INF")))) {
		name = name.substring(zipPath.length());
		if (webmap != null && !name.endsWith("/")) {
		    name = (webmap == null)? name: webmap.stripGZipSuffix(name);
		}
		int ind = name.indexOf('/');
		if (ind == -1 || ind == name.length()-1) {
		    set.add(name);
		} else {
		    set.add(name.substring(0, ind+1));
		}
	    }
	    zis.closeEntry();
	}
	TemplateProcessor.KeyMap[] dirmaps =
	    new TemplateProcessor.KeyMap[set.size()];
	int i = 0;
	for (String name: set) {
	    TemplateProcessor.KeyMap dirmap = new TemplateProcessor.KeyMap();
	    dirmap.put("href", htmlQuoteEncode(uri + zpath + name));
	    dirmap.put("entry", htmlEncode(name));
	    if (name.endsWith("/")) {
		dirmap.put("isDirectory", EMPTY_MAP);
	    }
	    dirmaps[i++] = dirmap;
	}
	map.put("bgcolor", colorSpec.getBackgroundColor());
	map.put("color", colorSpec.getColor());
	map.put("linkColor", colorSpec.getLinkColor());
	map.put("visitedColor", colorSpec.getVisitedColor());
	map.put("items", dirmaps);
	TemplateProcessor processor = new TemplateProcessor(map);
	Reader rd = new InputStreamReader
	    (EjwsUtilities.class.getResourceAsStream("directory.tpl"), "UTF-8");
	/*
	processor.processSystemResource("org/bzdev/ejws/directory.tpl",
					encoding, os);
	*/
	processor.processTemplate(rd, encoding, os);
    }

    /**
     * Print a directory listing in HTML format and encapsulate it in
     * an instance of WebMap.Info, given a ZIP file as the input.
     * For each zip entry, the string zpath (the third argument) is stripped
     * from the start of the name if present and the entry is ignored if there
     * is no match. For the remainder of the string, all characters after the
     * first '/' are also stripped off. Additional filtering occurs if the
     * last argument's value is "true" - in that case any name that equals
     * "WEB-INF" or starts with "WEB-INF/" before the zpath string is removed
     * is ignored.
     * The directory name will consist of the URI string concatenated with
     * the zpath string. In a typical case, the URI will end with a '/'
     * and the zpath string will not start with a '/'.
     * @param zipfile an input stream for a file in Zip format
     * @param uri a URI for the directory
     * @param zprepath the initial part of a zip-file entry's name
     * @param zpath the initial part of a zip-file entry's name
     * @param encoding the character encoding used by the HTML generated
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @return a WebMap.Info instance for the directory listing
     * @exception IOException an IO exception occurred
     */
    public static WebMap.Info
	printHtmlDir(ZipFile zipfile, String uri, String zprepath, String zpath,
		     String encoding, WebMap.ColorSpec colorSpec)
	throws IOException
    {
	ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
	printHtmlDir(zipfile, uri, zprepath, zpath, encoding, bos, colorSpec);
	return new WebMap.Info(new ByteArrayInputStream(bos.toByteArray()),
			       bos.size(),
			       "text/html;charset=\"" + htmlEncode(encoding)
			       + "\"",
			       null);
    }

    /**
     * Print a directory listing in HTML format to an output stream, given
     * a ZIP file as the input.
     * For each zip entry, the string zpath (the third argument) is stripped
     * from the start of the name if present and the entry is ignored if there
     * is no match. For the remainder of the string, all characters after the
     * first '/' are also stripped off. Additional filtering occurs if the
     * last argument's value is "true" - in that case any name that equals
     * "WEB-INF" or starts with "WEB-INF/" before the zpath string is removed
     * is ignored.
     * The directory name will consist of the URI string concatenated with
     * the zpath string. In a typical case, the URI will end with a '/'
     * and the zpath string will not start with a '/'.
     * @param zipfile a zip file
     * @param uri a URI for the directory
     * @param zprepath the initial part of a zip-file entry's name
     * @param zpath the initial part of a zip-file entry's name
     * @param encoding the character encoding used by the HTML generated
     * @param os the output stream on which to print the file
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @exception IOException an IO exception occurred
     */
    public static void printHtmlDir(ZipFile zipfile, String uri,
				    String zprepath, String zpath,
				    String encoding, OutputStream os,
				    WebMap.ColorSpec colorSpec)
	throws IOException
    {
	WebMap webmap = (colorSpec instanceof WebMap)? (WebMap)colorSpec:
	    null;
	boolean hideWebInf = (webmap == null)? false: webmap.getWebInfHidden();
	if (uri == null) uri = "/";
	if (!uri.endsWith("/")) {
	    uri = uri + "/";
	}
	if (zprepath == null) zprepath = "";
	if (!zprepath.endsWith("/") && zprepath.length() > 1) {
	    zprepath  = zprepath + "/";
	}
	if (zpath == null) zpath = "";
	if (zpath.length() > 0 && zpath.charAt(0) == '/') {
	    zpath = zpath.substring(1);
	}
	String zipPath = zprepath + zpath;
	// System.out.println("zipPath = " + zipPath);
	TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	map.put("dirname", uri + zpath);
	map.put("encoding", encoding);
	// InputStream reads a zip file
	TreeSet<String> set = new TreeSet<String>();
	// ZipInputStream zis = new ZipInputStream(is);
	Enumeration<? extends ZipEntry> entries = zipfile.entries();
	while(entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();
	    String name = entry.getName();
	    // System.out.println("zpath = \"" + zpath + "\"");
	    // System.out.print("name = \"" + name + "\"");
	    if (name.equals(zipPath)) continue;
	    if (name.startsWith(zipPath) &&
		(!hideWebInf || !(name.startsWith("WEB-INF/")
				  || name.equals("WEB-INF/")))) {
		name = name.substring(zipPath.length());
		if (webmap != null && !name.endsWith("/")) {
		    name = (webmap == null)? name: webmap.stripGZipSuffix(name);
		}
		int ind = name.indexOf('/');
		if (ind == -1 || ind == name.length()-1) {
		    set.add(name);
		    // System.out.println(", adding " + name);
		} else {
		    set.add(name.substring(0, ind+1));
		    //System.out.println(", adding " + name.substring(0,ind+1));
		}
	    }
	}
	TemplateProcessor.KeyMap[] dirmaps =
	    new TemplateProcessor.KeyMap[set.size()];
	int i = 0;
	for (String name: set) {
	    TemplateProcessor.KeyMap dirmap = new TemplateProcessor.KeyMap();
	    dirmap.put("href", htmlQuoteEncode(uri + zpath + name));
	    dirmap.put("entry", htmlEncode(name));
	    if (name.endsWith("/")) {
		dirmap.put("isDirectory", EMPTY_MAP);
	    }
	    dirmaps[i++] = dirmap;
	}
	map.put("bgcolor", colorSpec.getBackgroundColor());
	map.put("color", colorSpec.getColor());
	map.put("linkColor", colorSpec.getLinkColor());
	map.put("visitedColor", colorSpec.getVisitedColor());
	map.put("items", dirmaps);
	TemplateProcessor processor = new TemplateProcessor(map);
	Reader rd = new InputStreamReader
	    (EjwsUtilities.class.getResourceAsStream("directory.tpl"), "UTF-8");
	/*
	processor.processSystemResource("org/bzdev/ejws/directory.tpl",
					encoding, os);
	*/
	processor.processTemplate(rd, encoding, os);
    }

    /**
     * Print a directory listing in HTML format and encapsulate it
     * in an instance of WebMap.Info, given a set of paths.
     * The members of pathSet that are used are the ones starting
     * with the string prefix.  That string is removed, and the portion
     * of the remainder up to the first '/' is kept as the name of the
     * directory entry.  An HTML-formatted file is provided to the WebMap.Info
     * return value using the specified character encoding.
     * The directory name will be the prefix string.
     * @param pathSet a set of paths
     * @param prefix the initial path prefix
     * @param encoding the character encoding used by the HTML generated
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @return an object encapsulating the directory listing and information
     *         useful for displaying the listing.
     * @exception IOException an IO exception occurred
     */
    static public WebMap.Info printHtmlDir(Set<String> pathSet,
					   String prefix,
					   String encoding,
					   WebMap.ColorSpec colorSpec)
	throws IOException
    {
	ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
	printHtmlDir(pathSet, prefix, encoding, bos, colorSpec);
	return new WebMap.Info(new ByteArrayInputStream(bos.toByteArray()),
			       bos.size(),
			       "text/html;charset=\"" + htmlEncode(encoding)
			       + "\"",
			       null);
    }

    /**
     * Print a directory listing in HTML format to an output stream given
     * a set of paths.
     * The members of pathSet that are used are the ones starting
     * with the string prefix.  That string is removed, and the portion
     * of the remainder up to the first '/' is kept as the name of the
     * directory entry.  An HTML-formatted file is written to the output
     * stream using the specified character encoding.
     * The directory name will be the prefix string.
     * @param pathSet a set of paths
     * @param prefix the initial path prefix
     * @param encoding the character encoding used by the HTML generated
     * @param os the output stream
     * @param colorSpec the instance of WebMap.ColorSpec that called this method
     * @exception IOException an IO exception occurred
     */
    public static void printHtmlDir(Set<String> pathSet, String prefix,
				    String encoding, OutputStream os,
				    WebMap.ColorSpec colorSpec)
	throws IOException
    {


	WebMap webmap = (colorSpec instanceof WebMap)? (WebMap)colorSpec:
	    null;
	boolean hideWebInf = (webmap == null)? false: webmap.getWebInfHidden();

	TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	map.put("dirname", prefix);
	map.put("encoding", encoding);
	// InputStream reads a zip file
	TreeSet<String> set = new TreeSet<String>();
	for (String path: pathSet) {
	    if (path.startsWith(prefix)) {
		String p = path.substring(prefix.length());
		if (webmap != null && !p.endsWith("/")) {
		    p = (webmap == null)? p: webmap.stripGZipSuffix(p);
		}
		int ind = p.indexOf('/');
		if(ind > 0) {
		    String name = p.substring(0, ind+1);
		    set.add(name);
		} else {
		    set.add(p);
		}
	    }
	}
	TemplateProcessor.KeyMap[] dirmaps = new TemplateProcessor.KeyMap[set.size()];
	int i = 0;
	for (String name: set) {
	    TemplateProcessor.KeyMap dirmap = new TemplateProcessor.KeyMap();
	    dirmap.put("href", htmlQuoteEncode(name));
	    dirmap.put("entry", htmlEncode(URLDecoder.decode(name, "UTF-8")));
	    if (name.endsWith("/")) {
		dirmap.put("isDirectory", EMPTY_MAP);
	    }
	    dirmaps[i++] = dirmap;
	}
	map.put("bgcolor", colorSpec.getBackgroundColor());
	map.put("color", colorSpec.getColor());
	map.put("linkColor", colorSpec.getLinkColor());
	map.put("visitedColor", colorSpec.getVisitedColor());
	map.put("items", dirmaps);
	TemplateProcessor processor = new TemplateProcessor(map);
	Reader rd = new InputStreamReader
	    (EjwsUtilities.class.getResourceAsStream("directory.tpl"), "UTF-8");
	/*
	processor.processSystemResource("org/bzdev/ejws/directory.tpl",
					encoding, os);
	*/
	processor.processTemplate(rd, encoding, os);
    }
}

//  LocalWords:  WebMap ColorSpec os Appendable charset lt dir uri hideWebInf
//  LocalWords:  html dirname href zpath InputStream zipfile zis UTF
//  LocalWords:  ZipInputStream pathSet
