package org.bzdev.net;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

//@exbundle org.bzdev.net.lpack.Net


// private class
class OurHeaderOpsMap extends LinkedHashMap<String,List<String>>
    implements HeaderOps
{
    private final int OFFSET = (int)'A' - (int)'a';

    // Match the capitalization that Headers uses for compatibility:
    // because Headers implements Map, it has a putAll method, but that
    // method does not modify a key so it will have the desired
    // capitalization.
    private String capitalize(String key) {
	if (key == null) return null;
	int len = key.length();
	if (len == 0) return key;
	char[] chars = key.toCharArray();
	char ch = chars[0];
	
	if (ch >= 'a' && ch <= 'z') {
	    chars[0] = (char)(ch  + OFFSET);
	}

	for (int i = 1; i < chars.length; i++) {
	    ch = chars[i];
	    if (ch >= 'A' && ch <= 'Z') {
		chars[i] = (char)(ch - OFFSET);
	    }
	}
	return new String(chars);
    }

    OurHeaderOpsMap() {
	super();
    }

    @Override
    public List<String>get(Object key) {
	if (key == null) return null;
	else if (key instanceof String) {
	    return super.get(capitalize((String)key));
	} else {
	    return null;
	}
    }

    @Override
    public boolean containsKey(Object key) {
	if (key == null) return false;
	if (key instanceof String) {
	    return super.containsKey(capitalize((String)key));
	} else {
	    return false;
	}
    }
    
    @Override
    public List<String> put(String key, List<String> list) {
	return super.put(capitalize(key), list);
    }

    @Override
    public List<String> remove(Object key) {
	if (key == null) return null;
	else if (key instanceof String) {
	    return super.remove(capitalize((String)key));
	} else {
	    return null;
	}
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> map) {
	// iteration ensures proper key capitalization
	for (Map.Entry<? extends String,? extends List<String>>
		 entry: map.entrySet()) {
	    put(entry.getKey(), entry.getValue());
	}
    }
}

// Internal class used to provide various options. This is also used
// by HttpServerRequest.
class HeaderParser {

