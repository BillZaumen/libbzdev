package org.bzdev.devqsim;

/**
 * Bind a runnable to a simulation object.
 * This allows a simulation listener to attribute events to
 * specific simulation objects in cases where this cannot be
 * done directly. To create an instance of SimObjectCallable,
 * use one of bindRunnable methods defined by the SimObject
 * class and with a Runnable argument.
 */
public class SimObjectRunnable {
    SimObject source;
    Runnable runnable;
    String tag;

    SimObjectRunnable(SimObject source, Runnable runnable) {
	this.source = source;
	this.runnable = runnable;
	this.tag = null;
    }

    SimObjectRunnable(SimObject source, Runnable runnable, String tag) {
	this.source = source;
	this.runnable = runnable;
	this.tag = tag;
    }

    /**
     * Get the source that will be used for a simulation event.
     * @return the source simulation object
     */
    public final SimObject getSource() {return source;}

    /**
     * Get the runnable.
     * @return the runnable
     */
    public final Runnable getRunnable() {return runnable;}

    /**
     * Get a descriptive tag.
     * @return the string tagging the runnable; null if none provided
     */
    public final String getTag() {return tag;}
}

//  LocalWords:  runnable SimObjectCallable bindRunnable SimObject
