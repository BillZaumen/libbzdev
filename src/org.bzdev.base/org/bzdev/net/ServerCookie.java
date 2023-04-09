package org.bzdev.net;
import java.util.Locale;

//@exbundle org.bzdev.net.lpack.Net

class ServerCookieUtils {

    static String errorMsg(String key, Object... args) {
	return NetErrorMsg.errorMsg(key, args);
    }


    static final ServerCookie[] EMPTY = new ServerCookie[0];

    static class Cookie implements ServerCookie {
	String name;
	String value;
	String comment = null;
	String domain = null;
	int maxAge = -1;
	String path = null;
	boolean secure = false;
	boolean httpOnly = false;
	int version = 1;

	void checkToken(String token)
	    throws IllegalArgumentException, NullPointerException
	{
	    if (token == null) {
		throw new NullPointerException(errorMsg("nullToken"));
	    }
	    int len = token.length();
	    if (len == 0) {
		throw new IllegalArgumentException(errorMsg("emptyToken"));
	    }
	    for (int i = 0; i < len; i++) {
		char ch = token.charAt(i);
		if (ch >= 127 || ch <= 31) {
		    String msg = errorMsg("illegalTokenChar");
		    throw new IllegalArgumentException(msg);
		}
		switch (ch) {
		case '(': case ')': case '<': case '>': case '@':
		case ',': case ';': case ':': case '\\': case '"':
		case '/': case '[': case ']': case '?': case '=':
		case '{': case '}': case ' ': case '\t':
		    String msg2 = errorMsg("illegalTokenChar");
		    throw new IllegalArgumentException(msg2);
		}
	    }
	}

	void checkValue(String value)
	    throws IllegalArgumentException, NullPointerException
	{
	    if (value == null) {
		throw new NullPointerException(errorMsg("nullValue"));
	    }
	    int len = value.length();
	    if (len == 0) return;
	    int start= 0;
	    int end = len;
	    for (int i = start;i < end; i++) {
		char ch = value.charAt(i);
		if (ch == 0x21 || (ch >= 0x23 && ch <= 0x2B)
		    || (ch >= 0x2D && ch <= 0x3A)
		    || (ch >= 0x3C && ch <= 0x5B)
		    || (ch >= 0x5D && ch <= 0x7E)) {
		    continue;
		} else if ((ch == ' ') || (ch == '\t')) {
		    continue;
		} else {
		    String msg = errorMsg("illegalValue", "" + (int)ch);
		    throw new IllegalArgumentException(msg);
		}
	    }
	}

	void checkPath(String path) throws IllegalArgumentException {
	    if (path == null) return;
	    int len = path.length();
	    if (len == 0) {
		throw new IllegalArgumentException(errorMsg("nullPath"));
	    }
	    for (int i = 0; i < len; i++) {
		char ch = path.charAt(i);
		if (ch >= 127 || ch <= 31 || ch == ';') {
		    String msg = errorMsg("illegalPath", "" + (int) ch);
		    throw new IllegalArgumentException(msg);
		}
	    }
	}

	boolean isIPV6(String addr) {
	    return addr.matches("[a-fA-F0-9:]+");
	}

	void checkDomain(String domain) throws IllegalArgumentException {
	    if (domain == null) return;
	    int len = domain.length();
	    if (len == 0) {
		throw new IllegalArgumentException(errorMsg("emptyDomain"));
	    }
	    if (isIPV6(domain)) return;
	    // all other cases: ip4v and domain names
	    char ch = domain.charAt(0);

	    if (!((ch >= 'a' && ch <= 'z')
		  || (ch >= 'A' && ch <= 'Z')
		  || (ch >= '0' && ch <= '9'))) {
		throw new IllegalArgumentException(errorMsg("illegalDomain"));
	    }
	    if (domain.charAt(len-1) == '.') {
		throw new IllegalArgumentException(errorMsg("illegalDomain"));
	    }
	    boolean sawdot = false;
	    boolean sawhyphen = false;
	    for (int i = 1; i < len; i++) {
		ch = domain.charAt(i);
		if (ch == '.') {
		    if (sawdot) {
			String msg = errorMsg("illegalDomain");
			throw new IllegalArgumentException(msg);
		    }
		    if (sawhyphen) {
			String msg = errorMsg("illegalDomain");
			throw new IllegalArgumentException(msg);
		    }
		    sawdot = true;
		} else if (ch == '-') {
		    if (sawdot) {
			String msg = errorMsg("illegalDomain");
			throw new IllegalArgumentException(msg);
		    }
		    sawhyphen = true;
		} else if ((ch >= 'a' && ch <= 'z')
			   || (ch >= 'A' && ch <= 'Z')
			   || (ch >= '0' && ch <= '9')) {
		    sawdot = false;
		    sawhyphen = false;
		} else {
		    String msg = errorMsg("illegalDomain");
		    throw new IllegalArgumentException(msg);
		}
	    }
	    if (sawdot || sawhyphen) {
		    String msg = errorMsg("illegalDomain");
		throw new IllegalArgumentException(msg);
	    }
	}

