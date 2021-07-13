package org.bzdev.net;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

//@exbundle org.bzdev.net.lpack.Net


/**
 * Decoder for HTML and Javascript strings.
 */
public class WebDecoder {

    static String errorMsg(String key, Object... args) {
	return NetErrorMsg.errorMsg(key, args);
    }

    /**
     * Decode an HTML-encoded string.
     * The entities  '&amp;amp;', '&amp;quote;', '&amp;gt;' and '&amp;lt;'
     * are replaced with '&amp;', '&quot;', '&gt;' and '&lt;' respectively.
     * @param string the input string
     * @return the decoded string; null if the input string is null
     * @exception IllegalArgumentException there is a syntax error in the
     *            argument
     */
    public static String htmlDecode(String string)
	throws IllegalArgumentException
    {
	if (string == null) return null;
	StringTokenizer tk = new StringTokenizer(string, "&", true);
	if (!tk.hasMoreTokens()) {
	    return string;
	} else {
	    StringBuilder sb = new StringBuilder(128);
	    while (tk.hasMoreTokens()) {
		String s = tk.nextToken();
		if (s.equals("&")) {
		    if (!tk.hasMoreTokens()) {
			throw new
			    IllegalArgumentException(errorMsg("endingAmp"));
		    }
		    s = tk.nextToken();
		    if (s.startsWith("amp;")) {
			sb.append("&");
			s = s.substring(4);
			sb.append(s);
		    } else if (s.startsWith("quot;")) {
			sb.append("\"");
			s = s.substring(5);
			sb.append(s);
		    } else if (s.startsWith("gt;")) {
			sb.append(">");
			s = s.substring(3);
			sb.append(s);
		    } else if (s.startsWith("lt;")) {
			sb.append("<");
			s = s.substring(3);
			sb.append(s);
		    } else {
			if (s.length() > 8) {
			    s = s.substring(0, 8) + "...";
			}
			throw new IllegalArgumentException
			    (errorMsg("unexpectedEntity", s));
		    }
		} else {
		    sb.append(s);
		}
	    }
	    return sb.toString();
	}
    }


    private static Pattern qpattern = Pattern.compile("[^\\\\]*");

    /**
     * Decode a string that is encoded so that it can appear within
     * double quotes in a Java or ECMAScript program.
     * This method replaces escape sequences with their corresponding
     * characters.  The argument does not include the delimiting quotes
     * that would appear in Java or ECMA code.
     * @param string the string to decode
     * @return the decoded string.
     * @exception IllegalArgumentException there is a syntax error in the
     *            argument
     */
    public static String quoteDecode(String string)
	throws IllegalArgumentException
    {
	int length = string.length();
	Matcher matcher = qpattern.matcher(string);
	int index = 0;
	int next = 0;
	if (matcher.find()) {
	    next = matcher.end();
	    if (next == length) {
		return string;
	    }
	}
	StringBuffer sb = new StringBuffer();
	while (index < length) {
	    if (next > index) {
		sb.append(string.substring(index, next));
	    }
	    int ind = index;
	    while (string.charAt(ind) == '\\') ind++;
	    int delta = ind - index;
	    for (int i = 0; i < delta / 2; i += 2) {
		sb.append('\\');
		next += 2;
	    }
	    if ((delta % 2) == 1) {
		next++;
		if (next == length) {
		    throw new IllegalArgumentException
			(errorMsg("badString", string));
		}
		char ch = string.charAt(next);
		switch (ch) {
		case 'n':
		    sb.append('\n');
		    break;
		case 'f':
		    sb.append('\f');
		    break;
		case 'r':
		    sb.append('\r');
		    break;
		case '\'':
		    sb.append('\'');
		    break;
		case 'b':
		    sb.append('\b');
		    break;
		case 't':
		    sb.append('\t');
		    break;
		case '"':
		    sb.append('"');
		    break;
		default:
		    sb.append(ch);
		}
		next++;
	    }
	    index = next;
	    matcher.find(index);
	    next = matcher.end();
	}
	return sb.toString();
    }

    private static Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Convert a string containing application/x-www-form-encoded
     * data, given a default charset (UTF-8) and an ampersand as the separator
     * between keyword-value pairs, and create a keyword-value map.
     * The map's iterator will provide entries in the same order as the
     * keyword-value pairs in the argument.
     * @param string a String containing the URL-encoded data
     * @return a the map of keyword-value pairs (both elements are strings)
     */
    public static Map<String,String> formDecode(String string)
	throws IllegalArgumentException
    {
	return formDecode(string, false, UTF8);
    }

