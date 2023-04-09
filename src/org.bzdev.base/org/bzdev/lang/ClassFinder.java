package org.bzdev.lang;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Class for determining information about a class given its name.
 */
public class ClassFinder {

    // to suppress a constructor entry in the documentation
    private ClassFinder() {}

    /**
     * Determine if a class can be found using the system class loader.
     * <P>
     * The class name must be a fully qualified binary name - a period
     * separates package components and the class name from a preceding
     * package name, and a dollar sign separates a inner classes from outer
     * classes.
     * <P>
     * One use case for this method is an Output Stream Graphics provider
     * that checks if a particular class library is present.
     * @param name the binary class name of a class
     * @return true if the class can be found using the system class loader;
     *         false otherwise
     */
    public static boolean classExists(String name) {
	try {
	    Class<?> clazz =
		ClassLoader.getSystemClassLoader().loadClass(name);
	    if (clazz == null) {
		return false;
	    } else {
		return true;
	    }
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Get the binary name of a class given its canonical name.
     * @param name the canonical name
     * @return the binary name; null if the class cannot be found
     */
    public static String getBinaryName(final String name) {
	// put this in a doPrivileged block in case a security
	// policy forbids access to the class loader or the class
	// loader's methods: we are just generating a binary class name,
	// which isn't particularly sensitive given that one needs the
	// canonical name to get started.
	return 
	    AccessController.doPrivileged(new PrivilegedAction<String>() {
		    public String run() {
			Class<?> clasz = null;
			String n = name;
			for (;;) {
			    try {
				clasz = ClassLoader.getSystemClassLoader()
				    .loadClass(n);
				break;
			    } catch (ClassNotFoundException e) {
				int ind = n.lastIndexOf('.');
				if (ind != -1) {
				    n = n.substring(0, ind)
					+ "$" + n.substring(ind+1);
				    continue;
				} else {
				    return null;
				}
			    }
			}
			return n;
		    }
		});
    }
}

//  LocalWords:  doPrivileged