	private String stripQuotes(String s) {
	    if (s == null) return null;
	    int len = s.length();
	    if (len == 0) return s;
	    if (s.charAt(0) == '"'  & s.charAt(len-1) == '"') {
		return s.substring(1, len-1);
	    } else {
		return s;
	    }
	}



	public Cookie(String name, String value)
	    throws IllegalArgumentException, NullPointerException
	{
	    checkToken(name);
	    this.name = name;
	    setValue(value);
	}
	
	@Override
	public String getComment() {
	    return stripQuotes(comment);
	}

	@Override
	public String getDomain() {
	    return domain;
	}

	@Override
	public int getMaxAge() {
	    return maxAge;
	}

	@Override
	public String getName() {
	    return name;
	}

	@Override
	public String getPath() {
	    return path;
	}

	@Override
	public boolean getSecure() {
	    return secure;
	}

	@Override
	public String getValue() {
	    return stripQuotes(value);
	}

	@Override
	public int getVersion() {
	    return version;
	}

	@Override
	public boolean isHttpOnly() {
	    return httpOnly;
	}

	@Override
	public void setComment(String comment) throws IllegalArgumentException {
	    if (comment != null) {
		checkValue(comment);
	    }
	    this.comment = comment;
	}

	@Override
	public void setDomain(String domain) throws IllegalArgumentException {
	    checkDomain(domain);
	    this.domain = domain;
	}

	@Override
	public void setMaxAge(int maxAge) {
	    this.maxAge = maxAge;
	}

	@Override
	public void setPath(String path) throws IllegalArgumentException {
	    checkPath(path);
	    this.path = path;
	}

	@Override
	public void setSecure(boolean secure) {
	    this.secure = secure;
	}

	@Override
	public void setValue(String value)
	    throws IllegalArgumentException, NullPointerException
	{
	    checkValue(value);
	    this.value = value;
	}

	@Override
	public void setVersion(int version) {
	    this.version = version;
	}

	@Override
	public void setHttpOnly (boolean httpOnly) {
	    this.httpOnly = httpOnly;
	}

    }

}

/**
 * Interface for HTTP cookies.
 * This interface provides the same methods as the Servlet class
 * javax.servlet.http (Java EE 7). It is provided for applications that may
 * eventually migrate to a web server that supports servlets, but
 * that initially can get by with a simpler API.
 * <P>
 * The latest cookie specification is
 * <A HREF="https://tools.ietf.org/html/rfc6265#section-5.2.2">RFC 6265</A>.
 */
public interface ServerCookie {

    /**
     * Get the comment for this cookie.
     * @return the comment; null if not set
     * @see #setComment(String)
     */
    String getComment();

    /**
     * Get the domain name for this cookie
     * @return the domain name; null if not set
     * @see #setDomain(String)
     */
    String getDomain();

    /**
     * Get the maximum age in seconds for this cookie.
     * @return the maximum age in seconds; -1 (the default) if the cookie will
     *         persist until a browser shuts down
     * @see #setMaxAge(int)
     */
    int getMaxAge();

    /**
     * Get the name of this cookie
     * @return the cookie's name
     */
    String getName();

    /**
     * Get the path to which the client should return this cookie;
     * @return the path
     */
    String getPath();

    /**
     * Indicate if the client is expected to send cookies over a secure
     * connection.
     * @return true if the connection should be secure; false otherwise
     */
    boolean getSecure();

    /**
     * Get the value of the cookie
     * <P>
     * Please see
     *  <A HREF="https://tools.ietf.org/html/rfc6265#section-5.2.2">RFC 6265</A>
     * for a description of the allowed characters.
     * @return the cookie's value
     */
    String getValue();

    /**
     * Get the version of the protocol to which this cookie complies
     * @return 0 for the original Netscape specification; 1 for RFC 2109.
     */
    int getVersion();

    /**
     * Determine if the the HTTPOnly attribute is present
     * @return true if the HTTPOnly attribute is present; false otherwise
     */
    boolean isHttpOnly();

    /**
     * Set the comment for this cookie.
     * Please see
     *  <A HREF="https://tools.ietf.org/html/rfc6265#section-5.2.2">RFC 6265</A>
     * for a description of the allowed characters.
     * @param comment the comment.
     */
    void setComment(String comment);

    /**
     * Set the domain attribute.
     * @param domain the domain name
     */
    void setDomain(String domain);