    static int parseFirst(Map<String,String> map, String name,
			  String hdr, int offset, boolean acceptCommas)
	throws IllegalStateException
    {
	int hlen = hdr.length();
	char c;
	while ((c = hdr.charAt(offset)) == ' ' || c == '\t') offset++;

	int[] locs = new int[hlen-offset];
	boolean quoting = false;

	int n = 0;
	boolean prevNotBackslash = true; // false when '\' is special
	int m = 1;
	boolean saweq = false;
	boolean sawsc = false;
	int commentDepth = 0;
	int retval = hlen;

	for (int i = offset; i < hlen; i++) {
	    char ch = hdr.charAt(i);
	    switch(ch) {
	    case ',':
		if (acceptCommas) {
		    prevNotBackslash = true;
		    break;
		} else {
		    if (quoting == false && commentDepth == 0) {
			hlen = i;
			retval = hlen+1; 
			while (hlen > 0 && hdr.charAt(hlen-1) == ';') { 
			    hlen--;
			}
		    }
		    break;
		}
	    case '=':
		if (!quoting && ((m == 1 && !saweq) || sawsc)) {
		    if (m == 1) {
			saweq = true;
		    }
		    locs[n++] = i;
		    m++;
		    sawsc = false;
		}
		prevNotBackslash = true;
		break;
	    case '(':
		if (!quoting && prevNotBackslash) {
		    locs[n++] = i;
		    commentDepth++;
		}
		break;
	    case ')':
		if (!quoting && prevNotBackslash) {
		    locs[n++] = i;
		    commentDepth--;
		}
		break;
	    case ';':
		if (!quoting) {
		    locs[n++] = i;
		    m++;
		    if (sawsc) m++;
		    sawsc = true;
		}
		prevNotBackslash = true;
		break;
	    case '\"':
		if (prevNotBackslash && commentDepth == 0) {
		    locs[n++] = i;
		    quoting = !quoting;
		}
		prevNotBackslash = true;
		break;
	    case '\\':
		if ((quoting || commentDepth != 0) && prevNotBackslash) {
		    locs[n++] = i;
		    prevNotBackslash = false;
		} else {
		    prevNotBackslash = true;
		}
		break;
	    default:
		prevNotBackslash = true;
		break;
	    }
	}
	if (quoting) {
	    throw new IllegalStateException
		(NetErrorMsg.errorMsg("quotingError", name));
	}

	if (sawsc && hdr.charAt(hlen-1) != ';') {
	    m++;
	}
	String[] result = new String[m];
	StringBuilder sb = new StringBuilder();
	int mm = 0;
	int lastind = offset;
	sawsc = false;
	commentDepth = 0;
	boolean haseq = false;
	boolean commentEnded = false;
	String substring = null;
	for (int i = 0; i < n; i++) {
	    int ind = locs[i];
	    substring = hdr.substring(lastind, ind);
	    char ch = hdr.charAt(ind);
	    switch (ch) {
	    case '(':
		if (commentDepth == 0) {
		    if (haseq || commentEnded) {
			sb.append(substring.trim());
		    } else {
			sb.append(substring.stripTrailing());
		    }
		}
		commentDepth++;
		commentEnded = false;
		break;
	    case ')':
		commentDepth--;
		if (commentDepth == 0) {
		    // Replace a comment with a single space so we
		    // won't merge tokens.
		    if (haseq == false || sawsc == false) {
			sb.append(" ");
		    }
		    haseq = false;
		    commentEnded = true;
		} else if (commentDepth < 0) {
		    throw new IllegalStateException
			(NetErrorMsg.errorMsg("commentDepth", name));
		}
		break;
	    case '\"':
		if (commentDepth == 0) {
		    if (haseq || sawsc || commentEnded) {
			sb.append(substring.stripLeading());
		    } else {
			sb.append(substring);
		    }
		    haseq = false;
		    sawsc = false;
		    commentEnded = false;
		}
		break;
	    case '\\':
		if (commentDepth == 0) {
		    if (haseq || commentEnded) {
			sb.append(substring.stripLeading());
			haseq = false;
		    } else {
			sb.append(substring);
		    }
		    commentEnded = false;
		}
		break;
	    case '=':
		if (commentDepth == 0) {
		    sb.append(substring.trim());
		    result[mm++] = sb.toString();
		    sb.setLength(0);
		    sawsc = false;
		    haseq = true;
		    commentEnded = false;
		}
		break;
	    case ';':
		if (commentDepth == 0) {
		    if (sawsc) {
			String token = substring.trim();
			if (token.length() > 0) {
			    sb.append(token);
			}
		    } else {
			if (commentEnded) {
			    sb.append(substring.trim());
			} else {
			    sb.append(substring.stripTrailing());
			}
		    }
		    result[mm++] = sb.toString();
		    sb.setLength(0);
		    if (sawsc) {
			result[mm++] = null;
		    }
		    sawsc = true;
		    commentEnded = false;
		}
		break;
	    }
	    lastind = ind + 1;
	}
	if (commentDepth != 0) {
	    throw new IllegalStateException
		(NetErrorMsg.errorMsg("commentDepth", name));
	}
	if (lastind < hlen) {
	    substring = hdr.substring(lastind, hlen);
	    if (sawsc) {
		String token = substring.trim();
		if (token.length() > 0) {
		    sb.append(token);
		}
	    } else {
		if (commentEnded) {
		    sb.append(substring.trim());
		} else {
		    sb.append(substring.stripTrailing());
		}
	    }
	}
	result[mm++] = sb.toString();
	if (sawsc) {
	    result[mm++] = null;
	}
	int start = saweq? 0 : 1;
	// Map<String,String> map = new LinkedHashMap<>(result.length);
	if (start == 1) map.put(name, result[0]);
	for (int i = start; i < m; i += 2) {
	    if (result[i].trim().length() == 0) continue;
	    map.put(result[i].toLowerCase(Locale.US), result[i+1]);
	}
	return retval;
    }
}


/**
 * Interface for classes managing HTTP headers.
 * This is modeled after the class {@link com.sun.net.httpserver.Headers}
 * and is provided so that the module jdk.httpserver will not be needed.
 * This is useful if a class is to be used with Java's internal
 * HTTP server and also with a servlet.
 */
public interface HeaderOps extends Map<String,List<String>> {

    /**
     * Returns the first value (or only) value associated with
     * a specified key.
     * @param key the key
     * @return the value; null if there is none
     */
    default String getFirst(String key) {
	List<String> list = get(key);
	if (list == null) {
	    return null;
	} else {
	    return list.get(0);
	}
    }

    /**
     * Set the first (or only) value for a header with the specified key.
     * Existing values will be replaced.
     * @param key the key
     * @param value the value
     */ 
    default void set(String key, String value) {
	List<String> list = new LinkedList<String>();
	list.add(value);
	put(key, list);
    }

    /**
     * Add a value to a multivalued header.
     * If the header does not exist, a new entry will be created.
     * This method can be used after calling {@link #set(String,String)}.
     * @param key the key
     * @param value the value to add
     */ 
    default void add(String key, String value) {
	List<String> list = get(key);
	if (list == null) {
	    list = new LinkedList<String>();
	    list.add(value);
	    put(key, list);
	} else {
	    list.add(value);
	}
    }

    /**
     * Create a new instance of this interface with a default
     * implementation.
     * @return the new instance of {@link HeaderOps}
     */
    static HeaderOps newInstance() {
	return new OurHeaderOpsMap();
    }

