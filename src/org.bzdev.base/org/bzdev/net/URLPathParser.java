package org.bzdev.net;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.io.File;
import java.io.IOException;

//@exbundle org.bzdev.net.lpack.Net

/**
 * URL Path Parser for string of URLs separated by a '|' delimiter.
 * A component in a path should be either a URL or a file name.
 * A sequence of n "|" characters will be replaced by floor(n/2) "|"
 * characters and if n is odd, the final "|" is treated as the
 * separator.  As a result, except when it is the first component, a
 * file name cannot start with "|".  If it is really necessary to have
 * a "|" at the start of a file name, use a "file" URL (in which case
 * the "|" should be encoded as "%7C".)  If a path component's initial
 * characters, up to and including the first ":" are syntactically
 * valid as part of a URL, the component is interpreted as a URL, not
 * a file name.
 * <P>
 * In addition, a leading "~", followed by either the standard
 * file-separator character or "/", is replaced with the user's home
 * directory. The sequence "~~" at the start of a component will be
 * replaced by a single "~".  All subsequent "~" characters in that
 * component are left as is.  The method
 * {@link URLPathParser#getURLs(File,String,String)} and
 * {@link URLPathParser#getURLs(File,String,String, Appendable)} will
 * treat a leading "..." followed by the file separator or "/" as
 * a shorthand for a specified directory. The programs scrunner and
 * lsnof set that directory to the one containing the BZDev library's
 * JAR file.
 */
public class URLPathParser {

    static String errorMsg(String key, Object... args) {
	return NetErrorMsg.errorMsg(key, args);
    }

    // loose test - we use this only to see if there is a scheme in
    // a  string; otherwise the string is assumed to be a file name.
    //
    private static Pattern urlPattern =
	Pattern.compile("\\p{Alpha}[\\p{Alnum}.+-]*:.*");

    private static final String dotDotDot = "...";
    private static final String dotDotDotSep = dotDotDot + File.separator;
    private static final String dotDotDotSlash = dotDotDot + "/";
    private static final String tildeTilde = "~~";
    private static final String tildeSep = "~" + File.separator;
    private static final String tildeSlash = "~" + "/";

    /**
     * Parse a list of URLs or file names, separated by "|".
     * Relative files are resolved using the current working directory.
     * In addition, for each URL, a leading<code>~~</code> will
     * be replaced with <code>~</code>, and <code>~</code> followed by
     * the filename separator will be replaced with the user's home
     * directory followed by the filename separator (in this case, a
     * security exception will be thrown if the caller does not have
     * permission to acesss the system property
     * <code>user.home</code>).
     * @param urlPath the string representation of multiple URLs or files,
     *        separated by "|"
     * @return an array of URLs
     * @throws MalformedURLException if there was a syntax error
     */
    public static URL[] getURLs(String urlPath) throws MalformedURLException {
	return getURLs(null, urlPath);
    }

    /**
     * Parse a list of URLs or file names, separated by "|", given a directory.
     * Relative files are resolved using the current working directory.
     * If the argument urlPath is an empty string, the array returned will
     * have a length of 0. In addition, for each URL, a
     * leading<code>~~</code> will be replaced with <code>~</code>,
     * and <code>~</code> followed by the filename separator will be
     * replaced with the user's home directory followed by the
     * filename separator (in this case, a security exception will be
     * thrown if the caller does not have permission to acesss the
     * system property <code>user.home</code>).
     * @param dir the directory against which to resolve relative files;
     *        null indicates the current working directory
     * @param urlPath the string representation of multiple URLs or files,
     *        separated by "|"
     * @return an array of URLs
     * @exception MalformedURLException a URL was malformed
     */
    public static URL[] getURLs(File dir, String urlPath)
	throws MalformedURLException 
    {
	return getURLs(dir, urlPath, null);
    }