    /**
     * Set the meaximum age attribute.
     * Given RFC 6265, a -1 indicates that this attribute should not
     * appear in the cookie's Set-Cookie header.
     * @param expire the time in seconds before this cookie should
     *        expire; -1 if it should expire when the client shuts down
     */
    void setMaxAge(int expire);

    /**
     * Specify the path on the server that determines when the cookie is
     * visible - when the cookie can be sent to this server.
     * The cookie is visible for this path and its subpaths.
     * The URI must include any require '%' encoding so that only
     * printable characters (excluding a ';') may appear. This should
     * not include a query string or fragment.
     * @param uri the path
     */
    void setPath(String uri);

    /**
     * Indicate if the client should return a cookie on a secure
     * connection to the server.
     * @param secure true if the connection must be secure; false otherwise
     */
    void setSecure(boolean secure);

    /**
     * Set the value for this cookie.
     * <P>
     * Please see
     *  <A HREF="https://tools.ietf.org/html/rfc6265#section-5.2.2">RFC 6265</A>
     * for a description of the allowed characters.
     * @param value the value for this cookie
     */
    void setValue(String value);

    /**
     * Set the version for the Cookie protocol.
     * The Servlet API specification (Jarkarta Servlet 4.0) for
     * <A HREF="https://jakarta.ee/specifications/platform/8/apidocs/javax/servlet/http/Cookie.html#getVersion--">Cookie.getVersion()</A>
     * currently states, "Since RFC 2109 is still somewhat new,
     * consider version 1 as exprimental: do not use it yet on
     * production sites."  RFC 2109 was wrtten in February, 1997, and
     * the latest specification as of July 2011 is RFC 6265, dated
     * April 2011. Java EE 8 is dated August 2017 and Jarkarta EE,
     * is dated February 2018.
     * @param version 0 for the original Netscape cookie specification; 1
     *        for RFC 2109
     */
    void setVersion(int version);

    /**
     * Indicates whether or not this cookie is HTTPOnly.
     * An HttpOnly cookie is one that a browser may not use in 
     * "non-HTTP" APIs such as scripts.
     *
     * @param httpOnly true for an HTTPOnly cookie; false otherwise
     */
    void setHttpOnly(boolean httpOnly);

    /**
     * Fetch the cookies provided by a set of HTTP headers.
     * The cookies will be in the same order as in the HTTP headers.
     * @param headers the headers
     * @return the cookies; an empty array if there are none
     */
    static ServerCookie[] fetchCookies(HeaderOps headers) {
	String cookies = headers.getFirst("Cookie");
	if (cookies == null) return ServerCookieUtils.EMPTY;
	cookies = cookies.trim();
	if (cookies.length() == 0) return ServerCookieUtils.EMPTY;
	String[] array = cookies.split("; ");
	ServerCookie[] carray = new ServerCookie[array.length];
	int i = 0;
	for (String cookie: array) {
	    String[] pair = cookie.split("=");
	    if (pair.length == 2) {
		carray[i++] = new ServerCookieUtils.Cookie(pair[0].trim(),
							   pair[1].trim());
	    }
	}
	return carray;
    }

    /**
     * Create a new instance of {@link ServerCookie}.
     * @param name the name of the cookie
     * @param value the value of the cookie 
     * @return the new instance of {@link ServerCookie}
     */
    static ServerCookie newInstance(String name, String value) {
	return new ServerCookieUtils.Cookie(name, value);
    }

    /**
     * Add this cookie to the Set-Cookie headers.
     * @param headers the headers
     */
    default void addToHeaders(HeaderOps headers) {
	StringBuilder sb = new StringBuilder();
	String value = getValue();
	if (value.contains(" ") || value.contains("\t")) {
	    value = "\"" + value + "\"";
	}
	sb.append(getName() + "=" + value);
	int maxAge = getMaxAge();
	if (maxAge >= 0) {
	    sb.append(String.format((Locale)null, "; Max-Age=%d", maxAge));
	}
	String domain = getDomain();
	if (domain != null) {
	    domain = domain.trim();
	    if (domain.length() > 0) {
		sb.append("; Domain=" + domain);
	    }
	}
	String path = getPath();
	if (path != null) {
	    path = path.trim();
	    if (path.length() > 0) {
		sb.append("; Path=" + path);
	    }
	}
	if (getSecure()) {
	    sb.append("; Secure");
	}
	if (isHttpOnly()) {
	    sb.append("; HttpOnly");
	}
	String comment = getComment();
	if (comment != null) {
	    if (comment.length() > 0) {
		if (comment.contains(" ") || comment.contains("\t")) {
		    comment = "\"" + comment + "\"";
		}
		sb.append("; Comment=" + comment);
	    }
	}
	headers.add("Set-Cookie", sb.toString());
    }
}