    /**
     * Parse a single-valued HTTP header.
     * The result is map whose keys are the parameter names and whose
     * values are the parameter values.  As a special case, the value
     * of the header, excluding its parameters, has the header's name
     * as the header's key, unless it contains "=", in which case the
     * corresponding name and value becomes the first entry in the map
     * that is returned. Double quotes and escapes will be removed.
     * Each key in the map is a lower-case string.
     * <P>
     * For example, the header
     * <BLOCKQUOTE><PRE><CODE>
     *   content-type: text/plain; charset=utf-8
     * </CODE></PRE></BLOCKQUOTE>
     * would return a map with two entries: the key "content-type" with
     * "text/plain" as its value, and the key "charset" with "utf-8" as
     * its value. While generally parameters are name-value pairs, in a
     * few cases, parameters may consist of a name without a value, in
     * which case the value will be null. One should use the containsKey
     * method to determine if such entries exist.
     * <P>
     * Typically a comma (unless quoted or in a comment) is treated as
     * a delimiter separating multiple values.  A few headers (e.g.
     * cookies) use commas as part of a value.
     * @param name the name of the header
     * @param acceptCommas true if commas are valid in a value or a
     *        parameter; false if commas are delimiters separating
     *        multiple values
     * @return a map providing the value and parameters for the header;
     *         null if there is no header with the specified name
     * @exception IllegalStateException comments were not nested correctly
     */
    default Map<String,String> parseFirst(String name, boolean acceptCommas)
	throws IllegalStateException
    {
	String hdr = getFirst(name);
	if (hdr == null) return null;
	hdr = hdr.trim();
	int hlen = hdr.length();
	if (hlen == 0) {
	    return null;
	}
	int newlen = hlen;
	while (newlen > 0 && hdr.charAt(newlen-1) == ';') { 
	    newlen--;
	}
	if (newlen < hlen) {
	    hdr = hdr.substring(0, newlen);
	}
	Map<String,String> map = new LinkedHashMap<String,String>();
	HeaderParser.parseFirst(map, name, hdr, 0, acceptCommas);
	return map;
    }


    /**
     * Parse a multi-valued HTTP header.
     * The result is a list of maps, where each value in the header is
     * represented by a map whose keys are the parameter names and
     * whose values are the parameter values.  As a special case, the
     * value of the header, excluding its parameters, has the header's
     * name as the header's key, unless it contains "=", in which case the
     * corresponding name and value becomes the first entry in the map
     * that is returned. Double quotes and escapes will be
     * removed. All keys for these maps are in lower case.
     * <P>
     * For example, the header
     * <BLOCKQUOTE><PRE><CODE>
     *   accept: text/plain; charset=utf-8, text/html; charset=utf-8
     * </CODE></PRE></BLOCKQUOTE>
     * would return a list containing two maps, each with two entries:
     * the key "accept" with "text/plain" as its value, and the
     * key "charset" with "utf-8" as its value. While generally
     * parameters are name-value pairs, in a few cases, parameters may
     * consist of a name without a value, in which case the value will
     * be null. One should use the containsKey method to determine if
     * such entries exist.
     * <P>
     * Typically a comma (unless quoted or in a comment) is treated as
     * a delimiter separating multiple values.  A few headers (e.g.
     * cookies) use commas as part of a value, in which case there may
     * be multiple separate headers, all with the same name.
     * @param name the name of the header
     * @param acceptCommas true if commas are valid in a value or a
     *        parameter; false if headers are delimiters separating
     *        multiple values
     * @return a list of maps providing the value and parameters for each
     *         of the headers multiple values; null if there is no header
     *         with the specified name
     * @exception IllegalStateException comments were not nested correctly
     */
    default List<Map<String,String>>parseAll(String name,
					     boolean acceptCommas)
	throws IllegalStateException
    {
	LinkedList<Map<String,String>> list =
	    new LinkedList<Map<String,String>>();
	List<String> headers = get(name);
	if (headers == null) return Collections.emptyList();
	for (String hdr: headers) {
	    if (hdr == null) continue;
	    hdr = hdr.trim();
	    int hlen = hdr.length();
	    if (hlen == 0) continue;
	    int newlen = hlen;
	    while (newlen > 0 && hdr.charAt(newlen-1) == ';') { 
		newlen--;
	    }
	    if (newlen < hlen) {
		hdr = hdr.substring(0, newlen);
	    }
	    int offset = 0;
	    while (offset < hlen) {
		Map<String,String> map = new LinkedHashMap<String,String>();
		offset = HeaderParser.parseFirst(map, name, hdr, offset,
						 acceptCommas);
		list.add(map);
	    }
	}
	return list;
    }
}

//  LocalWords:  exbundle putAll HttpServerRequest quotingError jdk
//  LocalWords:  commentDepth LinkedHashMap httpserver servlet PRE
//  LocalWords:  multivalued BLOCKQUOTE charset utf containsKey html
//  LocalWords:  acceptCommas IllegalStateException