    /**
     * Convert a string containing application/x-www-form-encoded
     * data, given a default charset (UTF-8), and create a keyword-value map.
     * The map's iterator will provide entries in the same order as the
     * keyword-value pairs in the argument.
     * @param string a String containing the URL-encoded data
     * @param semicolonDelimiter true if the delimiter should be a semicolon
     *        (";") rather than an ampersand ("&amp;").
     * @return a the map of keyword-value pairs (both elements are strings)
     */
    public static Map<String,String> formDecode(String string,
						boolean semicolonDelimiter)
	throws IllegalArgumentException
    {
	return formDecode(string, semicolonDelimiter, UTF8);
    }

    /**
     * Convert a string containing application/x-www-form-encoded
     * data, given a charset, and create a keyword-value map.
     * The map's iterator will provide entries in the same order as the
     * keyword-value pairs in the argument.
     * @param string a String containing the URL-encoded data
     * @param semicolonDelimiter true if the delimiter should be a semicolon
     *        (";") rather than an ampersand ("&amp;").
     * @param charset the Charset used when the keyword-value pairs was encoded
     * @return a the map of keyword-value pairs (both elements are strings)
     */
    public static Map<String,String> formDecode(String string,
						boolean semicolonDelimiter,
						Charset charset)
	throws IllegalArgumentException
    {
	Map<String,String> map = new LinkedHashMap<String,String>();

	String delimiter = semicolonDelimiter? ";": "&";
	StringTokenizer tk = new StringTokenizer(string, delimiter, false);
	StringBuilder sb = new StringBuilder(128);
	String separator = "";
	while (tk.hasMoreTokens()) {
	    String entry  = tk.nextToken();
	    String[] pair = entry.split("=");
	    String key = URLDecoder.decode(pair[0], charset);
	    String value = (pair.length == 2)?
		URLDecoder.decode(pair[1], charset): null;
	    map.put(key, value);
	}
	return map;
    }

    /**
     * Read an input stream containing application/x-www-form-encoded
     * data, given a default charset (UTF-8) and an ampersand as the separator
     * between keyword-value pairs, and create a keyword-value map.
     * The map's iterator will provide entries in the same order as the
     * keyword-value pairs in the argument.
     * @param is the input stream
     * @return a the map of keyword-value pairs (both elements are strings)
     * @exception IOException an IO error occurred, including syntax errors
     *            in the input stream
     */
    public static Map<String,String> formDecode(InputStream is)
	throws IllegalArgumentException, IOException
    {
	return formDecode(is, false, UTF8);
    }

    /**
     * Read an input stream containing application/x-www-form-encoded
     * data, given a default charset (UTF-8), and create a keyword-value map.
     * The map's iterator will provide entries in the same order as the
     * keyword-value pairs in the argument.
     * @param is the input stream
     * @param semicolonDelimiter true if the delimiter should be a semicolon
     *        (";") rather than an ampersand ("&amp;").
     * @return a the map of keyword-value pairs (both elements are strings)
     * @exception IOException an IO error occurred, including syntax errors
     *            in the input stream
     */
    public static Map<String,String> formDecode(InputStream is,
						boolean semicolonDelimiter)
	throws IllegalArgumentException, IOException
    {
	return formDecode(is, semicolonDelimiter, UTF8);
    }


    /**
     * Read an input stream containing application/x-www-form-encoded
     * data, given a charset, and create a keyword-value map.
     * The map's iterator will provide entries in the same order as the
     * keyword-value pairs in the argument.
     * @param is the input stream
     * @param semicolonDelimiter true if the delimiter should be a semicolon
     *        (";") rather than an ampersand ("&amp;").
     * @param charset the Charset used when the keyword-value pairs was encoded
     * @return a the map of keyword-value pairs (both elements are strings)
     * @exception IOException an IO error occurred, including syntax errors
     *            in the input stream
     */
    public static Map<String,String> formDecode(InputStream is,
						boolean semicolonDelimiter,
						Charset charset)
	throws IllegalArgumentException, IOException
    {
	BufferedReader r = new BufferedReader(new InputStreamReader(is,
								    charset));
	try {
	return formDecode(r.readLine(), semicolonDelimiter, charset);
	} catch (IllegalArgumentException e) {
	    String msg = errorMsg("badFormEncoding");
	    throw new IOException(msg, e);
	}
    }

}

//  LocalWords:  Javascript lt IllegalArgumentException endingAmp UTF
//  LocalWords:  unexpectedEntity ECMA badString www charset
//  LocalWords:  semicolonDelimiter
