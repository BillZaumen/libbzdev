package org.bzdev.devqsim;
import org.bzdev.lang.Callable;

/**
 * Bind a callable to a simulation object.
 * This allows a simulation listener to attribute events to
 * specific simulation objects in cases where this cannot be
 * done directly. To create an instance of SimObjectCallable,
 * use one of bindCallable methods defined by the  SimObject
 * class and with a Callable argument.
 */
public class SimObjectCallable {
    SimObject source;
    Callable callable;
    String tag;

    SimObjectCallable(SimObject source, Callable callable) {
	this.source = source;
	this.callable = callable;
	this.tag = null;
    }

    SimObjectCallable(SimObject source, Callable callable, String tag) {
	this.source = source;
	this.callable = callable;
	this.tag = tag;
    }

    /**
     * Get the source that will be used for a simulation event.
     * @return the source simulation object
     */
    public final SimObject getSource() {return source;}

    /**
     * Get the callable.
     * @return the callable
     */
    public final Callable getCallable() {return callable;}

    /**
     * Get a descriptive tag.
     * @return the string tagging the callable; null if none provided
     */
    public final String getTag() {return tag;}

}

//  LocalWords:  SimObjectCallable bindCallable SimObject
