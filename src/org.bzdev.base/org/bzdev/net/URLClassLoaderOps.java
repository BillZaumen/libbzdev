package org.bzdev.net;
import java.net.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.concurrent.atomic.AtomicBoolean;

//@exbundle org.bzdev.net.lpack.Net


/**
 * Allow additional classes to be added to a URLClassLoader
 * used by the system class loader.
 * @deprecated A change in Java 9, 10, and 11 prevents this
 * class from working as it should.  It was used by scrunner
 * and lsnof and those were changed so that this class is no
 * longer used.
 */
@Deprecated
public class URLClassLoaderOps {

    static String errorMsg(String key, Object... args) {
	return NetErrorMsg.errorMsg(key, args);
    }

    /**
     * Permission to extend the class path at runtime to
     * include additional files or URLs.
     * @deprecated This permission is no longer necessary.
     */
    @Deprecated
    public static class URLPermission extends BasicPermission {
	/**
	 * Constructor.
	 * @param name the string org.bzdev.net.allow.url.class.path
	 */
	public URLPermission(String name) {
	    super(name);
	}

	/**
	 * Constructor with actions.
	 * @param name the string org.bzdev.net.allow.url.class.path
	 * @param actions a parameter that is ignored in this case although
	 *        the constructor is needed
	 */
	public URLPermission(String name, String actions) {
	    super(name, actions);
	}
    }

    private static final String PROPERTY = "org.bzdev.net.allow.url.class.path";

    private static AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Add a URL to the system class loader or one of its parents
     * The class loader that will be modified is an instance of URLClassLoader,
     * and is found by iterating over parent class loaders until the first
     * one that is an instance of URLClassLoader is found.
     * If the default security manager is present, the permission
     * org.bzdev.net.URLClassLoaderOps.URLPermission with a target
     * "org.bzdev.net.allow.url.class.path" must be granted.
     * @param url the URL to add
     * @exception SecurityException permission for his operation was not
     *            granted
     */
    public static void addURL(URL url) {
	if (closed.get()) return;
	URL[] urls = {url};
	addURLs(urls);
    }

    private static class MethodAndClassLoader {
	MethodAndClassLoader(Method method, URLClassLoader ucl) {
	    this.method = method;
	    this.ucl = ucl;
	}
	Method method;
	URLClassLoader ucl;
    }

    /**
     * Add multiple URLs to the system class loader or one of its parents
     * The class loader that will be modified is an instance of URLClassLoader,
     * and is found by iterating over parent class loaders until the first
     * one that is an instance of URLClassLoader is found.
     * If the default security manager is present, the permission
     * org.bzdev.net.URLClassLoaderOps.URLPermission with a target
     * "org.bzdev.net.allow.url.class.path" must be granted.
     * @param urls the URLs to add
     * @exception SecurityException permission for his operation was not
     *            granted
     */
    public static void addURLs(final URL[] urls) {
	return;
	/*
	if (closed.get()) return;
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new URLClassLoaderOps.URLPermission(PROPERTY));
	}
	MethodAndClassLoader mcl = AccessController.doPrivileged
	    (new PrivilegedAction<MethodAndClassLoader>() {
		public MethodAndClassLoader run() {
		    ClassLoader cl = ClassLoader.getSystemClassLoader();
		    do {
			if (cl instanceof URLClassLoader) {
			    URLClassLoader ucl =  (URLClassLoader) cl;
			    try {
				Method m =
				    URLClassLoader.class.getDeclaredMethod
				    ("addURL", URL.class);
				m.setAccessible(true);
				return new MethodAndClassLoader(m, ucl);
			    } catch (Exception e) {
				String msg = errorMsg("addURLToCL");
				throw new Error(msg, e);
			    }
			}
			cl = cl.getParent();
		    } while (cl != null);
		    return null;
		}
	    });
	if (mcl != null) {
	    try {
		for (URL url: urls) {
		    mcl.method.invoke(mcl.ucl, url);
		}
	    } catch (IllegalAccessException iae) {
		String msg = errorMsg("unexpectedIAE");
		throw new Error(msg, iae);
	    } catch (InvocationTargetException ite) {
		String msg = errorMsg("unexpectedITE");
		throw new Error(msg, ite);
	    }
	}
	*/
    }

    /**
     * Prevent new URLs from being added to the class loader.
     */
    public static void close() {
	closed.set(true);
    }

    /**
     * Determine if the URLCLassLoaderOps service has been closed.
     * A state of "closed" indicates that additional URLs cannot be
     * added.
     * @return true if closed; false otherwise
     */
    public static boolean isClosed() {
	return true;
	// return closed.get();
    }
}

//  LocalWords:  exbundle URLClassLoader scrunner lsnof runtime url
//  LocalWords:  SecurityException urls SecurityManager sm mcl ucl
//  LocalWords:  getSecurityManager checkPermission URLClassLoaderOps
//  LocalWords:  URLPermission MethodAndClassLoader AccessController
//  LocalWords:  doPrivileged PrivilegedAction ClassLoader instanceof
//  LocalWords:  getSystemClassLoader addURL setAccessible msg iae
//  LocalWords:  errorMsg addURLToCL getParent IllegalAccessException
//  LocalWords:  unexpectedIAE InvocationTargetException ite
//  LocalWords:  unexpectedITE URLCLassLoaderOps
