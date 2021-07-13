package org.bzdev.obnaming;
import java.util.ResourceBundle;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

//@exbundle org.bzdev.obnaming.lpack.Obnaming

/**
 * Utility class to get resource bundles, etc.
 * After Java 8, the module system restricts the visibility
 * of various classes.  This is a utility class that will
 * enable one to get a resource bundle associated with the
 * org.bzdev.obnaming module. It is used by automatically
 * generated code during initialization. As this code will
 * be in other packages and modules, this class has to be
 * public and in an exported package.
 */
public class ObnamingUtils {

    /**
     * Get a resource bundle visible to the obnaming package
     * @param name the class name for the bundle
     * @return the corresponding bundle
     */
    public static ResourceBundle getBundle(String name) {
	return ResourceBundle.getBundle(name);
    }

    /**
     * Set properties using a resource storing the properties in XML format.
     * @param properties the property object to set
     * @param path a path visible to the module org.bzdev.obnaming and
     *        denoting the resource.
     * @exception NullPointerException the path was null
     * @exception IOException an IO error or XML error occurred
     * @exception SecurityException a security manager was installed
     *            and the necessary permissions were not granted
     * @see java.lang.Class#getResourceAsStream(String)
     * @see java.util.Properties#loadFromXML(InputStream)
     */
    public static void setPropertiesForXMLResource(Properties properties,
						   String path)
	throws IOException, SecurityException, NullPointerException
    {
	if (path == null) {
	    throw new NullPointerException("noPath");
	}
	InputStream is = ObnamingUtils.class.getResourceAsStream(path);
	if (is == null) {
	    String msg = ObnamingErrorMsg.errorMsg("noResource", path);
	    throw new IOException(msg);
	}
	properties.loadFromXML(is);
    }
}

//  LocalWords:  exbundle obnaming NullPointerException IOException
//  LocalWords:  SecurityException getResourceAsStream loadFromXML
//  LocalWords:  InputStream noPath noResource
