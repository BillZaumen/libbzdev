package org.bzdev.drama;
import java.io.InputStream;
import java.io.IOException;
import org.bzdev.devqsim.SimulationLauncher;
import org.bzdev.util.JSObject;

/**
 * Launcher for simulations that use the drama package.
 * Normally this class is not used directly, but instead is loaded
 * by using an SPI.
 * @see org.bzdev.obnaming.ObjectNamerLauncher
 */
public class DramaSimulationLauncher extends SimulationLauncher {
    
    /**
     * Get an input stream containing a YAML file describing the classes
     * this launcher provides.
     * @return the input stream
     */
    public static InputStream getResourceStream() {
	return DramaSimulationLauncher.class
	    .getResourceAsStream("DramaSimulationLauncher.yaml");
    }

    /**
     * Constructor with additional class data.
     * The initializer is a JSObject obtained by parsing a YAML file or
     * by combining the results of parsing YAML files.
     * @param initializer an object describing the additional configuration;
     *        null if there is nothing else to configure
     * @exception ClassNotFoundException a class needed to initialize
     *            this object could not be found
     * @exception IOException an IO error occurred
     * @exception IllegalAccessException if a Constructor object is
     *            enforcing Java language access control and the underlying
     *            constructor is inaccessible.
     * @see org.bzdev.obnaming.ObjectNamerLauncher
     * @see org.bzdev.obnaming.ObjectNamerLauncher#combine(JSObject,JSObject...)
     * @see org.bzdev.obnaming.ObjectNamerLauncher#loadFromStream(Class,InputStream,int)
     */
    public DramaSimulationLauncher(JSObject initializer) 
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	super(combine(loadFromStream(DramaSimulationLauncher.class,
				     DramaSimulationLauncher
				         .getResourceStream(),
				     8),
		      initializer));
    }

    /**
     * Constructor.
     * @exception ClassNotFoundException a class needed to initialize
     *            this object could not be found
     * @exception IOException an IO error occurred
     * @exception IllegalAccessException if a Constructor object is
     *            enforcing Java language access control and the underlying
     *            constructor is inaccessible.
     */
    public DramaSimulationLauncher() 
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	this(null);
    }
}

//  LocalWords:  SPI YAML DramaSimulationLauncher yaml initializer
//  LocalWords:  JSObject loadFromStream InputStream
