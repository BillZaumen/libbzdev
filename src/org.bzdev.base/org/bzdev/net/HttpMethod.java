package org.bzdev.net;

/**
 * An enumeration for HTTP methods.
 */
public enum HttpMethod {
    /**
     * An HTTP HEAD method.
     */
    HEAD,
    /**
     * An HTTP GET method.
     */
    GET,
    /**
     * An HTTP POST method.
     */
    POST,
    /**
     * An HTTP PUT method.
     */
    PUT,
    /**
     * An HTTP DELETE method.
     */
    DELETE,
    /**
     * An HTTP CONNECT method.
     */
    CONNECT,
    /**
     * An HTTP OPTIONS method.
     */
    OPTIONS,
    /**
     * An HTTP TRACE method.
     */
    TRACE;

    @Override
    public String toString() {
	return super.toString().toLowerCase();
    }

    /**
     * Convert a string to this enum.
     * @param method the name of an HTTP method
     * @return the enum constant for a method
     * @exception IllegalArgumentException the name did not match an
     *            HTTP method
     */
    public static HttpMethod forName(String method)
        throws IllegalArgumentException
    {
	return valueOf(HttpMethod.class, method.trim().toUpperCase());
    }
}

//  LocalWords:  enum IllegalArgumentException
