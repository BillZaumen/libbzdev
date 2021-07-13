package org.bzdev.net;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.StringTokenizer;
import java.util.Map;

//@exbundle org.bzdev.net.lpack.Net

/**
 * Encoder for HTML and Javascript strings.
 */
public class WebEncoder {

    /**
     * Encode a string so that it will be displayed as is in an HTML
     * page.  If the string appears in an element's attribute value,
     * the attribute is assumed to be delimited by double quotes: the
     * characters encoded are '&amp;', '&quot;', '&gt;' and '&lt;'.
     * @param string the input string
     * @return the encoded string; null if the input string is null
     */
    public static String htmlEncode(String string) {
	if (string == null) return null;
	StringTokenizer tk = new StringTokenizer(string, "\"<>&", true);
	StringBuilder sb = new StringBuilder(64);

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
     * Encode a string so that it can be used inside a Javascript string.
     * The characters that are encoded are the '\', '"',
     * newline, form-feed, carriage return, "'", backspace, and tab
     *  characters, which are encoded as '\\', '\"', '\n', '\f', '\r',
     *  "\'" '\b', and '\t' respectively.
     * @param string the input string
     * @return the encoded string; null if the input string is null
     */
    public static String quoteEncode(String string) {
	if (string == null) return null;

	StringTokenizer tk = new StringTokenizer(string,
						 "\\\"\n\r\f\b\t'",
						 true);
	StringBuilder sb = new StringBuilder(64);

	while (tk.hasMoreTokens()) {
	    String s = tk.nextToken();
	    if (s.equals("\"")) sb.append("\\\"");
	    else if (s.equals("\\")) sb.append("\\\\");
	    else if (s.equals("\n")) sb.append("\\n");
	    else if (s.equals("\f")) sb.append("\\f");
	    else if (s.equals("\r")) sb.append("\\r");
	    else if (s.equals("'")) sb.append("\\'");
	    else if (s.equals("\b")) sb.append("\\b");
	    else if (s.equals("\t")) sb.append("\\t");
	    else sb.append(s);
	}
	return sb.toString();
    }

    private static Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Convert a map of keyword-value pairs to application/x-www-form-encoded
     * data, using a default charset with ampersands separating the
     * keyword-value pairs.
     * The default charset is UTF-8.
     * The order in which each keyword-value pair appears is determined by
     * the map's iterator.
     * @param map the map of keyword-value pairs (both elements are strings)
     * @return a String containing the URL-encoded data.
     */
    public static String formEncode(Map<String,String> map) {
	return formEncode(map, false, UTF8);
    }


    /**
     * Convert a map of keyword-value pairs to application/x-www-form-encoded
     * data, using a default charset.
     * The default charset is UTF-8.
     * The order in which each keyword-value pair appears is determined by
     * the map's iterator.
     * @param map the map of keyword-value pairs (both elements are strings)
     * @param semicolonDelimiter true if the delimiter should be a semicolon
     *        (";") rather than an ampersand ("&amp;").
     * @return a String containing the URL-encoded data.
     */
    public static String formEncode(Map<String,String> map,
				    boolean semicolonDelimiter)
    {
	return formEncode(map, semicolonDelimiter, UTF8);
    }

    /**
     * Convert a map of keyword-value pairs to application/x-www-form-encoded
     * data, given a charset for representing strings.
     * The order in which each keyword-value pair appears is determined by
     * the map's iterator.
     * Some servers may use a semicolon instead of an ampersand to simply
     * the placement of URLs with queries in HTML documents as HTML uses
     * ampersands to start an HTML entity. This is not an issue for HTML
     * forms and hence the ampersand is frequently used.
     * The charset should normally be UTF-8. A query string is first
     * converted to a sequence of bytes using the specified charset and
     * URL percent encoding is then applied.  After that encoding the
     * delimiters are added.  Charsets used for URLs have U.S. ASCII as
     * a subset, but the percent encodings generated for other characters
     * will depend on the charset.
     * @param map the map of keyword-value pairs (both elements are strings)
     * @param semicolonDelimiter true if the delimiter should be a semicolon
     *        (";") rather than an ampersand ("&amp;").
     * @param charset the charset used to represent strings.
     * @return a Sting containing the URL-encoded data.
     */
    public static String formEncode(Map<String,String> map,
				    boolean semicolonDelimiter,
				    Charset charset)
    {
	StringBuilder sb = new StringBuilder(128);
	String separator = "";
	for (Map.Entry<String,String> entry: map.entrySet()) {
	    sb.append(separator);
	    String key = entry.getKey();
	    String value = entry.getValue();
	    sb.append(URLEncoder.encode(key, charset));
	    sb.append("=");
	    sb.append(URLEncoder.encode(value, charset));
	    separator = semicolonDelimiter? ";": "&";
	}
	return sb.toString();
    }
}

//  LocalWords:  exbundle Javascript lt UTF www charset Charsets
//  LocalWords:  semicolonDelimiter encodings