    /**
     * Parse a list of URLs or file names, separated by "|", given a directory.
     * <P>
     * When the third argument <code>threeDotDir</code> is not null, some
     * substitutions may be performed
     * The string "...", if it starts a file name as opposed to a URL and
     * is followed by the name separator ("/" on Unix), will be replaced with
     * the directory given by the third argument when that argument is not
     * null.  In addition, <code>~~</code> will be replaced with <code>~</code>,
     * and <code>~</code> followed by the filename separator
     * will be replaced with the user's home directory followed by the
     * filename separator (in this case, a security exception will be thrown
     * if the caller does not have permission to acesss the system property
     * <code>user.home</code>).
     * <P>
     * A file component is not tested to see if the file (or directory)
     * exists and is readable by the application.
     * If the argument urlPath is an empty string, the array returned will
     * have a length of 0.
     * Relative files are resolved using the current working directory.
     * @param dir the directory against which to resolve relative files;
     *        null indicates the current working directory
     * @param urlPath the string representation of multiple URLs or files,
     *        separated by "|"
     * @param threeDotDir the directory that will replace a leading file-name
     *        component equal to "..."; null for no replacement
     * @return an array of URLs
     * @exception MalformedURLException a URL was malformed
     * @exception SecurityException a security exception occurred (most likely
     *            because the system property user.home could not be accessed)
     */
    public static URL[] getURLs(File dir, String urlPath, String threeDotDir)
	throws MalformedURLException, SecurityException
    {
	return getURLs(dir, urlPath, threeDotDir, null);
    }

    /**
     * Parse list of URLs or file names, separated by "|", given a directory
     * with an optional file-name test.
     * <P>
     * When the third argument <code>threeDotDir</code> is not null, some
     * substitutions may be performed
     * The string "...", if it starts a file name as opposed to a URL and
     * is followed by the name separator ("/" on Unix), will be replaced with
     * the directory given by the third argument when that argument is not
     * null.  In addition, for each URL, a leading<code>~~</code> will
     * be replaced with <code>~</code>, and <code>~</code> followed by
     * the filename separator will be replaced with the user's home
     * directory followed by the filename separator (in this case, a
     * security exception will be thrown if the caller does not have
     * permission to acesss the system property
     * <code>user.home</code>).

     * If the argument is an empty string, the array returned will
     * have a length of 0.
     * Relative files are resolved using the current working directory.
     * @param dir the directory against which to resolve relative files;
     *        null indicates the current working directory
     * @param urlPath the string representation of multiple URLs or files,
     *        separated by "|"
     * @param threeDotDir the directory that will replace a leading file-name
     *        component equal to "..."; null for no replacement
     * @param output an Appendable to log output for a file-name test; null
     *        a file-name test is not wanted
     * @return an array of URLs
     * @exception MalformedURLException a URL was malformed
     * @exception SecurityException a security exception occurred (most likely
     *            because the system property user.home could not be accessed)

     */
    public static URL[] getURLs(File dir, String urlPath, String threeDotDir,
				Appendable output)
	throws MalformedURLException, SecurityException
    {
	if (dir == null) {
	    dir = new File(System.getProperty("user.dir"));
	}
	StringTokenizer tokenizer = new StringTokenizer(urlPath, "|", true);
	ArrayList<URL> urls = new ArrayList<URL>();
	int index = 0;
	String tmp = "";
	boolean sawBar = false;
	while (tokenizer.hasMoreTokens()) {
	    String s = tokenizer.nextToken();
	    if (s.equals("|")) {
		if (sawBar) {
		    tmp = tmp + "|";
		    sawBar = false;
		} else {
		    sawBar = true;
		}
	    } else {
		if (sawBar) {
		    Matcher matcher = urlPattern.matcher(tmp);
		    if (urlPattern.matcher(tmp).matches()) {
			// the string tmp is a URL
			urls.add(new URL(tmp));
		    } else {
			// the string tmp is not a URL and hence is a file name
			if (threeDotDir != null) {
			    if (tmp.equals(dotDotDot)
				|| tmp.startsWith(dotDotDotSep)
				|| tmp.startsWith(dotDotDotSlash)) {
				tmp = threeDotDir + tmp.substring(3);
			    }
			}
			if (tmp.startsWith(tildeTilde)) {
			    tmp = tmp.substring(1);
			} else if (tmp.startsWith(tildeSep)
				   || tmp.startsWith(tildeSlash)) {
			    // Have to allow a security exception
			    // because the name of a user's home
			    // directory is sensitive data.
			    tmp = System.getProperty("user.home")
				+ tmp.substring(1);
			}
			File f  = new File(tmp);
			if (!f.isAbsolute()) f = new File(dir, tmp);
			urls.add(f.toURI().toURL());
		    }
		    tmp = s;
		    sawBar = false;
		} else {
		    tmp = tmp + s;
		}
	    }
	}
	if (tmp.length() > 0) {
	    Matcher matcher = urlPattern.matcher(tmp);
	    if (urlPattern.matcher(tmp).matches()) {
		// the string tmp is a URL
		if (output != null  && tmp.startsWith("file:")) {
		    URL url = new URL(tmp);
		    File file;
		    try {
			file = new File(url.toURI());
			if (!file.exists()) {
			    output.append(errorMsg("noFile", tmp) + "\n");
			} else if (!file.canRead()) {
			    output.append
				(errorMsg("notReadableFile", tmp) + "\n");
			} else if (!(file.isFile() || file.isDirectory())) {
			    output.append
				(errorMsg("notOrdinaryFile", tmp) + "\n");
			}
		    } catch (URISyntaxException e) {
			try {
			    output.append(errorMsg("notURL", tmp) + "\n");
			} catch (IOException eio2) {
			}
		    } catch (IOException eio) {}
		    urls.add(url);
		} else {
		    urls.add(new URL(tmp));
		}
	    } else {
		// the string tmp is not a URL and hence is a file name
			// the string tmp is not a URL and hence is a file name
		if (threeDotDir != null) {
		    if (tmp.equals(dotDotDot)
			|| tmp.startsWith(dotDotDotSep)
			|| tmp.startsWith(dotDotDotSlash)) {
			tmp = threeDotDir + tmp.substring(3);
		    }
		}
		if (tmp.startsWith(tildeTilde)) {
		    tmp = tmp.substring(1);
		} else if (tmp.startsWith(tildeSep)
			   || tmp.startsWith(tildeSlash)) {
		    // Have to allow a security exception
		    // because the name of a user's home
		    // directory is sensitive data.
		    tmp = System.getProperty("user.home")
			+ tmp.substring(1);
		}
		File f  = new File(tmp);
		if (!f.isAbsolute()) f = new File(dir, tmp);
		if (output != null) {
		    try {
			if (!f.exists()) {
			    output.append(errorMsg("noFile", tmp) + "\n");
			} else if (!f.canRead()) {
			    output.append
				(errorMsg("notReadableFile", tmp) + "\n");
			} else if (!(f.isFile() || f.isDirectory())) {
			    output.append
				(errorMsg("notOrdinaryFile", tmp) + "\n");
			}
		    } catch (IOException eio) {}
		}
		urls.add(f.toURI().toURL());
	    }
	    index++;
	}
	tmp = null;
	return urls.toArray(new URL[index]);
    }

