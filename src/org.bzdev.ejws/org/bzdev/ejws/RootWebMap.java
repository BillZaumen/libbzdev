package org.bzdev.ejws;

/**
 * WebMap for a web server's root.
 */
class RootWebMap extends WebMap {
    /**
     * Get an Info object for a resource.
     * @param prepath the initial portion of the request URI - the part
     *        before the path portion of the URI
     * @param path the path portion of a URI
     * @param query the query portion of a URL
     * @param fragment the fragment portion of a URI
     * @param requestInfo an object encapsulating request data
     *        (headers, input streams, etc.)
     * @return an Info object describing properties of a resource and
     *         providing an input stream to the resource
     * @return null in all cases
     */
    protected WebMap.Info getInfoFromPath(String prepath,
					  String path, String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
    {
	return null;
    }
}

//  LocalWords:  WebMap prepath
