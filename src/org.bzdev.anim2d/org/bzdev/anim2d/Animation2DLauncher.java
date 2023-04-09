package org.bzdev.anim2d;
import java.io.InputStream;
import java.io.IOException;

import org.bzdev.devqsim.SimulationLauncher;
import org.bzdev.util.JSObject;



/**
 * Launcher for 2D animations.
 * Normally this class is not used directly, but instead is loaded
 * by using an SPI.
 * @see org.bzdev.obnaming.ObjectNamerLauncher
 */
public class Animation2DLauncher extends SimulationLauncher {

    /**
     * Get an input stream containing a YAML file describing the classes
     * this launcher provides.
     * @return the input stream
     */
   public static InputStream getResourceStream() {
	return Animation2DLauncher.class
	    .getResourceAsStream("Animation2DLauncher.yaml");
    }


    /**
     * Constructor with additional class data.
     * The initializer is a JSObject obtained by parsing a YAML file or
     * by combining the results of parsing YAML files.
     * @param initializer an object describing the additional configuration;
     *        null if there is nothing else to configure
     * @exception ClassNotFoundException - if a needed class does not exist
     * @exception IOException - if an IO error occurred
     * @exception IllegalAccessException - if this method cannot access
     *            a class, field, method, or constructor
     * @see org.bzdev.obnaming.ObjectNamerLauncher
     * @see org.bzdev.obnaming.ObjectNamerLauncher#combine(JSObject,JSObject...)
     * @see org.bzdev.obnaming.ObjectNamerLauncher#loadFromStream(Class,InputStream,int)
     */
    public Animation2DLauncher(JSObject initializer)
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	super(combine(loadFromStream(Animation2DLauncher.class,
				     Animation2DLauncher.getResourceStream(),
				     8),
		      initializer));
    }

    /**
     * Constructor.
     * @exception ClassNotFoundException - if a needed class does not exist
     * @exception IOException - if an IO error occurred
     * @exception IllegalAccessException - if this method cannot access
     *            a class, field, method, or constructor
     */
    public Animation2DLauncher() 
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	this(null);
    }
}

//  LocalWords:  SPI YAML DLauncher yaml initializer JSObject
//  LocalWords:  loadFromStream InputStream
