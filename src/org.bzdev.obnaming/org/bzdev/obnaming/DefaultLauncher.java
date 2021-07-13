package org.bzdev.obnaming;
import java.io.InputStream;
import java.io.IOException;
import org.bzdev.util.JSObject;

/**
 * Default object-namer launcher.
 * Normally this class is not used directly, but instead is loaded
 * by using an SPI.  It provides no additional classes beyond those
 * provided by  its superclass.
 * @see ObjectNamerLauncher
 */
public final class DefaultLauncher extends ObjectNamerLauncher {
    
    /**
     * Get an input stream containing a YAML file describing the classes
     * this launcher provides. In this case, the YAML file will not specify
     * any objects.
     * @return the input stream
     */
    public static InputStream getResourceAsStream() {
	return DefaultLauncher.class
	    .getResourceAsStream("DefaultLauncher.yaml");
    }

    /**
     * Constructor with additional class data.
     * The initializer is a JSObject obtained by parsing a YAML file or
     * by combining the results of parsing YAML files.
     * @param initializer an object describing the additional configuration;
     *        null if there is nothing else to configure
     * @see org.bzdev.obnaming.ObjectNamerLauncher
     * @see org.bzdev.obnaming.ObjectNamerLauncher#combine(JSObject,JSObject...)
     * @see org.bzdev.obnaming.ObjectNamerLauncher#loadFromStream(Class,InputStream,int)
     */
    public DefaultLauncher(JSObject initializer)
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	super(combine(loadFromStream(DefaultLauncher.class,
				     DefaultLauncher.getResourceAsStream(),
				     8),
		      initializer));
    }

    /**
     * Constructor.
     */
    public DefaultLauncher()
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	this(null);
    }

    
}

//  LocalWords:  namer SPI superclass ObjectNamerLauncher YAML yaml
//  LocalWords:  DefaultLauncher initializer JSObject loadFromStream
//  LocalWords:  InputStream