    /**
     * Parse a path of URLs or file names that are separated by "|".
     * When an odd number of "|" characters is found, all but the
     * last are kept as part of a the current path component.
     * Each string in the array of strings that is returned can be
     * passed to on the getURLs method, which will then return an array
     * whose length is 1. If the argument is an empty string, the
     * array returned will have a length of 0.
     * <P>
     * This method does not treat "~" as a special character.
     * @param urlPath a string containing the URLs or file names.
     * @return an array of strings providing the paths or URLs.
     */
    public static String[] split(String urlPath) {
	StringTokenizer tokenizer = new StringTokenizer(urlPath, "|", true);
	ArrayList<String> urls = new ArrayList<String>();
	int index = 0;
	String tmp = "";
	boolean sawBar = false;
	while (tokenizer.hasMoreTokens()) {
	    String s = tokenizer.nextToken();
	    if (s.equals("|")) {
		if (sawBar) {
		    tmp = tmp + "||";
		    sawBar = false;
		} else {
		    sawBar = true;
		}
	    } else {
		if (sawBar) {
		    urls.add(tmp);
		    /*
		    Matcher matcher = urlPattern.matcher(tmp);
		    if (urlPattern.matcher(tmp).matches()) {
			// the string tmp is a URL
			// urls.add(new URL(tmp));
			urls.add(tmp);
		    } else {
			// the string tmp is not a URL and hence is a file name
			// File f  = new File(tmp);
			// if (!f.isAbsolute()) f = new File(dir, tmp);
			// urls.add(f.toURI().toURL());
			urls.add(tmp);
		    }
		    */
		    tmp = s;
		    sawBar = false;
		} else {
		    tmp = tmp + s;
		}
	    }
	}
	if (tmp.length() > 0) {
	    urls.add(tmp);
	    index++;
	}
	tmp = null;
	return urls.toArray(new String[index]);
    }
}

//  LocalWords:  exbundle bzdev urlPath dir MalformedURLException tmp
//  LocalWords:  threeDotDir acesss SecurityException Appendable urls
//  LocalWords:  noFile notReadableFile notOrdinaryFile notURL lsnof
//  LocalWords:  getURLs isAbsolute URLPathParser scrunner BZDev
//  LocalWords:  Matcher matcher urlPattern
