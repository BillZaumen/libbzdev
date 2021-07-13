package org.bzdev.devqsim;
import java.io.InputStream;

import java.io.IOException;

import org.bzdev.obnaming.ObjectNamerLauncher;
import org.bzdev.util.JSObject;

/**
 * Launcher for simulations.
 * Normally this class is not used directly, but instead is loaded
 * by using an SPI.
 * @see org.bzdev.obnaming.ObjectNamerLauncher
 */
public class SimulationLauncher extends ObjectNamerLauncher {

    /**
     * Get an input stream containing a YAML file describing the classes
     * this launcher provides.
     * @return the input stream
     */
    public static InputStream getResourceStream() {
	return SimulationLauncher.class
	    .getResourceAsStream("SimulationLauncher.yaml");
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
    public SimulationLauncher(JSObject initializer)
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	super(combine(loadFromStream(SimulationLauncher.class,
				     SimulationLauncher.getResourceStream(),
				     8),
		      initializer));
    }

    /**
     * Constructor.
     */
    public SimulationLauncher()
	throws ClassNotFoundException, IOException, IllegalAccessException
    {
	this(null);
    }
}

//  LocalWords:  SPI YAML SimulationLauncher yaml initializer
//  LocalWords:  JSObject loadFromStream InputStream
